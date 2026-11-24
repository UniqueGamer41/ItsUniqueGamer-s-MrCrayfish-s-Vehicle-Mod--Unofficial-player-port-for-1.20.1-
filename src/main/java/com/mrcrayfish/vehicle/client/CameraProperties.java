package com.mrcrayfish.vehicle.client;

import com.google.gson.JsonObject;
import com.mrcrayfish.vehicle.util.ExtraJSONUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class CameraProperties
{
    public static final CameraProperties DEFAULT_CAMERA = new CameraProperties(Type.SMOOTH, 0.25F, new Vec3(0, 1.5, 0), new Vec3(10, 0, 0), 4.0);
    public static final Type DEFAULT_TYPE = Type.SMOOTH;
    public static final float DEFAULT_STRENGTH = 0.25F;
    public static final Vec3 DEFAULT_POSITION = new Vec3(0, 1.5, 0);
    public static final Vec3 DEFAULT_ROTATION = new Vec3(10, 0, 0);
    public static final float DEFAULT_DISTANCE = 4.0F;

    private final Type type;
    private final float strength;
    private final Vec3 position;
    private final Vec3 rotation;
    private final double distance;

    public CameraProperties(Type type, float strength, Vec3 position, Vec3 rotation, double distance)
    {
        this.type = type;
        this.strength = strength;
        this.position = position;
        this.rotation = rotation;
        this.distance = distance;
    }

    public Type getType()
    {
        return this.type;
    }

    public float getStrength()
    {
        return this.strength;
    }

    public Vec3 getPosition()
    {
        return this.position;
    }

    public Vec3 getRotation()
    {
        return this.rotation;
    }

    public double getDistance()
    {
        return this.distance;
    }

    public JsonObject toJsonObject()
    {
        JsonObject object = new JsonObject();
        ExtraJSONUtils.write(object, "type", this.type, DEFAULT_TYPE);
        ExtraJSONUtils.write(object, "strength", this.strength, DEFAULT_STRENGTH);
        ExtraJSONUtils.write(object, "position", this.position, DEFAULT_POSITION);
        ExtraJSONUtils.write(object, "rotation", this.rotation, DEFAULT_ROTATION);
        ExtraJSONUtils.write(object, "distance", this.distance, DEFAULT_DISTANCE);
        return object;
    }

    public static CameraProperties fromJsonObject(JsonObject object)
    {
        CameraProperties.Type type = ExtraJSONUtils.getAsEnum(object, "type", Type.class, DEFAULT_TYPE);
        float strength = GsonHelper.getAsFloat(object, "strength", DEFAULT_STRENGTH);
        Vec3 position = ExtraJSONUtils.getAsVec3(object, "position", DEFAULT_POSITION);
        Vec3 rotation = ExtraJSONUtils.getAsVec3(object, "rotation", DEFAULT_ROTATION);
        double distance = GsonHelper.getAsFloat(object, "distance", DEFAULT_DISTANCE);
        return new CameraProperties(type, strength, position, rotation, distance);
    }

    public enum Type
    {
        LOCKED("locked"),
        SMOOTH("smooth");

        private String id;

        Type(String id)
        {
            this.id = id;
        }

        public String getId()
        {
            return this.id;
        }

        public static Type fromId(String id)
        {
            return Stream.of(values())
                    .filter(type -> type.id.equals(id))
                    .findFirst()
                    .orElse(LOCKED);
        }
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private Type type = DEFAULT_TYPE;
        private float strength = DEFAULT_STRENGTH;
        private Vec3 position = DEFAULT_POSITION;
        private Vec3 rotation = DEFAULT_ROTATION;
        private double distance = DEFAULT_DISTANCE;

        public Builder setType(Type type)
        {
            this.type = type;
            return this;
        }

        public Builder setStrength(float strength)
        {
            this.strength = strength;
            return this;
        }

        public Builder setPosition(Vec3 position)
        {
            this.position = position;
            return this;
        }

        public Builder setPosition(double x, double y, double z)
        {
            this.position = new Vec3(x, y, z);
            return this;
        }

        public Builder setRotation(Vec3 rotation)
        {
            this.rotation = rotation;
            return this;
        }

        public Builder setRotation(double x, double y, double z)
        {
            this.rotation = new Vec3(x, y, z);
            return this;
        }

        public Builder setDistance(double distance)
        {
            this.distance = distance;
            return this;
        }

        public CameraProperties build()
        {
            return new CameraProperties(this.type, this.strength, this.position, this.rotation, this.distance);
        }
    }
}






























