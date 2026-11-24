package com.mrcrayfish.vehicle.client;

import org.joml.Quaternionf;
import com.mrcrayfish.vehicle.util.port.Quaternion;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.util.MathUtil;
import com.mrcrayfish.vehicle.common.Seat;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.properties.VehicleProperties;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.TransformationHelper;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.mrcrayfish.vehicle.VehicleMod.LOGGER;

/**
 * A helper class that manages the camera rotations for vehicles
 *
 * Author: MrCrayfish
 */
@OnlyIn(Dist.CLIENT)
public class CameraHelper
{
    private static final Method SET_POSITION_METHOD = ObfuscationReflectionHelper.findMethod(Camera.class, "m_90584_", double.class, double.class, double.class);
    private static final Method MOVE_METHOD = ObfuscationReflectionHelper.findMethod(Camera.class, "m_90568_", double.class, double.class, double.class);
    private static final Method GET_MAX_MOVE_METHOD = ObfuscationReflectionHelper.findMethod(Camera.class, "m_90566_", double.class);
    private static final Field LEFT_FIELD = ObfuscationReflectionHelper.findField(Camera.class, "f_90556_");

    private VehicleProperties properties;
    private Quaternion currentRotation;
    private Quaternion prevRotation;
    private float pitchOffset;
    private float yawOffset;

    // Debug properties
    public float debugOffsetX;
    public float debugOffsetY;
    public float debugOffsetZ;
    public float debugOffsetPitch;
    public float debugOffsetYaw;
    public float debugOffsetRoll;
    public boolean debugEnableStrength = true;

    public void load(VehicleEntity vehicle)
    {
        this.properties = vehicle.getProperties();
        this.pitchOffset = 0F;
        this.yawOffset = 0F;
        this.currentRotation = new Quaternion(vehicle.getViewPitch(0F), -vehicle.getViewYaw(0F), 0F, true);
        this.prevRotation = new Quaternion(this.currentRotation);
    }

    public void tick(VehicleEntity vehicle, CameraType pov)
    {
        float strength = this.getStrength(pov);
        this.prevRotation = this.currentRotation;
        Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
        quaternion.mul(Vector3f.YP.rotationDegrees(-vehicle.getViewYaw(1F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetYaw : 0F)));
        quaternion.mul(Vector3f.XP.rotationDegrees(vehicle.getViewPitch(1F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetPitch : 0F)));
        //quaternion.mul(Vector3f.ZP.rotationDegrees(vehicle.getViewRoll(1F) + (Config.CLIENT.debugCamera.get() ? this.debugOffsetRoll : 0F)));
        this.currentRotation = MathUtil.slerp(this.currentRotation, quaternion, strength);

        // REMOVED THE CAMERA RE-CENTERING FIX SECTION
        // The original 1.19.2 code didn't have this, and it works correctly
    }

    private float getStrength(CameraType pov)
    {
        return (!Config.CLIENT.debugCamera.get() || this.debugEnableStrength) && pov == CameraType.THIRD_PERSON_BACK && this.properties.getCamera().getType() != CameraProperties.Type.LOCKED ? this.properties.getCamera().getStrength() : 1.0F;
    }

    public void setupVanillaCamera(Camera info, CameraType pov, VehicleEntity vehicle, AbstractClientPlayer player, float partialTicks)
    {
        switch (pov) {
            case FIRST_PERSON -> this.setupFirstPersonCamera(info, vehicle, player, partialTicks);
            case THIRD_PERSON_BACK -> this.setupThirdPersonCamera(info, vehicle, player, partialTicks, false);
            case THIRD_PERSON_FRONT -> this.setupThirdPersonCamera(info, vehicle, player, partialTicks, true);
        }
    }

