package dev.linkenvisuals.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.linkenvisuals.anim.Animations;
import dev.linkenvisuals.module.impl.StreamerModeModule;
import dev.linkenvisuals.util.input.LayoutKeys;
import dev.linkenvisuals.util.text.StreamerTextSanitizer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.OrderedText;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {
    @Shadow
    public abstract String getText();

    @Shadow
    public abstract void setText(String text);

    @Shadow
    public abstract String getSelectedText();

    @Shadow
    public abstract void write(String text);

    @Shadow
    public abstract void setCursorToEnd(boolean select);

    @Shadow
    public abstract void setSelectionEnd(int index);

    @Shadow
    public abstract boolean isActive();

    @Unique
    private final Deque<String> linkenvisuals$undo = new ArrayDeque<>();
    @Unique
    private final Deque<String> linkenvisuals$redo = new ArrayDeque<>();
    @Unique
    private String linkenvisuals$lastSnapshot;
    @Unique
    private boolean linkenvisuals$restoring;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void linkenvisuals$layoutShortcuts(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (input == null || !isActive()) {
            return;
        }
        boolean ctrl = input.hasCtrlOrCmd();
        boolean shift = input.hasShift();
        boolean alt = input.hasAlt();

        // Insert/Delete shortcuts are layout-independent.
        if (input.key() == GLFW.GLFW_KEY_INSERT) {
            if (ctrl && !shift) {
                linkenvisuals$copy();
                cir.setReturnValue(true);
            } else if (shift) {
                linkenvisuals$paste();
                cir.setReturnValue(true);
            }
            return;
        }
        if (input.key() == GLFW.GLFW_KEY_DELETE && shift && !ctrl && !alt) {
            linkenvisuals$cut();
            cir.setReturnValue(true);
            return;
        }
        if (!ctrl || alt) {
            return;
        }

        // Accept both the physical US key and the character printed on the
        // active layout (AZERTY/QWERTZ/Cyrillic etc.).
        if (LayoutKeys.matches(input, 'a') && !shift) {
            setCursorToEnd(false);
            setSelectionEnd(0);
            cir.setReturnValue(true);
            return;
        }
        if (LayoutKeys.matches(input, 'c') && !shift) {
            linkenvisuals$copy();
            cir.setReturnValue(true);
            return;
        }
        if (LayoutKeys.matches(input, 'v') && !shift) {
            linkenvisuals$paste();
            cir.setReturnValue(true);
            return;
        }
        if (LayoutKeys.matches(input, 'x') && !shift) {
            linkenvisuals$cut();
            cir.setReturnValue(true);
            return;
        }
        if (LayoutKeys.matches(input, 'z')) {
            if (shift) {
                linkenvisuals$redo();
            } else {
                linkenvisuals$undo();
            }
            cir.setReturnValue(true);
            return;
        }
        if (LayoutKeys.matches(input, 'y') && !shift) {
            linkenvisuals$redo();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "write", at = @At("HEAD"))
    private void linkenvisuals$beforeWrite(String text, CallbackInfo ci) {
        linkenvisuals$snapshot();
    }

    @Inject(method = "erase", at = @At("HEAD"))
    private void linkenvisuals$beforeErase(int offset, boolean wholeWord, CallbackInfo ci) {
        linkenvisuals$snapshot();
    }

    @Unique
    private void linkenvisuals$snapshot() {
        if (linkenvisuals$restoring) {
            return;
        }
        String text = getText();
        if (text.equals(linkenvisuals$lastSnapshot)) {
            return;
        }
        linkenvisuals$undo.push(text);
        while (linkenvisuals$undo.size() > 64) {
            linkenvisuals$undo.removeLast();
        }
        linkenvisuals$redo.clear();
        linkenvisuals$lastSnapshot = text;
    }

    @Unique
    private void linkenvisuals$undo() {
        if (linkenvisuals$undo.isEmpty()) {
            return;
        }
        String previous = linkenvisuals$undo.pop();
        linkenvisuals$redo.push(getText());
        while (linkenvisuals$redo.size() > 64) {
            linkenvisuals$redo.removeLast();
        }
        linkenvisuals$restoring = true;
        setText(previous);
        setCursorToEnd(false);
        linkenvisuals$restoring = false;
        linkenvisuals$lastSnapshot = null;
    }

    @Unique
    private void linkenvisuals$redo() {
        if (linkenvisuals$redo.isEmpty()) {
            return;
        }
        String next = linkenvisuals$redo.pop();
        linkenvisuals$undo.push(getText());
        while (linkenvisuals$undo.size() > 64) {
            linkenvisuals$undo.removeLast();
        }
        linkenvisuals$restoring = true;
        setText(next);
        setCursorToEnd(false);
        linkenvisuals$restoring = false;
        linkenvisuals$lastSnapshot = null;
    }

    @Unique
    private void linkenvisuals$copy() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.keyboard != null) {
            client.keyboard.setClipboard(getSelectedText());
        }
    }

    @Unique
    private void linkenvisuals$cut() {
        linkenvisuals$copy();
        write("");
    }

    @Unique
    private void linkenvisuals$paste() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.keyboard != null) {
            write(client.keyboard.getClipboard());
        }
    }

    // Anchored to the main text draw of the field: within renderWidget it is the single
    // drawText(OrderedText) call between format(String, int) and the following
    // TextRenderer.getWidth(OrderedText) (no drawText/format calls exist earlier in the
    // method; verified against yarn build.4 bytecode: format@234, drawText@258, getWidth@269).
    @WrapOperation(method = "renderWidget",
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;format(Ljava/lang/String;I)Lnet/minecraft/text/OrderedText;"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/OrderedText;)I")),
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawText"
                + "(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)V"))
    private void linkenvisuals$fadeInLetter(DrawContext context, TextRenderer renderer, OrderedText text,
                                             int x, int y, int color, boolean shadow,
                                             Operation<Void> original) {
        // Стример-режим: пароль в /login-команде маскируется прямо в поле ввода (та же длина).
        OrderedText masked = StreamerTextSanitizer.maskFieldText(getText(), text, StreamerModeModule.rules());
        if (masked != null) {
            original.call(context, renderer, masked, x, y, color, shadow);
            return;
        }

        String letter = Animations.typedLetter();
        float age = Animations.typeAge();
        MinecraftClient client = MinecraftClient.getInstance();
        if (letter == null || age < 0.0f || !(client.currentScreen instanceof ChatScreen)) {
            original.call(context, renderer, text, x, y, color, shadow);
            return;
        }

        int glyph = renderer.getWidth(letter);
        int edge = x + renderer.getWidth(text) - glyph;
        if (glyph <= 0 || edge <= x) {
            original.call(context, renderer, text, x, y, color, shadow);
            return;
        }

        context.enableScissor(x - 2, y - 4, edge, y + renderer.fontHeight + 4);
        original.call(context, renderer, text, x, y, color, shadow);
        context.disableScissor();

        float settle = Animations.ease(age);
        int alpha = Math.round(255 * settle);
        if (alpha <= 2) {
            return;
        }

        Matrix3x2fStack matrices = context.getMatrices();

        Animations.pushScale(matrices, edge + glyph * 0.5f, y + renderer.fontHeight * 0.5f,
            1.0f + (1.0f - settle) * 0.55f);
        matrices.translate(0.0f, -(1.0f - settle) * 2.5f);
        context.drawText(renderer, letter, edge, y, alpha << 24 | color & 0xFFFFFF, shadow);
        matrices.popMatrix();
    }
}
