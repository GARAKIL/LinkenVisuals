package dev.linkenvisuals.module.impl;

import dev.linkenvisuals.clickgui.Category;
import net.minecraft.util.Arm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the donor ViewModel sign rules, the consumable suppression and the
 * neutral defaults. The transform is applied on top of {@code HandPose}, so a
 * wrong sign would make the accepted hand editor and the numeric controls pull
 * in opposite directions.
 */
class ViewModelModuleTest {
    @Test
    void defaultsAreNeutralAndModuleIsVisual() {
        ViewModelModule module = new ViewModelModule();
        assertEquals("viewmodel", module.id());
        assertEquals("View Model", module.info().name());
        assertEquals(Category.VISUAL, module.info().category());
        assertEquals(0.0f, module.mainX.value(), 1.0e-6f);
        assertEquals(0.0f, module.offZ.value(), 1.0e-6f);
        assertEquals(1.0f, module.mainScaleX.value(), 1.0e-6f);
        assertEquals(1.0f, module.offScaleZ.value(), 1.0e-6f);
        assertEquals(12, module.info().leftSettings().size() + module.info().rightSettings().size());

        ViewModelModule.Transform neutral = ViewModelModule.resolve(true, Arm.RIGHT, false,
            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        assertTrue(neutral.identity());
        assertTrue(neutral.identityScale());
    }

    @Test
    void mainHandInvertsXAndMirrorsForLeftHandedPlayer() {
        ViewModelModule.Transform right = ViewModelModule.resolve(true, Arm.RIGHT, false,
            0.4f, -0.1f, 0.25f, 0.8f, 0.9f, 1.1f);
        assertEquals(-0.4f, right.x(), 1.0e-6f);
        assertEquals(-0.1f, right.y(), 1.0e-6f);
        assertEquals(0.25f, right.z(), 1.0e-6f);
        assertEquals(0.8f, right.scaleX(), 1.0e-6f);
        assertFalse(right.identity());

        ViewModelModule.Transform left = ViewModelModule.resolve(true, Arm.LEFT, false,
            0.4f, -0.1f, 0.25f, 0.8f, 0.9f, 1.1f);
        assertEquals(0.4f, left.x(), 1.0e-6f);
        assertEquals(-0.1f, left.y(), 1.0e-6f);
        assertEquals(0.25f, left.z(), 1.0e-6f);
    }

    @Test
    void offHandKeepsDonorXAndMirrorsOnlyForLeftHanded() {
        ViewModelModule.Transform right = ViewModelModule.resolve(false, Arm.RIGHT, false,
            0.4f, 0.2f, -0.3f, 1.2f, 1.0f, 0.7f);
        assertEquals(0.4f, right.x(), 1.0e-6f);
        assertEquals(0.2f, right.y(), 1.0e-6f);
        assertEquals(-0.3f, right.z(), 1.0e-6f);

        ViewModelModule.Transform left = ViewModelModule.resolve(false, Arm.LEFT, false,
            0.4f, 0.2f, -0.3f, 1.2f, 1.0f, 0.7f);
        assertEquals(-0.4f, left.x(), 1.0e-6f);
    }

    @Test
    void consumingDropsOnlyXZOffsetsAndKeepsScale() {
        ViewModelModule.Transform transform = ViewModelModule.resolve(true, Arm.RIGHT, true,
            0.4f, -0.15f, 0.3f, 0.6f, 0.7f, 0.8f);
        assertEquals(0.0f, transform.x(), 1.0e-6f);
        assertEquals(-0.15f, transform.y(), 1.0e-6f);
        assertEquals(0.0f, transform.z(), 1.0e-6f);
        assertEquals(0.6f, transform.scaleX(), 1.0e-6f);
        assertFalse(transform.identityScale());
    }

    @Test
    void diagnosticsOnlyFlipForNonIdentityDelta() {
        ViewModelModule.resetDiagnostics();
        assertFalse(ViewModelModule.lastAppliedVisible());
        ViewModelModule.Transform scaled = ViewModelModule.resolve(false, Arm.RIGHT, false,
            0.0f, 0.0f, 0.0f, 1.2f, 1.0f, 1.0f);
        assertFalse(scaled.identity());
        assertFalse(scaled.identityScale());
        ViewModelModule.resetDiagnostics();
        assertFalse(ViewModelModule.lastAppliedVisible());
    }
}
