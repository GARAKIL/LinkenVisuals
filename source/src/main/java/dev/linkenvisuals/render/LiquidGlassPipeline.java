package dev.linkenvisuals.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.linkenvisuals.LinkenVisuals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * Liquid Glass (Phase 5.3): donor's {@code liquidglassnew} effect ported to
 * RenderPipeline + UBO. SDF rounded box, radial backdrop blur, fresnel edge
 * glow and refraction distortion. Reuses {@link HudGlassCapture}'s downscale
 * chain — no duplicate capture.
 *
 * <p>UBO layout (std140, 96 bytes):
 * <pre>
 *   vec2 size;            // @0  panel size in px
 *   vec4 radius;          // @16 per-corner radius
 *   vec4 params1;         // @32 smoothness, cornerSmoothness, globalAlpha, blurRadius
 *   vec4 params2;         // @48 fresnelPower, fresnelAlpha, baseAlpha, fresnelMix
 *   vec4 fresnelColor;    // @64 rgb + unused
 *   vec2 backdropSize;    // @80 framebuffer size for backdrop UVs
 *   float distortStrength;// @88
 *   float fresnelInvert;  // @92 (0/1)
 * </pre>
 */
public final class LiquidGlassPipeline {
    static final int UNIFORM_SIZE = 96;

