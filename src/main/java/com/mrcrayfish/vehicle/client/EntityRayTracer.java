package com.mrcrayfish.vehicle.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;




public class EntityRayTracer
{
    public static class MatrixTransformation
    {
        private final MatrixTransformationType type;
        private float x, y, z;
        private float angle;

        private enum MatrixTransformationType
        {
            TRANSLATION, ROTATION, SCALE
        }

        public MatrixTransformation(MatrixTransformationType type, float x, float y, float z)
        {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public MatrixTransformation(MatrixTransformationType type, float x, float y, float z, float angle)
        {
            this(type, x, y, z);
            this.angle = angle;
        }

        public static MatrixTransformation createTranslation(float x, float y, float z)
        {
            return new MatrixTransformation(MatrixTransformationType.TRANSLATION, x, y, z);
        }

        public static MatrixTransformation createRotation(Vector3f axis, float angle)
        {
            return new MatrixTransformation(MatrixTransformationType.ROTATION, axis.x(), axis.y(), axis.z(), angle);
        }

        public static MatrixTransformation createScale(float x, float y, float z)
        {
            return new MatrixTransformation(MatrixTransformationType.SCALE, x, y, z);
        }

        public static MatrixTransformation createScale(float xyz)
        {
            return new MatrixTransformation(MatrixTransformationType.SCALE, xyz, xyz, xyz);
        }

        public void transform(Matrix4f matrix)
        {
            PoseStack matrixStack = new PoseStack();
            switch(type)
            {
                case ROTATION:
                    matrixStack.mulPose(Axis.of(new Vector3f(this.x, this.y, this.z)).rotationDegrees(this.angle));
                    break;
                case TRANSLATION:
                    matrixStack.translate(this.x, this.y, this.z);
                    break;
                case SCALE:
                    matrixStack.scale(this.x, this.y, this.z);
                    break;
            }
            matrix.mul(matrixStack.last().pose());
        }
    }
}