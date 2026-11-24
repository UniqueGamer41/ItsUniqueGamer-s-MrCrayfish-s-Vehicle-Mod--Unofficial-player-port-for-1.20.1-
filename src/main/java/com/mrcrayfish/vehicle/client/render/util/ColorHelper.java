package com.mrcrayfish.vehicle.client.render.util;

public class ColorHelper
{
    public static final float COMPONENT_RANGE = 255.0F;
    public static final float NORM = 1.0F / COMPONENT_RANGE;

    public static int packARGB(float r, float g, float b, float a) {
        return packARGB((int) (r * COMPONENT_RANGE), (int) (g * COMPONENT_RANGE), (int) (b * COMPONENT_RANGE), (int) (a * COMPONENT_RANGE));
    }

    public static int packARGB(int red, int green, int blue, int alpha)
    {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    public static int packABGR(int r, int g, int b, int a)
    {
        return (a & 0xFF) << 24 | (b & 0xFF) << 16 | (g & 0xFF) << 8 | (r & 0xFF);
    }

    public static int repackAlpha(int rgb, float alpha)
    {
        return (rgb & 0xFFFFFF) | ((int) (alpha * COMPONENT_RANGE) << 24);
    }

    public static int repackAlpha(int rgb, int alpha)
    {
        return (rgb & 0xFFFFFF) | (alpha & 0xFF) << 24;
    }

    public static int unpackARGBAlpha(int color)
    {
        return (color >> 24) & 0xFF;
    }

    public static int unpackARGBRed(int color)
    {
        return (color >> 16) & 0xFF;
    }

    public static int unpackARGBGreen(int color)
    {
        return (color >> 8) & 0xFF;
    }

    public static int unpackARGBBlue(int color)
    {
        return color & 0xFF;
    }

    public static int unpackABGRRed(int color)
    {
        return color & 0xFF;
    }

    public static int unpackABGRAlpha(int color)
    {
        return color >> 24 & 0xFF;
    }

    public static int unpackABGRGreen(int color)
    {
        return (color >> 8) & 0xFF;
    }

    /**
     * @param color The packed 32-bit ABGR color to unpack
     * @return The blue color component in the range of 0..255
     */
    public static int unpackABGRBlue(int color)
    {
        return (color >> 16) & 0xFF;
    }

    public static float normalize(int component)
    {
        return (component & 0xFF) * NORM;
    }
}
