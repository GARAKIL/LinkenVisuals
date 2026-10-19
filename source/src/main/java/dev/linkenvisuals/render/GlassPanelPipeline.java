package dev.linkenvisuals.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.ui.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

public final class GlassPanelPipeline {
    private static final RenderPipeline PIPELINE = Pipelines.register(
        RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withLocation(LinkenVisuals.id("pipeline/glass_panel"))
            .withVertexShader(Identifier.of("minecraft", "core/position_tex_color"))
            .withFragmentShader(LinkenVisuals.id("core/glass_panel"))
            .withSampler("Sampler0")
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, com.mojang.blaze3d.vertex.VertexFormat.DrawMode.QUADS)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build());

    private GlassPanelPipeline() {
    }

    public static void init() {
    }

    public static void draw(DrawContext context, float x, float y, float width, float height, int argb) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(width, height);
        // Radius is carried in the integer part of U, in device pixels so that it
        // matches RoundedRectPipeline (radiusLevel * guiScale) exactly; a mismatch
        // used to leave square glass corners sticking out around rounded bodies.
        MinecraftClient mc = MinecraftClient.getInstance();
        float guiScale = mc != null && mc.getWindow() != null ? (float) mc.getWindow().getScaleFactor() : 1.0f;
        float u = (float) Theme.radiusLevel() * guiScale;
        context.drawTexture(PIPELINE, HudGlassCapture.textureId(), 0, 0, u, 0.0f, 1, 1, 1, 1, 1, 1, argb);
        matrices.popMatrix();
    }
}
