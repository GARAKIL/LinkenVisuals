package dev.linkenvisuals.hud;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.module.impl.CustomVitalsModule;
import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.Theme;
import dev.linkenvisuals.util.ColorMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Donor CustomVitals replacement for the vanilla health/food/armour/air/XP
 * bars. It is intentionally hotbar-anchored (182 px, above the hotbar), not a
 * draggable HudElement: this is the screen-anchored layer the donor uses.
 */
public final class CustomVitalsHud {
    private static final float TOTAL_W = 182.0f;
    private static final float SLOT = 18.0f;
    private static final int HEALTH_RED = 0xE0324A;
    private static final int HEALTH_GREEN = 0x7CB342;
    private static final int HEALTH_WITHER = 0x6E4A57;
    private static final int FOOD_AMBER = 0xD9A441;
    private static final int FOOD_GREEN = 0x8BC34A;
    private static final int AIR_BLUE = 0x4D9BFF;
    private static final int MOUNT_PINK = 0xC96A85;
    private static final int ARMOR_SILVER = 0xC8D2DC;

    private static float displayedHealth = -1.0f;
    private static long lastFrameMs;
    private static float damageFlash;
    private static boolean lastRenderVisible;

    private CustomVitalsHud() {
    }

    public static boolean active() {
        CustomVitalsModule module = Modules.customVitals();
        MinecraftClient client = MinecraftClient.getInstance();
        return module != null && module.enabled()
            && (client.options == null || !client.options.hudHidden);
    }

    public static boolean lastRenderVisible() {
        return lastRenderVisible;
    }

    public static void render(DrawContext context) {
        lastRenderVisible = false;
        CustomVitalsModule module = Modules.customVitals();
        MinecraftClient client = MinecraftClient.getInstance();
        if (module == null || !module.enabled() || client == null
            || client.player == null || client.world == null
            || (client.options != null && client.options.hudHidden)) {
            return;
        }
        boolean editing = HudManager.isEditing() || client.currentScreen instanceof ChatScreen;
        PlayerEntity player = client.player;
        if (!editing && (player.isSpectator() || player.getAbilities().creativeMode)) {
            return;
        }

        State state = capture(client, player, module, editing);
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        float x0 = width * 0.5f - TOTAL_W * 0.5f;
        float bottom = height - SLOT - 4.0f - 2.0f - 1.0f;
        switch (state.style) {
            case STACK -> renderStack(context, x0, bottom, state);
            case BARS -> renderBars(context, x0, bottom, state);
            default -> renderSegments(context, x0, bottom, state);
        }
        lastRenderVisible = true;
    }

    private static State capture(MinecraftClient client, PlayerEntity player,
                                 CustomVitalsModule module, boolean editing) {
        State state = new State();
        state.style = switch (module.styleIndex()) {
            case 1 -> Style.STACK;
            case 2 -> Style.BARS;
            default -> Style.SEGMENTS;
        };
        state.segments = Math.max(3, Math.min(10, module.segmentCount()));
        state.armorRow = module.armorRow();
        state.health = (int) Math.ceil(player.getHealth());
        state.healthFraction = player.getHealth() / Math.max(1.0f, player.getMaxHealth());
        state.food = player.getHungerManager().getFoodLevel();
        state.foodFraction = state.food / 20.0f;
        state.experience = player.experienceProgress;
        state.level = player.experienceLevel;
        state.armor = player.getArmor();
        state.air = player.getAir();
        state.maxAir = Math.max(1, player.getMaxAir());
        state.airFraction = Math.max(0.0f, Math.min(1.0f, state.air / (float) state.maxAir));
        state.showAir = player.isSubmergedInWater() || state.air < state.maxAir;
        state.poison = player.hasStatusEffect(StatusEffects.POISON);
        state.wither = player.hasStatusEffect(StatusEffects.WITHER);
        state.hunger = player.hasStatusEffect(StatusEffects.HUNGER);
        if (player.getVehicle() instanceof LivingEntity mount) {
            state.riding = true;
            state.mountFraction = mount.getHealth() / Math.max(1.0f, mount.getMaxHealth());
        }
        if (player instanceof ClientPlayerEntity clientPlayer) {
            state.jumpFraction = Math.max(0.0f, Math.min(1.0f, clientPlayer.getMountJumpStrength()));
        }
        if (editing && state.armor == 0) {
            state.armor = 12;
        }
        return state;
    }

