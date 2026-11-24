package com.mrcrayfish.vehicle.util;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class CommonUtils
{
    public static void writeItemStackToTag(CompoundTag compound, String key, ItemStack stack)
    {
        if(!stack.isEmpty())
        {
            compound.put(key, stack.save(new CompoundTag()));
        }
    }

    public static ItemStack readItemStackFromTag(CompoundTag compound, String key)
    {
        if(compound.contains(key, CompoundTag.TAG_COMPOUND))
        {
            return ItemStack.of(compound.getCompound(key));
        }
        return ItemStack.EMPTY;
    }

    public static boolean isLookingAt(Player player, Vec3 target, double maxDistance, double tolerance)
    {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().normalize();

        Vec3 toTarget = target.subtract(eyePos);
        double distance = toTarget.length();

        if(distance > maxDistance)
            return false;

        toTarget = toTarget.normalize();

        double dot = lookVec.dot(toTarget);

        return dot > (1.0 - tolerance);
    }

    public static void sendInfoMessage(Player player, String message)
    {
        if(player instanceof ServerPlayer)
        {
            player.displayClientMessage(Component.translatable(message), true);
        }
    }

    public static boolean isMouseWithin(int mouseX, int mouseY, int x, int y, int width, int height)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static float yaw(Vec3 vec)
    {
        return (float) (Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90F);
    }

    public static float pitch(Vec3 vec)
    {
        if(vec.normalize().y != 0)
        {
            // Fixes the absolute of the value being slighter greater than 1.0
            double y = Mth.clamp(vec.normalize().y, -1.0, 1.0);
            // If abs of y is grater than 1.0, returns NaN when calling Math#asin and crashes world
            return (float) Math.toDegrees(Math.asin(y));
        }
        return 0F;
    }

    public static float yaw(Vector3f vec)
    {
        return yaw(new Vec3(vec));
    }

    public static float pitch(Vector3f vec)
    {
        return pitch(new Vec3(vec));
    }

    public static Vec3 lerp(Vec3 start, Vec3 end, float time)
    {
        double x = Mth.lerp(time, start.x, end.x);
        double y = Mth.lerp(time, start.y, end.y);
        double z = Mth.lerp(time, start.z, end.z);
        return new Vec3(x, y, z);
    }

    public static Vec3 clampSpeed(Vec3 motion)
    {
        return motion.normalize().scale(Mth.clamp(motion.length(), 0F, Config.SERVER.globalSpeedLimit.get()));
    }

    public static Vec3 clampLVSpeed(Vec3 motion, float maxspeed)
    {
        return motion.normalize().scale(Mth.clamp(motion.length(), 0F, maxspeed));
    }


}
