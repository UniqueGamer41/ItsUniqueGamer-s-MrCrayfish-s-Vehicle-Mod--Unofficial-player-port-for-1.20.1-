package com.mrcrayfish.vehicle.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public class FluidExtractorRecipeSerializer implements RecipeSerializer<FluidExtractorRecipe>
{
    @Override
    @NotNull
    public FluidExtractorRecipe fromJson(@NotNull ResourceLocation recipeId, JsonObject json)
    {
        if(!json.has("ingredient"))
        {
            throw new JsonSyntaxException("Missing ingredient, expected to find a item");
        }

        ItemStack ingredient = CraftingHelper.getItemStack(json.getAsJsonObject("ingredient"), false);
        if(!json.has("result"))
        {
            throw new JsonSyntaxException("Missing result, expected to find a fluid entry");
        }

        FluidEntry result = FluidEntry.fromJson(json.getAsJsonObject("result"));
        return new FluidExtractorRecipe(recipeId, ingredient, result);
    }

    @Nullable
    @Override
    public FluidExtractorRecipe fromNetwork(@NotNull ResourceLocation recipeId, FriendlyByteBuf buffer)
    {
        ItemStack ingredient = buffer.readItem();
        FluidEntry result = FluidEntry.read(buffer);
        return new FluidExtractorRecipe(recipeId, ingredient, result);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, FluidExtractorRecipe recipe)
    {
        buffer.writeItem(recipe.ingredient());
        recipe.result().write(buffer);
    }
}