    private static final RenderPipeline PIPELINE = Pipelines.register(
        RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withLocation(LinkenVisuals.id("pipeline/liquid_glass"))
            .withVertexShader(LinkenVisuals.id("core/liquid_glass"))
            .withFragmentShader(LinkenVisuals.id("core/liquid_glass"))
            .withSampler("Sampler0")
            .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR,
                com.mojang.blaze3d.vertex.VertexFormat.DrawMode.QUADS)
            .withUniform("LiquidGlass", UniformType.UNIFORM_BUFFER)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build());

    private static GpuBuffer uniforms;
    private static final ByteBuffer UNIFORM_DATA =
        ByteBuffer.allocateDirect(UNIFORM_SIZE).order(ByteOrder.nativeOrder());

    private LiquidGlassPipeline() {
    }

    public static void init() {
    }

    /** Rich parameter set mirroring the donor's LiquidGlassNewBuilder defaults. */
    public record Params(
        float radius,
        float smoothness,
        float cornerSmoothness,
        float globalAlpha,
        float fresnelPower,
        int fresnelColor,
        float fresnelAlpha,
        float baseAlpha,
        boolean fresnelInvert,
        float fresnelMix,
        float distortStrength,
        float blurRadius
    ) {
        public static Params defaults() {
            return new Params(12.0f, 1.0f, 2.0f, 1.0f, 2.0f, 0xFFFFFF, 0.8f, 0.2f,
                false, 1.0f, 0.03f, 3.2f);
        }
    }

    /**
     * Draws a liquid-glass panel at the given screen rect. Creates its own
     * encoder/pass (like {@link BlitPipeline}) so the UBO can be bound —
     * {@code DrawContext} does not expose uniform binding.
     *
     * <p>The backdrop is sampled from {@link HudGlassCapture}'s 1/8 texture;
     * call {@link HudGlassCapture#refresh()} before the GUI frame.
     */
    public static void draw(float x, float y, float width, float height,
                            Params params, int tintArgb) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer main = client.getFramebuffer();
        if (main == null || main.getColorAttachmentView() == null
            || main.getDepthAttachmentView() == null) {
            return;
        }

        AbstractTexture backdrop = client.getTextureManager().getTexture(HudGlassCapture.textureId());
        GpuTextureView backdropView = backdrop != null ? backdrop.getGlTextureView() : null;
        if (backdropView == null) {
            return;
        }

        ensureUniforms();
        writeUniforms(width, height, params, main.textureWidth, main.textureHeight);

        // Quad in screen pixels: position (3) + uv (2) + color (4).
        ByteBuffer data = MemoryUtil.memAlloc(4 * 36);
        try {
            float r = (tintArgb >> 16 & 0xFF) / 255.0f;
            float g = (tintArgb >> 8 & 0xFF) / 255.0f;
            float b = (tintArgb & 0xFF) / 255.0f;
            float a = (tintArgb >>> 24) / 255.0f;
            putVertex(data, x, y, 0.0f, 0.0f, 0.0f, r, g, b, a);
            putVertex(data, x, y + height, 0.0f, 0.0f, 1.0f, r, g, b, a);
            putVertex(data, x + width, y + height, 0.0f, 1.0f, 1.0f, r, g, b, a);
            putVertex(data, x + width, y, 0.0f, 1.0f, 0.0f, r, g, b, a);
            data.flip();

            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            GpuBuffer verts = RenderSystem.getDevice().createBuffer(
                () -> "linkenvisuals:liquid_glass_quad", GpuBuffer.USAGE_VERTEX, data);
            try (RenderPass pass = encoder.createRenderPass(() -> "linkenvisuals:liquid_glass",
                main.getColorAttachmentView(), OptionalInt.empty(),
                main.getDepthAttachmentView(), OptionalDouble.empty())) {
                pass.setPipeline(PIPELINE);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("LiquidGlass", uniforms);
                pass.bindTexture("Sampler0", backdropView,
                    RenderSystem.getSamplerCache().get(FilterMode.LINEAR));
                pass.setVertexBuffer(0, verts);
                pass.draw(0, 4);
            } finally {
                verts.close();
            }
        } finally {
            MemoryUtil.memFree(data);
        }
    }

    private static void putVertex(ByteBuffer data, float x, float y, float z,
                                  float u, float v, float r, float g, float b, float a) {
        data.putFloat(x).putFloat(y).putFloat(z);
        data.putFloat(u).putFloat(v);
        data.putFloat(r).putFloat(g).putFloat(b).putFloat(a);
    }

    private static void writeUniforms(float width, float height, Params params,
                                      int fbWidth, int fbHeight) {
        UNIFORM_DATA.clear();
        UNIFORM_DATA.putFloat(width); // @0 size.x
        UNIFORM_DATA.putFloat(height); // @2 size.y (offset 4)
        // @8 pad to 16
        UNIFORM_DATA.putFloat(0.0f);
        UNIFORM_DATA.putFloat(0.0f);
        float r = params.radius();
        UNIFORM_DATA.putFloat(r); // @16 radius
        UNIFORM_DATA.putFloat(r);
        UNIFORM_DATA.putFloat(r);
        UNIFORM_DATA.putFloat(r);
        UNIFORM_DATA.putFloat(params.smoothness()); // @32 params1
        UNIFORM_DATA.putFloat(params.cornerSmoothness());
        UNIFORM_DATA.putFloat(params.globalAlpha());
        UNIFORM_DATA.putFloat(params.blurRadius());
        UNIFORM_DATA.putFloat(params.fresnelPower()); // @48 params2
        UNIFORM_DATA.putFloat(params.fresnelAlpha());
        UNIFORM_DATA.putFloat(params.baseAlpha());
        UNIFORM_DATA.putFloat(params.fresnelMix());
        int fc = params.fresnelColor();
        UNIFORM_DATA.putFloat((fc >> 16 & 0xFF) / 255.0f); // @64 fresnelColor
        UNIFORM_DATA.putFloat((fc >> 8 & 0xFF) / 255.0f);
        UNIFORM_DATA.putFloat((fc & 0xFF) / 255.0f);
        UNIFORM_DATA.putFloat(0.0f);
        UNIFORM_DATA.putFloat(fbWidth); // @80 backdropSize
        UNIFORM_DATA.putFloat(fbHeight);
        UNIFORM_DATA.putFloat(params.distortStrength()); // @88
        UNIFORM_DATA.putFloat(params.fresnelInvert() ? 1.0f : 0.0f); // @92
        UNIFORM_DATA.flip();

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(uniforms.slice(), UNIFORM_DATA);
    }

    private static void ensureUniforms() {
        if (uniforms == null) {
            uniforms = RenderSystem.getDevice().createBuffer(() -> "linkenvisuals:liquid_glass_uniforms",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, UNIFORM_SIZE);
        }
    }

    /** Binds the UBO to an externally-created pass. For direct encoder use. */
    public static void bindUniforms(RenderPass pass) {
        ensureUniforms();
        pass.setUniform("LiquidGlass", uniforms);
    }

    public static RenderPipeline pipeline() {
        return PIPELINE;
    }

    public static void shutdown() {
        if (uniforms != null) {
            uniforms.close();
            uniforms = null;
        }
    }
}
