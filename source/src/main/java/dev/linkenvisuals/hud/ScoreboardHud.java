package dev.linkenvisuals.hud;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.Theme;
import dev.linkenvisuals.util.ColorMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Donor-style custom scoreboard. It is a normal HUD element, so it follows the
 * LINKEN HUD layout/editor and can be moved independently of the vanilla
 * sidebar; the vanilla sidebar is cancelled by {@code InGameHudMixin} while
 * this element is enabled.
 */
public final class ScoreboardHud extends HudElement {
    private static final float PADDING = 7.0f;
    private static final float SIDE_BAR = 2.5f;
    private static final float LINE = 11.0f;
    private static final float MIN_WIDTH = 132.0f;
    private static final int MAX_LINES = 15;

    private Data data = Data.EMPTY;
    private long cacheUntil;
    private float cachedWidth;
    private float cachedHeight;

    public ScoreboardHud() {
        super("scoreboard", 0.94f, 0.18f);
    }

    @Override
    public float width() {
        refresh();
        return cachedWidth;
    }

    @Override
    public float height() {
        refresh();
        return cachedHeight;
    }

    @Override
    public void render(DrawContext context, float x, float y) {
        refresh();
        if (data.lines().isEmpty() || cachedWidth <= 0.0f || cachedHeight <= 0.0f) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return;
        }

        float maxX = context.getScaledWindowWidth() - cachedWidth - 4.0f;
        if (x > maxX) {
            x = Math.max(4.0f, maxX);
        }
        float radius = Math.min(ModuleRegistry.scoreboardRadius().value(),
            Math.min(cachedWidth, cachedHeight) * 0.5f);
        int alpha = Math.round(ModuleRegistry.scoreboardOpacity().value() * 2.55f);
        int background = ColorMath.withAlpha(0x0E121A, Math.round(alpha * 0.86f));
        RoundedRectPipeline.draw(context, x, y, cachedWidth, cachedHeight, radius, background);
        RoundedRectPipeline.drawOutline(context, x, y, cachedWidth, cachedHeight, radius, 1.0f,
            ColorMath.withAlpha(Theme.accent(), Math.round(alpha * 0.62f)));

        float sideInset = Math.min(4.0f, radius * 0.5f);
        RoundedRectPipeline.draw(context, x + 2.0f, y + sideInset, SIDE_BAR,
            Math.max(2.0f, cachedHeight - sideInset * 2.0f), SIDE_BAR * 0.5f,
            ColorMath.withAlpha(Theme.accent(), Math.round(alpha * 0.9f)));

        int textX = Math.round(x + SIDE_BAR + PADDING);
        int textRight = Math.round(x + cachedWidth - PADDING);
        int titleWidth = client.textRenderer.getWidth(data.title());
        int titleX = textX + Math.max(0, (textRight - textX - titleWidth) / 2);
        int titleY = Math.round(y + PADDING);
        context.drawText(client.textRenderer, data.title(), titleX, titleY, 0xFFFFFFFF, false);

        int dividerY = titleY + Math.round(LINE);
        context.fill(textX, dividerY, textRight, dividerY + 1,
            ColorMath.withAlpha(Theme.accent(), Math.round(alpha * 0.55f)));

        int rowY = dividerY + 4;
        for (Line line : data.lines()) {
            context.drawText(client.textRenderer, line.name(), textX, rowY, 0xFFE8E8E8, false);
            if (data.showScores() && line.score() != null) {
                int scoreWidth = client.textRenderer.getWidth(line.score());
                context.drawText(client.textRenderer, line.score(), textRight - scoreWidth, rowY,
                    0xFF000000 | Theme.accent(), false);
            }
            rowY += Math.round(LINE);
        }
    }

    private void refresh() {
        if (ModuleRegistry.customScoreboard() == null || !ModuleRegistry.customScoreboard().value()) {
            data = Data.EMPTY;
            cachedWidth = 0.0f;
            cachedHeight = 0.0f;
            return;
        }
        long now = System.currentTimeMillis();
        if (now < cacheUntil) {
            return;
        }
        data = collect();
        cachedWidth = data.width();
        cachedHeight = data.lines().isEmpty() ? 0.0f
            : PADDING * 2.0f + LINE * (data.lines().size() + 1.0f) + 3.0f;
        cacheUntil = now + 50L;
    }

    private Data collect() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.textRenderer == null) {
            return Data.EMPTY;
        }
        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = sidebarObjective(scoreboard, client);
        if (objective == null) {
            return Data.EMPTY;
        }

        List<ScoreboardEntry> sorted = scoreboard.getScoreboardEntries(objective).stream()
            .filter(entry -> !entry.hidden())
            .sorted(Comparator.comparingInt(ScoreboardEntry::value).reversed()
                .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER))
            .limit(MAX_LINES)
            .toList();
        if (sorted.isEmpty()) {
            return Data.EMPTY;
        }

        TextRenderer renderer = client.textRenderer;
        boolean showScores = showScores(sorted);
        List<Line> lines = new ArrayList<>(sorted.size());
        int maxWidth = renderer.getWidth(objective.getDisplayName());
        for (ScoreboardEntry entry : sorted) {
            Team team = scoreboard.getScoreHolderTeam(entry.owner());
            Text name = Team.decorateName(team, entry.name());
            Text score = Text.literal(String.valueOf(entry.value()));
            lines.add(new Line(name, score));
            int width = renderer.getWidth(name) + (showScores ? 10 + renderer.getWidth(score) : 0);
            maxWidth = Math.max(maxWidth, width);
        }
        float width = Math.max(MIN_WIDTH, maxWidth + SIDE_BAR + PADDING * 2.0f);
        return new Data(objective.getDisplayName(), List.copyOf(lines), showScores, width);
    }

    private static ScoreboardObjective sidebarObjective(Scoreboard scoreboard, MinecraftClient client) {
        if (client.player != null) {
            Team team = scoreboard.getScoreHolderTeam(client.player.getNameForScoreboard());
            if (team != null) {
                ScoreboardDisplaySlot slot = ScoreboardDisplaySlot.fromFormatting(team.getColor());
                if (slot != null) {
                    ScoreboardObjective objective = scoreboard.getObjectiveForSlot(slot);
                    if (objective != null) {
                        return objective;
                    }
                }
            }
        }
        return scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
    }

    private static boolean showScores(List<ScoreboardEntry> sorted) {
        if (sorted.size() <= 1) {
            return sorted.isEmpty() || sorted.get(0).value() != 0;
        }
        boolean sequential = true;
        boolean allEqual = true;
        for (int i = 1; i < sorted.size(); i++) {
            int previous = sorted.get(i - 1).value();
            int current = sorted.get(i).value();
            if (current != previous - 1) {
                sequential = false;
            }
            if (current != previous) {
                allEqual = false;
            }
        }
        return !(sequential || allEqual);
    }

    private record Line(Text name, Text score) {
    }

    private record Data(Text title, List<Line> lines, boolean showScores, float width) {
        private static final Data EMPTY = new Data(Text.empty(), List.of(), false, 0.0f);
    }
}
