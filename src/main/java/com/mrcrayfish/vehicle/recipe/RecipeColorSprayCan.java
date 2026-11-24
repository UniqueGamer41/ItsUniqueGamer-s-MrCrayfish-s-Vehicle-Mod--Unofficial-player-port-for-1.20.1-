package com.mrcrayfish.vehicle.recipe;

import com.google.common.collect.Lists;
import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import com.mrcrayfish.vehicle.item.IDyeable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Author: MrCrayfish
 */
public class RecipeColorSprayCan extends CustomRecipe
{
    public RecipeColorSprayCan(ResourceLocation id)
    {
        super(id, CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingContainer inventory, @NotNull Level level)
    {
        ItemStack dyeableItem = ItemStack.EMPTY;
        List<ItemStack> dyes = Lists.newArrayList();

        for(int i = 0; i < inventory.getContainerSize(); ++i)
        {
            ItemStack stack = inventory.getItem(i);
            if(!stack.isEmpty())
            {
                if(stack.getItem() instanceof IDyeable)
                {
                    if(!dyeableItem.isEmpty())
                    {
                        return false;
                    }
                    dyeableItem = stack.copy();
                }
                else
                {
                    if(!stack.is(Tags.Items.DYES))
                    {
                        return false;
                    }
                    dyes.add(stack);
                }
            }
        }

        return !dyeableItem.isEmpty() && !dyes.isEmpty();
    }

    @Override
    @NotNull
    public ItemStack assemble(CraftingContainer inventory, @NotNull RegistryAccess registryAccess)
    {
        ItemStack dyeableItem = ItemStack.EMPTY;
        List<DyeItem> dyes = Lists.newArrayList();

        for(int i = 0; i < inventory.getContainerSize(); ++i)
        {
            ItemStack stack = inventory.getItem(i);
            if(!stack.isEmpty())
            {
                if(stack.getItem() instanceof IDyeable)
                {
                    if(!dyeableItem.isEmpty())
                    {
                        return ItemStack.EMPTY;
                    }
                    dyeableItem = stack.copy();
                }
                else
                {
                    if(!(stack.getItem() instanceof DyeItem))
                    {
                        return ItemStack.EMPTY;
                    }
                    dyes.add((DyeItem) stack.getItem());
                }
            }
        }

        return !dyeableItem.isEmpty() && !dyes.isEmpty() ? IDyeable.dyeStack(dyeableItem, dyes) : ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 2;
    }

    @Override
    @NotNull
    public RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.COLOR_SPRAY_CAN.get();
    }
}
