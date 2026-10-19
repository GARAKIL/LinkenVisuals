package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import dev.linkenvisuals.clickgui.setting.Setting;
import dev.linkenvisuals.clickgui.setting.SliderSetting;
import dev.linkenvisuals.module.Module;
import dev.linkenvisuals.render.state.ItemPhysicsRenderState;
import dev.linkenvisuals.util.ItemPhysicsMath;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * ItemPhysics (Phase 5.7): dropped items tumble while airborne and lie flat on
 * the ground. Ported from the donor's {@code ItemPhysics} (108 lines) and
 * {@code ItemEntityRendererMixin}.
 *
 * <p>Differences from the donor: the tumble angle is a pure function of the
 * render-state age (frame-rate independent, no mutation of the client
 * {@code ItemEntity} pitch), and the item stack layout is delegated to
 * vanilla's static {@code ItemEntityRenderer.render(...)} helper instead of
 * being reimplemented against the removed {@code getTransformation()} API.
 */
public final class ItemPhysicsModule extends Module {
    public final SliderSetting rotationSpeed;
    public final SliderSetting scale;

    public ItemPhysicsModule() {
        this(new SliderSetting("Rotation Speed", 0.0f, 8.0f, 0.1f, 1.0f, ""),
             new SliderSetting("Scale", 0.5f, 1.5f, 0.05f, 1.0f, ""));
    }

    private ItemPhysicsModule(SliderSetting rotationSpeed, SliderSetting scale) {
        super("itemphysics", "Item Physics", "item entity physics",
            Category.VISUAL,
            List.of(),
            List.<Setting>of(rotationSpeed, scale));
        this.rotationSpeed = rotationSpeed;
        this.scale = scale;
    }

    /** Copies the entity facts the renderer cannot read from the render state. */
    public static void captureEntityState(ItemEntity entity, ItemEntityRenderState state) {
        ItemPhysicsRenderState physics = (ItemPhysicsRenderState) state;
        physics.linkenvisuals$setOnGround(entity.isOnGround());
        physics.linkenvisuals$setYRotation((float) Math.toRadians(entity.getYaw()));
        physics.linkenvisuals$setAdditionalOffset(needsAdditionalOffset(entity));
    }

    /** Tumble angle for the current render state. */
    public float xRotation(ItemEntityRenderState state, ItemPhysicsRenderState physics) {
        boolean onGround = physics.linkenvisuals$isOnGround();
        boolean hasDepth = state.itemRenderState.getModelBoundingBox().getLengthZ() > 0.0625;
        if (onGround && !hasDepth) {
            // Donor: flat items lie level once they land.
            return 0.0f;
        }
        return ItemPhysicsMath.spin(Math.max(0.0f, state.age) / 20.0f, rotationSpeed.value());
    }

    public float scaleValue() {
        return scale.value();
    }

    /** True when the item sits on snow/soul sand/mud and needs to be raised. */
    public static boolean needsAdditionalOffset(ItemEntity entity) {
        BlockPos pos = entity.getBlockPos();
        if (isOffsetBlock(entity.getEntityWorld().getBlockState(pos))) {
            return true;
        }
        return isOffsetBlock(entity.getEntityWorld().getBlockState(pos.down()));
    }

    private static boolean isOffsetBlock(BlockState state) {
        return state.isOf(Blocks.SNOW) || state.isOf(Blocks.SOUL_SAND) || state.isOf(Blocks.MUD);
    }
}
