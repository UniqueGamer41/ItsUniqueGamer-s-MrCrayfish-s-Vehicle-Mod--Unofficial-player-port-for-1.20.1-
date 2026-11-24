package com.mrcrayfish.vehicle.client.model.baked;

import com.mrcrayfish.vehicle.block.RotatedObjectBlock;     // TODO: fix package
import com.mrcrayfish.vehicle.block.SteepBoostRampBlock;    // TODO: fix package
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.mrcrayfish.vehicle.client.model.baked.RampQuads.*;

/**
 * Author: MrCrayfish
 */
public class BakedModelSteepRamp implements BakedModel
{
    private final TextureAtlasSprite mainTexture;
    private final TextureAtlasSprite rampTexture;
    private final ItemTransforms transforms;
    private final Map<Integer, List<BakedQuad>> cache = new ConcurrentHashMap<>();

    public BakedModelSteepRamp(Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, ItemTransforms transforms)
    {
        this.mainTexture = bakedTextureGetter.apply(new ResourceLocation("minecraft", "block/gray_concrete"));
        this.rampTexture = bakedTextureGetter.apply(new ResourceLocation("vehicle", "block/boost_pad"));
        this.transforms = transforms;
    }

    @Override
    @NotNull
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand)
    {
        if(side != null)
            return Collections.emptyList();

        Direction facing = DEFAULT_FACING;
        boolean left = false;
        boolean right = false;

        if(state != null)
        {
            if(state.hasProperty(RotatedObjectBlock.DIRECTION))
            {
                Direction dir = state.getValue(RotatedObjectBlock.DIRECTION);
                if(dir.getAxis() != Direction.Axis.Y)
                {
                    facing = dir;
                }
            }
            left = state.hasProperty(SteepBoostRampBlock.LEFT) && state.getValue(SteepBoostRampBlock.LEFT);
            right = state.hasProperty(SteepBoostRampBlock.RIGHT) && state.getValue(SteepBoostRampBlock.RIGHT);
        }

        int key = facing.get2DDataValue() | (left ? 4 : 0) | (right ? 8 : 0);
        Direction f = facing;
        boolean l = left, r = right;
        return this.cache.computeIfAbsent(key, k -> this.buildQuads(f, l, r));
    }

    private List<BakedQuad> buildQuads(Direction facing, boolean left, boolean right)
    {
        float startHeight = 0.0F;
        float endHeight = 1.0F;
        int rot = rotation(facing);
        final float px = 0.0625F;
        int offsetStart = left ? 0 : 1;
        int offsetEnd = right ? 16 : 15;
        List<BakedQuad> quads = new ArrayList<>();

        //Boost Pad
        add(quads, this.rampTexture, rot,
                v(0, startHeight, offsetStart * px, offsetStart, 0),
                v(0, startHeight, offsetEnd * px, offsetEnd, 0),
                v(1, endHeight, offsetEnd * px, offsetEnd, 16),
                v(1, endHeight, offsetStart * px, offsetStart, 16));

        if(!left)
        {
            //Left Trim
            add(quads, this.mainTexture, rot,
                    v(0, startHeight, 0, 0, 0),
                    v(0, startHeight, px, 1, 0),
                    v(1, endHeight, px, 1, 16),
                    v(1, endHeight, 0, 0, 16));
        }

        if(!right)
        {
            //Right Trim
            add(quads, this.mainTexture, rot,
                    v(0, startHeight, 15 * px, 15, 0),
                    v(0, startHeight, 1, 16, 0),
                    v(1, endHeight, 1, 16, 16),
                    v(1, endHeight, 15 * px, 15, 16));
        }

        //Left Side
        add(quads, this.mainTexture, rot,
                v(1, 0, 0, 0, 0),
                v(0, 0, 0, 16, 0),
                v(0, startHeight, 0, 16, 16 * startHeight),
                v(1, endHeight, 0, 0, 16 * endHeight));

        //Back Side
        add(quads, this.mainTexture, rot,
                v(1, 0, 1, 0, 0),
                v(1, 0, 0, 16, 0),
                v(1, endHeight, 0, 16, 16 * endHeight),
                v(1, endHeight, 1, 0, 16 * endHeight));

        //Right Side
        add(quads, this.mainTexture, rot,
                v(0, 0, 1, 0, 0),
                v(1, 0, 1, 16, 0),
                v(1, endHeight, 1, 16, 16 * endHeight),
                v(0, startHeight, 1, 0, 16 * startHeight));

        //Bottom
        add(quads, this.mainTexture, rot,
                v(0, 0, 1, 0, 0),
                v(0, 0, 0, 16, 0),
                v(1, 0, 0, 16, 16),
                v(1, 0, 1, 0, 16));

        return Collections.unmodifiableList(quads);
    }

    @Override
    public boolean useAmbientOcclusion()
    {
        return true;
    }

    @Override
    public boolean isGui3d()
    {
        return true;
    }

    @Override
    public boolean usesBlockLight()
    {
        return true;
    }

    @Override
    public boolean isCustomRenderer()
    {
        return false;
    }

    @Override
    @NotNull
    public TextureAtlasSprite getParticleIcon()
    {
        return this.mainTexture;
    }

    @Override
    @NotNull
    public ItemTransforms getTransforms()
    {
        return this.transforms;
    }

    @Override
    @NotNull
    public ItemOverrides getOverrides()
    {
        return ItemOverrides.EMPTY;
    }
}