package com.mrcrayfish.vehicle.client.model.baked;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.List;

/**
 * Helper that replaces the old BakedQuadBuilder for the ramp models.
 */
final class RampQuads
{
    /** Direction the hand-written geometry rises toward (+X). */
    static final Direction BASE_FACING = Direction.EAST;
    /** Facing used when there is no block state (item rendering). */
    static final Direction DEFAULT_FACING = Direction.NORTH;

    record V(float x, float y, float z, float u, float v) {}

    static V v(float x, float y, float z, float u, float v)
    {
        return new V(x, y, z, u, v);
    }

    static int rotation(Direction facing)
    {
        return Math.floorMod(facing.get2DDataValue() - BASE_FACING.get2DDataValue(), 4);
    }

    /**
     * Builds a quad in the vanilla BLOCK vertex layout (8 ints per vertex).
     * Vertices must be counter-clockwise when viewed from the front.
     */
    static void add(List<BakedQuad> out, TextureAtlasSprite sprite, int rot, V a, V b, V c, V d)
    {
        V[] vs = { a, b, c, d };
        float[][] p = new float[4][];
        for(int i = 0; i < 4; i++)
        {
            p[i] = rotate(vs[i], rot);
        }

        float d1x = p[2][0] - p[0][0], d1y = p[2][1] - p[0][1], d1z = p[2][2] - p[0][2];
        float d2x = p[3][0] - p[1][0], d2y = p[3][1] - p[1][1], d2z = p[3][2] - p[1][2];
        float nx = d1y * d2z - d1z * d2y;
        float ny = d1z * d2x - d1x * d2z;
        float nz = d1x * d2y - d1y * d2x;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if(len > 0.0F)
        {
            nx /= len;
            ny /= len;
            nz /= len;
        }

        int packedNormal = ((int) (nx * 127.0F) & 0xFF) | (((int) (ny * 127.0F) & 0xFF) << 8) | (((int) (nz * 127.0F) & 0xFF) << 16);
        Direction direction = Direction.getNearest(nx, ny, nz);

        int[] data = new int[32];
        for(int i = 0; i < 4; i++)
        {
            int o = i * 8;
            data[o] = Float.floatToRawIntBits(p[i][0]);
            data[o + 1] = Float.floatToRawIntBits(p[i][1]);
            data[o + 2] = Float.floatToRawIntBits(p[i][2]);
            data[o + 3] = 0xFFFFFFFF;
            data[o + 4] = Float.floatToRawIntBits(sprite.getU(vs[i].u()));
            data[o + 5] = Float.floatToRawIntBits(sprite.getV(vs[i].v()));
            data[o + 6] = 0;
            data[o + 7] = packedNormal;
        }
        out.add(new BakedQuad(data, -1, direction, sprite, true));
    }

    /** Rotates a vertex clockwise (seen from above) around the block centre, rot * 90 degrees. */
    private static float[] rotate(V v, int rot)
    {
        float x = v.x() - 0.5F;
        float z = v.z() - 0.5F;
        for(int i = 0; i < rot; i++)
        {
            float nx = -z;
            z = x;
            x = nx;
        }
        return new float[] { x + 0.5F, v.y(), z + 0.5F };
    }

    private RampQuads() {}
}