//package com.mrcrayfish.vehicle.client;
//
//import com.google.gson.JsonObject;
//import com.mrcrayfish.vehicle.util.ExtraJSONUtils;
//import net.minecraft.util.GsonHelper;
//import net.minecraft.world.phys.Vec3;
//
//import java.util.stream.Stream;
//
///**
// * Author: MrCrayfish
// */
//public class CameraProperties
//{
//    public static final CameraProperties DEFAULT_CAMERA = new CameraProperties(Type.SMOOTH, 0.25F, new Vec3(0, 1.5, 0), new Vec3(10, 0, 0), 4.0);
//    public static final Type DEFAULT_TYPE = Type.SMOOTH;
//    public static final float DEFAULT_STRENGTH = 0.25F;
//    public static final Vec3 DEFAULT_POSITION = new Vec3(0, 1.5, 0);
//    public static final Vec3 DEFAULT_ROTATION = new Vec3(10, 0, 0);
//    public static final float DEFAULT_DISTANCE = 4.0F;
//
//    private final Type type;
//    private final float strength;
//    private final Vec3 position;
//    private final Vec3 rotation;
//    private final double distance;
//
//    public CameraProperties(Type type, float strength, Vec3 position, Vec3 rotation, double distance)
//    {
//        this.type = type;
//        this.strength = strength;
//        this.position = position;
//        this.rotation = rotation;
//        this.distance = distance;
//    }
//
//    public Type getType()
//    {
//        return this.type;
//    }
//
//    public float getStrength()
//    {
//        return this.strength;
//    }
//
//    public Vec3 getPosition()
//    {
//        return this.position;
//    }
//
//    public Vec3 getRotation()
//    {
//        return this.rotation;
//    }
//
//    public double getDistance()
//    {
//        return this.distance;
//    }
//
//    public JsonObject toJsonObject()
//    {
//        JsonObject object = new JsonObject();
//        ExtraJSONUtils.write(object, "type", this.type, DEFAULT_TYPE);
//        ExtraJSONUtils.write(object, "strength", this.strength, DEFAULT_STRENGTH);
//        ExtraJSONUtils.write(object, "position", this.position, DEFAULT_POSITION);
//        ExtraJSONUtils.write(object, "rotation", this.rotation, DEFAULT_ROTATION);
//        ExtraJSONUtils.write(object, "distance", this.distance, DEFAULT_DISTANCE);
//        return object;
//    }
//
//    public static CameraProperties fromJsonObject(JsonObject object)
//    {
//        CameraProperties.Type type = ExtraJSONUtils.getAsEnum(object, "type", Type.class, DEFAULT_TYPE);
//        float strength = GsonHelper.getAsFloat(object, "strength", DEFAULT_STRENGTH);
//        Vec3 position = ExtraJSONUtils.getAsVec3(object, "position", DEFAULT_POSITION);
//        Vec3 rotation = ExtraJSONUtils.getAsVec3(object, "rotation", DEFAULT_ROTATION);
//        double distance = GsonHelper.getAsFloat(object, "distance", DEFAULT_DISTANCE);
//        return new CameraProperties(type, strength, position, rotation, distance);
//    }
//
//    public enum Type
//    {
//        LOCKED("locked"),
//        SMOOTH("smooth");
//
//        private String id;
//
//        Type(String id)
//        {
//            this.id = id;
//        }
//
//        public String getId()
//        {
//            return this.id;
//        }
//
//        public static Type fromId(String id)
//        {
//            return Stream.of(values())
//                    .filter(type -> type.id.equals(id))
//                    .findFirst()
//                    .orElse(LOCKED);
//        }
//    }
//
//    public static Builder builder()
//    {
//        return new Builder();
//    }
//
//    public static class Builder
//    {
//        private Type type = DEFAULT_TYPE;
//        private float strength = DEFAULT_STRENGTH;
//        private Vec3 position = DEFAULT_POSITION;
//        private Vec3 rotation = DEFAULT_ROTATION;
//        private double distance = DEFAULT_DISTANCE;
//
//        public Builder setType(Type type)
//        {
//            this.type = type;
//            return this;
//        }
//
//        public Builder setStrength(float strength)
//        {
//            this.strength = strength;
//            return this;
//        }
//
//        public Builder setPosition(Vec3 position)
//        {
//            this.position = position;
//            return this;
//        }
//
//        public Builder setPosition(double x, double y, double z)
//        {
//            this.position = new Vec3(x, y, z);
//            return this;
//        }
//
//        public Builder setRotation(Vec3 rotation)
//        {
//            this.rotation = rotation;
//            return this;
//        }
//
//        public Builder setRotation(double x, double y, double z)
//        {
//            this.rotation = new Vec3(x, y, z);
//            return this;
//        }
//
//        public Builder setDistance(double distance)
//        {
//            this.distance = distance;
//            return this;
//        }
//
//        public CameraProperties build()
//        {
//            return new CameraProperties(this.type, this.strength, this.position, this.rotation, this.distance);
//        }
//    }
//}
