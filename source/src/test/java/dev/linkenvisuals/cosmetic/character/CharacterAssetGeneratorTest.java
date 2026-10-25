package dev.linkenvisuals.cosmetic.character;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharacterAssetGeneratorTest {
    @Test
    void generatesAndValidatesCharacterAssets() throws IOException {
        File dir = new File("src/main/resources/assets/linkenvisuals/characters");
        CharacterAssetGenerator.generateAll(dir);

        File baconMesh = new File(dir, "roblox_bacon.mesh");
        File baconPng = new File(dir, "roblox_bacon.png");
        File jasonMesh = new File(dir, "jason_gta6.mesh");
        File jasonPng = new File(dir, "jason_gta6.png");

        assertTrue(baconMesh.exists(), "roblox_bacon.mesh should exist");
        assertTrue(baconPng.exists(), "roblox_bacon.png should exist");
        assertTrue(jasonMesh.exists(), "jason_gta6.mesh should exist");
        assertTrue(jasonPng.exists(), "jason_gta6.png should exist");

        // Validate Roblox Bacon Mesh
        try (FileInputStream in = new FileInputStream(baconMesh)) {
            RudyMesh.Part[] parts = RudyMesh.parse(in, 1);
            assertEquals(6, parts.length);
            for (int i = 0; i < parts.length; i++) {
                assertEquals(i, parts[i].part());
                assertTrue(parts[i].vertices().length >= 24);
                for (float v : parts[i].vertices()) {
                    assertTrue(Float.isFinite(v));
                }
            }
        }

        // Validate Jason GTA 6 Mesh
        try (FileInputStream in = new FileInputStream(jasonMesh)) {
            RudyMesh.Part[] parts = RudyMesh.parse(in, 1);
            assertEquals(6, parts.length);
            for (int i = 0; i < parts.length; i++) {
                assertEquals(i, parts[i].part());
                assertTrue(parts[i].vertices().length >= 24);
                for (float v : parts[i].vertices()) {
                    assertTrue(Float.isFinite(v));
                }
            }
        }

        // Validate Textures
        BufferedImage baconImg = ImageIO.read(baconPng);
        assertNotNull(baconImg);
        assertEquals(512, baconImg.getWidth());
        assertEquals(512, baconImg.getHeight());

        BufferedImage jasonImg = ImageIO.read(jasonPng);
        assertNotNull(jasonImg);
        assertEquals(512, jasonImg.getWidth());
        assertEquals(512, jasonImg.getHeight());
    }
}
