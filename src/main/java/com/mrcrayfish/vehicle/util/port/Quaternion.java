package com.mrcrayfish.vehicle.util.port;

import org.joml.Quaternionf;

public class Quaternion extends Quaternionf {

    public Quaternion(float p_i48100_1_, float p_i48100_2_, float p_i48100_3_, float p_i48100_4_) {
        this.x = p_i48100_1_;
        this.y = p_i48100_2_;
        this.z = p_i48100_3_;
        this.w = p_i48100_4_;
    }

    public Quaternion(float pitch, float yaw, float roll, boolean degrees) {
        if (degrees) {
            pitch *= Math.PI / 180F;
            yaw *= Math.PI / 180F;
            roll *= Math.PI / 180F;
        }
        super.rotationYXZ(yaw, pitch, roll);
    }

    public Quaternion(Vector3f p_i48101_1_, float p_i48101_2_, boolean p_i48101_3_) {
        if (p_i48101_3_) {
            p_i48101_2_ *= ((float)Math.PI / 180F);
        }

        float f = sin(p_i48101_2_ / 2.0F);
        this.x = p_i48101_1_.x() * f;
        this.y = p_i48101_1_.y() * f;
        this.z = p_i48101_1_.z() * f;
        this.w = cos(p_i48101_2_ / 2.0F);
    }

    public Quaternion(Quaternion p_i48103_1_) {
        this.x = p_i48103_1_.x;
        this.y = p_i48103_1_.y;
        this.z = p_i48103_1_.z;
        this.w = p_i48103_1_.w;
    }

    public Quaternion(Quaternionf p_i48103_1_) {
        this.x = p_i48103_1_.x;
        this.y = p_i48103_1_.y;
        this.z = p_i48103_1_.z;
        this.w = p_i48103_1_.w;
    }

    private static float cos(float p_214904_0_) {
        return (float)Math.cos((double)p_214904_0_);
    }

    private static float sin(float p_214903_0_) {
        return (float)Math.sin((double)p_214903_0_);
    }

}
