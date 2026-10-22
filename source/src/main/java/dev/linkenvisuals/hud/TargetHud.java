package dev.linkenvisuals.hud;

import dev.linkenvisuals.clickgui.ModuleRegistry;
import dev.linkenvisuals.clickgui.link.LinkGuiConfig;
import dev.linkenvisuals.clickgui.link.LinkGuiTheme;
import dev.linkenvisuals.module.impl.StreamerModeModule;
import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.render.RoundedRectPipeline;
import dev.linkenvisuals.ui.Theme;
import dev.linkenvisuals.util.ColorMath;
import dev.linkenvisuals.util.text.StreamerRules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Donor-style target HUD. The card keeps the donor geometry (104×30 body,
 * optional 25 px armour/offhand row), picks the player under the crosshair or
 * in a narrow aim cone, keeps the last target for one second, and mirrors the
 * donor name/HP/absorption/hit-flash treatment.
 */
public final class TargetHud extends HudElement {
    private static final float WIDTH = 104.0f;
    private static final float TOP_HEIGHT = 30.0f;
    private static final float HEAD_SIZE = 20.0f;
    private static final float PADDING = 4.0f;
    private static final float GAP = 4.0f;
    private static final float BOX_SIZE = 15.0f;
    private static final float BOX_GAP = 4.0f;
    private static final float ITEM_ROW_HEIGHT = 25.0f;

    private static final float NAME_SIZE = 7.6f;
    private static final float HP_SIZE = 6.6f;
    private static final float PERCENT_SIZE = 4.6f;

    private static final double MAX_RANGE = 3.5;
    private static final double AIM_HITBOX_MARGIN = 0.75;
    private static final long TARGET_DELAY_MS = 1000L;
    private static final long ITEMS_REFRESH_MS = 100L;
    private static final long CONE_CACHE_MS = 120L;
    private static final long FRIENDS_CACHE_MS = 1000L;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private final Tween fade = new Tween(true);
    private final Tween healthBar = new Tween(false);
    private final Tween absorptionBar = new Tween(false);
    private final Tween hurt = new Tween(true);
    private final List<ItemStack> items = new ArrayList<>(5);

    private PlayerEntity lastTarget;
    private PlayerEntity renderTarget;
    private long lastTargetTime;

    private PlayerEntity coneTarget;
    private long coneCacheUntil;

    private PlayerEntity itemsTarget;
    private long itemsCacheUntil;

    private List<String> friends = List.of();
    private long friendsCacheUntil;

    private float lastHealth = -1.0f;
    private float hitFlash;

    private String cachedRawName;
    private String cachedDisplayName;
    private StreamerRules cachedNameRules;
    private String cachedFittedSource;
    private String cachedFittedName;
    private float cachedFittedWidth = -1.0f;
    private String cachedHpText;
    private long cachedHpKey = Long.MIN_VALUE;

    private float cachedHeight = TOP_HEIGHT;

    private GlyphAtlas bold;
    private long lastFrameNanos = System.nanoTime();

    private static boolean lastRenderVisible;

    public TargetHud() {
        super("targethud", 0.5f, 0.44f);
    }

    /** Autotest hook: the most recent frame actually drew the target card. */
    public static boolean lastRenderVisible() {
        return lastRenderVisible;
    }

    @Override
    public float width() {
        refreshGeometry(System.currentTimeMillis());
        return WIDTH;
    }

    @Override
    public float height() {
        refreshGeometry(System.currentTimeMillis());
        return cachedHeight;
    }

    @Override
    public void render(DrawContext context, float x, float y) {
        lastRenderVisible = false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) {
            return;
        }
        boolean editing = HudManager.isEditing() || client.currentScreen instanceof ChatScreen;
        if (!isEnabled() && !editing) {
            renderTarget = null;
            return;
        }

        long now = System.currentTimeMillis();
        long nowNanos = System.nanoTime();
        float deltaSeconds = (float) Math.min((nowNanos - lastFrameNanos) / 1_000_000_000.0, 0.05);
        lastFrameNanos = nowNanos;

