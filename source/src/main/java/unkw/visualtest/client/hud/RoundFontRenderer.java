package unkw.visualtest.client.hud;

import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.ui.linkmenu.LinkenFonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

final class RoundFontRenderer {
    private static final Identifier METRICS = Identifier.of("visual-test", "font/round.json");
    private static final Identifier ATLAS = Identifier.of("visual-test", "textures/font/round.png");
    private static final List<TextCommand> COMMANDS = new ArrayList<>();
    private static GlyphAtlas atlas;

    private RoundFontRenderer() {
    }

    private static GlyphAtlas getAtlas() {
        if (atlas == null) {
            try {
                atlas = GlyphAtlas.load(METRICS, ATLAS);
            } catch (Throwable t) {
                atlas = LinkenFonts.body();
            }
        }
        return atlas;
    }

    static void beginFrame() {
        COMMANDS.clear();
    }

    static void queue(String value, float x, float y, float size, int color) {
        if (value != null && !value.isEmpty() && size > 0.0F) {
            COMMANDS.add(new TextCommand(value, x, y, size, color));
        }
    }

    static float width(String value, float size) {
        if (value == null || value.isEmpty()) {
            return 0.0F;
        }
        GlyphAtlas font = getAtlas();
        return font != null ? font.width(value, size) : value.length() * size * 0.5F;
    }

    static void flush(DrawContext context) {
        if (COMMANDS.isEmpty()) {
            return;
        }
        GlyphAtlas font = getAtlas();
        if (font != null) {
            for (TextCommand command : COMMANDS) {
                MsdfText.draw(context, font, command.value(), command.x(), command.y(), command.size(), command.color());
            }
        }
        COMMANDS.clear();
    }

    private record TextCommand(String value, float x, float y, float size, int color) {
    }
}
