package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.KeySetting;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.util.InventoryMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * ElytraSwap (Phase 5.7): swaps the chest armor with an elytra — or with the
 * best carried chestplate when an elytra is already worn — through inventory
 * clicks. Ported from the donor's {@code ElytraSwap} (283 lines).
 *
 * <p>Deviations from the donor (documented in MERGE_DECISIONS D35): only the
 * paced (one tick between clicks) mode is kept — the donor itself marks its
 * {@code Instant} mode as anti-cheat aggressive — and the donor-server
 * {@code /fly} command is not ported. Chestplate priority is fixed to strict
 * best-first ordering (donor returned chainmail before netherite).
 *
 * <p>In survival a temporary {@code InventoryScreen} is opened so clicks run
 * against a visible container, then closed after the swap; the screen is
 * closed early if the player closes it manually, and all clicks are paced one
 * tick apart.
 */
public final class ElytraSwapModule extends Module {
    private static final int CHEST_SLOT_ID = 6;
    private static final int SWAP_STEP_DELAY_TICKS = 1;
    private static final int WAIT_SCREEN_TIMEOUT_TICKS = 10;
    private static final int INVENTORY_CLOSE_DELAY_TICKS = 2;
    private static final int INVENTORY_SIZE = 36;

    public final KeySetting key;

    private boolean keyWasDown;
    private SwapPhase phase = SwapPhase.NONE;
    private int sourceSlot = -1;
    private int stepDelay;
    private int waitScreenTicks;
    private int closeTicks;
    private boolean openedInventory;

    public ElytraSwapModule() {
        this(new KeySetting("Key", GLFW.GLFW_KEY_UNKNOWN));
    }

    private ElytraSwapModule(KeySetting key) {
        super("elytraswap", "Elytra Swap", "swap chestplate/elytra",
            Category.PLAYER,
            List.of(),
            List.<Setting>of(key));
        this.key = key;
    }

    @Override
    protected void onDisable() {
        resetSwap();
        closeTicks = 0;
        openedInventory = false;
        keyWasDown = false;
    }

    /** Polls the bind and drives the swap; called from the client tick. */
    public void tick(MinecraftClient client) {
        if (!enabled() || client.player == null) {
            resetSwap();
            closeTicks = 0;
            openedInventory = false;
            keyWasDown = false;
            return;
        }
        int keyCode = key.value();
        boolean down = keyCode != GLFW.GLFW_KEY_UNKNOWN
            && GLFW.glfwGetKey(client.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
        boolean pressed = down && !keyWasDown;
        keyWasDown = down;
        if (pressed) {
            beginSwap(client);
        }
        tickSwap(client);
        tickClose(client);
    }

    private void beginSwap(MinecraftClient client) {
        if (client.currentScreen != null || phase != SwapPhase.NONE
                || client.interactionManager == null || client.player == null) {
            return;
        }
        ItemStack chest = client.player.getEquippedStack(EquipmentSlot.CHEST);
        int sourceIndex = chest.isOf(Items.ELYTRA)
            ? bestChestplateIndex(client)
            : findItem(client, Items.ELYTRA);
        if (sourceIndex < 0) {
            return;
        }
        sourceSlot = InventoryMath.indexToSlot(sourceIndex);
        stepDelay = 0;
        if (client.player.isCreative()) {
            openedInventory = false;
            phase = SwapPhase.PICK_SOURCE;
            return;
        }
        client.setScreen(new InventoryScreen(client.player));
        openedInventory = true;
        phase = SwapPhase.WAIT_SCREEN;
        waitScreenTicks = WAIT_SCREEN_TIMEOUT_TICKS;
    }

    private void tickSwap(MinecraftClient client) {
        if (phase == SwapPhase.NONE) {
            return;
        }
        if (stepDelay > 0) {
            stepDelay--;
            return;
        }
        if (phase == SwapPhase.WAIT_SCREEN) {
            if (client.currentScreen instanceof HandledScreen<?>) {
                phase = SwapPhase.PICK_SOURCE;
                stepDelay = SWAP_STEP_DELAY_TICKS;
                return;
            }
            if (--waitScreenTicks <= 0) {
                resetSwap();
            }
            return;
        }
        if (client.player == null || client.interactionManager == null) {
            resetSwap();
            return;
        }
        // Manual close mid-swap: stop before clicks reach a container the
        // server no longer expects us to use.
        if (!openedInventory && !client.player.isCreative() && client.currentScreen == null) {
            resetSwap();
            return;
        }

        int syncId = client.player.playerScreenHandler.syncId;
        switch (phase) {
            case PICK_SOURCE -> {
                clickSlot(client, syncId, sourceSlot);
                phase = SwapPhase.PICK_CHEST;
                stepDelay = SWAP_STEP_DELAY_TICKS;
            }
            case PICK_CHEST -> {
                clickSlot(client, syncId, CHEST_SLOT_ID);
                phase = SwapPhase.RETURN_SOURCE;
                stepDelay = SWAP_STEP_DELAY_TICKS;
            }
            case RETURN_SOURCE -> {
                clickSlot(client, syncId, sourceSlot);
                if (hasCursorStack(client)) {
                    phase = SwapPhase.STASH_CURSOR;
                    stepDelay = SWAP_STEP_DELAY_TICKS;
                } else {
                    finishSwap();
                }
            }
            case STASH_CURSOR -> {
                stashCursorStack(client, syncId);
                finishSwap();
            }
            default -> resetSwap();
        }
    }

    private void tickClose(MinecraftClient client) {
        if (closeTicks <= 0) {
            return;
        }
        if (!(client.currentScreen instanceof InventoryScreen)) {
            closeTicks = 0;
            openedInventory = false;
            return;
        }
        stashCursorStack(client, client.player != null
            ? client.player.playerScreenHandler.syncId : 0);
        if (hasCursorStack(client)) {
            return;
        }
        closeTicks--;
        if (closeTicks <= 0) {
            client.currentScreen.close();
            openedInventory = false;
        }
    }

    private void stashCursorStack(MinecraftClient client, int syncId) {
        if (!hasCursorStack(client) || client.player == null) {
            return;
        }
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (client.player.getInventory().getStack(i).isEmpty()) {
                clickSlot(client, syncId, InventoryMath.indexToSlot(i));
                return;
            }
        }
    }

