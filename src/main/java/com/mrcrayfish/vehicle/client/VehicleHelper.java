package com.mrcrayfish.vehicle.client;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Buttons;
import com.mrcrayfish.controllable.client.Controller;
import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.audio.MovingEngineSound;
import com.mrcrayfish.vehicle.client.audio.MovingHornSound;
import com.mrcrayfish.vehicle.client.handler.ControllerHandler;
import com.mrcrayfish.vehicle.entity.HelicopterEntity;
import com.mrcrayfish.vehicle.entity.PoweredVehicleEntity;
import com.mrcrayfish.vehicle.init.ModParticleTypes;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
public class VehicleHelper
{
    private static final Minecraft MINECRAFT = Minecraft.getInstance();
    private static final WeakHashMap<PoweredVehicleEntity, EnumMap<SoundType, TickableSoundInstance>> SOUND_TRACKER = new WeakHashMap<>();

    public static void tryPlayEngineSound(PoweredVehicleEntity vehicle, RandomSource source)
    {
        if(vehicle.getEngineSound() != null && vehicle.getControllingPassenger() != null && vehicle.isEnginePowered())
        {
            Map<SoundType, TickableSoundInstance> soundMap = SOUND_TRACKER.computeIfAbsent(vehicle, v -> new EnumMap<>(SoundType.class));
            TickableSoundInstance sound = soundMap.get(SoundType.ENGINE);
            if(sound == null || sound.isStopped() || !MINECRAFT.getSoundManager().isActive(sound))
            {
                sound = new MovingEngineSound(MINECRAFT.player, vehicle, source);
                soundMap.put(SoundType.ENGINE, sound);
                MINECRAFT.getSoundManager().play(sound);
            }
        }
    }

    public static void tryPlayHornSound(PoweredVehicleEntity vehicle, RandomSource source)
    {
        if(vehicle.hasHorn() && vehicle.getHornSound() != null)
        {
            Map<SoundType, TickableSoundInstance> soundMap = SOUND_TRACKER.computeIfAbsent(vehicle, v -> new EnumMap<>(SoundType.class));
            TickableSoundInstance sound = soundMap.get(SoundType.HORN);
            if(sound == null || sound.isStopped() || !MINECRAFT.getSoundManager().isActive(sound))
            {
                sound = new MovingHornSound(MINECRAFT.player, vehicle, source);
                soundMap.put(SoundType.HORN, sound);
                MINECRAFT.getSoundManager().play(sound);
            }
        }
    }

    public static void playSound(SoundEvent soundEvent, BlockPos pos, float volume, float pitch)
    {
        SoundInstance sound = new SimpleSoundInstance(soundEvent, SoundSource.BLOCKS, volume, pitch, RandomSource.createNewThreadLocalInstance(), pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F);
        MINECRAFT.submitAsync(() -> MINECRAFT.getSoundManager().play(sound));
    }