    private static void renderSegments(DrawContext context, float x0, float bottom, State state) {
        updateAnimation(state);
        final float expHeight = 6.0f;
        final float segmentHeight = 6.0f;
        final float gap = 3.0f;
        final float thinHeight = 3.0f;
        final float middle = 8.0f;
        int count = state.segments;
        float half = (TOTAL_W - middle) * 0.5f;
        float segmentWidth = (half - (count - 1) * gap) / count;

        float pulse = 1.0f;
        if (state.healthFraction > 0.0f && state.healthFraction < 0.3f) {
            float t = (System.currentTimeMillis() % 1000L) / 1000.0f;
            pulse = 0.72f + 0.28f * (0.5f + 0.5f * (float) Math.sin(t * Math.PI * 2.0f));
        }
        int healthAlpha = Math.round(248.0f * pulse);
        int healthColor = state.wither ? HEALTH_WITHER
            : state.poison ? HEALTH_GREEN : HEALTH_RED;
        int foodColor = state.hunger ? FOOD_GREEN : FOOD_AMBER;

        float expY = bottom - expHeight;
        float mainY = expY - gap - segmentHeight;
        boolean showArmor = state.armorRow && state.armor > 0;
        float thinY = mainY - 2.0f - thinHeight;

        if (showArmor) {
            segmentRow(context, x0, thinY, half, segmentWidth, thinHeight, gap, count,
                state.armor / 20.0f, ARMOR_SILVER, false, 210, 0.0f);
        }
        if (state.showAir) {
            segmentRow(context, x0 + TOTAL_W - half, thinY, half, segmentWidth, thinHeight, gap, count,
                state.airFraction, AIR_BLUE, true, 235, 0.0f);
        }

        float ghost = Math.max(0.0f, displayedHealth - state.healthFraction);
        segmentRow(context, x0, mainY, half, segmentWidth, segmentHeight, gap, count,
            state.healthFraction, healthColor, false, healthAlpha, ghost);
        if (damageFlash > 0.01f) {
            RoundedRectPipeline.draw(context, x0 - 1.0f, mainY - 1.0f, half + 2.0f, segmentHeight + 2.0f,
                3.0f, ColorMath.withAlpha(0xFFFFFF, Math.round(70.0f * damageFlash)));
        }

        if (state.riding) {
            segmentRow(context, x0 + TOTAL_W - half, mainY, half, segmentWidth, segmentHeight, gap, count,
                state.mountFraction, MOUNT_PINK, true, 245, 0.0f);
        } else {
            segmentRow(context, x0 + TOTAL_W - half, mainY, half, segmentWidth, segmentHeight, gap, count,
                state.foodFraction, foodColor, true, 245, 0.0f);
        }

        boolean jump = state.riding && state.jumpFraction > 0.0f;
        float experience = jump ? state.jumpFraction : state.experience;
        int first = jump ? 0xF2C572 : Theme.accent();
        int second = jump ? 0xD99A2E : Theme.secondary();
        RoundedRectPipeline.draw(context, x0, expY, TOTAL_W, expHeight, expHeight * 0.5f,
            HudStyle.inset(225));
        RoundedRectPipeline.drawOutline(context, x0, expY, TOTAL_W, expHeight, expHeight * 0.5f, 1.0f,
            ColorMath.withAlpha(Theme.accent(), 45));
        if (experience > 0.0f) {
            float filled = Math.max(expHeight, TOTAL_W * clamp(experience));
            RoundedRectPipeline.drawHorizontalGradient(context, x0, expY, filled, expHeight,
                expHeight * 0.5f, ColorMath.withAlpha(first, 255), ColorMath.withAlpha(second, 255));
        }
        if (!jump && state.level > 0) {
            drawLevelChip(context, x0 + TOTAL_W * 0.5f, expY + expHeight * 0.5f, expHeight,
                Integer.toString(state.level));
        }
    }

