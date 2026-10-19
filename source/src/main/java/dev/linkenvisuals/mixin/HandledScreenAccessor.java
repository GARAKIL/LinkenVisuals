package dev.linkenvisuals.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes {@code HandledScreen.getSlotAt} to the ItemScroller logic. */
@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Invoker("getSlotAt")
    Slot linkenvisuals$getSlotAt(double mouseX, double mouseY);
}
