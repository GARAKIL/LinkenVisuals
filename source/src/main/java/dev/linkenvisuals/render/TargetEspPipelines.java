package dev.linkenvisuals.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.linkenvisuals.LinkenVisuals;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/**
 * Pipelines and render layers for TargetEsp. Both donor textures are drawn as
 * camera-facing billboards that respect world depth: a target behind a wall is
 * never outlined through it.
 */
public final class TargetEspPipelines {
    public static final Identifier MARKER_TEXTURE = LinkenVisuals.id("textures/esp/marker.png");
    public static final Identifier GHOST_TEXTURE = LinkenVisuals.id("textures/esp/ghost_bloom.png");

    /** Translucent sprites: marker, ghosts and circle dots. */
    private static final RenderPipeline SPRITE_PIPELINE = Pipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
            .withLocation(LinkenVisuals.id("pipeline/target_esp_sprite"))
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build());

    /** Additive glow behind the crystal bodies (donor's {@code additive=true}). */
    private static final RenderPipeline GLOW_PIPELINE = Pipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
            .withLocation(LinkenVisuals.id("pipeline/target_esp_glow"))
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
            .withBlend(BlendFunction.LIGHTNING)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build());

    /** Crystal bodies: flat-shaded triangles with theme colors. */
    private static final RenderPipeline BODY_PIPELINE = Pipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(LinkenVisuals.id("pipeline/target_esp_body"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .build());

    public static final RenderLayer MARKER_LAYER = RenderLayer.of("linkenvisuals_target_esp_marker",
        RenderSetup.builder(SPRITE_PIPELINE).texture("Sampler0", MARKER_TEXTURE).translucent()
            .expectedBufferSize(4096).build());

    public static final RenderLayer GHOST_LAYER = RenderLayer.of("linkenvisuals_target_esp_ghost",
        RenderSetup.builder(SPRITE_PIPELINE).texture("Sampler0", GHOST_TEXTURE).translucent()
            .expectedBufferSize(65536).build());

    public static final RenderLayer GLOW_LAYER = RenderLayer.of("linkenvisuals_target_esp_glow",
        RenderSetup.builder(GLOW_PIPELINE).texture("Sampler0", GHOST_TEXTURE)
            .expectedBufferSize(8192).build());

    public static final RenderLayer BODY_LAYER = RenderLayer.of("linkenvisuals_target_esp_body",
        RenderSetup.builder(BODY_PIPELINE).translucent().expectedBufferSize(8192).build());

    private TargetEspPipelines() {
    }

    /** Forces class initialisation from {@link Pipelines#warmUp()}. */
    public static void init() {
    }
}
