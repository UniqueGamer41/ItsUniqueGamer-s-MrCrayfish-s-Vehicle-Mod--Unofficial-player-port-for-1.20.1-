package com.mrcrayfish.vehicle.client.util;

import com.mrcrayfish.vehicle.util.port.Quaternion;
import net.minecraft.util.Mth;

public class MathUtil
{
    /**
     * Spherically interpolates between two quaternions with a weight
     * Source taken from Bones framework for JPCT, see <a href="https://github.com/raftAtGit/Bones">...</a>
     * Code has been adapted to work with Quaternion from Minecraft's math package.
     *
     * @param start the starting quaternion
     * @param end the destination quaternion
     * @param t the weight of the interpolation in the range of [0, 1]
     */
    public static Quaternion slerp(Quaternion start, Quaternion end, float t)
    {
        // Skip operation if equal
        if(start.equals(end))
        {
            return start;
        }

        float dot = start.x() * end.x() + start.y() * end.y() + start.z() * end.z() + start.w() * end.w();
        if(dot < 0.0F)
        {
            end = new Quaternion(-end.x(), -end.y(), -end.z(), -end.w());
            dot = -dot;
        }

        float scale0 = 1 - t;
        float scale1 = t;

        // Only run calculations if angle between two quaternions is big enough.
        if((1.0F - dot) > 0.1F)
        {
            float theta = (float) Math.acos(dot);
            float invSinTheta = 1.0F / Mth.sin(theta);
            scale0 = Mth.sin((1.0F - t) * theta) * invSinTheta;
            scale1 = Mth.sin((t * theta)) * invSinTheta;
        }

        // Calculate new quaternion. Interpolation is linear unless above calculations are run.
        float i = (scale0 * start.x()) + (scale1 * end.x());
        float j = (scale0 * start.y()) + (scale1 * end.y());
        float k = (scale0 * start.z()) + (scale1 * end.z());
        float r = (scale0 * start.w()) + (scale1 * end.w());

        return new Quaternion(i, j, k, r);
    }
}