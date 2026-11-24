package com.mrcrayfish.vehicle.common.cosmetic.actions;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.util.Axis;
import com.mrcrayfish.vehicle.util.EasingHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public class OpenableAction extends Action
{
    private final Axis axis;
    private final float angle;
    private final int animationLength;
    @Nullable
    private final ResourceLocation openSound;
    @Nullable
    private final ResourceLocation closeSound;

    private boolean state = false; //Explicit to clearly indicate default state
    private int prevAnimationTick;
    private int animationTick;

    public OpenableAction(Axis axis, float angle, @Nullable ResourceLocation openSound, @Nullable ResourceLocation closeSound, int animationLength)
    {
        this.axis = axis;
        this.angle = angle;
        this.openSound = openSound;
        this.closeSound = closeSound;
        this.animationLength = animationLength;
    }

    @Override
    public void onInteract(VehicleEntity vehicle, Player player)
    {
        this.state = !this.state;
        this.setDirty();
        //System.out.println("[OpenableAction] onInteract - State changed to: " + this.state + " (Server: " + !vehicle.level().isClientSide() + ")");
    }

    @Override
    public void load(CompoundTag tag, boolean sync)
    {
        boolean newState = tag.getBoolean("Open");
        if(this.state != newState)
        {
            this.state = newState;
            if(!sync && this.state)
            {
                this.animationTick = this.prevAnimationTick = this.animationLength;
            }
        }
    }

    @Override
    public CompoundTag save(boolean sync)
    {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Open", this.state);
        return tag;
    }

    @Override
    public void serialize(JsonObject object)
    {
        JsonObject rotation = new JsonObject();
        rotation.addProperty("axis", this.axis.getKey());
        rotation.addProperty("angle", this.angle);
        rotation.addProperty("animationLength", this.animationLength);
        object.add("rotation", rotation);
        JsonObject sound = new JsonObject();
        if(this.openSound != null) sound.addProperty("open", this.openSound.toString());
        if(this.closeSound != null) sound.addProperty("close", this.closeSound.toString());
        if(sound.size() > 0) object.add("sound", sound);
    }

    @Override
    public void tick(VehicleEntity vehicle)
    {
        if(vehicle.level().isClientSide())
        {
            this.prevAnimationTick = this.animationTick;
            if(this.state)
            {
                if(this.animationTick == 0)
                {
                    //System.out.println("[OpenableAction] CLIENT - Starting OPEN animation");
                    this.playSound(true, vehicle);
                }
                if(this.animationTick < this.animationLength)
                {
                    this.animationTick++;
                }
            }
            else if(this.animationTick > 0)
            {
                if(this.animationTick == this.animationLength)
                {
                    System.out.println("[OpenableAction] CLIENT - Starting CLOSE animation");
                }
                this.animationTick--;
                if(this.animationTick == 0)
                {
                    this.playSound(false, vehicle);
                }
            }
        }
    }

    public boolean isOpen()
    {
        return this.state;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void beforeRender(PoseStack matrixStack, VehicleEntity vehicle, float partialTicks)
    {
        if(this.animationTick != 0 || this.prevAnimationTick != 0)
        {
            float progress = Mth.lerp(partialTicks, this.prevAnimationTick, this.animationTick) / (float) this.animationLength;
            progress = (float) EasingHelper.easeOutBack(progress);
            //System.out.println("[Render] Door rotation: " + (this.angle * progress) + " degrees");
            matrixStack.mulPose(this.axis.getAxis().rotationDegrees(this.angle * progress));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void gatherTransforms(List<MatrixTransform> transforms)
    {
        if(this.prevAnimationTick != 0)
        {
            float progress = (float) this.prevAnimationTick / (float) this.animationLength;
            progress = (float) EasingHelper.easeOutBack(progress);
            transforms.add(MatrixTransform.rotate(this.axis.getAxis().rotationDegrees(this.angle * progress)));
        }
    }

    private void playSound(boolean state, VehicleEntity vehicle)
    {
        ResourceLocation sound = state ? this.openSound : this.closeSound;
        if(sound != null)
        {
            SoundEvent event = ForgeRegistries.SOUND_EVENTS.getValue(sound);
            if(event != null)
            {
                Vec3 position = vehicle.position();
                float pitch = 0.8F + 0.2F * vehicle.level().random.nextFloat();
                vehicle.level().playSound(null, position.x, position.y, position.z, event, SoundSource.NEUTRAL, 1.0F, pitch);
            }
        }
    }
}