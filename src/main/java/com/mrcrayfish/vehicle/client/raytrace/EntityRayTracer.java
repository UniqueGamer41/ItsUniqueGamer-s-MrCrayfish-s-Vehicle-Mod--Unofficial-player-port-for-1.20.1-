package com.mrcrayfish.vehicle.client.raytrace;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.VehicleMod;
import com.mrcrayfish.vehicle.client.event.VehicleRayTraceEvent;
import com.mrcrayfish.vehicle.client.model.ComponentModel;
import com.mrcrayfish.vehicle.client.model.VehicleModels;
import com.mrcrayfish.vehicle.client.raytrace.data.CosmeticRayTraceData;
import com.mrcrayfish.vehicle.client.raytrace.data.InteractableBoxRayTraceData;
import com.mrcrayfish.vehicle.client.raytrace.data.RayTraceData;
import com.mrcrayfish.vehicle.client.raytrace.data.ComponentModelRayTraceData;
import com.mrcrayfish.vehicle.common.cosmetic.actions.Action;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.network.PacketHandler;
import com.mrcrayfish.vehicle.network.message.MessageInteractCosmetic;
import com.mrcrayfish.vehicle.network.message.MessageInteractKey;
import com.mrcrayfish.vehicle.network.message.MessagePickupVehicle;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import com.mrcrayfish.vehicle.util.port.Vector4f;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Author: Phylogeny, MrCrayfish
 * <p>
 * This class allows precise ray trace to be performed on the rendered model item parts, as well as on additional interaction boxes, of entities.
 */
public class EntityRayTracer
{
    private static EntityRayTracer instance;

    //TODO lazy load ray trace transforms and regenerate when joining a server
    private final Map<EntityType<?>, Supplier<RayTraceTransforms>> entityRayTraceTransformSuppliers = new Reference2ObjectOpenHashMap<>();

    /**
     * Maps raytraceable entities to maps, which map rendered model item parts to the triangles that comprise static versions of the faces of their BakedQuads
     */
    private final Map<EntityType<?>, List<RayTraceData>> entityRayTraceData = new Reference2ObjectOpenHashMap<>();
    private final Map<EntityType<?>, Function<VehicleEntity, List<RayTraceData>>> entityDynamicRayTraceData = new Reference2ObjectOpenHashMap<>();

    /**
     * Scales and offsets for rendering the entities in crates
     */
    private final Map<EntityType<?>, Pair<Float, Float>> entityCrateScalesAndOffsets = new Reference2ObjectOpenHashMap<>();
    private static final Pair<Float, Float> SCALE_AND_OFFSET_DEFAULT = new ImmutablePair<>(0.25F, 0.0F);

    /**
     * Interactable boxes
     */
    private final Map<EntityType<?>, List<InteractableBox<?>>> entityInteractableBoxes = new Reference2ObjectOpenHashMap<>();
    private final Map<EntityType<?>, List<RayTraceData>> entityInteractableBoxData = Util.make(() -> {
        Reference2ObjectOpenHashMap<EntityType<?>, List<RayTraceData>> map = new Reference2ObjectOpenHashMap<>();
        map.defaultReturnValue(new ObjectArrayList<>());
        return map;
    });

    /**
     * The result of clicking and holding on a continuously interactable raytrace part. Every tick that this is not null,
     * both the raytrace and the interaction of this part will be performed.
     */
    @Nullable
    private VehicleRayTraceResult continuousInteraction;

    /**
     * The object returned by the interaction function of the result of clicking and holding on a continuously interactable raytrace part.
     */
    private InteractionHand continuousInteractionHand;

    /**
     * Counts the number of ticks that a continuous interaction has been performed for
     */
    private int continuousInteractionTickCounter;

    private EntityRayTracer() {}

    public static EntityRayTracer instance()
    {
        if(instance == null)
        {
            instance = new EntityRayTracer();
        }
        return instance;
    }

    /**
     * Clears registration data and triggers re-registration in the next client tick
     */
    public void clearDataForReregistration()
    {
        this.entityRayTraceData.clear();
        this.entityInteractableBoxData.clear();
        this.entityCrateScalesAndOffsets.clear();
    }

    @Nullable
    public VehicleRayTraceResult getContinuousInteraction()
    {
        return this.continuousInteraction;
    }

    @Nullable
    public InteractionHand getContinuousInteractionHand()
    {
        return this.continuousInteractionHand;
    }

    public int getContinuousInteractionTickCounter()
    {
        return this.continuousInteractionTickCounter;
    }

