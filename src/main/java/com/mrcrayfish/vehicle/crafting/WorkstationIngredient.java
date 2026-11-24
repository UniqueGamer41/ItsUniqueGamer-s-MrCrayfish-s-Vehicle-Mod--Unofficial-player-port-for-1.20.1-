package com.mrcrayfish.vehicle.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Author: MrCrayfish
 */
public class WorkstationIngredient extends Ingredient
{
    public static WorkstationIngredient of(ItemLike provider, int count)
    {
        return new WorkstationIngredient(new Ingredient.ItemValue(new ItemStack(provider)), count);
    }

    public static WorkstationIngredient of(ItemStack stack, int count)
    {
        return new WorkstationIngredient(new Ingredient.ItemValue(stack), count);
    }

    public static WorkstationIngredient of(TagKey<Item> tag, int count)
    {
        return new WorkstationIngredient(new Ingredient.TagValue(tag), count);
    }

    public static WorkstationIngredient of(ResourceLocation id, int count)
    {
        return new WorkstationIngredient(new MissingSingleItemList(id), count);
    }

    private final Ingredient.Value itemList;
    private final int count;

    protected WorkstationIngredient(Stream<? extends Ingredient.Value> itemList, int count)
    {
        super(itemList);
        this.itemList = null;
        this.count = count;
    }

    private WorkstationIngredient(Ingredient.Value itemList, int count)
    {
        super(Stream.of(itemList));
        this.itemList = itemList;
        this.count = count;
    }

    public int getCount()
    {
        return this.count;
    }

    @Override
    @NotNull
    public IIngredientSerializer<? extends Ingredient> getSerializer()
    {
        return Serializer.INSTANCE;
    }

    public static WorkstationIngredient fromJson(JsonObject object)
    {
        Ingredient.Value value = valueFromJson(object);
        int count = GsonHelper.getAsInt(object, "count", 1);
        return new WorkstationIngredient(Stream.of(value), count);
    }

    @Override
    @NotNull
    public JsonElement toJson()
    {
        JsonObject object = this.itemList.serialize();
        object.addProperty("count", this.count);
        return object;
    }

    public static class Serializer implements IIngredientSerializer<WorkstationIngredient>
    {
        public static final WorkstationIngredient.Serializer INSTANCE = new WorkstationIngredient.Serializer();

        @Override
        @NotNull
        public WorkstationIngredient parse(FriendlyByteBuf buffer)
        {
            int itemCount = buffer.readVarInt();
            int count = buffer.readVarInt();
            Stream<Ingredient.ItemValue> values = Stream.generate(() -> new ItemValue(buffer.readItem())).limit(itemCount);

            return new WorkstationIngredient(values, count);
        }

        @Override
        @NotNull
        public WorkstationIngredient parse(@NotNull JsonObject object)
        {
            return WorkstationIngredient.fromJson(object);
        }

        @Override
        public void write(FriendlyByteBuf buffer, WorkstationIngredient ingredient)
        {
            buffer.writeVarInt(ingredient.getItems().length);
            buffer.writeVarInt(ingredient.count);

            for(ItemStack stack : ingredient.getItems())
            {
                buffer.writeItem(stack);
            }
        }
    }

    //

    /**
     * Allows ability to define an ingredient from another mod without depending. Serializes the data
     * to be read by the regular {@link Value}. Only use this for generating data.
     */
    public static class MissingSingleItemList implements Ingredient.Value
    {
        private final ResourceLocation id;

        public MissingSingleItemList(ResourceLocation id)
        {
            this.id = id;
        }

        @Override
        @NotNull
        public Collection<ItemStack> getItems()
        {
            return Collections.emptyList();
        }

        @Override
        @NotNull
        public JsonObject serialize()
        {
            JsonObject object = new JsonObject();
            object.addProperty("item", this.id.toString());
            return object;
        }
    }
}
