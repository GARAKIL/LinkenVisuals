package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.module.Module;

import java.util.List;

/**
 * ItemScroller (Phase 5.7): inventory drag-move.
 * Donor's {@code ItemScroller} (397 lines) — inventory packet logic is a follow-up.
 * Module stub registered.
 */
public final class ItemScrollerModule extends Module {
    public ItemScrollerModule() {
        super("itemscroller", "Item Scroller", "inventory drag-move",
            Category.PLAYER, List.of(), List.of());
    }
}