        boolean hasTarget = shouldShowTarget(client, now);
        float alpha = fade.value(hasTarget ? 1.0f : 0.0f, 200L);
        if (alpha <= 0.001f) {
            renderTarget = null;
            return;
        }
        PlayerEntity target = renderTarget;
        if (target == null || shouldHide(target) || isFriend(target)) {
            renderTarget = null;
            return;
        }

        refreshItems(target, now, false);
        float height = TOP_HEIGHT + (items.isEmpty() ? 0.0f : ITEM_ROW_HEIGHT);
        cachedHeight = height;
        float radius = HudStyle.radius(WIDTH, height);
        float fa = Math.max(0.0f, Math.min(1.0f, alpha));
        int globalAlpha = Math.round(255.0f * fa);

        float currentHealth = round1(target.getHealth());
        float maxHealth = Math.max(1.0f, target.getMaxHealth());
        float absorption = round1(target.getAbsorptionAmount());
        if (lastHealth >= 0.0f && currentHealth < lastHealth) {
            hitFlash = 1.0f;
        }
        lastHealth = currentHealth;
        if (hitFlash > 0.0f) {
            hitFlash = Math.max(0.0f, hitFlash - deltaSeconds * 3.0f);
        }

        HudSurface.panel(context, id(), x, y, WIDTH, height, fa);
        if (hitFlash > 0.01f) {
            RoundedRectPipeline.draw(context, x, y, WIDTH, height, radius,
                ColorMath.withAlpha(0xFF3232, Math.round(55.0f * hitFlash * fa)));
        }

        // Player head.
        float headX = x + PADDING;
        float headY = y + (TOP_HEIGHT - HEAD_SIZE) * 0.5f;
        RoundedRectPipeline.draw(context, headX, headY, HEAD_SIZE, HEAD_SIZE, 2.5f,
            HudStyle.inset(Math.round(205.0f * fa)));
        drawPlayerHead(context, target, headX, headY, HEAD_SIZE, 2.5f, headTint(target, globalAlpha));

        // Name and health text.
        float textX = headX + HEAD_SIZE + GAP;
        float rightLimit = x + WIDTH - PADDING;
        float nameMaxWidth = Math.max(0.0f, rightLimit - textX);
        GlyphAtlas font = font();
        String name = displayName(target);
        if (!name.equals(cachedFittedSource) || nameMaxWidth != cachedFittedWidth) {
            cachedFittedSource = name;
            cachedFittedWidth = nameMaxWidth;
            cachedFittedName = fitText(font, name, NAME_SIZE, nameMaxWidth);
        }
        int textAlpha = Math.round(255.0f * fa);
        MsdfText.draw(context, font, cachedFittedName, textX, y + PADDING + 1.0f, NAME_SIZE,
            textColor(textAlpha));

        float hpValue = Math.max(0.0f, currentHealth + absorption);
        long hpKey = Math.round(hpValue * 10.0f);
        if (cachedHpText == null || hpKey != cachedHpKey) {
            cachedHpKey = hpKey;
            cachedHpText = formatHp(hpValue) + " HP";
        }
        MsdfText.draw(context, font, cachedHpText, textX,
            y + PADDING + NAME_SIZE + 3.0f, HP_SIZE, textColor(Math.round(205.0f * fa)));

        // Health / absorption rail along the bottom of the head.
        float barHeight = 2.0f;
        float barX = textX;
        float barY = headY + HEAD_SIZE - barHeight;
        float barWidth = rightLimit - textX;
        RoundedRectPipeline.draw(context, barX, barY, barWidth, barHeight, 1.0f,
            ColorMath.withAlpha(0x000000, Math.round(180.0f * fa)));

