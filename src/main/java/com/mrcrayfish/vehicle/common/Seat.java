package com.mrcrayfish.vehicle.common;

import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.util.ExtraJSONUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;

/**
 * Author: MrCrayfish
 */
public class Seat
{
    public static final Vec3 DEFAULT_POSITION = Vec3.ZERO;
    public static final boolean DEFAULT_IS_DRIVER_SEAT = false;
    public static final float DEFAULT_YAW_OFFSET = 0F;

    private final Vec3 position;
    private final boolean isDriver;
    private final float yawOffset;

    protected Seat(Vec3 position)
    {
        this(position, false);
    }

    protected Seat(Vec3 position, float yawOffset)
    {
        this(position, false, yawOffset);
    }

    protected Seat(Vec3 position, boolean isDriver)
    {
        this(position, isDriver, DEFAULT_YAW_OFFSET);
    }

    public Seat(Vec3 position, boolean isDriver, float yawOffset)
    {
        this.position = position;
        this.isDriver = isDriver;
        this.yawOffset = yawOffset;
    }

    public Vec3 getPosition()
    {
        return this.position;
    }

    public boolean isDriver()
    {
        return this.isDriver;
    }

    public float getYawOffset()
    {
        return this.yawOffset;
    }

    public JsonObject toJsonObject()
    {
        JsonObject object = new JsonObject();
        ExtraJSONUtils.write(object, "position", this.position, DEFAULT_POSITION);
        ExtraJSONUtils.write(object, "driver", this.isDriver, DEFAULT_IS_DRIVER_SEAT);
        ExtraJSONUtils.write(object, "yawOffset", this.yawOffset, DEFAULT_YAW_OFFSET);
        return object;
    }

    public static Seat fromJsonObject(JsonObject object)
    {
        Vec3 position = ExtraJSONUtils.getAsVec3(object, "position", DEFAULT_POSITION);
        boolean isDriverSeat = GsonHelper.getAsBoolean(object, "driver", DEFAULT_IS_DRIVER_SEAT);
        float yawOffset = GsonHelper.getAsFloat(object, "yawOffset", DEFAULT_YAW_OFFSET);
        return new Seat(position, isDriverSeat, yawOffset);
    }

    public static Seat of(double x, double y, double z)
    {
        return new Seat(new Vec3(x, y, z));
    }

    public static Seat of(double x, double y, double z, boolean driver)
    {
        return new Seat(new Vec3(x, y, z), driver);
    }

    public static Seat of(double x, double y, double z, float yawOffset)
    {
        return new Seat(new Vec3(x, y, z), yawOffset);
    }

    public static Seat of(double x, double y, double z, boolean driver, float yawOffset)
    {
        return new Seat(new Vec3(x, y, z), driver, yawOffset);
    }
}
