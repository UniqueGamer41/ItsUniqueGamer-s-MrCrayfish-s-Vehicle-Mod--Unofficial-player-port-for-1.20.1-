package com.mrcrayfish.vehicle.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mrcrayfish.vehicle.common.VehicleRegistry;
import com.mrcrayfish.vehicle.common.entity.Transform;
import com.mrcrayfish.vehicle.entity.IEngineType;
import com.mrcrayfish.vehicle.util.port.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class ExtraJSONUtils
{
    public static final DecimalFormat FORMAT = new DecimalFormat("#.###");
    private static final boolean VERBOSE_MODE = false;

    public static void write(JsonObject object, String key, Number number, Number defaultValue)
    {
        if(!number.equals(defaultValue) || VERBOSE_MODE)
        {
            object.addProperty(key, number);
        }
    }

    public static void write(JsonObject object, String key, Boolean state, Boolean defaultValue)
    {
        if(!state.equals(defaultValue) || VERBOSE_MODE)
        {
            object.addProperty(key, state);
        }
    }

    public static void write(JsonObject object, String key, Transform transform, Transform defaultValue)
    {
        if(!transform.equals(defaultValue) || VERBOSE_MODE)
        {
            JsonObject transformObject = new JsonObject();
            write(transformObject, "translate", transform.getTranslate(), Vec3.ZERO);
            write(transformObject, "rotation", transform.getRotation(), Vec3.ZERO);
            write(transformObject, "scale", transform.getScale(), 1.0);
            object.add(key, transformObject);
        }
    }

    public static void write(JsonObject object, String key, Vec3 vec, Vec3 defaultValue)
    {
        if(!vec.equals(defaultValue) || VERBOSE_MODE)
        {
            JsonArray array = new JsonArray();
            array.add(Double.parseDouble(FORMAT.format(vec.x)));
            array.add(Double.parseDouble(FORMAT.format(vec.y)));
            array.add(Double.parseDouble(FORMAT.format(vec.z)));
            object.add(key, array);
        }
    }

    public static void write(JsonObject object, String key, IEngineType type, IEngineType defaultValue)
    {
        if(!type.equals(defaultValue) || VERBOSE_MODE)
        {
            object.addProperty(key, type.getId().toString());
        }
    }

    public static void write(JsonObject object, String key, @Nullable ResourceLocation resourceLocation, @Nullable ResourceLocation defaultValue)
    {
        if(resourceLocation != null && (!resourceLocation.equals(defaultValue) || VERBOSE_MODE))
        {
            object.addProperty(key, resourceLocation.toString());
        }
    }

    public static <T extends Enum<?>> void write(JsonObject object, String key, T value, T defaultValue)
    {
        if(!value.equals(defaultValue) || VERBOSE_MODE)
        {
            object.addProperty(key, value.name().toLowerCase(Locale.ENGLISH));
        }
    }

    public static Vec3 getAsVec3(JsonObject object, String memberName, Vec3 defaultValue)
    {
        if(object.has(memberName))
        {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(object, memberName);
            if(jsonArray.size() != 3)
            {
                throw new JsonParseException("Expected 3 " + memberName + " values, found: " + jsonArray.size());
            }
            else
            {
                double x = GsonHelper.convertToFloat(jsonArray.get(0), memberName + "[0]");
                double y = GsonHelper.convertToFloat(jsonArray.get(1), memberName + "[1]");
                double z = GsonHelper.convertToFloat(jsonArray.get(2), memberName + "[2]");
                return new Vec3(x, y, z);
            }
        }
        return defaultValue;
    }

    public static Vector3f getAsVector3f(JsonObject object, String memberName) throws JsonParseException
    {
        if(!object.has(memberName))
            throw new JsonParseException("Missing member " + memberName);

        JsonArray jsonArray = GsonHelper.getAsJsonArray(object, memberName);
        if(jsonArray.size() != 3)
            throw new JsonParseException("Expected 3 " + memberName + " values, found: " + jsonArray.size());

        float x = GsonHelper.convertToFloat(jsonArray.get(0), memberName + "[0]");
        float y = GsonHelper.convertToFloat(jsonArray.get(1), memberName + "[1]");
        float z = GsonHelper.convertToFloat(jsonArray.get(2), memberName + "[2]");
        return new Vector3f(x, y, z);
    }

    public static Transform getAsTransform(JsonObject object, String key, Transform defaultValue)
    {
        if(object.has(key) && object.get(key).isJsonObject())
        {
            JsonObject transform = object.getAsJsonObject(key);
            Vec3 translate = ExtraJSONUtils.getAsVec3(transform, "translate", Vec3.ZERO);
            Vec3 rotation = ExtraJSONUtils.getAsVec3(transform, "rotation", Vec3.ZERO);
            double scale = GsonHelper.getAsFloat(transform, "scale", 1.0F);
            return Transform.create(translate, rotation, scale);
        }
        return defaultValue;
    }

    public static IEngineType getAsEngineType(JsonObject object, String key, IEngineType defaultValue)
    {
        String rawId = GsonHelper.getAsString(object, key, "");
        if(!rawId.isEmpty())
        {
            ResourceLocation id = new ResourceLocation(rawId);
            IEngineType type = VehicleRegistry.getEngineTypeFromId(id);
            return type != null ? type : defaultValue;
        }
        return defaultValue;
    }

    public static ResourceLocation getAsResourceLocation(JsonObject object, String key, ResourceLocation defaultValue)
    {
        if(object.has(key) && object.get(key).isJsonPrimitive())
        {
            return new ResourceLocation(GsonHelper.getAsString(object, key));
        }
        return defaultValue;
    }

    public static <T extends Enum<?>> T getAsEnum(JsonObject object, String key, Class<T> enumClass, T defaultValue)
    {
        if(object.has(key) && object.get(key).isJsonPrimitive())
        {
            String enumString = GsonHelper.getAsString(object, key);
            return Stream.of(enumClass.getEnumConstants()).filter(side -> side.name().equalsIgnoreCase(enumString)).findFirst().orElse(defaultValue);
        }
        return defaultValue;
    }
}