    private static void segmentRow(DrawContext context, float x, float y, float rowWidth,
                                   float segmentWidth, float segmentHeight, float gap, int count,
                                   float fraction, int color, boolean rightToLeft, int alpha,
                                   float ghostFraction) {
        RoundedRectPipeline.draw(context, x - 1.0f, y - 1.0f, rowWidth + 2.0f, segmentHeight + 2.0f,
            3.0f, HudStyle.inset(120));
        float filled = clamp(fraction) * count;
        float ghost = clamp(fraction + ghostFraction) * count;
        int emptyBorder = ColorMath.withAlpha(0xFFFFFF, 55);
        for (int k = 0; k < count; k++) {
            float segmentX = rightToLeft ? x + (count - 1 - k) * (segmentWidth + gap)
                : x + k * (segmentWidth + gap);
            RoundedRectPipeline.draw(context, segmentX, y, segmentWidth, segmentHeight, 2.0f,
                HudStyle.inset(165));
            RoundedRectPipeline.drawOutline(context, segmentX, y, segmentWidth, segmentHeight, 2.0f,
                1.0f, emptyBorder);
            float fill = clamp(filled - k);
            if (fill > 0.0f) {
                float fillWidth = Math.max(1.5f, segmentWidth * fill);
                float fillX = rightToLeft ? segmentX + segmentWidth - fillWidth : segmentX;
                RoundedRectPipeline.draw(context, fillX, y, fillWidth, segmentHeight, 2.0f,
                    ColorMath.withAlpha(color, alpha));
            }
            float ghostFill = clamp(ghost - k) - fill;
            if (ghostFill > 0.02f) {
                float ghostWidth = Math.max(1.5f, segmentWidth * ghostFill);
                float base = rightToLeft
                    ? segmentX + segmentWidth - clamp(filled - k) * segmentWidth
                    : segmentX + clamp(filled - k) * segmentWidth;
                float ghostX = rightToLeft ? base - ghostWidth : base;
                RoundedRectPipeline.draw(context, ghostX, y, ghostWidth, segmentHeight, 2.0f,
                    ColorMath.withAlpha(0xFFFFFF, 150));
            }
        }
    }

    private static void drawLevelChip(DrawContext context, float centerX, float centerY,
                                      float expHeight, String text) {
        GlyphAtlas font = Fonts.titleFont();
        float size = 6.0f;
        float width = font.width(text, size);
        float chipHeight = expHeight + 3.0f;
        float chipWidth = width + 8.0f;
        float left = centerX - chipWidth * 0.5f;
        float top = centerY - chipHeight * 0.5f;
        RoundedRectPipeline.draw(context, left, top, chipWidth, chipHeight, chipHeight * 0.5f,
            HudStyle.inset(235));
        RoundedRectPipeline.drawOutline(context, left, top, chipWidth, chipHeight, chipHeight * 0.5f,
            1.0f, ColorMath.withAlpha(Theme.accent(), 95));
        MsdfText.drawCenteredOn(context, font, text, centerX, centerY - 0.5f, size,
            ColorMath.withAlpha(Theme.secondary(), 255));
    }

    private static void renderBars(DrawContext context, float x0, float bottom, State state) {
        float expHeight = 3.0f;
        float rowHeight = 4.0f;
        float gap = 3.0f;
        float expY = bottom - expHeight;
        float rowY = expY - gap - rowHeight;
        float half = (TOTAL_W - 6.0f) * 0.5f;
        bar(context, x0, rowY, half, rowHeight, 2.0f, state.healthFraction, HEALTH_RED);
        bar(context, x0 + TOTAL_W - half, rowY, half, rowHeight, 2.0f, state.foodFraction, FOOD_AMBER);
        bar(context, x0, expY, TOTAL_W, expHeight, 1.5f, state.experience, Theme.accent());
        if (state.level > 0) {
            GlyphAtlas font = Fonts.titleFont();
            String text = Integer.toString(state.level);
            float size = 6.5f;
            float width = font.width(text, size);
            MsdfText.draw(context, font, text, x0 + TOTAL_W * 0.5f - width * 0.5f,
                rowY + rowHeight * 0.5f - size * 0.5f, size, ColorMath.withAlpha(Theme.secondary(), 255));
        }
    }