        float healthWidth = healthBar.value(barWidth * Math.max(0.0f, Math.min(1.0f, currentHealth / maxHealth)), 200L);
        float absorptionWidth = absorptionBar.value(barWidth * Math.max(0.0f, absorption / maxHealth), 200L);
        int primary = ColorMath.withAlpha(Theme.accent(), globalAlpha);
        int secondary = ColorMath.withAlpha(Theme.secondary(), globalAlpha);
        if (healthWidth > 0.5f) {
            RoundedRectPipeline.drawHorizontalGradient(context, barX, barY,
                Math.max(0.5f, healthWidth), barHeight, 1.0f, primary, secondary);
        }
        if (absorptionWidth > 0.5f) {
            RoundedRectPipeline.drawHorizontalGradient(context, barX, barY,
                Math.max(0.5f, absorptionWidth), barHeight, 1.0f,
                ColorMath.withAlpha(0xFFD700, globalAlpha), ColorMath.withAlpha(0xDAA520, globalAlpha));
        }

        // Donor keeps the offhand first, then helmet → boots.
        if (!items.isEmpty()) {
            float rowWidth = items.size() * BOX_SIZE + Math.max(0, items.size() - 1) * BOX_GAP;
            float startX = x + (WIDTH - rowWidth) * 0.5f;
            float itemY = y + TOP_HEIGHT - 1.0f;
            for (int i = 0; i < items.size(); i++) {
                drawItemCell(context, items.get(i), startX + i * (BOX_SIZE + BOX_GAP), itemY,
                    fa, globalAlpha);
            }
        }