    /**
     * Registers a {@link RayTraceTransforms} for a vehicle. This allows triangles to be generated
     * for preforming ray tracing on a vehicle. This allows for advanced interactions like being able
     * to click on any part of the vehicle to mount it, fueling the vehicle through it's fuel port,
     * and using a key in the key hole.
     *
     * @param type the entity type of the vehicle
     * @param transforms the ray trace transforms for the vehicle
     * @param <T> an entity type that is a vehicle entity and implements IEntityRayTraceable
     */
    public synchronized <T extends VehicleEntity> void registerTransforms(EntityType<T> type, Supplier<RayTraceTransforms> transforms)
    {
        this.entityRayTraceTransformSuppliers.putIfAbsent(type, transforms);
    }

    /**
     * Registers an interaction box for the specified entity type. An interaction box allows you to
     * run a custom action when it is interacted with on the vehicle. This is currently used for the
     * chest on the Moped. This interaction box will be global across all entities that match the
     * given entity type. The AxisAlignBB supplier is positioned relative to the center of the vehicle.
     *
     * @param type        the entity type to pair the interaction box to
     * @param boxSupplier a supplier that returns an AxisAlignedBB
     * @param action      the custom action to run when the box is interacted with
     * @param active      a predicate to determine if the interaction box is active
     * @param <T>         an entity that extends vehicle entity
     */
    public synchronized <T extends VehicleEntity> void registerInteractionBox(EntityType<T> type, Supplier<AABB> boxSupplier, BiConsumer<T, Boolean> action, Predicate<T> active)
    {
        this.entityInteractableBoxes.computeIfAbsent(type, (t) -> new ObjectArrayList<>()).add(new InteractableBox<>(boxSupplier, action, active));
    }

    /**
     * Registers a function that provides dynamic RayTraceData based on the given vehicle entity.
     * Unlike registering normal transforms, the RayTraceData list returned from the provided function
     * is not global across all entities of that type. It is recommended that the list of RayTraceData
     * returned from the function is cached instead of creating new instances when the function is called.
     *
     * @param type     the entity type to pair the function to
     * @param function a function that returns custom raytracedata
     * @param <T>      an entity that extends vehicle entity
     */
    @SuppressWarnings("unchecked")
    public synchronized <T extends VehicleEntity> void registerDynamicRayTraceData(EntityType<T> type, Function<T, List<RayTraceData>> function)
    {
        this.entityDynamicRayTraceData.put(type, (Function<VehicleEntity, List<RayTraceData>>) function);
    }

    /**
     * Create static triangles for raytraceable entity
     *
     * @param entityType the entity type
     * @param transforms matrix transforms for each part
     */
    private <T extends VehicleEntity> void generateEntityTriangles(EntityType<T> entityType, Map<RayTraceData, List<MatrixTransform>> transforms)
    {
        List<RayTraceData> dataList = new ArrayList<>(transforms.keySet());
        this.entityRayTraceData.put(entityType, dataList);
    }

    /**
     * Gets entity's scale and offset for rendering in a crate
     *
     * @param entityType the entity type
     *
     * @return pair of scale and offset
     */
    public Pair<Float, Float> getCrateScaleAndOffset(EntityType<? extends VehicleEntity> entityType)
    {
        Pair<Float, Float> scaleAndOffset = this.entityCrateScalesAndOffsets.get(entityType);
        this.initializeTransforms(entityType);
        return scaleAndOffset == null ? SCALE_AND_OFFSET_DEFAULT : scaleAndOffset;
    }

    /**
     * Converts a model into triangles that represent its quads
     *
     * @param model rendered model of entity
     * @param matrix part-specific matrix mirroring the static GL operations performed on that part during rendering - should be null for dynamic triangles
     *
     * @return list of all triangles
     */
    public static List<Triangle> createTrianglesFromBakedModel(BakedModel model, @Nullable Matrix4f matrix)
    {
        List<Triangle> triangles = new ArrayList<>();
        try
        {
            XoroshiroRandomSource random = new XoroshiroRandomSource(42L);
            createTrianglesFromBakedModel(model.getQuads(null, null, random, ModelData.EMPTY, RenderType.lines()), matrix, triangles);
            for(Direction facing : Direction.values())
            {
                random.setSeed(42L);
                createTrianglesFromBakedModel(model.getQuads(null, facing, random, ModelData.EMPTY, RenderType.lines()), matrix, triangles);
            }
        }
        catch(Exception ignored){}
        return triangles;
    }

    /**
     * Converts BakedQuads from a BakedModel into triangles
     *
     * @param quads list of BakedQuad
     * @param matrix part-specific matrix mirroring the static GL operations performed on that part during rendering - should be null for dynamic triangles
     * @param triangles list of all triangles for the given raytraceable entity class
     */
    private static void createTrianglesFromBakedModel(List<BakedQuad> quads, @Nullable Matrix4f matrix, List<Triangle> triangles)
    {
        for(BakedQuad quad : quads)
        {
            int size = DefaultVertexFormat.BLOCK.getIntegerSize();
            Integer[] data = ArrayUtils.toObject(quad.getVertices());
            createTrianglesFromDataAndAdd(triangles, matrix, size, data, Float::intBitsToFloat);
        }
    }

