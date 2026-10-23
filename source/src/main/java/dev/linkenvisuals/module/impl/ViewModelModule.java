package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;

import java.util.List;

/**
 * ViewModel (D105): donor manual first-person hand offsets and per-axis scale.
 *
 * <p>The donor module has twelve numeric settings: main/off hand X/Y/Z offsets
 * and X/Y/Z scale. They are applied right before {@code HeldItemRenderer.renderItem}
 * (see {@code HeldItemRendererMixin}), i.e. after the vanilla equip/swing
 * transform and before the item display transform, exactly like the donor.
 * {@code HandPose}/{@code HandEditor} stay the separate, accepted free-form
 * editor; this module is an additional delta on top of it.
 *
 * <p>The donor also exposes an "Animated" (Punchy) mode. The Punchy animation
 * pipeline is not part of this port, so no dead toggle is exposed here.
 */
public final class ViewModelModule extends Module {
    public final SliderSetting mainX;
    public final SliderSetting mainY;
    public final SliderSetting mainZ;
    public final SliderSetting mainScaleX;
    public final SliderSetting mainScaleY;
    public final SliderSetting mainScaleZ;
    public final SliderSetting offX;
    public final SliderSetting offY;
    public final SliderSetting offZ;
    public final SliderSetting offScaleX;
    public final SliderSetting offScaleY;
    public final SliderSetting offScaleZ;

    private static boolean lastAppliedVisible;

    public ViewModelModule() {
        this(
            new SliderSetting("Main X Offset", -2.0f, 2.0f, 0.05f, 0.0f, ""),
            new SliderSetting("Main Y Offset", -2.0f, 2.0f, 0.05f, 0.0f, ""),
            new SliderSetting("Main Z Offset", -2.0f, 2.0f, 0.05f, 0.0f, ""),
            new SliderSetting("Main X Scale", 0.0f, 3.0f, 0.05f, 1.0f, "x"),
            new SliderSetting("Main Y Scale", 0.0f, 3.0f, 0.05f, 1.0f, "x"),
            new SliderSetting("Main Z Scale", 0.0f, 3.0f, 0.05f, 1.0f, "x"),
            new SliderSetting("Off X Offset", -2.0f, 2.0f, 0.05f, 0.0f, ""),
            new SliderSetting("Off Y Offset", -2.0f, 2.0f, 0.05f, 0.0f, ""),
            new SliderSetting("Off Z Offset", -2.0f, 2.0f, 0.05f, 0.0f, ""),
            new SliderSetting("Off X Scale", 0.0f, 3.0f, 0.05f, 1.0f, "x"),
            new SliderSetting("Off Y Scale", 0.0f, 3.0f, 0.05f, 1.0f, "x"),
            new SliderSetting("Off Z Scale", 0.0f, 3.0f, 0.05f, 1.0f, "x"));
    }

    private ViewModelModule(SliderSetting mainX, SliderSetting mainY, SliderSetting mainZ,
                            SliderSetting mainScaleX, SliderSetting mainScaleY, SliderSetting mainScaleZ,
                            SliderSetting offX, SliderSetting offY, SliderSetting offZ,
                            SliderSetting offScaleX, SliderSetting offScaleY, SliderSetting offScaleZ) {
        super("viewmodel", "View Model", "hand offsets and scale with items", Category.VISUAL,
            List.of(mainX, mainY, mainZ, mainScaleX, mainScaleY, mainScaleZ),
            List.of(offX, offY, offZ, offScaleX, offScaleY, offScaleZ));
        this.mainX = mainX;
        this.mainY = mainY;
        this.mainZ = mainZ;
        this.mainScaleX = mainScaleX;
        this.mainScaleY = mainScaleY;
        this.mainScaleZ = mainScaleZ;
        this.offX = offX;
        this.offY = offY;
        this.offZ = offZ;
        this.offScaleX = offScaleX;
        this.offScaleY = offScaleY;
        this.offScaleZ = offScaleZ;
    }

    /**
     * Donor sign rules: the main-hand X setting is inverted, the off-hand X is
     * not, and both are mirrored for a left-handed player. While a consumable
     * is being used the X/Z offsets are dropped so food/drink does not jump;
     * Y and scale still apply, matching the donor.
     */
    public static Transform resolve(boolean mainHand, Arm mainArm, boolean consuming,
                                    float x, float y, float z,
                                    float scaleX, float scaleY, float scaleZ) {
        float resolvedX = mainHand ? -x : x;
        float resolvedZ = z;
        if (consuming) {
            resolvedX = 0.0f;
            resolvedZ = 0.0f;
        }
        if (mainArm == Arm.LEFT) {
            resolvedX = -resolvedX;
        }
        return new Transform(resolvedX, y, resolvedZ, scaleX, scaleY, scaleZ);
    }

    public void apply(MatrixStack matrices, AbstractClientPlayerEntity player,
                      Hand hand, ItemStack stack) {
        if (!enabled()) {
            return;
        }
        boolean mainHand = hand == Hand.MAIN_HAND;
        boolean consumable = stack.contains(DataComponentTypes.FOOD)
            || stack.getItem() instanceof PotionItem;
        boolean consuming = consumable
            && player.getActiveItem().getItem() == stack.getItem();
        Transform transform = resolve(mainHand, player.getMainArm(), consuming,
            mainHand ? mainX.value() : offX.value(),
            mainHand ? mainY.value() : offY.value(),
            mainHand ? mainZ.value() : offZ.value(),
            mainHand ? mainScaleX.value() : offScaleX.value(),
            mainHand ? mainScaleY.value() : offScaleY.value(),
            mainHand ? mainScaleZ.value() : offScaleZ.value());
        if (transform.identity()) {
            return;
        }
        matrices.translate(transform.x(), transform.y(), transform.z());
        if (!transform.identityScale()) {
            matrices.scale(transform.scaleX(), transform.scaleY(), transform.scaleZ());
        }
        lastAppliedVisible = true;
    }

    /** True when the last non-identity ViewModel delta was applied this session. */
    public static boolean lastAppliedVisible() {
        return lastAppliedVisible;
    }

    public static void resetDiagnostics() {
        lastAppliedVisible = false;
    }

    @Override
    protected void onDisable() {
        lastAppliedVisible = false;
    }

    /** Resolved delta; offsets in blocks/vanilla hand units, scales in x. */
    public record Transform(float x, float y, float z,
                            float scaleX, float scaleY, float scaleZ) {
        public boolean identityScale() {
            return near(scaleX, 1.0f) && near(scaleY, 1.0f) && near(scaleZ, 1.0f);
        }

        public boolean identity() {
            return near(x, 0.0f) && near(y, 0.0f) && near(z, 0.0f) && identityScale();
        }

        private static boolean near(float value, float target) {
            return Math.abs(value - target) < 0.0001f;
        }
    }
}