    private static void renderStack(DrawContext context, float x0, float bottom, State state) {
        float padding = 3.0f;
        float barHeight = 4.0f;
        float rowGap = 3.0f;
        float panelHeight = padding * 2.0f + barHeight * 3.0f + rowGap * 2.0f;
        float panelY = bottom - panelHeight;
        HudSurface.panel(context, "vitals", x0, panelY, TOTAL_W, panelHeight, 1.0f);
        float numberWidth = 15.0f;
        float barWidth = TOTAL_W - padding * 2.0f - numberWidth;
        float barX = x0 + padding;
        float y = panelY + padding;
        stackRow(context, barX, y, barWidth, barHeight, state.healthFraction, HEALTH_RED,
            Integer.toString(state.health), x0 + TOTAL_W - padding);
        y += barHeight + rowGap;
        stackRow(context, barX, y, barWidth, barHeight, state.foodFraction, FOOD_AMBER,
            Integer.toString(state.food), x0 + TOTAL_W - padding);
        y += barHeight + rowGap;
        stackRow(context, barX, y, barWidth, barHeight, state.experience, Theme.accent(),
            Integer.toString(state.level), x0 + TOTAL_W - padding);
    }

    private static void stackRow(DrawContext context, float x, float y, float width, float height,
                                 float fraction, int color, String number, float rightEdge) {
        bar(context, x, y, width, height, 2.0f, fraction, color);
        GlyphAtlas font = Fonts.titleFont();
        float size = 6.0f;
        float numberWidth = font.width(number, size);
        MsdfText.draw(context, font, number, rightEdge - numberWidth,
            y + (height - size) * 0.5f - 0.5f, size, ColorMath.withAlpha(0xFFFFFF, 235));
    }

    private static void bar(DrawContext context, float x, float y, float width, float height,
                            float radius, float fraction, int color) {
        RoundedRectPipeline.draw(context, x, y, width, height, radius, HudStyle.inset(200));
        float clamped = clamp(fraction);
        if (clamped <= 0.0f) {
            return;
        }
        float filled = Math.max(height, width * clamped);
        RoundedRectPipeline.draw(context, x, y, filled, height, radius,
            ColorMath.withAlpha(color, 245));
    }

    private static void updateAnimation(State state) {
        long now = System.currentTimeMillis();
        float delta = lastFrameMs == 0L ? 1.0f / 60.0f
            : Math.min(0.1f, (now - lastFrameMs) / 1000.0f);
        lastFrameMs = now;
        if (displayedHealth < 0.0f) {
            displayedHealth = state.healthFraction;
        }
        if (state.healthFraction < displayedHealth) {
            damageFlash = 1.0f;
            displayedHealth += (state.healthFraction - displayedHealth)
                * (1.0f - (float) Math.exp(-6.0f * delta));
        } else {
            displayedHealth = state.healthFraction;
        }
        damageFlash *= (float) Math.exp(-4.0f * delta);
        if (damageFlash < 0.01f) {
            damageFlash = 0.0f;
        }
    }

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private enum Style {
        SEGMENTS, STACK, BARS
    }

    private static final class State {
        Style style = Style.SEGMENTS;
        int segments = 8;
        boolean armorRow = true;
        float healthFraction;
        float foodFraction;
        float experience;
        float airFraction;
        float mountFraction;
        float jumpFraction;
        int health;
        int food;
        int level;
        int armor;
        int air;
        int maxAir;
        boolean showAir;
        boolean riding;
        boolean poison;
        boolean wither;
        boolean hunger;
    }
}
