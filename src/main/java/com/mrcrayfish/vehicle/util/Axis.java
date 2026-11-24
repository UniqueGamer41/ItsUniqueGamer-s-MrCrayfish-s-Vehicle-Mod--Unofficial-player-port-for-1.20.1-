package com.mrcrayfish.vehicle.util;


import com.mrcrayfish.vehicle.util.port.Vector3f;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public enum Axis
{
    X(Vector3f.XP, "x"),
    Y(Vector3f.YP, "y"),
    Z(Vector3f.ZP, "z");

    public static final Axis[] VALUES = values();
    private static final Map<String, Axis> BY_KEY = Arrays.stream(VALUES).collect(Collectors.toMap(Axis::getKey, (axis) -> axis));

    private final Vector3f axis;
    private final String key;

    Axis(Vector3f axis, String key)
    {
        this.axis = axis;
        this.key = key;
    }

    public Vector3f getAxis()
    {
        return this.axis;
    }

    public String getKey()
    {
        return this.key;
    }

    public static Axis fromKey(String key)
    {
        return BY_KEY.getOrDefault(key, Axis.X);
    }
}
