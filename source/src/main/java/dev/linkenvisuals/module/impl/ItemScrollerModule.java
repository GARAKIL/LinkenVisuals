package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.util.DragPath;
import dev.linkenvisuals.util.ItemScrollerMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * ItemScroller (Phase 5.7): modifier drags move items between slots and the
 * mouse wheel transfers items, ported from the donor's {@code ItemScroller}
 * (326 lines).
 *
 * <p>Two safety guards come from the donor's own history: the effective delay
 * never drops below {@link ItemScrollerMath#MIN_DELAY_MS} and one drag call
 * emits at most {@link ItemScrollerMath#MAX_MOVES_PER_CALL} moves (donor
 * comment: faster bursts got players kicked by anti-cheats).
 *
 * <p>Hooks live in {@code HandledScreenMixin}: {@code mouseClicked} starts the
 * drag, {@code mouseDragged} feeds {@link #dragOver}, {@code mouseReleased}
 * stops it and {@code mouseScrolled} maps to {@link #handleScroll}.
 */
public final class ItemScrollerModule extends Module {
    /** Long drags are sampled once per pixel of travel, capped here. */
    private static final int MAX_PATH_SAMPLES = 48;

    public final SliderSetting delay;

    private final Set<Integer> draggedSlots = new HashSet<>();
    private final ItemScrollerMath.MoveBudget moveBudget = new ItemScrollerMath.MoveBudget();
    private boolean dragging;
    private ItemScrollerMath.DragMode dragMode = ItemScrollerMath.DragMode.NONE;
    private ItemStack dragReferenceStack = ItemStack.EMPTY;
    private double lastMouseX;
    private double lastMouseY;

    public ItemScrollerModule() {
        this(new SliderSetting("Delay", 0.0f, 250.0f, 5.0f, 0.0f, "ms"));
    }

    private ItemScrollerModule(SliderSetting delay) {
        super("itemscroller", "Item Scroller", "drag to move items, scroll to transfer",
            Category.PLAYER, List.of(), List.<Setting>of(delay));
        this.delay = delay;
    }

    @Override
    protected void onDisable() {
        stopDragging();
    }

    public boolean isDragging() {
        return dragging;
    }

    /** Resolves the slot under a screen point; supplied by the mixin. */
    @FunctionalInterface
    public interface SlotResolver {
        Slot slotAt(double mouseX, double mouseY);
    }

    /** Starts a modifier drag; {@code false} leaves the vanilla click untouched. */
    public boolean beginDrag(ScreenHandler handler, Slot slot, double mouseX, double mouseY,
                             int button, boolean shiftDown, boolean ctrlDown, boolean altDown) {
        if (!active()) {
            return false;
        }
        ItemScrollerMath.DragMode mode =
            ItemScrollerMath.resolveDragMode(button, shiftDown, ctrlDown, altDown);
        if (mode == ItemScrollerMath.DragMode.NONE || !canUse(handler, slot, mode)) {
            return false;
        }
        dragging = true;
        dragMode = mode;
        draggedSlots.clear();
        dragReferenceStack = slot.getStack().copy();
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        moveBudget.reset();
        tryDragSlot(handler, slot);
        moveBudget.schedule(System.currentTimeMillis(), effectiveDelay());
        return true;
    }

    public void stopDragging() {
        dragging = false;
        dragMode = ItemScrollerMath.DragMode.NONE;
        draggedSlots.clear();
        dragReferenceStack = ItemStack.EMPTY;
        moveBudget.reset();
    }

    /**
     * Continues an active drag along the sampled mouse path. Returns whether a
     * move happened; the mixin suppresses vanilla dragging while {@link
     * #isDragging()} regardless.
     */
    public boolean dragOver(ScreenHandler handler, double mouseX, double mouseY,
                            Slot slotAtMouse, SlotResolver resolver) {
        if (!dragging || handler == null) {
            return false;
        }
        long delayMs = effectiveDelay();
        long now = System.currentTimeMillis();
        int budget = ItemScrollerMath.clampBudget(moveBudget.rawBudget(now, delayMs));
        if (budget <= 0) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return false;
        }

        Set<Slot> sampled = new LinkedHashSet<>();
        for (double[] point :
                DragPath.sample(lastMouseX, lastMouseY, mouseX, mouseY, MAX_PATH_SAMPLES)) {
            Slot slot = resolver.slotAt(point[0], point[1]);
            if (slot != null) {
                sampled.add(slot);
            }
        }
        if (slotAtMouse != null) {
            sampled.add(slotAtMouse);
        }

        boolean moved = false;
        for (Slot slot : sampled) {
            if (budget <= 0) {
                break;
            }
            if (tryDragSlot(handler, slot)) {
                moved = true;
                budget--;
                moveBudget.schedule(now, delayMs);
            }
        }

        lastMouseX = mouseX;
        lastMouseY = mouseY;
        return moved;
    }

    /**
     * Wheel transfer. Plain wheel moves one item across inventories, shift
     * moves whole stacks of the same type, alt/ctrl sweeps every matching
     * stack and alt+shift sweeps everything.
     */
    public boolean handleScroll(ScreenHandler handler, Slot slot, double verticalAmount,
                                boolean shiftDown, boolean ctrlDown, boolean altDown) {
        if (!active() || handler == null || slot == null || verticalAmount == 0.0) {
            return false;
        }
        if (!handler.getCursorStack().isEmpty()) {
            return false;
        }
        boolean up = verticalAmount > 0.0;
        boolean matching = altDown || ctrlDown;
        boolean everything = matching && shiftDown;

        if (everything) {
            return moveStacksByScroll(handler, slot, up, false, false);
        }
        if (matching) {
            return moveStacksByScroll(handler, slot, up, true, false);
        }
        if (shiftDown) {
            return moveStacksByScroll(handler, slot, up, true, true);
        }
        if (up) {
            return tryMoveSingleItemToOtherInventory(handler, slot);
        }
        if (!slot.hasStack()) {
            return false;
        }
        if (slot.getStack().getCount() >= slot.getStack().getMaxCount()) {
            return false;
        }
        return tryMoveSingleItemToThisInventory(handler, slot);
    }

    /** Called by the mixin when the scroll event must stop reaching vanilla. */

    private boolean moveStacksByScroll(ScreenHandler handler, Slot hoveredSlot,
                                       boolean moveToOtherInventory, boolean matchingOnly,
                                       boolean singleStackOnly) {
        ItemStack referenceStack = hoveredSlot.getStack();
        if (matchingOnly && referenceStack.isEmpty()) {
            return false;
        }

        boolean moved = false;
        Slot hoveredDeferred = null;

        for (int i = handler.slots.size() - 1; i >= 0; i--) {
            Slot sourceSlot = handler.slots.get(i);
            if (!isSourceSlotForScrollMove(sourceSlot, hoveredSlot, referenceStack,
                    moveToOtherInventory, matchingOnly)) {
                continue;
            }
            if (moveToOtherInventory && sourceSlot.id == hoveredSlot.id && matchingOnly) {
                hoveredDeferred = sourceSlot;
                continue;
            }
            if (quickMoveStack(handler, sourceSlot)) {
                moved = true;
                if (singleStackOnly) {
                    return true;
                }
            }
        }

        if (hoveredDeferred != null && quickMoveStack(handler, hoveredDeferred)) {
            moved = true;
        }
        return moved;
    }

    private boolean isSourceSlotForScrollMove(Slot sourceSlot, Slot hoveredSlot,
                                              ItemStack referenceStack,
                                              boolean moveToOtherInventory,
                                              boolean matchingOnly) {
        if (!sourceSlot.hasStack()) {
            return false;
        }
        if (!sourceSlot.canTakeItems(client().player)) {
            return false;
        }
        boolean sameInventoryAsHovered = areSlotsInSameInventory(sourceSlot, hoveredSlot);
        if (moveToOtherInventory == sameInventoryAsHovered) {
            return false;
        }
        if (!matchingOnly) {
            return true;
        }
        return ItemStack.areItemsAndComponentsEqual(sourceSlot.getStack(), referenceStack);
    }

    private boolean tryMoveSingleItemToOtherInventory(ScreenHandler handler, Slot slot) {
        if (!slot.hasStack() || !slot.canTakeItems(client().player)) {
            return false;
        }
        if (!handler.getCursorStack().isEmpty()) {
            return false;
        }
        ItemStack stack = slot.getStack();
        if (stack.getCount() > 1 && !slot.canInsert(stack)) {
            return false;
        }
        return clickSlotsToMoveSingleItemByShiftClick(handler, slot.id);
    }

    private boolean tryMoveSingleItemToThisInventory(ScreenHandler handler, Slot targetSlot) {
        if (!targetSlot.hasStack()) {
            return false;
        }
        if (!targetSlot.canInsert(targetSlot.getStack())) {
            return false;
        }
        ItemStack targetStack = targetSlot.getStack();

        for (int i = handler.slots.size() - 1; i >= 0; i--) {
            Slot fromSlot = handler.slots.get(i);
            if (fromSlot.id == targetSlot.id) {
                continue;
            }
            if (areSlotsInSameInventory(fromSlot, targetSlot)) {
                continue;
            }
            if (!fromSlot.hasStack() || !fromSlot.canTakeItems(client().player)) {
                continue;
            }
            ItemStack sourceStack = fromSlot.getStack();
            if (sourceStack.getCount() > 1 && !fromSlot.canInsert(sourceStack)) {
                continue;
            }
            if (!ItemStack.areItemsAndComponentsEqual(sourceStack, targetStack)) {
                continue;
            }
            return clickSlotsToMoveSingleItem(handler, fromSlot.id, targetSlot.id);
        }
        return false;
    }

    /**
     * Shift-click exactly one item out of a stack: pick the whole stack up,
     * split one back with a right click, shift-click the single item away and
     * return the cursor remainder.
     */
    private boolean clickSlotsToMoveSingleItemByShiftClick(ScreenHandler handler, int slotFromId) {
        if (slotFromId < 0 || slotFromId >= handler.slots.size()) {
            return false;
        }
        Slot slot = handler.slots.get(slotFromId);
        if (!slot.hasStack()) {
            return false;
        }
        ItemStack stack = slot.getStack();
        if (stack.getCount() > 1) {
            performClick(handler, slotFromId, 0, SlotActionType.PICKUP);
            if (slot.hasStack()) {
                performClick(handler, slotFromId, 0, SlotActionType.PICKUP);
                return false;
            }
            performClick(handler, slotFromId, 1, SlotActionType.PICKUP);
        }
        performClick(handler, slotFromId, 0, SlotActionType.QUICK_MOVE);
        if (!handler.getCursorStack().isEmpty()) {
            performClick(handler, slotFromId, 0, SlotActionType.PICKUP);
        }
        return true;
    }

    /** Picks the source stack up (half with a right click when stacked) and drops one item. */
    private boolean clickSlotsToMoveSingleItem(ScreenHandler handler, int slotFromId, int slotToId) {
        if (slotFromId < 0 || slotFromId >= handler.slots.size()) {
            return false;
        }
        if (slotToId < 0 || slotToId >= handler.slots.size()) {
            return false;
        }
        Slot fromSlot = handler.slots.get(slotFromId);
        if (!fromSlot.hasStack()) {
            return false;
        }
        if (fromSlot.getStack().getCount() > 1) {
            performClick(handler, slotFromId, 1, SlotActionType.PICKUP);
        } else {
            performClick(handler, slotFromId, 0, SlotActionType.PICKUP);
        }
        performClick(handler, slotToId, 1, SlotActionType.PICKUP);
        if (!handler.getCursorStack().isEmpty()) {
            performClick(handler, slotFromId, 0, SlotActionType.PICKUP);
        }
        return true;
    }

    private boolean quickMoveStack(ScreenHandler handler, Slot slot) {
        if (slot == null || !slot.hasStack() || !slot.canTakeItems(client().player)) {
            return false;
        }
        ItemStack before = slot.getStack().copy();
        performClick(handler, slot.id, 0, SlotActionType.QUICK_MOVE);
        ItemStack after = slot.getStack();
        if (!after.isEmpty() && ItemStack.areItemsAndComponentsEqual(before, after)
                && after.getCount() == before.getCount()) {
            return false;
        }
        return true;
    }

    private boolean canUse(ScreenHandler handler, Slot slot, ItemScrollerMath.DragMode mode) {
        if (handler == null || slot == null || !slot.hasStack()) {
            return false;
        }
        if (!handler.getCursorStack().isEmpty()) {
            return false;
        }
        if (!slot.canTakeItems(client().player)) {
            return false;
        }
        if (mode == ItemScrollerMath.DragMode.MOVE_ONE && slot.getStack().getCount() > 1
                && !slot.canInsert(slot.getStack())) {
            return false;
        }
        return true;
    }

    private boolean tryDragSlot(ScreenHandler handler, Slot slot) {
        if (dragMode == ItemScrollerMath.DragMode.NONE || !canUse(handler, slot, dragMode)) {
            return false;
        }
        if (draggedSlots.contains(slot.id)) {
            return false;
        }
        boolean moved = switch (dragMode) {
            case MOVE_STACKS -> quickMoveStack(handler, slot);
            case MOVE_ONE -> tryMoveSingleItemToOtherInventory(handler, slot);
            case MOVE_MATCHING -> !dragReferenceStack.isEmpty()
                && ItemStack.areItemsAndComponentsEqual(slot.getStack(), dragReferenceStack)
                && quickMoveStack(handler, slot);
            case NONE -> false;
        };
        if (moved) {
            draggedSlots.add(slot.id);
        }
        return moved;
    }

    private boolean areSlotsInSameInventory(Slot first, Slot second) {
        return first.inventory == second.inventory;
    }

    private void performClick(ScreenHandler handler, int slotId, int button,
                              SlotActionType actionType) {
        if (handler == null || client().player == null || client().interactionManager == null) {
            return;
        }
        client().interactionManager.clickSlot(handler.syncId, slotId, button, actionType, client().player);
    }

    private boolean active() {
        return enabled() && MinecraftClient.getInstance() != null && client().player != null
            && client().interactionManager != null;
    }

    private long effectiveDelay() {
        return ItemScrollerMath.effectiveDelayMs((long) delay.value());
    }

    private static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }
}
