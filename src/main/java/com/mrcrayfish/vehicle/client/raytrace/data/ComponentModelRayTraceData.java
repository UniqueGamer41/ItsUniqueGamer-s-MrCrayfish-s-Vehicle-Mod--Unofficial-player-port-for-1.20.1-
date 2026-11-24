package com.mrcrayfish.vehicle.client.raytrace.data;

import com.mrcrayfish.vehicle.client.model.ComponentModel;
import com.mrcrayfish.vehicle.client.raytrace.EntityRayTracer;
import com.mrcrayfish.vehicle.client.raytrace.ITriangleList;
import com.mrcrayfish.vehicle.client.raytrace.MatrixTransform;
import com.mrcrayfish.vehicle.client.raytrace.RayTraceFunction;
import com.mrcrayfish.vehicle.client.raytrace.TransformHelper;
import com.mrcrayfish.vehicle.client.raytrace.Triangle;
import com.mrcrayfish.vehicle.client.model.complex.ComplexModel;
import com.mrcrayfish.vehicle.client.model.complex.transforms.Transform;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.util.port.Matrix4f;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public class ComponentModelRayTraceData extends RayTraceData
{
    private final ComponentModel model;

    public ComponentModelRayTraceData(ComponentModel model)
    {
        this(model, null);
    }

    public ComponentModelRayTraceData(ComponentModel model, @Nullable RayTraceFunction function)
    {
        super(function);
        this.model = model;
    }

    public ComponentModel getModel()
    {
        return this.model;
    }

    @Override
    public ITriangleList createTriangleList()
    {
        return new ComplexTriangleList(this.model, this.matrix);
    }

    public static class ComplexTriangleList implements ITriangleList
    {
        private final Matrix4f baseMatrix;
        private final List<Triangle> baseTriangles;
        private final List<Pair<List<Triangle>, BiFunction<RayTraceData, Entity, Matrix4f>>> matrixPairs = new ArrayList<>();

        private ComplexTriangleList(BakedModel model, Matrix4f baseMatrix)
        {
            this.baseMatrix = baseMatrix;
            this.baseTriangles = EntityRayTracer.createTrianglesFromBakedModel(model, baseMatrix);
        }

        public ComplexTriangleList(ComponentModel model, Matrix4f baseMatrix)
        {
            this(model.getBaseModel(), baseMatrix);
            if(model.getComplexModel() != null)
            {
                this.createAndAddMatrixPair(model.getComplexModel(), new ArrayList<>());
            }
            else
            {
                this.matrixPairs.add(this.createEntry(model.getBaseModel(), Collections.emptyList()));
            }
        }

        public ComplexTriangleList(ComplexModel model, Matrix4f baseMatrix)
        {
            this(model.getModel(), baseMatrix);
            this.createAndAddMatrixPair(model, new ArrayList<>());
        }

        private void createAndAddMatrixPair(ComplexModel model, List<Transform> complexTransforms)
        {
            complexTransforms.addAll(model.getTransforms());
            List<Transform> modelTransforms = new ArrayList<>(complexTransforms);
            this.matrixPairs.add(this.createEntry(model.getModel(), modelTransforms));

            ComplexModel[] children = model.getChildren();
            for(int idx = 0; idx < children.length; idx++)
            {
                ComplexModel child = children[idx];
                this.createAndAddMatrixPair(child, complexTransforms);
            }

            complexTransforms.removeAll(model.getTransforms());
        }

        private Pair<List<Triangle>, BiFunction<RayTraceData, Entity, Matrix4f>> createEntry(BakedModel model, List<Transform> modelTransforms)
        {
            return Pair.of(EntityRayTracer.createTrianglesFromBakedModel(model, null), (data, entity) -> {
                VehicleEntity vehicle = ((VehicleEntity) entity);
                List<MatrixTransform> baseTransforms = new ArrayList<>();
                modelTransforms.forEach(transform -> baseTransforms.add(transform.create(vehicle, 0F)));
                Matrix4f matrix = this.baseMatrix.copy();
                matrix.multiply(TransformHelper.createMatrixFromTransformsForPart(baseTransforms));
                return matrix;
            });
        }

        @Override
        public List<Triangle> getTriangles(RayTraceData data, Entity entity)
        {
            List<Triangle> triangles = new ArrayList<>();
            this.matrixPairs.forEach(pair ->
            {
                Matrix4f matrix = pair.getRight().apply(data, entity);
                for(Triangle triangle : pair.getLeft())
                {
                    triangles.add(new Triangle(EntityRayTracer.getTransformedTriangle(triangle.vertices(), matrix)));
                }
            });
            return triangles;
        }

        @Override
        public List<Triangle> getTriangles()
        {
            return this.baseTriangles;
        }
    }
}