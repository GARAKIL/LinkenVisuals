package dev.linkenvisuals.render;

import dev.linkenvisuals.hands.HandOutline;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Byte-for-byte guard: every custom {@code layout(std140)} block in our shaders
 * must equal the uniform-buffer size allocated on the Java side. If a shader
 * block gains/loses a member, this test fails and forces a re-verification of
 * the matching {@code GpuBuffer} allocation.
 */
class Std140LayoutTest {
    private record BlockCase(String file, String block, int expectedBytes) {
    }

    private static final List<BlockCase> CASES = List.of(
        new BlockCase("kill_effect.fsh", "Uniforms", KillShaderRenderer.UNIFORM_SIZE),
        new BlockCase("hit_bubble.fsh", "Uniforms", HitBubbleRenderer.UNIFORM_SIZE),
        new BlockCase("rain.fsh", "Uniforms", RainRenderer.UNIFORM_SIZE),
        new BlockCase("hand_outline.fsh", "Uniforms", HandOutline.OUTLINE_UNIFORM),
        new BlockCase("hand_trail.fsh", "Uniforms", HandOutline.TRAIL_UNIFORM),
        new BlockCase("screen_filter.fsh", "FilterData", ScreenFilterPipeline.BUFFER_SIZE),
        new BlockCase("ambience_composite.fsh", "Uniforms", AmbiencePipeline.UNIFORM_SIZE),
        new BlockCase("bright_pass.fsh", "Uniforms", AmbiencePipeline.UNIFORM_SIZE),
        new BlockCase("menu_background.fsh", "MenuBackgroundData", BackgroundPipeline.UNIFORM_SIZE),
        new BlockCase("shader_sky.fsh", "Uniforms", ShaderSkyPipeline.UNIFORM_SIZE)
    );

    @Test
    void shaderBlocksMatchJavaBufferAllocations() throws IOException {
        for (BlockCase c : CASES) {
            String source = read("/assets/linkenvisuals/shaders/core/" + c.file());
            assertEquals(c.expectedBytes(), std140Size(source, c.block()),
                c.file() + " block " + c.block() + " drifted from its Java allocation");
        }
    }

    @Test
    void killEffectsUboIs224Bytes() {
        assertEquals(224, KillShaderRenderer.UNIFORM_SIZE);
    }

    /** Computes the std140 block size (rounded up to 16) for the given block name. */
    static int std140Size(String source, String block) {
        Matcher matcher = Pattern
            .compile("layout\\(std140\\)\\s*uniform\\s+" + block + "\\s*\\{(.*?)\\};", Pattern.DOTALL)
            .matcher(source);
        assertTrue(matcher.find(), "std140 block '" + block + "' not found");
        int offset = 0;
        for (String rawLine : matcher.group(1).split("\n")) {
            String line = rawLine.replaceAll("//.*", "").trim();
            if (line.isEmpty()) {
                continue;
            }
            Matcher member = Pattern.compile("^(\\w+)\\s+(\\w+)(?:\\[(\\d+)\\])?\\s*;").matcher(line);
            assertTrue(member.find(), "cannot parse std140 member: " + line);
            String type = member.group(1);
            boolean array = member.group(3) != null;
            int count = array ? Integer.parseInt(member.group(3)) : 1;
            int size = typeSize(type);
            int alignment = typeAlignment(type);
            offset = roundUp(offset, array ? 16 : alignment);
            offset += array ? count * roundUp(size, 16) : size;
        }
        return roundUp(offset, 16);
    }

    private static int typeSize(String type) {
        return switch (type) {
            case "float", "int" -> 4;
            case "vec2" -> 8;
            case "vec3" -> 12;
            case "vec4" -> 16;
            case "mat4" -> 64;
            default -> throw new AssertionError("unsupported std140 type: " + type);
        };
    }

    private static int typeAlignment(String type) {
        return switch (type) {
            case "float", "int" -> 4;
            case "vec2" -> 8;
            default -> 16;
        };
    }

    private static int roundUp(int value, int alignment) {
        return (value + alignment - 1) / alignment * alignment;
    }

    private static String read(String resource) throws IOException {
        try (InputStream in = Std140LayoutTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, "missing shader resource: " + resource);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