    private void finishSwap() {
        phase = SwapPhase.NONE;
        sourceSlot = -1;
        stepDelay = 0;
        waitScreenTicks = 0;
        if (openedInventory) {
            closeTicks = INVENTORY_CLOSE_DELAY_TICKS;
        }
    }

    private void resetSwap() {
        phase = SwapPhase.NONE;
        sourceSlot = -1;
        stepDelay = 0;
        waitScreenTicks = 0;
    }

    private boolean hasCursorStack(MinecraftClient client) {
        return client.player != null
            && client.player.playerScreenHandler != null
            && !client.player.playerScreenHandler.getCursorStack().isEmpty();
    }

    private void clickSlot(MinecraftClient client, int syncId, int slotId) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }
        client.interactionManager.clickSlot(syncId, slotId, 0, SlotActionType.PICKUP, client.player);
    }

    private static int findItem(MinecraftClient client, Item item) {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (client.player.getInventory().getStack(i).isOf(item)) {
                return i;
            }
        }
        return -1;
    }

    private static int bestChestplateIndex(MinecraftClient client) {
        int[] ranks = new int[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ranks[i] = chestplateRank(client.player.getInventory().getStack(i));
        }
        return InventoryMath.pickBest(ranks, 0, INVENTORY_SIZE - 1);
    }

    private static int chestplateRank(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        Item item = stack.getItem();
        if (item == Items.NETHERITE_CHESTPLATE) {
            return 5;
        }
        if (item == Items.DIAMOND_CHESTPLATE) {
            return 4;
        }
        if (item == Items.IRON_CHESTPLATE) {
            return 3;
        }
        if (item == Items.GOLDEN_CHESTPLATE) {
            return 2;
        }
        if (item == Items.CHAINMAIL_CHESTPLATE) {
            return 1;
        }
        if (item == Items.LEATHER_CHESTPLATE) {
            return 0;
        }
        return -1;
    }

    private enum SwapPhase {
        NONE,
        WAIT_SCREEN,
        PICK_SOURCE,
        PICK_CHEST,
        RETURN_SOURCE,
        STASH_CURSOR
    }
}