    /**
     * Transforms a static triangle by the part-specific matrix and adds it to the list of all triangles for the given raytraceable entity class, or simply
     * adds a dynamic triangle to that list
     *
     * @param triangle array of the three vertices that comprise the triangle
     * @param matrix part-specific matrix mirroring the static GL operations performed on that part during rendering - should be null for dynamic triangles
     * @param triangles list of all triangles for the given raytraceable entity class
     */
    private static void transformTriangleAndAdd(float[] triangle, @Nullable Matrix4f matrix, List<Triangle> triangles)
    {
        triangles.add(new Triangle(matrix != null ? getTransformedTriangle(triangle, matrix) : triangle));
    }

    /**
     * gets a new triangle transformed by the passed part-specific matrix
     *
     * @param triangle array of the three vertices that comprise the triangle
     * @param matrix part-specific matrix mirroring the GL operation performed on that part during rendering
     *
     * @return new transformed triangle
     */
    public static float[] getTransformedTriangle(float[] triangle, Matrix4f matrix)
    {
        float[] triangleNew = new float[9];
        for (int i = 0; i < 9; i += 3)
        {
            com.mrcrayfish.vehicle.util.port.Vector4f vec = new Vector4f(triangle[i], triangle[i + 1], triangle[i + 2], 1);
            vec.transform(matrix);
            triangleNew[i] = vec.x();
            triangleNew[i + 1] = vec.y();
            triangleNew[i + 2] = vec.z();
        }
        return triangleNew;
    }

    /**
     * Performs a specific and general interaction with a raytraceable entity
     *
     * @param entity raytraceable entity
     * @param result the result of the raytrace
     */
    public static void interactWithEntity(Entity entity, EntityHitResult result)
    {
        Minecraft.getInstance().gameMode.interact(Minecraft.getInstance().player, entity, InteractionHand.MAIN_HAND);
        Minecraft.getInstance().gameMode.interactAt(Minecraft.getInstance().player, entity, result, InteractionHand.MAIN_HAND);
    }

    /**
     * Performs a raytrace and interaction each tick that a continuously interactable part is right-clicked and held while looking at
     *
     * @param event tick event
     */
    @SubscribeEvent
    public void rayTraceEntitiesContinuously(TickEvent.ClientTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;

        Player player = Minecraft.getInstance().player;

        if(this.continuousInteraction == null || player == null)
            return;

        VehicleRayTraceResult result = rayTraceEntities(this.continuousInteraction.isRightClick());
        if(result == null || result.getEntity() != this.continuousInteraction.getEntity() || result.getData() != this.continuousInteraction.getData())
        {
            this.continuousInteraction = null;
            this.continuousInteractionTickCounter = 0;
            return;
        }

        this.continuousInteractionHand = result.performContinuousInteraction(player);

        if(this.continuousInteractionHand == null)
        {
            this.continuousInteraction = null;
            this.continuousInteractionTickCounter = 0;
        }
        else
        {
            this.continuousInteractionTickCounter++;
        }
    }

    @SubscribeEvent
    public void onClientConnect(ClientPlayerNetworkEvent.LoggingIn event)
    {
        // Clear cache when player logs in as the server may have datapacks
        this.clearDataForReregistration();
    }

    @SubscribeEvent
    public void onClientTick(InputEvent.Key event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return;

        if(!FMLLoader.isProduction() && event.getKey() == GLFW.GLFW_KEY_EQUAL && (event.getModifiers() & GLFW.GLFW_MOD_CONTROL) != GLFW.GLFW_FALSE)
            this.clearDataForReregistration();
    }

    /**
     * Performs raytrace on interaction boxes and item part triangles
     *
     * @param event mouse event
     */
    @SubscribeEvent
    public void onMouseEvent(InputEvent.MouseButton event)
    {
        Minecraft mc = Minecraft.getInstance();
        if(mc.getOverlay() != null || mc.screen != null || mc.player == null || !mc.mouseHandler.isMouseGrabbed())
            return;

        if(event.getAction() == GLFW.GLFW_RELEASE)
            return;

        // Return if not right and/or left clicking, if the mouse is being released, or if there are no entity classes to raytrace
        boolean rightClick = event.getButton() == Minecraft.getInstance().options.keyUse.getKey().getValue();
        boolean leftClick = event.getButton() == Minecraft.getInstance().options.keyAttack.getKey().getValue();

        if(!rightClick && (!Config.CLIENT.enabledLeftClick.get() || !leftClick))
            return;

        if(this.performRayTrace(rightClick))
        {
            event.setCanceled(true);
        }
    }

    private boolean performRayTrace(boolean rightClick)
    {
        VehicleRayTraceResult result = this.rayTraceEntities(rightClick);
        if(result != null)
        {
            this.continuousInteractionHand = result.performContinuousInteraction(Minecraft.getInstance().player);
            if(this.continuousInteractionHand != null)
            {
                this.continuousInteraction = result;
                this.continuousInteractionTickCounter = 1;
            }
            return true;
        }
        return false;
    }

