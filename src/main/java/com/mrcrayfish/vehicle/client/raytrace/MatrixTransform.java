package com.mrcrayfish.vehicle.client.raytrace;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrcrayfish.vehicle.util.port.Matrix4f;
import org.joml.Quaternionf;

/**
 * Matrix transformation that corresponds to one of the three supported GL operations that might be performed on a rendered item part
 */
public abstract class MatrixTransform
{
    public static MatrixTransform translate(float x, float y, float z)
    {
        return new Translate(x, y, z);
    }

    public static MatrixTransform rotate(Quaternionf quaternion)
    {
        return new Rotation(quaternion);
    }

    public static MatrixTransform scale(float x, float y, float z)
    {
        return new Scale(x, y, z);
    }

    public static MatrixTransform scale(float scale)
    {
        return new Scale(scale);
    }

    /**
     * Applies the matrix transformation that this class represents to the passed matrix
     *
     * @param matrix matrix to construct this transformation to
     */
    public abstract void transform(com.mrcrayfish.vehicle.util.port.Matrix4f matrix);

    public static class Translate extends MatrixTransform
    {
        private final float x, y, z;

        public Translate(float x, float y, float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void transform(com.mrcrayfish.vehicle.util.port.Matrix4f matrix)
        {
            PoseStack matrixStack = new PoseStack();
            matrixStack.translate(this.x, this.y, this.z);
            matrix.mul(matrixStack.last().pose());
        }
    }

    public static class Rotation extends MatrixTransform
    {
        private final Quaternionf quaternion;

        public Rotation(Quaternionf quaternion)
        {
            this.quaternion = quaternion;
        }

        @Override
        public void transform(Matrix4f matrix)
        {
            PoseStack matrixStack = new PoseStack();
            matrixStack.mulPose(this.quaternion);
            matrix.multiply(matrixStack.last().pose());
        }
    }

    public static class Scale extends MatrixTransform
    {
        private final float x, y, z;

        public Scale(float x, float y, float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Scale(float s)
        {
            this.x = s;
            this.y = s;
            this.z = s;
        }

        @Override
        public void transform(com.mrcrayfish.vehicle.util.port.Matrix4f matrix)
        {
            PoseStack matrixStack = new PoseStack();
            matrixStack.scale(this.x, this.y, this.z);
            matrix.multiply(matrixStack.last().pose());
        }
    }
}