    public static void playSound(SoundEvent soundEvent, float volume, float pitch)
    {
        MINECRAFT.submitAsync(() -> MINECRAFT.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, volume, pitch)));
    }

    @OnlyIn(Dist.CLIENT)
    public static float getSteeringAngle(PoweredVehicleEntity vehicle)
    {
        float steeringAngle = vehicle.getSteeringAngle();
        if(vehicle.getControllingPassenger() != null)
        {
            Entity entity = vehicle.getControllingPassenger();
            if(!(entity instanceof LivingEntity livingEntity))
                return 0F;

            if(ClientHandler.isControllableLoaded())
            {
                Controller controller = Controllable.getController();
                if(Controllable.getInput().isControllerInUse() && controller != null)
                {
                    float leftStick = -Mth.clamp(controller.getLThumbStickXValue(), -1.0F, 1.0F);
                    float strengthModifier = Math.abs(leftStick) > 0.1F ? 0.1F : 0.2F;
                    return steeringAngle + (vehicle.getMaxSteeringAngle() * leftStick - steeringAngle) * strengthModifier;
                }
            }

            float turnValue = Mth.clamp(livingEntity.xxa, -1.0F, 1.0F);
            float strengthModifier = livingEntity.xxa != 0 ? 0.05F : 0.2F;
            return steeringAngle + (vehicle.getMaxSteeringAngle() * turnValue - steeringAngle) * strengthModifier;
        }
        return steeringAngle * 0.85F;
    }



    public static boolean isHandbraking()
    {
        if(ClientHandler.isControllableLoaded())
        {
            Controller controller = Controllable.getController();
            if(controller != null && Controllable.getInput().isControllerInUse())
            {
                if(controller.isButtonPressed(Buttons.RIGHT_BUMPER))
                {
                    return true;
                }
            }
        }
        return MINECRAFT.options.keyJump.isDown();
    }

    public static boolean isHonking()
    {
        if(ClientHandler.isControllableLoaded())
        {
            Controller controller = Controllable.getController();
            if(controller != null && Controllable.getInput().isControllerInUse())
            {

                //ControllerHandler.HORN.getButton() -> Buttons.RIGHT_THUM_STICK
                if(controller.isButtonPressed(Buttons.RIGHT_THUMB_STICK))
                {
                    return true;
                }
            }
        }
        return KeyBinds.KEY_HORN.isDown();
    }

    public static float getLift()
    {
        float up = MINECRAFT.options.keyJump.isDown() ? 1.0F : 0F;
        float down = MINECRAFT.options.keySprint.isDown() ? -1.0F : 0F;
        if(ClientHandler.isControllableLoaded())
        {
            Controller controller = Controllable.getController();
            if(controller != null && Controllable.getInput().isControllerInUse())
            {
                up = getTriggerInput(Buttons.A);
                down = -getTriggerInput(Buttons.B);
            }
        }
        return up + down;
    }

    public static float getElevator()
    {
        if(ClientHandler.isControllableLoaded())
        {
            Controller controller = Controllable.getController();
            if(controller != null && Controllable.getInput().isControllerInUse())
            {
                return controller.getLThumbStickYValue();
            }
        }
        float up = MINECRAFT.options.keyJump.isDown() ? 1.0F : 0F;
        float down = MINECRAFT.options.keySprint.isDown() ? -1.0F : 0F;
        return up + down;
    }

    public static float getTravelDirection(HelicopterEntity vehicle)
    {
        if(ClientHandler.isControllableLoaded())
        {
            Controller controller = Controllable.getController();
            if(controller != null)
            {
                float xAxis = controller.getLThumbStickXValue();
                float yAxis = controller.getLThumbStickYValue();
                if(xAxis != 0.0F || yAxis != 0.0F)
                {
                    float angle = (float) Math.toDegrees(Math.atan2(-xAxis, yAxis)) + 180F;
                    return vehicle.getYRot() + angle;
                }
            }
        }

        //TODO fix keyboard movement for heli
        /*PoweredVehicleEntity.AccelerationDirection accelerationDirection = vehicle.getAcceleration();
        PoweredVehicleEntity.TurnDirection turnDirection = vehicle.getTurnDirection();
        if(vehicle.getControllingPassenger() != null)
        {
            if(accelerationDirection == PoweredVehicleEntity.AccelerationDirection.FORWARD)
            {
                return vehicle.yRot + turnDirection.getDir() * -45F;
            }
            else if(accelerationDirection == PoweredVehicleEntity.AccelerationDirection.REVERSE)
            {
                return vehicle.yRot + 180F + turnDirection.getDir() * 45F;
            }
            else
            {
                return vehicle.yRot + turnDirection.getDir() * -90F;
            }
        }*/
        return vehicle.getYRot();
    }

    public static float getTravelSpeed(HelicopterEntity helicopter)
    {
        if(ClientHandler.isControllableLoaded())
        {
            Controller controller = Controllable.getController();
            if(controller != null)
            {
                float xAxis = controller.getLThumbStickXValue();
                float yAxis = controller.getLThumbStickYValue();
                if(xAxis != 0.0F || yAxis != 0.0F)
                {
                    return (float) Math.min(1.0, Math.sqrt(Math.pow(xAxis, 2) + Math.pow(yAxis, 2)));
                }
            }
        }
        return 0F; //TODO fix heli travel speed
        //return helicopter.getAcceleration() != PoweredVehicleEntity.AccelerationDirection.NONE || helicopter.getTurnDirection() != PoweredVehicleEntity.TurnDirection.FORWARD ? 1.0F : 0.0F;
    }

    public static float getThrottle(LivingEntity livingEntity)
    {
        if(ClientHandler.isControllableLoaded())
        {
            Controller controller = Controllable.getController();
            if(controller != null && Controllable.getInput().isControllerInUse())
            {
                boolean forward = Controllable.isButtonPressed(Buttons.A);
                boolean reverse = Controllable.isButtonPressed(Buttons.B);
                if(forward && !reverse)
                {
                    return getTriggerInput(Buttons.A);
                }
                else if(!forward && reverse)
                {
                    return -getTriggerInput(Buttons.B);
                }
                return 0.0F;
            }
        }
        return Mth.clamp(livingEntity.zza, -1.0F, 1.0F);
    }

    @OnlyIn(Dist.CLIENT)
    private static float getTriggerInput(int button)
    {
        Controller controller = Controllable.getController();
        if(controller == null || !Controllable.getInput().isControllerInUse())
            return 0.0F;

        if(button == Buttons.RIGHT_TRIGGER)
        {
            return (controller.getRTriggerValue() - 0.1F) / 0.9F;
        }
        else if(button == Buttons.LEFT_TRIGGER)
        {
            return (controller.getLTriggerValue() - 0.1F) / 0.9F;
        }
        return controller.isButtonPressed(button) ? 1.0F : 0.0F;
    }

    public static boolean canFollowVehicleOrientation(Entity passenger)
    {
        if(passenger.equals(MINECRAFT.player))
        {
            return Config.CLIENT.followVehicleOrientation.get();
        }
        return false;
    }

    public static void spawnWheelParticle(BlockPos pos, BlockState state, double x, double y, double z, Vec3 motion)
    {
        Minecraft mc = MINECRAFT;
        ClientLevel world = mc.level;
        if(world != null)
        {
            TerrainParticle.Provider provider = new TerrainParticle.Provider();

            Particle particle = provider.createParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                    world,
                    x, y, z,
                    motion.x, motion.y, motion.z);

            particle.setPower((float) motion.length());
            mc.particleEngine.add(particle);
        }
    }

    public static void spawnSmokeParticle(double x, double y, double z, Vec3 motion)
    {
        Minecraft mc = MINECRAFT;
        ClientLevel world = mc.level;
        if(world != null)
        {
            Particle particle = mc.particleEngine.createParticle(ModParticleTypes.TYRE_SMOKE.get(), x, y, z, motion.x, motion.y, motion.z);
            if(particle != null)
            {
                mc.particleEngine.add(particle);
            }
        }
    }

    public static boolean isThirdPersonBack()
    {
        return MINECRAFT.options.getCameraType() == CameraType.THIRD_PERSON_BACK;
    }

    public static boolean isThirdPersonFront()
    {
        return MINECRAFT.options.getCameraType() == CameraType.THIRD_PERSON_FRONT;
    }

    private enum SoundType
    {
        ENGINE,
        HORN
    }
}