    /**
     * Performs raytrace on interaction boxes and item part triangles of all raytraceable entities within reach of the player,
     * and returns the result if the clicked raytraceable entity returns true from
     *
     * @param rightClick whether the click was a right-click or a left-click
     *
     * @return the result of the raytrace - returns null, if it fails
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private <T extends VehicleEntity> VehicleRayTraceResult rayTraceEntities(boolean rightClick)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = Objects.requireNonNull(minecraft.player);
        float reach = Objects.requireNonNull(minecraft.gameMode).getPickRange();
        Vec3 eyeVec = player.getEyePosition(1.0F);
        Vec3 forwardVec = eyeVec.add(player.getViewVector(1.0F).scale(reach));
        AABB box = new AABB(eyeVec, eyeVec).inflate(reach);
        VehicleRayTraceResult closestRayTraceResult = null;
        double closestDistance = Double.MAX_VALUE;
        for(VehicleEntity entity : Objects.requireNonNull(minecraft.level).getEntitiesOfClass(VehicleEntity.class, box))
        {
            EntityType<T> type = (EntityType<T>) entity.getType();
            if(this.entityRayTraceTransformSuppliers.containsKey(type))
            {
                /* Initialize the vehicle triangles if they don't exist. Lazy loading for memory
                 * sake, not that it does much */
                this.initializeTransforms(type);

