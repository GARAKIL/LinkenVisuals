package dev.linkenvisuals.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import dev.linkenvisuals.LinkenVisuals;
import dev.linkenvisuals.ui.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public final class RoundedRectPipeline {
    private static final Identifier WHITE = LinkenVisuals.id("textures/gui/white.png");

    private static final RenderPipeline FILL = build("rounded_rect", "core/rounded_rect");
    private static final RenderPipeline OUTLINE = build("rounded_rect_outline", "core/rounded_rect_outline");

    private RoundedRectPipeline() {
    }

    public static void init() {
    }

    private static RenderPipeline build(String name, String shader) {
        return Pipelines.register(
            RenderPipeline.builder(RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET)
                .withLocation(LinkenVisuals.id("pipeline/" + name))
                .withVertexShader(LinkenVisuals.id("core/rounded_rect"))
                .withFragmentShader(LinkenVisuals.id(shader))
                .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, com.mojang.blaze3d.vertex.VertexFormat.DrawMode.QUADS)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withCull(false)
                .build());
    }

    public static void draw(DrawContext context, float x, float y, float width, float height, int argb) {
        quad(context, FILL, x, y, width, height, (float) Theme.radiusLevel(), 0.0f, argb);
    }

    public static void draw(DrawContext context, float x, float y, float width, float height, float radius, int argb) {
        quad(context, FILL, x, y, width, height, radius, 0.0f, argb);
    }

    public static void drawOutline(DrawContext context, float x, float y, float width, float height, int argb) {
        quad(context, OUTLINE, x, y, width, height, (float) Theme.radiusLevel(), 1.0f, argb);
    }

    public static void drawOutline(DrawContext context, float x, float y, float width, float height, float radius, int argb) {
        quad(context, OUTLINE, x, y, width, height, radius, 1.0f, argb);
    }

    public static void drawOutline(DrawContext context, float x, float y, float width, float height, float radius, float thickness, int argb) {
        quad(context, OUTLINE, x, y, width, height, radius, thickness, argb);
    }

    public static void drawVerticalGradient(DrawContext context, float x, float y, float width, float height,
                                             float radius, int top, int bottom) {
        quad(context, FILL, x, y, width, height, radius, 0.0f, top, bottom, bottom, top);
    }

    public static void drawHorizontalGradient(DrawContext context, float x, float y, float width, float height,
                                               float radius, int left, int right) {
        quad(context, FILL, x, y, width, height, radius, 0.0f, left, left, right, right);
    }

    private static void quad(DrawContext context, RenderPipeline pipeline, float x, float y, float width, float height, float radius, float thickness, int argb) {
        quad(context, pipeline, x, y, width, height, radius, thickness, argb, argb, argb, argb);
    }

    private static void quad(DrawContext context, RenderPipeline pipeline, float x, float y, float width, float height,
                             float radius, float thickness, int topLeft, int bottomLeft, int bottomRight, int topRight) {
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        float guiScale = mc != null && mc.getWindow() != null ? (float) mc.getWindow().getScaleFactor() : 1.0f;
        float radiusPx = radius * guiScale;
        float thicknessPx = Math.max(1.0f, thickness * guiScale);

        Matrix3x2f pose = new Matrix3x2f(context.getMatrices());
        float z = radiusPx + thicknessPx * 1000.0f;

        ScreenRect bounds = new ScreenRect(
            (int) Math.floor(x),
            (int) Math.floor(y),
            (int) Math.ceil(width),
            (int) Math.ceil(height)
        ).transformEachVertex(pose);

        ScreenRect scissor = context.scissorStack != null ? context.scissorStack.peekLast() : null;

        context.state.addSimpleElement(new SimpleGuiElementRenderState() {
            @Override
            public void setupVertices(VertexConsumer consumer) {
                Vector2f p0 = pose.transformPosition(x, y, new Vector2f());
                Vector2f p1 = pose.transformPosition(x, y + height, new Vector2f());
                Vector2f p2 = pose.transformPosition(x + width, y + height, new Vector2f());
                Vector2f p3 = pose.transformPosition(x + width, y, new Vector2f());

                consumer.vertex(p0.x, p0.y, z).texture(0.0f, 0.0f).color(topLeft);
                consumer.vertex(p1.x, p1.y, z).texture(0.0f, 1.0f).color(bottomLeft);
                consumer.vertex(p2.x, p2.y, z).texture(1.0f, 1.0f).color(bottomRight);
                consumer.vertex(p3.x, p3.y, z).texture(1.0f, 0.0f).color(topRight);
            }

            @Override
            public RenderPipeline pipeline() {
                return pipeline;
            }

            @Override
            public TextureSetup textureSetup() {
                return TextureSetup.empty();
            }

            @Override
            public ScreenRect scissorArea() {
                return scissor;
            }

            @Override
            public ScreenRect bounds() {
                return bounds;
            }
        });
    }
}