    private void setupFirstPersonCamera(Camera info, VehicleEntity vehicle, AbstractClientPlayer player, float partialTicks)
    {
        try
        {
            int index = vehicle.getSeatTracker().getSeatIndex(player.getUUID());
            if(index != -1)
            {
                if(Config.CLIENT.followVehicleOrientation.get())
                {
                    this.setVehicleRotation(info, vehicle, player, partialTicks);
                }

                Seat seat = this.properties.getSeats().get(index);
                Vec3 eyePos = seat.getPosition().add(0, this.properties.getAxleOffset() + this.properties.getWheelOffset(), 0).scale(this.properties.getBodyTransform().getScale()).multiply(-1, 1, 1).add(this.properties.getBodyTransform().getTranslate()).scale(0.0625);
                eyePos = eyePos.add(0, player.getMyRidingOffset() + player.getEyeHeight(), 0);
                Vector3f rotatedEyePos = new Vector3f(eyePos.toVector3f());
                rotatedEyePos.transform(MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks));
                float cameraX = (float) (Mth.lerp(partialTicks, vehicle.xo, vehicle.getX()) + rotatedEyePos.x());
                float cameraY = (float) (Mth.lerp(partialTicks, vehicle.yo, vehicle.getY()) + rotatedEyePos.y());
                float cameraZ = (float) (Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ()) + rotatedEyePos.z());
                SET_POSITION_METHOD.invoke(info, cameraX, cameraY, cameraZ);
            }
        }
        catch(InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void setupThirdPersonCamera(Camera info, VehicleEntity vehicle, AbstractClientPlayer player, float partialTicks, boolean front)
    {
        try
        {
            if(Config.CLIENT.followVehicleOrientation.get())
            {
                this.setVehicleRotation(info, vehicle, player, partialTicks);
            }

            if(Config.CLIENT.useVehicleAsFocusPoint.get() && !front)
            {
                Vec3 position = this.properties.getCamera().getPosition();
                Vector3f rotatedPosition = new Vector3f(position.toVector3f());
                if(Config.CLIENT.debugCamera.get()) rotatedPosition.add(this.debugOffsetX, this.debugOffsetY, this.debugOffsetZ);
                rotatedPosition.transform(MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks));
                float cameraX = (float) (Mth.lerp(partialTicks, vehicle.xo, vehicle.getX()) + rotatedPosition.x());
                float cameraY = (float) (Mth.lerp(partialTicks, vehicle.yo, vehicle.getY()) + rotatedPosition.y());
                float cameraZ = (float) (Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ()) + rotatedPosition.z());
                SET_POSITION_METHOD.invoke(info, cameraX, cameraY, cameraZ);
            }
            else
            {
                int index = vehicle.getSeatTracker().getSeatIndex(player.getUUID());
                if(index != -1)
                {
                    Seat seat = this.properties.getSeats().get(index);
                    Vec3 eyePos = seat.getPosition().add(0, this.properties.getAxleOffset() + this.properties.getWheelOffset(), 0).scale(this.properties.getBodyTransform().getScale()).multiply(-1, 1, 1).add(this.properties.getBodyTransform().getTranslate()).scale(0.0625);
                    eyePos = eyePos.add(0, player.getMyRidingOffset() + player.getEyeHeight(), 0);
                    Vector3f rotatedEyePos = new Vector3f(eyePos.toVector3f());
                    rotatedEyePos.transform(TransformationHelper.slerp(this.prevRotation, this.currentRotation, partialTicks));
                    float cameraX = (float) (Mth.lerp(partialTicks, vehicle.xo, vehicle.getX()) + rotatedEyePos.x());
                    float cameraY = (float) (Mth.lerp(partialTicks, vehicle.yo, vehicle.getY()) + rotatedEyePos.y());
                    float cameraZ = (float) (Mth.lerp(partialTicks, vehicle.zo, vehicle.getZ()) + rotatedEyePos.z());
                    SET_POSITION_METHOD.invoke(info, cameraX, cameraY, cameraZ);
                }
            }

            double distance = front ? 4.0 : this.properties.getCamera().getDistance();
            LOGGER.info("Camera distance = {}", this.properties.getCamera().getDistance());
            MOVE_METHOD.invoke(info, -distance, 0.0, 0.0);
        }
        catch(InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void setVehicleRotation(Camera info, VehicleEntity vehicle, AbstractClientPlayer player, float partialTicks)
    {
        try
        {
            Quaternionf rotation = info.rotation();
            rotation.set(0.0F, 0.0F, 0.0F, 1.0F);

            rotation.mul(MathUtil.slerp(this.prevRotation, this.currentRotation, partialTicks));

            // Applies the player's pitch and yaw offset
            Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);

            if(VehicleHelper.isThirdPersonFront())
            {
                quaternion.mul(Vector3f.YP.rotationDegrees(180F));
            }

            if(vehicle.canApplyYawOffset(player) && Config.CLIENT.shouldFollowYaw.get())
            {
                quaternion.mul(Vector3f.YP.rotationDegrees(-this.yawOffset));
            }
            else
            {
                // Use the player's current view rotation relative to the vehicle
                quaternion.mul(Vector3f.YP.rotationDegrees(-player.getViewYRot(partialTicks)));

                // Add back the vehicle's base yaw if following orientation
                if(Config.CLIENT.shouldFollowYaw.get())
                {
                    quaternion.mul(Vector3f.YP.rotationDegrees(vehicle.getViewYaw(partialTicks)));
                }
            }

            if(Config.CLIENT.shouldFollowPitch.get())
            {
                quaternion.mul(Vector3f.XP.rotationDegrees(VehicleHelper.isThirdPersonFront() ? -this.pitchOffset : this.pitchOffset));
            }
            else
            {
                quaternion.mul(Vector3f.XP.rotationDegrees(Mth.lerp(partialTicks, player.xRotO, player.xRot)));
            }

            // If the player is in third person, applies additional vehicle specific camera rotations
            if(Config.CLIENT.useVehicleAsFocusPoint.get() && VehicleHelper.isThirdPersonBack())
            {
                CameraProperties camera = vehicle.getProperties().getCamera();
                Vec3 cameraRotation = camera.getRotation();
                quaternion.mul(Vector3f.YP.rotationDegrees((float) cameraRotation.y));
                quaternion.mul(Vector3f.XP.rotationDegrees((float) cameraRotation.x));
                quaternion.mul(Vector3f.ZP.rotationDegrees((float) cameraRotation.z));
            }

            // Finally applies local rotations to the camera
            rotation.mul(quaternion);

            org.joml.Vector3f forward =  info.getLookVector();
            forward.set(0.0F, 0.0F, 1.0F);
            forward.rotate(rotation);

            org.joml.Vector3f up =  info.getUpVector();
            up.set(0.0F, 1.0F, 0.0F);
            up.rotate(rotation);

            org.joml.Vector3f left = (org.joml.Vector3f) LEFT_FIELD.get(info);
            left.set(1.0F, 0.0F, 0.0F);
            left.rotate(rotation);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public void turnPlayerView(double x, double y)
    {
        this.pitchOffset += y * 0.15F;
        this.yawOffset += x * 0.15F;
        this.pitchOffset = Mth.clamp(this.pitchOffset, -90F, 90F);
        this.yawOffset = Mth.clamp(this.yawOffset, -120F, 120F);
    }
}