        lastRenderVisible = true;
    }

    private boolean isEnabled() {
        return ModuleRegistry.targetHud() == null || ModuleRegistry.targetHud().value();
    }

    private void refreshGeometry(long now) {
        if (renderTarget != null && !shouldHide(renderTarget)) {
            refreshItems(renderTarget, now, false);
        } else if (itemsTarget != null) {
            items.clear();
            itemsTarget = null;
        }
        cachedHeight = TOP_HEIGHT + (items.isEmpty() ? 0.0f : ITEM_ROW_HEIGHT);
    }

    private void refreshItems(PlayerEntity target, long now, boolean force) {
        if (!force && target == itemsTarget && now < itemsCacheUntil) {
            return;
        }
        items.clear();
        if (!target.getOffHandStack().isEmpty()) {
            items.add(target.getOffHandStack());
        }
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = target.getEquippedStack(slot);
            if (!stack.isEmpty() && stack.getItem() != Items.AIR) {
                items.add(stack);
            }
        }
        itemsTarget = target;
        itemsCacheUntil = now + ITEMS_REFRESH_MS;
    }

    private boolean shouldShowTarget(MinecraftClient client, long now) {
        boolean editing = HudManager.isEditing() || client.currentScreen instanceof ChatScreen;
        if (editing) {
            PlayerEntity current = crosshairPlayer(client);
            if (current != null && !isFriend(current) && !shouldHide(current)) {
                renderTarget = current;
                return true;
            }
            PlayerEntity self = client.player;
            if (self == null || shouldHide(self)) {
                renderTarget = null;
                return false;
            }
            renderTarget = self;
            return true;
        }

        PlayerEntity current = currentTarget(client);
        if (current != null) {
            if (isFriend(current) || shouldHide(current)) {
                renderTarget = null;
                lastTarget = null;
                return false;
            }
            lastTarget = current;
            renderTarget = current;
            lastTargetTime = now;
            return true;
        }

        if (lastTarget != null) {
            if (now - lastTargetTime < TARGET_DELAY_MS
                && !lastTarget.isRemoved()
                && lastTarget.isAlive()
                && client.player.distanceTo(lastTarget) <= MAX_RANGE
                && !shouldHide(lastTarget)
                && !isFriend(lastTarget)) {
                renderTarget = lastTarget;
                return true;
            }
            lastTarget = null;
        }
        return false;
    }

    private PlayerEntity crosshairPlayer(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) {
            return null;
        }
        if (!(((EntityHitResult) hit).getEntity() instanceof PlayerEntity player)
            || player == client.player) {
            return null;
        }
        if (client.player.distanceTo(player) > MAX_RANGE) {
            return null;
        }
        return player;
    }

    private PlayerEntity currentTarget(MinecraftClient client) {
        PlayerEntity crosshair = crosshairPlayer(client);
        if (crosshair != null) {
            return crosshair;
        }
        long now = System.currentTimeMillis();
        if (now < coneCacheUntil) {
            return coneTarget;
        }
        coneCacheUntil = now + CONE_CACHE_MS;
        coneTarget = computeConeTarget(client);
        return coneTarget;
    }

    private PlayerEntity computeConeTarget(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return null;
        }
        Vec3d eye = client.player.getEyePos();
        Vec3d rayEnd = eye.add(client.player.getRotationVec(1.0f).multiply(MAX_RANGE));
        PlayerEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player || shouldHide(player) || isFriend(player)
                || client.player.distanceTo(player) > MAX_RANGE) {
                continue;
            }
            var hit = player.getBoundingBox().expand(AIM_HITBOX_MARGIN).raycast(eye, rayEnd);
            if (hit.isEmpty()) {
                continue;
            }
            double distance = hit.get().squaredDistanceTo(eye);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = player;
            }
        }
        return best != null && hasLineOfSight(client, eye, best) ? best : null;
    }

    private static boolean hasLineOfSight(MinecraftClient client, Vec3d eye, PlayerEntity target) {
        Vec3d center = target.getBoundingBox().getCenter();
        BlockHitResult hit = client.world.raycast(new RaycastContext(
            eye, center, RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE, client.player));
        return hit == null || hit.getType() != HitResult.Type.BLOCK
            || hit.getPos().squaredDistanceTo(eye) >= center.squaredDistanceTo(eye) - 1.0e-3;
    }

    private static boolean shouldHide(PlayerEntity player) {
        return player == null || player.isInvisible() || player.hasStatusEffect(StatusEffects.INVISIBILITY);
    }

    private boolean isFriend(PlayerEntity player) {
        if (player == null || player.getGameProfile() == null) {
            return false;
        }
        String name = player.getGameProfile().name();
        if (name == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now >= friendsCacheUntil) {
            friends = LinkGuiConfig.friends();
            friendsCacheUntil = now + FRIENDS_CACHE_MS;
        }
        for (String friend : friends) {
            if (friend.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String displayName(PlayerEntity target) {
        String raw = target.getGameProfile() == null ? null : target.getGameProfile().name();
        if (raw == null || raw.isBlank()) {
            raw = "Player";
        }
        StreamerRules rules = StreamerModeModule.rules();
        if (raw.equals(cachedRawName) && rules == cachedNameRules && cachedDisplayName != null) {
            return cachedDisplayName;
        }
        cachedRawName = raw;
        cachedNameRules = rules;
        cachedDisplayName = rules == null ? raw : rules.sanitize(raw);
        return cachedDisplayName;
    }

    private void drawPlayerHead(DrawContext context, PlayerEntity player, float x, float y,
                                float size, float radius, int tint) {
        Identifier skin = null;
        if (player instanceof AbstractClientPlayerEntity clientPlayer && clientPlayer.getSkin() != null) {
            skin = clientPlayer.getSkin().body().texturePath();
        }
        if (skin == null) {
            RoundedRectPipeline.draw(context, x, y, size, size, radius,
                ColorMath.withAlpha(Theme.accent(), tint >>> 24));
            return;
        }
        int px = Math.round(x);
        int py = Math.round(y);
        int side = Math.round(size);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, skin, px, py, 8.0f, 8.0f,
            side, side, 8, 8, 64, 64, tint);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, skin, px, py, 40.0f, 8.0f,
            side, side, 8, 8, 64, 64, tint);
    }

    private int headTint(PlayerEntity player, int alpha) {
        if (player.hurtTime > 0) {
            float progress = Math.max(0.0f, Math.min(10.0f, player.hurtTime)) / 10.0f;
            float intensity = hurt.value(progress, 300L);
            int greenBlue = Math.max(0, Math.min(255, Math.round(255.0f - 105.0f * intensity)));
            return ColorMath.withAlpha(0xFF0000 | greenBlue << 8 | greenBlue, alpha);
        }
        hurt.value(0.0f, 300L);
        return ColorMath.withAlpha(0xFFFFFF, alpha);
    }

    private void drawItemCell(DrawContext context, ItemStack stack, float x, float y,
                              float alpha, int globalAlpha) {
        RoundedRectPipeline.draw(context, x, y, BOX_SIZE, BOX_SIZE, 3.5f,
            HudStyle.inset(Math.round(235.0f * alpha), 0.42f));
        RoundedRectPipeline.drawBorder(context, x, y, BOX_SIZE, BOX_SIZE, 3.5f, 1.0f, 2.0f, 2.0f,
            ColorMath.withAlpha(Theme.accent(), Math.round(170.0f * alpha)));

        float scale = 0.72f * alpha;
        float itemPx = 16.0f * scale;
        float ix = x + (BOX_SIZE - itemPx) * 0.5f;
        float iy = y + (BOX_SIZE - itemPx) * 0.5f;
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(ix, iy);
        matrices.scale(scale, scale);
        context.drawItem(stack, 0, 0);
        matrices.popMatrix();

        if (!stack.isDamageable() || stack.getMaxDamage() <= 0) {
            return;
        }
        float durability = 1.0f - stack.getDamage() / (float) stack.getMaxDamage();
        int percent = Math.max(0, Math.min(100, Math.round(durability * 100.0f)));
        boolean light = LinkGuiTheme.isLight();
        int colour = percent < 25 ? (light ? 0xC62828 : 0xFF6C6C)
            : percent < 50 ? (light ? 0xB07400 : 0xFFD352)
            : (light ? 0x228A3C : 0x6CDD7A);
        MsdfText.drawCentered(context, font(), percent + "%", x + BOX_SIZE * 0.5f,
            y + BOX_SIZE + 1.5f, PERCENT_SIZE, ColorMath.withAlpha(colour, globalAlpha));
    }

    private GlyphAtlas font() {
        if (bold == null) {
            bold = Fonts.titleFont();
        }
        return bold;
    }

    static String formatHp(float hp) {
        if (hp < 1000.0f) {
            return String.format(Locale.ROOT, "%.1f", hp);
        }
        if (hp < 1_000_000.0f) {
            return String.format(Locale.ROOT, "%.1fK", hp / 1000.0f);
        }
        return String.format(Locale.ROOT, "%.1fM", hp / 1_000_000.0f);
    }

    static String fitText(GlyphAtlas font, String text, float size, float maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (font.width(text, size) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        float ellipsisWidth = font.width(ellipsis, size);
        if (ellipsisWidth >= maxWidth) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            builder.append(text.charAt(i));
            if (font.width(builder.toString(), size) + ellipsisWidth > maxWidth) {
                builder.deleteCharAt(builder.length() - 1);
                break;
            }
        }
        return builder.append(ellipsis).toString();
    }

    private static int textColor(int alpha) {
        return ColorMath.withAlpha(LinkGuiTheme.isLight() ? 0x1C1F26 : 0xFFFFFF, alpha);
    }

    private static float round1(float value) {
        return Math.round(value * 10.0f) / 10.0f;
    }

    /** Time-based donor animation: linear for bars, ease-out-cubic for fades. */
    private static final class Tween {
        private final boolean easeOut;
        private float value;
        private float from;
        private float target;
        private long startedAt;
        private long durationMs = 1L;
        private boolean active;

        private Tween(boolean easeOut) {
            this.easeOut = easeOut;
        }

        private float value(float next, long duration) {
            long now = System.nanoTime();
            if (!active || next != target) {
                from = value;
                target = next;
                startedAt = now;
                durationMs = Math.max(1L, duration);
                active = true;
            }
            float t = Math.min(1.0f, (now - startedAt) / (durationMs * 1_000_000.0f));
            float eased = easeOut ? 1.0f - (1.0f - t) * (1.0f - t) * (1.0f - t) : t;
            value = from + (target - from) * eased;
            return value;
        }
    }
}
