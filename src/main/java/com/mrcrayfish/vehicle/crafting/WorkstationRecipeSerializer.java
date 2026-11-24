package com.mrcrayfish.vehicle.crafting;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
public class WorkstationRecipeSerializer implements RecipeSerializer<WorkstationRecipe>
{
    @Override
    @NotNull
    public WorkstationRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject parent)
    {
        ImmutableList.Builder<WorkstationIngredient> builder = ImmutableList.builder();
        JsonArray input = GsonHelper.getAsJsonArray(parent, "materials");

        for(int i = 0; i < input.size(); i++)
        {
            JsonObject object = input.get(i).getAsJsonObject();
            builder.add(WorkstationIngredient.fromJson(object));
        }

        if(!parent.has("vehicle"))
        {
            throw new JsonSyntaxException("Missing vehicle entry");
        }

        ResourceLocation vehicle = new ResourceLocation(GsonHelper.getAsString(parent, "vehicle"));
        Optional<EntityType<?>> optional = EntityType.byString(GsonHelper.getAsString(parent, "vehicle"));
        if(optional.isEmpty())
        {
            throw new JsonSyntaxException("Invalid vehicle entity: " + vehicle);
        }

        return new WorkstationRecipe(recipeId, optional.get(), builder.build());
    }

    @Nullable
    @Override
    public WorkstationRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer)
    {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(buffer.readResourceLocation());
        ImmutableList.Builder<WorkstationIngredient> builder = ImmutableList.builder();

        int size = buffer.readVarInt();
        for(int i = 0; i < size; i++)
        {
            builder.add((WorkstationIngredient) Ingredient.fromNetwork(buffer));
        }

        return new WorkstationRecipe(recipeId, entityType, builder.build());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, WorkstationRecipe recipe)
    {
        buffer.writeResourceLocation(ForgeRegistries.ENTITY_TYPES.getKey(recipe.getVehicle()));
        buffer.writeVarInt(recipe.getMaterials().size());

        for(WorkstationIngredient stack : recipe.getMaterials())
        {
            stack.toNetwork(buffer);
        }
    }
}
