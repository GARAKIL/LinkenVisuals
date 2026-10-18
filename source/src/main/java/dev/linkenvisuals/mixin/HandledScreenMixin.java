package dev.linkenvisuals.mixin;

import dev.linkenvisuals.anim.Animations;
import dev.linkenvisuals.render.GlowPipeline;
import dev.linkenvisuals.render.NeonEdge;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow
    protected Slot focusedSlot;

    @Unique
    private boolean linkenvisuals$letThrough;

    @Inject(method = "render", at = @At("HEAD"))
    private void linkenvisuals$pushContainer(DrawContext context, int mouseX, int mouseY, float delta,
                                              CallbackInfo ci) {
        Animations.tick();
        float open = Animations.ease(Animations.container());

        float spring = open + (float) Math.sin(open * Math.PI) * 0.045f;
        Animations.pushGrow(context.getMatrices(),
            context.getScaledWindowWidth() * 0.5f, context.getScaledWindowHeight() * 0.5f, spring, 0.74f);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void linkenvisuals$popContainer(DrawContext context, int mouseX, int mouseY, float delta,
                                             CallbackInfo ci) {
        context.getMatrices().popMatrix();
        if (Animations.containerClosed()) {
            linkenvisuals$letThrough = true;
            Animations.clearContainerClose();
            ((Screen) (Object) this).close();
        }
    }

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$deferClose(CallbackInfo ci) {
        if (!linkenvisuals$letThrough && Animations.beginContainerClose()) {
            ci.cancel();
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void linkenvisuals$onRemoved(CallbackInfo ci) {
        linkenvisuals$letThrough = false;
        Animations.clearContainerClose();
    }

    @Inject(method = "drawSlots", at = @At("HEAD"))
    private void linkenvisuals$markFocus(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        Animations.focusSlot(focusedSlot != null ? focusedSlot.id : -1);
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void linkenvisuals$pushSlot(DrawContext context, Slot slot, int mouseX, int mouseY,
                                         CallbackInfo ci) {
        Animations.pushScale(context.getMatrices(), slot.x + 8.0f, slot.y + 8.0f,
            Animations.slotScale(slot.id));
    }

    @Inject(method = "drawSlot", at = @At("RETURN"))
    private void linkenvisuals$popSlot(DrawContext context, Slot slot, int mouseX, int mouseY,
                                        CallbackInfo ci) {
        context.getMatrices().popMatrix();

        float burst = Animations.slotBurst(slot.id);
        if (burst >= 0.0f) {
            float fade = (1.0f - burst) * (1.0f - burst);
            GlowPipeline.radial(context, slot.x + 8.0f, slot.y + 8.0f, 8.0f + burst * 13.0f,
                Math.round(150 * fade) << 24 | NeonEdge.cool() & 0xFFFFFF);
        }
    }

    @Inject(method = "renderCursorStack", at = @At("HEAD"))
    private void linkenvisuals$pushCursor(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        Animations.pushScale(context.getMatrices(), mouseX, mouseY, Animations.cursorScale());
    }

    @Inject(method = "renderCursorStack", at = @At("RETURN"))
    private void linkenvisuals$popCursor(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
        at = @At("HEAD"))
    private void linkenvisuals$knockSlot(Slot slot, int slotId, int button, SlotActionType action,
                                          CallbackInfo ci) {
        if (slot != null) {
            Animations.slotGrabbed(slot.id);
        }
    }
}
