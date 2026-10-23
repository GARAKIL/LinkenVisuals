package dev.linkenvisuals.hud;

import dev.linkenvisuals.clickgui.setting.KeybindSetting;
import dev.linkenvisuals.helper.HelperActions;
import dev.linkenvisuals.module.Modules;
import dev.linkenvisuals.render.Fonts;
import dev.linkenvisuals.render.GlyphAtlas;
import dev.linkenvisuals.render.MsdfText;
import dev.linkenvisuals.util.ColorMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Donor HelperItemsHud: compact tiles for the bound Ft/Hw/Rw helper items that
 * are present in the hotbar. The donor derives labels from BindSettings; this
 * port uses the same KeybindSetting labels and a 100 ms tile cache.
 */
public final class HelperItemsHud extends HudElement {
    private static final float TILE = 18.0f;
    private static final float GAP = 4.0f;
    private static final float PAD = 5.0f;
    private static final float HEIGHT = 29.0f;
    private static final float ITEM_SCALE = 0.875f;

    private static final List<Mapping> MAPPINGS = List.of(
        new Mapping("fthelper", "minecraft:ender_eye", "Desor"),
        new Mapping("fthelper", "minecraft:netherite_scrap", "Trapka"),
        new Mapping("fthelper", "minecraft:sugar", "Yavka"),
        new Mapping("fthelper", "minecraft:phantom_membrane", "Bozhka"),
        new Mapping("fthelper", "minecraft:dried_kelp", "Plast"),
        new Mapping("fthelper", "minecraft:snowball", "Snowball"),
        new Mapping("hwhelper", "minecraft:nether_star", "Stan"),
        new Mapping("hwhelper", "minecraft:popped_chorus_fruit", "Trapka"),
        new Mapping("hwhelper", "minecraft:prismarine_shard", "Boom Trapka"),
        new Mapping("hwhelper", "minecraft:fire_charge", "Boom Thing"),
        new Mapping("rwhelper", "minecraft:firework_star", "Anti Fly"),
        new Mapping("rwhelper", "minecraft:flower_banner_pattern", "Exp Scroll"),
        new Mapping("rwhelper", "minecraft:heart_of_the_sea", "Trap"),
        new Mapping("rwhelper", "minecraft:firework_star", "Dark Pulse")
    );

    private static List<Tile> cached;
    private static long cacheAt;
    private static boolean lastRenderVisible;

    public HelperItemsHud() {
        super("helperitemshud", 0.5f, 0.75f);
    }

    public static boolean lastRenderVisible() {
        return lastRenderVisible;
    }

    @Override
    public float width() {
        List<Tile> tiles = tiles(false);
        return PAD * 2.0f + Math.max(1, tiles.size()) * TILE
            + Math.max(0, tiles.size() - 1) * GAP;
    }

    @Override
    public float height() {
        return HEIGHT;
    }

    @Override
    public void render(DrawContext context, float x, float y) {
        lastRenderVisible = false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        List<Tile> tiles = tiles(HudManager.isEditing());
        if (tiles.isEmpty()) {
            return;
        }
        float w = PAD * 2.0f + tiles.size() * TILE + Math.max(0, tiles.size() - 1) * GAP;
        HudSurface.panel(context, "helperitems", x, y, w, HEIGHT, 1.0f);
        GlyphAtlas font = Fonts.titleFont();
        if (font == null) {
            return;
        }
        float cursor = x + PAD;
        for (Tile tile : tiles) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(cursor, y + 3.0f);
            context.getMatrices().scale(ITEM_SCALE, ITEM_SCALE);
            context.drawItem(tile.stack, 0, 0);
            context.getMatrices().popMatrix();
            if (font.canRender(tile.label)) {
                float textW = font.width(tile.label, 5.8f);
                MsdfText.drawOnExact(context, font, tile.label,
                    cursor + (TILE - textW) * 0.5f, y + HEIGHT - 6.0f, 5.8f,
                    ColorMath.withAlpha(0xFFFFFF, 235));
            }
            cursor += TILE + GAP;
        }
        lastRenderVisible = true;
    }

    private static List<Tile> tiles(boolean preview) {
        long now = System.currentTimeMillis();
        if (cached != null && now - cacheAt < 100L) {
            return cached;
        }
        cacheAt = now;
        List<Tile> out = new ArrayList<>();
        for (Mapping mapping : MAPPINGS) {
            dev.linkenvisuals.module.Module module = mapping.module();
            if (module == null || !module.enabled()) {
                continue;
            }
            KeybindSetting bind = bind(mapping);
            if (bind == null || bind.code() < 0) {
                continue;
            }
            ItemStack stack = itemStack(mapping);
            if (!preview && HelperActions.findHotbar(MinecraftClient.getInstance(),
                stack.getItem()) < 0) {
                continue;
            }
            out.add(new Tile(stack, fit(bind.label())));
        }
        if (preview && out.isEmpty()) {
            out.add(new Tile(new ItemStack(Items.SNOWBALL), "R"));
            out.add(new Tile(new ItemStack(Items.NETHER_STAR), "Z"));
            out.add(new Tile(new ItemStack(Items.CLOCK), "V"));
        }
        cached = out;
        return out;
    }

    private static ItemStack itemStack(Mapping mapping) {
        net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(
            net.minecraft.util.Identifier.of(mapping.itemId));
        return new ItemStack(item);
    }

    private static KeybindSetting bind(Mapping mapping) {
        if ("fthelper".equals(mapping.moduleId) && Modules.ftHelper() != null) {
            return switch (mapping.setting) {
                case "Desor" -> Modules.ftHelper().desor;
                case "Trapka" -> Modules.ftHelper().trapka;
                case "Yavka" -> Modules.ftHelper().yavka;
                case "Bozhka" -> Modules.ftHelper().bozhka;
                case "Plast" -> Modules.ftHelper().plast;
                default -> Modules.ftHelper().snowball;
            };
        }
        if ("hwhelper".equals(mapping.moduleId) && Modules.hwHelper() != null) {
            return switch (mapping.setting) {
                case "Stan" -> Modules.hwHelper().stan;
                case "Trapka" -> Modules.hwHelper().trapka;
                case "Boom Trapka" -> Modules.hwHelper().boomTrapka;
                default -> Modules.hwHelper().boomThing;
            };
        }
        if ("rwhelper".equals(mapping.moduleId) && Modules.rwHelper() != null) {
            return switch (mapping.setting) {
                case "Anti Fly" -> Modules.rwHelper().antiFly;
                case "Exp Scroll" -> Modules.rwHelper().expScroll;
                case "Trap" -> Modules.rwHelper().trap;
                default -> Modules.rwHelper().darkPulse;
            };
        }
        return null;
    }

    private static String fit(String label) {
        if (label == null || label.isBlank() || "NONE".equalsIgnoreCase(label)) {
            return "?";
        }
        String value = label.trim();
        if (value.length() <= 3) {
            return value;
        }
        return value.substring(0, 3);
    }

    private record Mapping(String moduleId, String itemId, String setting) {
        private dev.linkenvisuals.module.Module module() {
            return switch (moduleId) {
                case "fthelper" -> Modules.ftHelper();
                case "hwhelper" -> Modules.hwHelper();
                default -> Modules.rwHelper();
            };
        }
    }

    private record Tile(ItemStack stack, String label) {
    }
}
