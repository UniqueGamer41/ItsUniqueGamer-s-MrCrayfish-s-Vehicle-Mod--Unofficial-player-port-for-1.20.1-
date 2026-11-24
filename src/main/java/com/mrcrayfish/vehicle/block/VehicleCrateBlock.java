package com.mrcrayfish.vehicle.block;

import com.google.common.base.Strings;
import com.mrcrayfish.vehicle.init.ModBlocks;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.init.ModTileEntities;
import com.mrcrayfish.vehicle.item.IDyeable;
import com.mrcrayfish.vehicle.block.entity.VehicleCrateBlockEntity;
import com.mrcrayfish.vehicle.client.render.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("deprecation")
public class VehicleCrateBlock extends RotatedEntityObjectBlock
{
    public static final List<ResourceLocation> REGISTERED_CRATES = new ArrayList<>();
    private static final VoxelShape PANEL = box(0, 0, 0, 16, 2, 16);

    public VehicleCrateBlock()
    {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .dynamicShape()
                .noOcclusion()
                .strength(1.5F, 5.0F)
        );
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos)
    {
        return true;
    }

    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState state, BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx)
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof VehicleCrateBlockEntity && ((VehicleCrateBlockEntity) blockEntity).isOpened())
        {
            return PANEL;
        }

        return Shapes.block();
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos)
    {
        return this.isBelowBlockTopSolid(level, pos) && this.canOpen(level, pos);
    }

    private boolean canOpen(LevelReader reader, BlockPos pos)
    {
        for(Direction side : Direction.Plane.HORIZONTAL)
        {
            BlockPos adjacentPos = pos.relative(side);
            BlockState state = reader.getBlockState(adjacentPos);
            if(state.isAir())
                continue;
            if(!state.canBeReplaced() || this.isBelowBlockTopSolid(reader, adjacentPos))
            {
                return false;
            }
        }
        return true;
    }

    private boolean isBelowBlockTopSolid(LevelReader reader, BlockPos pos)
    {
        return reader.getBlockState(pos.below()).isFaceSturdy(reader, pos.below(), Direction.UP);
    }

    @Override
    @NotNull
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, BlockHitResult result)
    {
        if(result.getDirection() == Direction.UP && player.getItemInHand(hand).is(ModItems.WRENCH.get()))
        {
            this.openCrate(level, pos, state, player);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity entity, @NotNull ItemStack stack)
    {
        if(entity instanceof Player && ((Player) entity).isCreative())
        {
            this.openCrate(level, pos, state, entity);
        }
    }

    private void openCrate(LevelReader world, BlockPos pos, BlockState state, LivingEntity placer)
    {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof VehicleCrateBlockEntity && this.canOpen(world, pos))
        {
            if(world.isClientSide())
            {
                this.spawnCrateOpeningParticles((ClientLevel) world, pos, state);
            }
            else
            {
                ((VehicleCrateBlockEntity) tileEntity).open(placer.getUUID());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnCrateOpeningParticles(ClientLevel world, BlockPos pos, BlockState state)
    {
        double y = 0.875;
        double x, z;

        TerrainParticle.Provider provider = new TerrainParticle.Provider();
        for(int j = 0; j < 4; ++j)
        {
            for(int l = 0; l < 4; ++l)
            {
                x = (j + 0.5D) / 4.0D;
                z = (l + 0.5D) / 4.0D;

                Minecraft.getInstance().particleEngine.add(provider.createParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        world,
                        pos.getX() + x, pos.getY() + y, pos.getZ() + z,
                        x - 0.5D, y - 0.5D, z - 0.5D)
                );
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state)
    {
        return new VehicleCrateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type)
    {
        if(level.isClientSide())
        {
            return createTickerHelper(type, ModTileEntities.VEHICLE_CRATE.get(), VehicleCrateBlockEntity::onClientTick);
        }

        return createTickerHelper(type, ModTileEntities.VEHICLE_CRATE.get(), VehicleCrateBlockEntity::onServerTick);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltips, @NotNull TooltipFlag flag)
    {
        Component vehicleName = EntityType.PIG.getDescription();
        CompoundTag tagCompound = stack.getTag();
        if(tagCompound != null)
        {
            if(tagCompound.contains("BlockEntityTag", CompoundTag.TAG_COMPOUND))
            {
                CompoundTag blockEntityTag = tagCompound.getCompound("BlockEntityTag");
                String entityType = blockEntityTag.getString("vehicle");
                if(!Strings.isNullOrEmpty(entityType))
                {
                    vehicleName = EntityType.byString(entityType).orElse(EntityType.PIG).getDescription();
                }
            }
        }

        if(Screen.hasShiftDown())
        {
            tooltips.addAll(RenderUtil.lines(Component.translatable(this.getDescriptionId() + ".info", vehicleName), 150));
        }
        else
        {
            tooltips.add(vehicleName.copy().withStyle(ChatFormatting.BLUE));
            tooltips.add(Component.translatable("vehicle.info_help").withStyle(ChatFormatting.YELLOW));
        }
    }

    public static ItemStack create(ResourceLocation entityId, int color, ItemStack engine, ItemStack wheel)
    {
        CompoundTag blockEntityTag = new CompoundTag();
        blockEntityTag.putString("vehicle", entityId.toString());
        blockEntityTag.putInt(IDyeable.NBT_KEY, color);
        blockEntityTag.put("engineStack", engine.save(new CompoundTag()));
        blockEntityTag.put("wheelStack", wheel.save(new CompoundTag()));
        CompoundTag itemTag = new CompoundTag();
        itemTag.put("BlockEntityTag", blockEntityTag);
        ItemStack stack = new ItemStack(ModBlocks.VEHICLE_CRATE.get());
        stack.setTag(itemTag);
        return stack;
    }

    public static synchronized void registerVehicle(ResourceLocation id)
    {
        if(!REGISTERED_CRATES.contains(id))
        {
            REGISTERED_CRATES.add(id);
            Collections.sort(REGISTERED_CRATES);
        }
    }
}