                VehicleRayTraceResult rayTraceResult = this.rayTraceEntityRotated(entity, eyeVec, forwardVec, reach, rightClick);
                if(rayTraceResult != null)
                {
                    double distance = rayTraceResult.getDistanceToEyes();
                    if(distance < closestDistance)
                    {
                        closestRayTraceResult = rayTraceResult;
                        closestDistance = distance;
                    }
                }
            }
            else
            {
                VehicleMod.LOGGER.warn("The vehicle '" + type.getDescriptionId() + "' does not have any registered ray trace transforms.");
            }
        }
        if(closestRayTraceResult != null)
        {
            double eyeDistance = closestRayTraceResult.getDistanceToEyes();
            if(eyeDistance <= reach)
            {
                /* If the hit entity is a raytraceable entity, and if the player's eyes are inside what MC
                 * thinks the player is looking at, then process the hit regardless of what MC thinks */
                boolean bypass = this.entityRayTraceData.containsKey(closestRayTraceResult.getEntity().getType());
                HitResult result = Minecraft.getInstance().hitResult;
                if(bypass && result != null && result.getType() != HitResult.Type.MISS)
                {
                    AABB boxMC = null;
                    if(result.getType() == HitResult.Type.ENTITY)
                    {
                        boxMC = closestRayTraceResult.getEntity().getBoundingBox();
                    }
                    else if(result.getType() == HitResult.Type.BLOCK)
                    {
                        BlockPos pos = ((BlockHitResult) result).getBlockPos();
                        boxMC = closestRayTraceResult.getEntity().level().getBlockState(pos).getShape(closestRayTraceResult.getEntity().level(), pos).bounds();
                    }
                    bypass = boxMC != null && boxMC.contains(eyeVec);
                }

                Vec3 hit = forwardVec;
                if(!bypass && result != null && result.getType() != HitResult.Type.MISS)
                {
                    /* Set hit to what MC thinks the player is looking at if the player is not
                     * looking at the hit entity */
                    if(result.getType() == HitResult.Type.ENTITY && ((EntityHitResult) result).getEntity() == closestRayTraceResult.getEntity())
                    {
                        bypass = true;
                    }
                    else
                    {
                        hit = result.getLocation();
                    }
                }

                /* If not bypassed, process the hit only if it is closer to the player's eyes than
                 * what MC thinks the player is looking */
                if(bypass || eyeDistance < hit.distanceTo(eyeVec))
                {
                    if(this.processHit(closestRayTraceResult))
                    {
                        return closestRayTraceResult;
                    }
                }
            }
        }
        return null;
    }

    private <T extends VehicleEntity> void initializeTransforms(EntityType<T> type)
    {
        if(!this.entityRayTraceData.containsKey(type))
        {
            List<MatrixTransform> transforms = new ObjectArrayList<>();
            TransformHelper.createBodyTransforms(transforms, type);
            HashMap<RayTraceData, List<MatrixTransform>> parts = Maps.newHashMap();

            RayTraceTransforms rayTraceTransforms = this.entityRayTraceTransformSuppliers.get(type).get();
            rayTraceTransforms.load(this, transforms, parts);
            System.out.println("=== PARTS MAP BEFORE GENERATE ===");

            for(RayTraceData data : parts.keySet())
            {
                if(data instanceof ComponentModelRayTraceData component)
                {
                    System.out.println(
                            "MAP PART = " +
                                    component.getModel().getModelLocation()
                    );
                }
                else
                {
                    System.out.println("MAP DATA = " + data.getClass().getName());
                }
            }

            System.out.println("PARTS MAP SIZE = " + parts.size());
            System.out.println("=== END PARTS MAP ===");

            this.generateEntityTriangles(type, parts);

            // ADD THIS
            System.out.println("=== RAYTRACE PARTS ===");

            for(RayTraceData data : this.entityRayTraceData.get(type))
            {
                if(data instanceof ComponentModelRayTraceData component)
                {
                    System.out.println(
                            "PART MODEL = " + component.getModel().getModelLocation()
                    );
                }
                else
                {
                    System.out.println(
                            "PART DATA = " + data.getClass().getName()
                    );
                }
            }

            System.out.println("=== END PARTS ===");
            // END DEBUG

            this.generateScalingAndOffset(type);
            this.generateInteractableBoxes(type, transforms);
        }
    }

    private <T extends VehicleEntity> void generateScalingAndOffset(EntityType<T> type)
    {
        float min = 0;
        float max = 0;
        for(RayTraceData data : this.entityRayTraceData.get(type))
        {
            ITriangleList triangleList = data.getTriangleList();
            if(triangleList != null)
            {
                for(Triangle triangle : triangleList.getTriangles())
                {
                    float[] vertices = triangle.vertices();
                    for(int i = 0; i < vertices.length; i += 3)
                    {
                        min = Math.min(min, vertices[i]);
                        min = Math.min(min, vertices[i + 1]);
                        min = Math.min(min, vertices[i + 2]);
                        max = Math.max(max, vertices[i]);
                        max = Math.max(max, vertices[i + 1]);
                        max = Math.max(max, vertices[i + 2]);
                    }
                }
            }
        }
        float range = max - min;
        this.entityCrateScalesAndOffsets.put(type, new ImmutablePair<>(1 / (range * 1.25F), -(min + range * 0.5F)));
    }

    private <T extends VehicleEntity> void generateInteractableBoxes(EntityType<T> type, List<MatrixTransform> transforms)
    {
        List<InteractableBox<?>> boxes = this.entityInteractableBoxes.get(type);
        if(boxes != null)
        {
            //noinspection ForLoopReplaceableByForEach
            for(int idx = 0; idx < boxes.size(); idx++)
            {
                InteractableBox<?> box = boxes.get(idx);

                RayTraceData data = box.getData();
                data.clearTriangles();
                data.setMatrix(TransformHelper.createMatrixFromTransformsForInteractionBox(transforms));
                this.entityInteractableBoxData.get(type).add(data);
            }
        }
    }

    /**
     * Gets a list of the ray trace data components from applicable interaction boxes. The applicable
     * state is based on the active predicate of the interactable box. See {@link InteractableBox#isActive(VehicleEntity)}
     *
     * @param entity the entity to get interactable boxes
     * @return a list of ray trace data
     */
    private List<RayTraceData> getApplicableInteractableBoxes(VehicleEntity entity)
    {
        List<InteractableBox<?>> boxes = this.entityInteractableBoxes.get(entity.getType());

        if(boxes != null)
        {
            List<RayTraceData> availableData = new ObjectArrayList<>();

            //noinspection ForLoopReplaceableByForEach
            for(int idx = 0; idx < boxes.size(); idx++)
            {
                InteractableBox<?> box = boxes.get(idx);

                if(box.isActive(entity))
                {
                    availableData.add(box.getData());
                }
            }

            return availableData;
        }

        return Collections.emptyList();
    }

    /**
     * Performs raytrace on interaction boxes and item part triangles of raytraceable entity
     *
     * @param entity the vehicle entity
     * @param eyeVec position of the player's eyes
     * @param forwardVec eyeVec extended by reach distance in the direction the player is looking in
     * @param reach distance at which players can interact with objects in the world
     * @param rightClick whether the click was a right-click or a left-click
     *
     * @return the result of the raytrace
     */
    @Nullable
    public VehicleRayTraceResult rayTraceEntityRotated(VehicleEntity entity, Vec3 eyeVec, Vec3 forwardVec, double reach, boolean rightClick)
    {
        // Rotate the ray trace vectors in the opposite direction as the entity's rotation yaw
        Vec3 entityPos = entity.position();
        double angle = Math.toRadians(-entity.getYRot());
        Vec3 eyeVecRotated = rotateVecXZ(eyeVec, angle, entityPos);
        Vec3 forwardVecRotated = rotateVecXZ(forwardVec, angle, entityPos);
        Vec3 look = forwardVecRotated.subtract(eyeVecRotated).normalize().scale(reach);
        float[] direction = new float[]{(float) look.x, (float) look.y, (float) look.z};

        // Perform ray trace on the entity's interaction boxes
        double distanceShortest = Double.MAX_VALUE;
        InterceptResult lookBox = rayTracePartTriangles(entity, entityPos, eyeVecRotated, distanceShortest, direction, this.getApplicableInteractableBoxes(entity), false, this.entityInteractableBoxData.get(entity.getType()));
        distanceShortest = updateShortestDistance(lookBox, distanceShortest);
        InterceptResult lookPart = rayTracePartTriangles(entity, entityPos, eyeVecRotated, distanceShortest, direction, null, true, this.entityRayTraceData.get(entity.getType()));

        // Allows for dynamic triangles
        if(this.entityDynamicRayTraceData.containsKey(entity.getType()))
        {
            distanceShortest = updateShortestDistance(lookPart, distanceShortest);
            Function<VehicleEntity, List<RayTraceData>> function = this.entityDynamicRayTraceData.get(entity.getType());
            InterceptResult customPart = rayTracePartTriangles(entity, entityPos, eyeVecRotated, distanceShortest, direction, null, true, function.apply(entity));
            if(customPart != null)
            {
                lookPart = customPart;
            }
        }

        InterceptResult result = lookPart != null ? lookPart : lookBox;
        return result != null ? new VehicleRayTraceResult(entity, rotateVecXZ(result.getHitPos(), -angle, entityPos), result.getDistance(), this, result.getPart(), rightClick) : null;
    }

    /**
     * Sets the current shortest distance to the current closest viewed object
     *
     * @param result current closest viewed object
     * @param distanceShortest distance from eyes to the current closest viewed object
     *
     * @return new shortest distance
     */
    private static double updateShortestDistance(InterceptResult result, double distanceShortest)
    {
        if(result != null)
        {
            distanceShortest = result.getDistance();
        }
        return distanceShortest;
    }

    /**
     * Performs raytrace on part triangles of raytraceable entity
     *
     * @param entity raytraced entity
     * @param entityPos position of the raytraced entity
     * @param eyePos position of the player's eyes taking into account the rotation yaw of the raytraced entity
     * @param closestDistance distance from eyes to the current closest viewed object
     * @param direction normalized direction vector the player is looking in scaled by the player reach distance
     * @param partsApplicable list of parts that currently construct to the raytraced entity - if null, all are applicable
     * @param parts triangles for the part
     *
     * @return the result of the part raytrace
     */
    private static InterceptResult rayTracePartTriangles(Entity entity, Vec3 entityPos, Vec3 eyePos, double closestDistance, float[] direction, @Nullable List<RayTraceData> partsApplicable, boolean invalidateParts, List<RayTraceData> parts)
    {
        InterceptResult closestResult = null;
        if(parts != null)
        {
            for(RayTraceData data : parts)
            {
                if(partsApplicable == null || (invalidateParts != partsApplicable.contains(data)))
                {
                    ITriangleList triangleList = data.getTriangleList();
                    if(triangleList == null)
                        continue;

                    for(Triangle triangle : triangleList.getTriangles(data, entity))
                    {
                        InterceptResult result = InterceptResult.calculate(entityPos, eyePos, direction, triangle.vertices(), data);
                        if(result != null && result.getDistance() < closestDistance)
                        {
                            closestResult = result;
                            closestDistance = result.getDistance();
                        }
                    }
                }
            }
        }
        return closestResult;
    }

    /**
     * Rotates the x and z components of a vector about the y axis
     *
     * @param vec vector to rotate
     * @param angle angle in radians to rotate about the y axis
     * @param rotationPoint vector containing the x/z position to rotate around
     *
     * @return the passed vector rotated by 'angle' around 'rotationPoint'
     */
    private static Vec3 rotateVecXZ(Vec3 vec, double angle, Vec3 rotationPoint)
    {
        double x = rotationPoint.x + Math.cos(angle) * (vec.x - rotationPoint.x) - Math.sin(angle) * (vec.z - rotationPoint.z);
        double z = rotationPoint.z + Math.sin(angle) * (vec.x - rotationPoint.x) + Math.cos(angle) * (vec.z - rotationPoint.z);
        return new Vec3(x, vec.y, z);
    }

    public <T extends VehicleEntity> void renderRayTraceElements(T entity, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, float yaw)
    {
        if(!Config.CLIENT.renderOutlines.get())
            return;

        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-yaw));
        VertexConsumer builder = renderTypeBuffer.getBuffer(RenderType.LINES);
        this.renderRayTraceTriangles(entity, matrixStack, builder);
        matrixStack.popPose();
    }

    /**
     * Renders the triangles of the parts of a raytraceable entity
     *
     * @param entity raytraced entity
     * @param matrixStack the current matrix stack
     * @param builder tessellator's vertex buffer
     */
    @SuppressWarnings("unchecked")
    private <T extends VehicleEntity> void renderRayTraceTriangles(T entity, PoseStack matrixStack, VertexConsumer builder)
    {
        EntityType<T> type = (EntityType<T>) entity.getType();
        this.initializeTransforms(type);
        drawTriangleList(entity, this.entityRayTraceData.get(type), matrixStack, builder, 0xFFB64C);
        drawTriangleList(entity, this.entityInteractableBoxData.get(type), matrixStack, builder, 0x00FF00);

        if(this.entityDynamicRayTraceData.containsKey(entity.getType()))
        {
            Function<VehicleEntity, List<RayTraceData>> function = this.entityDynamicRayTraceData.get(entity.getType());
            drawTriangleList(entity, function.apply(entity), matrixStack, builder, 0xFFB64C);
        }
    }

    private static <T extends VehicleEntity> void drawTriangleList(T entity, @Nullable List<RayTraceData> dataList, PoseStack matrixStack, VertexConsumer builder, int baseColor)
    {
        Optional.ofNullable(dataList).ifPresent(list ->
        {
            list.forEach(data ->
            {
                if(!Config.CLIENT.forceRenderAllInteractableBoxes.get() && data instanceof InteractableBoxRayTraceData interactableBoxData)
                {
                    if(!interactableBoxData.getInteractableBox().isActive(entity))
                    {
                        return;
                    }
                }
                int color = data.getRayTraceFunction() != null ? 0x00FF00 : baseColor;
                float r = (float) (color >> 16 & 255) / 255.0F;
                float g = (float) (color >> 8 & 255) / 255.0F;
                float b = (float) (color & 255) / 255.0F;
                ITriangleList triangleList = data.getTriangleList();
                if(triangleList != null)
                {
                    triangleList.getTriangles(data, entity).forEach(triangle -> triangle.draw(matrixStack, builder, r, g, b, 0.4F));
                }
            });
        });
    }

    public static void renderShape(PoseStack matrixStack, VertexConsumer builder, VoxelShape shape, float red, float green, float blue, float alpha)
    {
        Matrix4f pose = matrixStack.last().pose();
        shape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> {
            builder.vertex(pose, (float) minX, (float) minY, (float) minZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(pose, (float) maxX, (float) maxY, (float) maxZ).color(red, green, blue, alpha).endVertex();
        });
    }

    /**
     * Converts interaction box to list of triangles that represents it
     *
     * @param box raytraceable interaction box
     * @param matrixFactory function for dynamic triangles that takes the part and the raytraced
     * entity as arguments and outputs that part's dynamically generated transformation matrix
     *
     * @return triangle list
     */
    public static ITriangleList boxToTriangles(AABB box, @Nullable BiFunction<RayTraceData, Entity, Matrix4f> matrixFactory)
    {
        int size = 3;
        List<Triangle> triangles = Lists.newArrayList();
        createTrianglesFromDataAndAdd(triangles, null, size, new Double[]{box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.minY, box.minZ, box.minX, box.minY, box.minZ}, Double::floatValue);
        createTrianglesFromDataAndAdd(triangles, null, size, new Double[]{box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.minY, box.minZ}, Double::floatValue);
        createTrianglesFromDataAndAdd(triangles, null, size, new Double[]{box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ}, Double::floatValue);
        createTrianglesFromDataAndAdd(triangles, null, size, new Double[]{box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ}, Double::floatValue);
        createTrianglesFromDataAndAdd(triangles, null, size, new Double[]{box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ}, Double::floatValue);
        createTrianglesFromDataAndAdd(triangles, null, size, new Double[]{box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ}, Double::floatValue);
        return new TriangleList(triangles, matrixFactory);
    }

    /**
     * Version of {@link EntityRayTracer#boxToTriangles(AABB, BiFunction) boxToTriangles}
     * without a matrix-generating function for static interaction boxes
     *
     * @param box raytraceable interaction box
     *
     * @return triangle list
     */
    public static ITriangleList boxToTriangles(AABB box)
    {
        return boxToTriangles(box, null);
    }

    /**
     * Converts quad into a pair of triangles that represents it
     * @param triangles list of all triangles for the given raytraceable entity class
     * @param matrix
     * @param data four vertices of a quad
     */
    private static <T> void createTrianglesFromDataAndAdd(List<Triangle> triangles, Matrix4f matrix, int size, T[] data, Function<T, Float> cast)
    {
        float[] triangle1 = new float[9];
        float[] triangle2 = new float[9];
        // Corner 1
        triangle1[0] = cast.apply(data[0]);
        triangle1[1] = cast.apply(data[1]);
        triangle1[2] = cast.apply(data[2]);
        // Corner 2
        triangle1[3] = triangle2[6] = cast.apply(data[size]);
        triangle1[4] = triangle2[7] = cast.apply(data[size + 1]);
        triangle1[5] = triangle2[8] = cast.apply(data[size + 2]);
        // Corner 3
        size *= 2;
        triangle2[0] = cast.apply(data[size]);
        triangle2[1] = cast.apply(data[size + 1]);
        triangle2[2] = cast.apply(data[size + 2]);
        // Corner 4
        size *= 1.5;
        triangle1[6] = triangle2[3] = cast.apply(data[size]);
        triangle1[7] = triangle2[4] = cast.apply(data[size + 1]);
        triangle1[8] = triangle2[5] = cast.apply(data[size + 2]);
        transformTriangleAndAdd(triangle1, matrix, triangles);
        transformTriangleAndAdd(triangle2, matrix, triangles);
    }

    /**
     * Interface that allows entities to be raytraceable
     * <p>
     * <strong>Note:</strong>
     * <ul>
     *     <li>This must be implemented by all entities that raytraces are to be performed on.</li>
     *     <li>Only classes that extend {@link Entity} should implement this interface.</li>
     * </ul>
     */
    private boolean processHit(VehicleRayTraceResult result)
    {
        System.out.println("Processing hit...");

        RayTraceData datac = result.getData();
        Entity entityc = result.getEntity();

        System.out.println("Processing hit...");
        System.out.println("[KEY DEBUG] data class = " +
                (datac == null ? "NULL" : datac.getClass().getName()));

        if(result.getData() instanceof ComponentModelRayTraceData data)
        {
            ComponentModel model = data.getModel();

            System.out.println("HIT MODEL: " + model.getModelLocation());
            System.out.println("KEY MODEL: " + VehicleModels.KEY_HOLE.getModelLocation());
            System.out.println(
                    "[RAYTRACE] HIT = " + data.getModel().getModelLocation()
            );

            if(model == VehicleModels.KEY_HOLE || model == VehicleModels.KEYHOLE_ATV || model == VehicleModels.KEY_HOLE_BC || model == VehicleModels.KEYHOLE_CH || model == VehicleModels.KEYHOLE_DBI || model == VehicleModels.KEYHOLE_DBU || model == VehicleModels.KEYHOLE_GK || model == VehicleModels.GOLF_KH || model == VehicleModels.KEYHOLE_JK || model == VehicleModels.KEYHOLE_LM || model == VehicleModels.KEYHOLE_MBI || model == VehicleModels.MINI_BUS_KEYHOLE || model == VehicleModels.KEYHOLE_MOPED || model == VehicleModels.KEYHOLE_OFFROADER || model == VehicleModels.KEYHOLE_QB || model == VehicleModels.KEYHOLE_SC || model == VehicleModels.KEYHOLE_SB || model == VehicleModels.KEYHOLE_SPC || model == VehicleModels.SPORTS_PLANE_KEYHOLE || model == VehicleModels.KEYHOLE_TRACTOR)
            {
                System.out.println("KEYHOLE HIT!");

                PacketHandler.getPlayChannel().sendToServer(
                        new MessageInteractKey(result.getEntity()));

                return true;
            }
        }

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null)
            return false;

        Entity entity = result.getEntity();
        boolean rightClick = result.isRightClick();

        if(MinecraftForge.EVENT_BUS.post(new VehicleRayTraceEvent(mc.player, result)))
            return false;

        if(entity instanceof VehicleEntity && this.entityInteractableBoxes.containsKey(entity.getType()))
        {
            Optional<InteractableBox<?>> optional = this.entityInteractableBoxes.get(entity.getType()).stream().filter(box -> box.getData() == result.getData()).findFirst();
            if(optional.isPresent())
            {
                optional.get().handle((VehicleEntity) entity, rightClick);
                return true;
            }
        }

        RayTraceData data = result.getData();
        if(!mc.player.isCrouching() && entity instanceof VehicleEntity vehicle && data instanceof CosmeticRayTraceData)
        {
            mc.player.swing(InteractionHand.MAIN_HAND);
            ResourceLocation cosmeticId = ((CosmeticRayTraceData) data).getCosmeticId();
            Collection<Action> actions = vehicle.getCosmeticTracker().getActions(cosmeticId);
            if(!actions.isEmpty())
            {
                actions.forEach(action -> action.onInteract(vehicle, mc.player));
                PacketHandler.getPlayChannel().sendToServer(new MessageInteractCosmetic(vehicle.getId(), cosmeticId));
                return true;
            }
        }

        boolean isContinuous = result.getData().getRayTraceFunction() != null;
        if(isContinuous || !(mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult) mc.hitResult).getEntity() == entity))
        {
            Player player = mc.player;
            boolean notRiding = player.getVehicle() != entity;
            if(!rightClick && notRiding)
            {
                mc.gameMode.attack(player, entity);
                return true;
            }
            if(notRiding)
            {
                if(player.isCrouching() && !player.isSpectator())
                {
                    PacketHandler.getPlayChannel().sendToServer(new MessagePickupVehicle(entity));
                    return true;
                }
                if(!isContinuous)
                {
                    interactWithEntity(entity, result);
                }
            }
            return notRiding;
        }
        return false;
    }
}