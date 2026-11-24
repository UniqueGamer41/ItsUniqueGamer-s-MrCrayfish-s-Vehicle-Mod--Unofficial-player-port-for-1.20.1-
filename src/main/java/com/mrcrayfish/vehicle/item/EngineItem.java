package com.mrcrayfish.vehicle.item;

import com.mrcrayfish.vehicle.common.VehicleRegistry;
import com.mrcrayfish.vehicle.entity.IEngineTier;
import com.mrcrayfish.vehicle.entity.IEngineType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
public class EngineItem extends PartItem
{
    private final IEngineType type;
    private final IEngineTier tier;

    public EngineItem(IEngineType type, IEngineTier tier, Item.Properties properties)
    {
        super(properties);
        VehicleRegistry.registerEngine(type, tier, this);
        this.type = type;
        this.tier = tier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltips, @NotNull TooltipFlag flag)
    {
        tooltips.add(Component.translatable("vehicle.engine_info.acceleration").append(": ").withStyle(ChatFormatting.YELLOW).append(Component.literal(this.tier.getPowerMultiplier() + "x").withStyle(ChatFormatting.GRAY)));
        tooltips.add(Component.translatable("vehicle.engine_info.additional_max_speed").append(": ").withStyle(ChatFormatting.YELLOW).append(Component.literal((this.tier.getAdditionalMaxSpeed()) + "bps").withStyle(ChatFormatting.GRAY)));
    }

    public IEngineType getEngineType()
    {
        return this.type;
    }

    public IEngineTier getEngineTier()
    {
        return this.tier;
    }
}
