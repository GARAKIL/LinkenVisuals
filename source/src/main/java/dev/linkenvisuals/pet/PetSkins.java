package dev.linkenvisuals.pet;

import java.util.List;

/**
 * Pet skins (Phase 5.6, ported from the donor's {@code PetSkin} enum). Every skin is a real vanilla
 * mob rendered client-side around the player; the kind selects the flight/walk simulation.
 */
public enum PetSkins {
    NONE("None", Kind.NONE),
    BAT("Bat", Kind.FLYER),
    PARROT("Parrot", Kind.FLYER),
    RAVEN("Raven", Kind.FLYER),
    FAIRY("Fairy", Kind.FLYER),
    BEE("Bee", Kind.FLYER),
    VEX("Vex", Kind.FLYER),
    FOX("Fox", Kind.FOX),
    PIG("Pig", Kind.WALKER),
    FROG("Frog", Kind.WALKER),
    PUFFERFISH("Pufferfish", Kind.PUFFER),
    SLIME("Slime", Kind.WALKER);

    public enum Kind {
        NONE, FLYER, WALKER, FOX, PUFFER
    }

    private final String label;
    private final Kind kind;

    PetSkins(String label, Kind kind) {
        this.label = label;
        this.kind = kind;
    }

    public String label() {
        return label;
    }

    public Kind kind() {
        return kind;
    }

    /** Labels for the Cosmetics "Pet" mode setting (the same order as the donor's skin list). */
    public static List<String> labels() {
        return List.of(NONE.label, BAT.label, PARROT.label, RAVEN.label, FAIRY.label, BEE.label, VEX.label,
            FOX.label, PIG.label, FROG.label, PUFFERFISH.label, SLIME.label);
    }

    public static PetSkins fromLabel(String label) {
        for (PetSkins skin : values()) {
            if (skin.label.equals(label)) {
                return skin;
            }
        }
        return NONE;
    }
}
