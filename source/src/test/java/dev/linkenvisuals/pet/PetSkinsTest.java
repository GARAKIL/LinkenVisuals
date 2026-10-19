package dev.linkenvisuals.pet;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Label mapping tests for the pet skin list used by the Cosmetics "Pet" mode setting. */
class PetSkinsTest {

    @Test
    void labelsRoundTrip() {
        List<String> labels = PetSkins.labels();
        assertEquals(PetSkins.values().length, labels.size());
        for (PetSkins skin : PetSkins.values()) {
            assertEquals(skin, PetSkins.fromLabel(skin.label()));
        }
    }

    @Test
    void unknownLabelFallsBackToNone() {
        assertEquals(PetSkins.NONE, PetSkins.fromLabel("Dragon"));
    }
}
