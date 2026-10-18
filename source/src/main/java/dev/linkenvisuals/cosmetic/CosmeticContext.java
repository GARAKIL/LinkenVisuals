package dev.linkenvisuals.cosmetic;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CosmeticContext {
    public VertexConsumer target;

    public static final int SOLID = 0;

    public static final int SHEET = 1;

    public static final int LIGHT = 2;

    public int pass;
    public boolean lit;
    public Matrix4f matrix;

    public final Vector3f eye = new Vector3f();
    public AbstractClientPlayerEntity player;

    public float time;
    public float size;
    public int color;

    public float speed;

    public float hurt;
    public float height;

    public CosmeticContext() {
    }

    public CosmeticContext(VertexConsumer target, int pass, Matrix4f matrix,
                           AbstractClientPlayerEntity player, float time, float size, int color,
                           float speed, float hurt, float height) {
        set(target, pass, matrix, player, time, size, color, speed, hurt, height);
    }

    /**
     * Rebinds this context to new render state; the values are valid only until the next
     * {@code set} call. Lets hot paths keep one context instead of allocating per frame.
     */
    public void set(VertexConsumer target, int pass, Matrix4f matrix,
                    AbstractClientPlayerEntity player, float time, float size, int color,
                    float speed, float hurt, float height) {
        this.target = target;
        this.pass = pass;
        this.lit = pass == LIGHT;
        this.matrix = matrix;
        this.player = player;
        this.time = time;
        this.size = size;
        this.color = color;
        this.speed = speed;
        this.hurt = hurt;
        this.height = height;

        SCRATCH.set(matrix).invert().transformPosition(eye.set(0.0f, 0.0f, 0.0f));
    }

    private static final Matrix4f SCRATCH = new Matrix4f();

    public float drive() {
        return Math.min(1.0f, speed / 5.6f);
    }
}
