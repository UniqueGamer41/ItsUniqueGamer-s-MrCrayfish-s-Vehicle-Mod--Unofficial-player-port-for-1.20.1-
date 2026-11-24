package com.mrcrayfish.vehicle.crafting;

import com.google.common.collect.ImmutableList;
import com.mrcrayfish.vehicle.init.ModRecipeSerializers;
import com.mrcrayfish.vehicle.block.entity.WorkstationBlockEntity;
import com.mrcrayfish.vehicle.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
public class WorkstationRecipe implements Recipe<WorkstationBlockEntity>
{
    private final ResourceLocation id;
    private final EntityType<?> vehicle;
    private final ImmutableList<WorkstationIngredient> materials;

    public WorkstationRecipe(ResourceLocation id, EntityType<?> vehicle, ImmutableList<WorkstationIngredient> materials)
    {
        this.id = id;
        this.vehicle = vehicle;
        this.materials = materials;
    }

    public EntityType<?> getVehicle()
    {
        return this.vehicle;
    }

    public ImmutableList<WorkstationIngredient> getMaterials()
    {
        return this.materials;
    }

    @Override
    public boolean matches(@NotNull WorkstationBlockEntity inv, @NotNull Level worldIn)
    {
        return false;
    }

    // New assemble method with RegistryAccess for 1.19.4 compatibility
//    @Override
//    @NotNull
//    public ItemStack assemble(@NotNull WorkstationBlockEntity inv, @NotNull RegistryAccess registryAccess)
//    {
//        // Implement your recipe assembly logic here
//        return ItemStack.EMPTY; // Replace with actual recipe output if applicable
//    }

    // Original assemble method (if used elsewhere in the mod)
    @Override
    @NotNull
    public ItemStack assemble(@NotNull WorkstationBlockEntity inv, @NotNull RegistryAccess registryAccess)
    {
        return ItemStack.EMPTY;
        // return this.assemble(inv, RegistryAccess.EMPTY); // Delegate to the new assemble method
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return true;
    }

    // New getResultItem method with RegistryAccess for 1.19.4 compatibility
//    @Override
//    @NotNull
//    public ItemStack getResultItem(@NotNull RegistryAccess registryAccess)
//    {
//        // Provide the result item based on registry data if necessary
//        return this.getResultItem()
//        return ItemStack.EMPTY;// Replace with actual result item if applicable
//    }

    // Original getResultItem method for compatibility
    //@Override
    @NotNull
    public ItemStack getResultItem(@NotNull RegistryAccess registryAccess)
    {
        ResourceLocation entityId = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(this.vehicle);
        if (entityId != null) {
            // Use defaults for preview: white color (0xFFFFFF), empty engine & wheels
            return com.mrcrayfish.vehicle.block.VehicleCrateBlock.create(entityId, 0xFFFFFF, ItemStack.EMPTY, ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    @Override
    @NotNull
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    @NotNull
    public RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.WORKSTATION.get();
    }

    @Override
    @NotNull
    public RecipeType<?> getType()
    {
        return RecipeTypes.WORKSTATION.get();
    }

    public boolean hasMaterials(Player player)
    {
        for (WorkstationIngredient ingredient : this.getMaterials())
        {
            if (!InventoryUtil.hasWorkstationIngredient(player, ingredient))
            {
                return false;
            }
        }
        return true;
    }

    public void consumeMaterials(Player player)
    {
        for (WorkstationIngredient ingredient : this.getMaterials())
        {
            InventoryUtil.removeWorkstationIngredient(player, ingredient);
        }
    }
}
