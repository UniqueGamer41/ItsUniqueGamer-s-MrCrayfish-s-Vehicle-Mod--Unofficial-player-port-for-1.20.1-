package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.Config;
import com.mrcrayfish.vehicle.client.render.util.RenderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class SprayCanItem extends Item implements IDyeable
{
    public static final String REMAINING_SPRAYS_TAG = "remainingSprays";
    public static final String CAPACITY = "capacity";

    public SprayCanItem(Item.Properties properties)
    {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltips, @NotNull TooltipFlag flag)
    {
        if(Screen.hasShiftDown())
        {
            tooltips.addAll(RenderUtil.lines(Component.translatable(this.getDescriptionId() + ".info"), 150));
        }
        else
        {
            if(this.hasColor(stack))
            {
                tooltips.add(Component.literal(String.format("#%06X", this.getColor(stack))).withStyle(ChatFormatting.BLUE));
            }
            else
            {
                tooltips.add(Component.translatable(this.getDescriptionId() + ".empty").withStyle(ChatFormatting.RED));
            }

            tooltips.add(Component.translatable("vehicle.info_help").withStyle(ChatFormatting.YELLOW));
        }
    }

    public static CompoundTag getStackTag(ItemStack stack)
    {
        if (stack.getItem() instanceof SprayCanItem sprayCan)
        {
            CompoundTag compound = stack.getOrCreateTag();

            if (!compound.contains(REMAINING_SPRAYS_TAG, IntTag.TAG_INT))
            {
                compound.putInt(REMAINING_SPRAYS_TAG, sprayCan.getCapacity(stack));
            }
        }
        return stack.getTag();
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains(REMAINING_SPRAYS_TAG, IntTag.TAG_INT))
        {
            int remainingSprays = compound.getInt(REMAINING_SPRAYS_TAG);
            return this.hasColor(stack) && remainingSprays < this.getCapacity(stack);
        }
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains(REMAINING_SPRAYS_TAG, IntTag.TAG_INT))
        {
            return (int) Mth.clamp(1.0 - (compound.getInt(REMAINING_SPRAYS_TAG) / (double) this.getCapacity(stack)), 0.0, 1.0);
        }
        return 0;
    }

    public float getRemainingSprays(ItemStack stack)
    {
        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains(REMAINING_SPRAYS_TAG, IntTag.TAG_INT))
        {
            return compound.getInt(REMAINING_SPRAYS_TAG) / (float) this.getCapacity(stack);
        }
        return 0.0F;
    }

    public int getCapacity(ItemStack stack)
    {
        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains(CAPACITY, IntTag.TAG_INT))
        {
            return compound.getInt(CAPACITY);
        }
        return Config.SERVER.sprayCanCapacity.get();
    }

    public void refill(ItemStack stack)
    {
        CompoundTag compound = getStackTag(stack);
        compound.putInt(REMAINING_SPRAYS_TAG, this.getCapacity(stack));
    }
}
