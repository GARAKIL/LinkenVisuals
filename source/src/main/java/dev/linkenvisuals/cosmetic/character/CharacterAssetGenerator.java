package dev.linkenvisuals.cosmetic.character;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Procedural generator for custom 3D character models (Roblox Bacon and Jason GTA 6).
 * Outputs rigid triangle-soup .mesh assets (compatible with {@link RudyMesh}) and
 * high-resolution 512x512 texture maps matching the reference images.
 */
public final class CharacterAssetGenerator {
    private static final int TEX_SIZE = 512;

    private CharacterAssetGenerator() {
    }

    public static void main(String[] args) throws IOException {
        File dir = new File(args.length > 0 ? args[0] : "source/src/main/resources/assets/linkenvisuals/characters");
        dir.mkdirs();
        generateAll(dir);
    }

    public static void generateAll(File outputDir) throws IOException {
        outputDir.mkdirs();
        generateRobloxBacon(new File(outputDir, "roblox_bacon.mesh"), new File(outputDir, "roblox_bacon.png"));
        generateJasonGta6(new File(outputDir, "jason_gta6.mesh"), new File(outputDir, "jason_gta6.png"));
    }

    // =========================================================================
    // 1. ROBLOX BACON HAIR (R6 Blocky / Pal Hair / Motorcycle Tee)
    // =========================================================================

    public static void generateRobloxBacon(File meshFile, File textureFile) throws IOException {
        List<MeshPart> parts = new ArrayList<>();

        // Part 0: Head (Roblox cylindrical head + 3D bacon Pal hair)
        MeshBuilder head = new MeshBuilder(0, 0);
        // Base head: x: -0.22..0.22, y: -0.44..0.0, z: -0.22..0.22
        head.addBox(-0.22f, -0.44f, -0.22f, 0.22f, 0.0f, 0.22f,
            0.0f, 0.0f, 0.25f, 0.25f);
        // Pal Hair - Top mass
        head.addBox(-0.24f, -0.52f, -0.24f, 0.24f, -0.40f, 0.24f,
            0.25f, 0.0f, 0.50f, 0.25f);
        // Pal Hair - Distinctive center front bacon hair strand draping over forehead
        head.addBox(-0.06f, -0.48f, -0.26f, 0.06f, -0.22f, -0.21f,
            0.25f, 0.25f, 0.35f, 0.40f);
        // Pal Hair - Left front curved bacon strand
        head.addBox(-0.18f, -0.46f, -0.25f, -0.07f, -0.26f, -0.21f,
            0.35f, 0.25f, 0.45f, 0.40f);
        // Pal Hair - Right front curved bacon strand
        head.addBox(0.07f, -0.46f, -0.25f, 0.18f, -0.26f, -0.21f,
            0.35f, 0.25f, 0.45f, 0.40f);
        // Pal Hair - Left and Right side hair volumes
        head.addBox(-0.26f, -0.44f, -0.20f, -0.21f, -0.18f, 0.20f,
            0.25f, 0.0f, 0.50f, 0.25f);
        head.addBox(0.21f, -0.44f, -0.20f, 0.26f, -0.18f, 0.20f,
            0.25f, 0.0f, 0.50f, 0.25f);
        // Pal Hair - Back hair layer
        head.addBox(-0.24f, -0.44f, 0.21f, 0.24f, -0.16f, 0.26f,
            0.25f, 0.0f, 0.50f, 0.25f);
        parts.add(head.build());

        // Part 1: Torso (R6 Roblox Torso: open black jacket + blue motorcycle t-shirt)
        MeshBuilder body = new MeshBuilder(1, 0);
        // Main torso: x: -0.25..0.25, y: 0.0..0.72, z: -0.13..0.13
        body.addBox(-0.25f, 0.0f, -0.13f, 0.25f, 0.72f, 0.13f,
            0.0f, 0.25f, 0.25f, 0.50f);
        // 3D Jacket collar / lapels on front
        body.addBox(-0.26f, 0.02f, -0.145f, -0.11f, 0.70f, -0.125f,
            0.25f, 0.40f, 0.35f, 0.60f);
        body.addBox(0.11f, 0.02f, -0.145f, 0.26f, 0.70f, -0.125f,
            0.25f, 0.40f, 0.35f, 0.60f);
        parts.add(body.build());

        // Part 2: Right Arm (Black sleeve + grey arm + 3D C-clamp hand)
        MeshBuilder rightArm = new MeshBuilder(2, 0);
        // Sleeve: x: -0.12..0.12, y: -0.06..0.22, z: -0.12..0.12
        rightArm.addBox(-0.12f, -0.06f, -0.12f, 0.12f, 0.22f, 0.12f,
            0.0f, 0.50f, 0.15f, 0.70f);
        // Lower arm: x: -0.10..0.10, y: 0.22..0.48, z: -0.10..0.10
        rightArm.addBox(-0.10f, 0.22f, -0.10f, 0.10f, 0.48f, 0.10f,
            0.15f, 0.50f, 0.25f, 0.70f);
        // 3D Roblox C-clamp hand (wrist base + 2 curved prongs with empty center)
        rightArm.addBox(-0.09f, 0.48f, -0.09f, 0.09f, 0.54f, 0.09f,
            0.15f, 0.50f, 0.25f, 0.70f);
        rightArm.addBox(-0.09f, 0.54f, -0.09f, -0.02f, 0.65f, 0.09f,
            0.15f, 0.50f, 0.25f, 0.70f);
        rightArm.addBox(0.02f, 0.54f, -0.09f, 0.09f, 0.65f, 0.09f,
            0.15f, 0.50f, 0.25f, 0.70f);
        parts.add(rightArm.build());

        // Part 3: Left Arm (Symmetrical)
        MeshBuilder leftArm = new MeshBuilder(3, 0);
        leftArm.addBox(-0.12f, -0.06f, -0.12f, 0.12f, 0.22f, 0.12f,
            0.0f, 0.50f, 0.15f, 0.70f);
        leftArm.addBox(-0.10f, 0.22f, -0.10f, 0.10f, 0.48f, 0.10f,
            0.15f, 0.50f, 0.25f, 0.70f);
        leftArm.addBox(-0.09f, 0.48f, -0.09f, 0.09f, 0.54f, 0.09f,
            0.15f, 0.50f, 0.25f, 0.70f);
        leftArm.addBox(-0.09f, 0.54f, -0.09f, -0.02f, 0.65f, 0.09f,
            0.15f, 0.50f, 0.25f, 0.70f);
        leftArm.addBox(0.02f, 0.54f, -0.09f, 0.09f, 0.65f, 0.09f,
            0.15f, 0.50f, 0.25f, 0.70f);
        parts.add(leftArm.build());

        // Part 4: Right Leg (Dark grey/green denim jeans + white sneaker shoe)
        MeshBuilder rightLeg = new MeshBuilder(4, 0);
        // Jeans: x: -0.12..0.12, y: 0.0..0.56, z: -0.12..0.12
        rightLeg.addBox(-0.12f, 0.0f, -0.12f, 0.12f, 0.56f, 0.12f,
            0.0f, 0.70f, 0.15f, 0.95f);
        // White rounded sneaker: x: -0.125..0.125, y: 0.56..0.73, z: -0.14..0.13
        rightLeg.addBox(-0.125f, 0.56f, -0.14f, 0.125f, 0.73f, 0.13f,
            0.15f, 0.70f, 0.30f, 0.95f);
        parts.add(rightLeg.build());

        // Part 5: Left Leg
        MeshBuilder leftLeg = new MeshBuilder(5, 0);
        leftLeg.addBox(-0.12f, 0.0f, -0.12f, 0.12f, 0.56f, 0.12f,
            0.0f, 0.70f, 0.15f, 0.95f);
        leftLeg.addBox(-0.125f, 0.56f, -0.14f, 0.125f, 0.73f, 0.13f,
            0.15f, 0.70f, 0.30f, 0.95f);
        parts.add(leftLeg.build());

        writeMeshFile(meshFile, parts);
        writeRobloxBaconTexture(textureFile);
    }

    private static void writeRobloxBaconTexture(File file) throws IOException {
        BufferedImage img = new BufferedImage(TEX_SIZE, TEX_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background: transparent
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, TEX_SIZE, TEX_SIZE);

        // --- Region 1: Head Face & Skin (0..128, 0..128) ---
        Color robloxSkin = new Color(0xDFE2E4);
        g.setColor(robloxSkin);
        g.fillRect(0, 0, 128, 128);
        // Shading edges
        g.setColor(new Color(0xC5C9CC));
        g.drawRect(1, 1, 126, 126);
        // Roblox smiley face: eyes at x=42 and x=86, y=55; smile at y=76
        g.setColor(new Color(0x111214));
        g.fillOval(38, 48, 12, 18);
        g.fillOval(78, 48, 12, 18);
        // Curved smile
        g.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(36, 62, 56, 32, 200, 140);

        // --- Region 2: Pal Bacon Hair (128..256, 0..128) & Bangs (128..256, 128..205) ---
        Color hairDark = new Color(0x4A2511);
        Color hairMid = new Color(0x783E1D);
        Color hairLight = new Color(0xA96332);
        Color hairBaconStreak = new Color(0xC27A45);

        g.setPaint(new GradientPaint(128, 0, hairMid, 256, 128, hairDark));
        g.fillRect(128, 0, 128, 205);
        // Bacon hair wavy streaks
        g.setStroke(new BasicStroke(4.0f));
        for (int y = 8; y < 200; y += 12) {
            g.setColor(y % 24 == 0 ? hairLight : hairBaconStreak);
            g.drawLine(132, y, 252, y + 6);
        }

        // --- Region 3: Torso (0..128, 128..256) - Black Jacket + Blue Motorcycle Tee ---
        // Blue t-shirt center
        Color teeBlue = new Color(0x0E82CA);
        g.setColor(teeBlue);
        g.fillRect(0, 128, 128, 128);
        // Subtle knit texture
        g.setColor(new Color(0x0C74B5));
        for (int y = 130; y < 254; y += 4) {
            g.drawLine(0, y, 128, y);
        }
        // Motorcycle Graphic Print in center of tee (x=24..104, y=148..228)
        g.setColor(new Color(0x121B22));
        // Rear and front wheels
        g.setStroke(new BasicStroke(3.5f));
        g.drawOval(28, 186, 26, 26);
        g.drawOval(74, 186, 26, 26);
        // Spokes & hubs
        g.fillOval(38, 196, 6, 6);
        g.fillOval(84, 196, 6, 6);
        // Frame, tank, engine, handlebars
        g.drawLine(41, 199, 60, 188); // swingarm
        g.drawLine(60, 188, 85, 175); // top tube/tank
        g.drawLine(85, 175, 87, 199); // fork
        g.drawLine(60, 188, 62, 204); // seat post
        g.drawLine(62, 204, 85, 199); // bottom tube
        // Engine block
        g.fillRect(56, 192, 18, 14);
        // Handlebars and headlight
        g.drawLine(85, 175, 80, 168);
        g.drawLine(76, 168, 84, 168);
        // Gas tank & seat
        g.fillRoundRect(62, 175, 20, 8, 4, 4);
        g.fillRoundRect(48, 179, 16, 6, 3, 3);

        // --- Region 4: Open Jacket Lapels & Sleeves (128..180, 205..308 & 0..77, 256..358) ---
        Color jacketBlack = new Color(0x18191B);
        Color jacketDark = new Color(0x101112);
        Color jacketZipper = new Color(0x35373B);
        g.setColor(jacketBlack);
        g.fillRect(128, 205, 128, 103);
        g.fillRect(0, 256, 77, 102);
        // Lapel seam & zipper edge
        g.setColor(jacketZipper);
        g.setStroke(new BasicStroke(2.0f));
        g.drawLine(140, 205, 140, 308);
        g.drawLine(244, 205, 244, 308);

        // --- Region 5: Lower Arm & Clamp Hand (77..128, 256..358) ---
        g.setColor(robloxSkin);
        g.fillRect(77, 256, 51, 102);
        g.setColor(new Color(0xC0C3C6));
        g.drawRect(78, 257, 49, 100);

        // --- Region 6: Dark Denim Jeans (0..77, 358..486) ---
        Color jeansBase = new Color(0x283033);
        Color jeansShadow = new Color(0x1E2427);
        Color jeansStitch = new Color(0x424E52);
        g.setColor(jeansBase);
        g.fillRect(0, 358, 77, 128);
        // Denim twill diagonal texture
        g.setColor(jeansShadow);
        g.setStroke(new BasicStroke(1.0f));
        for (int i = -77; i < 77; i += 4) {
            g.drawLine(i, 358, i + 128, 486);
        }
        // Seams and pockets
        g.setColor(jeansStitch);
        g.drawLine(8, 358, 8, 486);
        g.drawLine(68, 358, 68, 486);

        // --- Region 7: White Rounded Sneakers (77..154, 358..486) ---
        Color shoeWhite = new Color(0xEEEEF0);
        Color shoeSole = new Color(0x45484D);
        g.setColor(shoeWhite);
        g.fillRect(77, 358, 77, 128);
        // Rubber sole band at bottom
        g.setColor(shoeSole);
        g.fillRect(77, 462, 77, 24);
        // Toe cap seam
        g.setColor(new Color(0xD0D3D6));
        g.drawRoundRect(82, 365, 66, 90, 16, 16);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    // =========================================================================
    // 2. JASON FROM GTA 6 (Muscular Athletic / Silver Chain / Camo Cargo)
    // =========================================================================

    public static void generateJasonGta6(File meshFile, File textureFile) throws IOException {
        List<MeshPart> parts = new ArrayList<>();

        // Part 0: Head (Handsome rugged face, backward camo cap, tactical sunglasses)
        MeshBuilder head = new MeshBuilder(0, 0);
        // Base head: x: -0.22..0.22, y: -0.44..0.0, z: -0.22..0.22
        head.addBox(-0.22f, -0.44f, -0.22f, 0.22f, 0.0f, 0.22f,
            0.0f, 0.0f, 0.25f, 0.25f);
        // Backward Baseball Cap Crown
        head.addBox(-0.23f, -0.50f, -0.23f, 0.23f, -0.40f, 0.23f,
            0.25f, 0.0f, 0.50f, 0.25f);
        // 3D Backward Brim / Visor projecting back over neck
        head.addBox(-0.16f, -0.44f, 0.22f, 0.16f, -0.40f, 0.35f,
            0.25f, 0.0f, 0.50f, 0.25f);
        // 3D Tactical Sunglasses on face
        head.addBox(-0.20f, -0.32f, -0.245f, 0.20f, -0.23f, -0.215f,
            0.25f, 0.25f, 0.45f, 0.35f);
        parts.add(head.build());

        // Part 1: Torso (Muscular bare upper body, 3D silver chain, tactical belt)
        MeshBuilder body = new MeshBuilder(1, 0);
        // Defined torso: x: -0.25..0.25, y: 0.0..0.72, z: -0.13..0.13
        body.addBox(-0.25f, 0.0f, -0.13f, 0.25f, 0.72f, 0.13f,
            0.0f, 0.25f, 0.25f, 0.50f);
        // 3D Silver chain necklace hanging down from neck to mid-chest
        // Left chain rail
        body.addBox(-0.12f, 0.04f, -0.142f, -0.08f, 0.22f, -0.125f,
            0.25f, 0.35f, 0.35f, 0.50f);
        // Right chain rail
        body.addBox(0.08f, 0.04f, -0.142f, 0.12f, 0.22f, -0.125f,
            0.25f, 0.35f, 0.35f, 0.50f);
        // Center chain loop at bottom
        body.addBox(-0.08f, 0.20f, -0.144f, 0.08f, 0.235f, -0.125f,
            0.25f, 0.35f, 0.35f, 0.50f);
        // Tactical belt volume at waist
        body.addBox(-0.255f, 0.65f, -0.135f, 0.255f, 0.72f, 0.135f,
            0.35f, 0.35f, 0.50f, 0.50f);
        parts.add(body.build());

        // Part 2: Right Arm (Muscular arm + wrist strap)
        MeshBuilder rightArm = new MeshBuilder(2, 0);
        // Shoulder, bicep, forearm: x: -0.12..0.12, y: -0.06..0.66, z: -0.12..0.12
        rightArm.addBox(-0.12f, -0.06f, -0.12f, 0.12f, 0.66f, 0.12f,
            0.0f, 0.50f, 0.15f, 0.75f);
        // Wristband / bracelet on right wrist
        rightArm.addBox(-0.125f, 0.48f, -0.125f, 0.125f, 0.53f, 0.125f,
            0.15f, 0.50f, 0.25f, 0.60f);
        parts.add(rightArm.build());

        // Part 3: Left Arm (Muscular arm + 3D sports watch)
        MeshBuilder leftArm = new MeshBuilder(3, 0);
        leftArm.addBox(-0.12f, -0.06f, -0.12f, 0.12f, 0.66f, 0.12f,
            0.0f, 0.50f, 0.15f, 0.75f);
        // 3D Tactical Sports Watch on left wrist (strap + bezel)
        leftArm.addBox(-0.13f, 0.46f, -0.13f, 0.13f, 0.52f, 0.13f,
            0.15f, 0.60f, 0.25f, 0.75f);
        leftArm.addBox(-0.145f, 0.47f, -0.04f, -0.125f, 0.51f, 0.04f,
            0.15f, 0.60f, 0.25f, 0.75f);
        parts.add(leftArm.build());

        // Part 4: Right Leg (Camouflage cargo pants/shorts, 3D side pocket, athletic sock & sneaker)
        MeshBuilder rightLeg = new MeshBuilder(4, 0);
        // Camo cargo pants: y: 0.0..0.54
        rightLeg.addBox(-0.125f, 0.0f, -0.125f, 0.125f, 0.54f, 0.125f,
            0.0f, 0.75f, 0.15f, 1.0f);
        // 3D Cargo Side Flap Pocket on outer thigh (right side = -X)
        rightLeg.addBox(-0.16f, 0.18f, -0.06f, -0.12f, 0.36f, 0.06f,
            0.0f, 0.75f, 0.15f, 1.0f);
        // High striped athletic sock: y: 0.54..0.63
        rightLeg.addBox(-0.115f, 0.54f, -0.115f, 0.115f, 0.63f, 0.115f,
            0.15f, 0.75f, 0.25f, 0.88f);
        // Sneaker running shoe: y: 0.63..0.75
        rightLeg.addBox(-0.125f, 0.63f, -0.15f, 0.125f, 0.75f, 0.13f,
            0.15f, 0.88f, 0.30f, 1.0f);
        parts.add(rightLeg.build());

        // Part 5: Left Leg (Symmetrical with outer side pocket at +X)
        MeshBuilder leftLeg = new MeshBuilder(5, 0);
        leftLeg.addBox(-0.125f, 0.0f, -0.125f, 0.125f, 0.54f, 0.125f,
            0.0f, 0.75f, 0.15f, 1.0f);
        leftLeg.addBox(0.12f, 0.18f, -0.06f, 0.16f, 0.36f, 0.06f,
            0.0f, 0.75f, 0.15f, 1.0f);
        leftLeg.addBox(-0.115f, 0.54f, -0.115f, 0.115f, 0.63f, 0.115f,
            0.15f, 0.75f, 0.25f, 0.88f);
        leftLeg.addBox(-0.125f, 0.63f, -0.15f, 0.125f, 0.75f, 0.13f,
            0.15f, 0.88f, 0.30f, 1.0f);
        parts.add(leftLeg.build());

        writeMeshFile(meshFile, parts);
        writeJasonTexture(textureFile);
    }

    private static void writeJasonTexture(File file) throws IOException {
        BufferedImage img = new BufferedImage(TEX_SIZE, TEX_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- Region 1: Jason's Face (0..128, 0..128) ---
        Color skinBase = new Color(0xC99370);
        Color skinShadow = new Color(0xAB7756);
        Color stubbleColor = new Color(0x563E32);
        Color eyeBrown = new Color(0x3B2519);

        g.setColor(skinBase);
        g.fillRect(0, 0, 128, 128);
        // Facial bone structure shading
        g.setColor(skinShadow);
        g.fillPolygon(new int[]{20, 64, 108}, new int[]{100, 125, 100}, 3);
        // Eyes and eyebrows
        g.setColor(eyeBrown);
        g.fillRect(36, 42, 18, 5); // left brow
        g.fillRect(74, 42, 18, 5); // right brow
        g.setColor(Color.WHITE);
        g.fillRect(38, 48, 14, 8);
        g.fillRect(76, 48, 14, 8);
        g.setColor(eyeBrown);
        g.fillOval(43, 49, 7, 7);
        g.fillOval(81, 49, 7, 7);
        // Beard & 5 o'clock stubble around jaw and upper lip
        g.setColor(stubbleColor);
        for (int y = 72; y < 122; y += 3) {
            for (int x = 24; x < 104; x += 3) {
                if ((x * 17 + y * 31) % 5 < 3) {
                    g.fillRect(x, y, 2, 2);
                }
            }
        }

        // --- Region 2: Camo Cap & Tactical Sunglasses (128..256, 0..128 & 128..230, 128..179) ---
        // MultiCam pattern for cap
        Color camoBase = new Color(0x6A6D52);
        Color camoTan = new Color(0x9C9174);
        Color camoBrown = new Color(0x4E3C2C);
        Color camoDarkGreen = new Color(0x3B4431);

        g.setColor(camoBase);
        g.fillRect(128, 0, 128, 128);
        for (int i = 0; i < 24; i++) {
            g.setColor(i % 3 == 0 ? camoTan : (i % 3 == 1 ? camoBrown : camoDarkGreen));
            int cx = 130 + (i * 29) % 110;
            int cy = 6 + (i * 37) % 110;
            g.fillOval(cx, cy, 28, 18);
        }

        // Tactical Sunglasses (128..230, 128..179)
        Color frameColor = new Color(0x1B1E1C);
        Color lensEmerald = new Color(0x124738);
        Color lensHighlight = new Color(0x35A788);
        g.setColor(frameColor);
        g.fillRoundRect(128, 128, 102, 51, 8, 8);
        // Dual polarized emerald lenses
        g.setPaint(new GradientPaint(134, 134, lensHighlight, 172, 172, lensEmerald));
        g.fillRoundRect(134, 134, 40, 38, 6, 6);
        g.fillRoundRect(182, 134, 40, 38, 6, 6);

        // --- Region 3: Muscular Bare Torso (0..128, 128..256) ---
        g.setColor(skinBase);
        g.fillRect(0, 128, 128, 128);
        // Pectoral muscles definition
        g.setColor(skinShadow);
        g.setStroke(new BasicStroke(3.0f));
        g.drawRoundRect(14, 140, 46, 30, 12, 12);
        g.drawRoundRect(68, 140, 46, 30, 12, 12);
        // Six-pack abdominal muscles
        g.drawRoundRect(28, 180, 32, 20, 8, 8);
        g.drawRoundRect(68, 180, 32, 20, 8, 8);
        g.drawRoundRect(28, 204, 32, 20, 8, 8);
        g.drawRoundRect(68, 204, 32, 20, 8, 8);
        g.drawRoundRect(30, 228, 30, 18, 6, 6);
        g.drawRoundRect(68, 228, 30, 18, 6, 6);
        // Chest and abdominal hair texture
        g.setColor(new Color(0x563826));
        for (int y = 146; y < 190; y += 4) {
            for (int x = 40; x < 88; x += 4) {
                if ((x * 13 + y * 7) % 4 == 0) {
                    g.fillRect(x, y, 1, 2);
                }
            }
        }

        // --- Region 4: Silver Curb Chain Necklace (128..180, 180..256) ---
        Color silverHigh = new Color(0xF2F5F8);
        Color silverMid = new Color(0xCCD2D8);
        Color silverDark = new Color(0x767F88);
        g.setColor(new Color(0x30353A));
        g.fillRect(128, 180, 52, 76);
        for (int y = 182; y < 254; y += 8) {
            g.setColor(silverHigh);
            g.fillOval(136, y, 36, 6);
            g.setColor(silverDark);
            g.fillOval(144, y + 1, 20, 4);
        }

        // --- Region 5: Tactical Belt (180..256, 180..256) ---
        Color beltOlive = new Color(0x3A4032);
        Color buckleMatte = new Color(0x1B1C1D);
        g.setColor(beltOlive);
        g.fillRect(180, 180, 76, 76);
        // Buckle in center
        g.setColor(buckleMatte);
        g.fillRoundRect(198, 198, 40, 38, 6, 6);
        g.setColor(new Color(0x5A5C5E));
        g.drawRect(205, 205, 26, 24);

        // --- Region 6: Muscular Arm Skin & Watch (0..128, 256..384) ---
        g.setColor(skinBase);
        g.fillRect(0, 256, 77, 128);
        // Muscle contours
        g.setColor(skinShadow);
        g.setStroke(new BasicStroke(2.0f));
        g.drawArc(10, 270, 32, 60, 290, 140); // bicep contour
        g.drawArc(36, 310, 28, 50, 100, 140); // forearm contour
        // Sports watch (77..128, 256..384)
        Color watchCase = new Color(0x1F2224);
        Color watchDial = new Color(0x0C1215);
        Color watchAccent = new Color(0x4BB580);
        g.setColor(watchCase);
        g.fillRect(77, 256, 51, 128);
        g.setColor(watchDial);
        g.fillOval(84, 290, 36, 36);
        g.setColor(watchAccent);
        g.fillOval(100, 306, 4, 4); // hands center

        // --- Region 7: Camo Cargo Shorts/Pants (0..77, 384..512) ---
        g.setColor(camoBase);
        g.fillRect(0, 384, 77, 128);
        for (int i = 0; i < 28; i++) {
            g.setColor(i % 3 == 0 ? camoTan : (i % 3 == 1 ? camoBrown : camoDarkGreen));
            int cx = (i * 23) % 65;
            int cy = 388 + (i * 31) % 115;
            g.fillOval(cx, cy, 22, 16);
        }
        // Pocket flap seam
        g.setColor(new Color(0x28231C));
        g.setStroke(new BasicStroke(2.0f));
        g.drawRect(6, 412, 64, 48);

        // --- Region 8: Athletic Striped Socks & Sneakers (77..154, 384..512) ---
        Color sockWhite = new Color(0xEDEFEF);
        Color stripeRed = new Color(0xB52B27);
        Color stripeBlue = new Color(0x234E96);
        Color shoeNavy = new Color(0x1D2D44);
        Color shoeMidsole = new Color(0xF2F4F5);

        // Socks: y=384..448
        g.setColor(sockWhite);
        g.fillRect(77, 384, 77, 64);
        // Dual retro athletic stripes
        g.setColor(stripeRed);
        g.fillRect(77, 396, 77, 6);
        g.setColor(stripeBlue);
        g.fillRect(77, 406, 77, 6);

        // Sneakers: y=448..512
        g.setColor(shoeNavy);
        g.fillRect(77, 448, 77, 64);
        // White athletic midsole
        g.setColor(shoeMidsole);
        g.fillRect(77, 496, 77, 12);
        // Dark rubber outsole
        g.setColor(new Color(0x282C30));
        g.fillRect(77, 508, 77, 4);

        g.dispose();
        ImageIO.write(img, "png", file);
    }

    // =========================================================================
    // MESH DATA STRUCTURES & UTILITIES
    // =========================================================================

    public record MeshPart(int part, int material, float[] vertices) {
    }

    public static final class MeshBuilder {
        private final int part;
        private final int material;
        private final List<Float> vertices = new ArrayList<>();

        public MeshBuilder(int part, int material) {
            this.part = part;
            this.material = material;
        }

        /**
         * Adds an axis-aligned box with normals and UV coordinates.
         * Generates 12 triangles (36 vertices, each with 8 floats).
         */
        public void addBox(float x0, float y0, float z0, float x1, float y1, float z1,
                           float u0, float v0, float u1, float v1) {
            // Front (+Z towards North/South depending on model)
            quad(x0, y1, z1, x1, y1, z1, x1, y0, z1, x0, y0, z1, 0, 0, 1, u0, v1, u1, v0);
            // Back (-Z)
            quad(x1, y1, z0, x0, y1, z0, x0, y0, z0, x1, y0, z0, 0, 0, -1, u0, v1, u1, v0);
            // Top (-Y in entity space is UP)
            quad(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, 0, -1, 0, u0, v0, u1, v1);
            // Bottom (+Y is DOWN)
            quad(x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0, 0, 1, 0, u0, v0, u1, v1);
            // Right face (+X)
            quad(x1, y1, z1, x1, y1, z0, x1, y0, z0, x1, y0, z1, 1, 0, 0, u0, v1, u1, v0);
            // Left face (-X)
            quad(x0, y1, z0, x0, y1, z1, x0, y0, z1, x0, y0, z0, -1, 0, 0, u0, v1, u1, v0);
        }

        private void quad(float x0, float y0, float z0,
                          float x1, float y1, float z1,
                          float x2, float y2, float z2,
                          float x3, float y3, float z3,
                          float nx, float ny, float nz,
                          float u0, float v0, float u1, float v1) {
            // Triangle 1: 0, 1, 2
            v(x0, y0, z0, nx, ny, nz, u0, v0);
            v(x1, y1, z1, nx, ny, nz, u1, v0);
            v(x2, y2, z2, nx, ny, nz, u1, v1);
            // Triangle 2: 0, 2, 3
            v(x0, y0, z0, nx, ny, nz, u0, v0);
            v(x2, y2, z2, nx, ny, nz, u1, v1);
            v(x3, y3, z3, nx, ny, nz, u0, v1);
        }

        private void v(float x, float y, float z, float nx, float ny, float nz, float u, float v) {
            vertices.add(x);
            vertices.add(y);
            vertices.add(z);
            vertices.add(nx);
            vertices.add(ny);
            vertices.add(nz);
            vertices.add(u);
            vertices.add(v);
        }

        public MeshPart build() {
            float[] data = new float[vertices.size()];
            for (int i = 0; i < data.length; i++) {
                data[i] = vertices.get(i);
            }
            return new MeshPart(part, material, data);
        }
    }

    private static void writeMeshFile(File file, List<MeshPart> parts) throws IOException {
        file.getParentFile().mkdirs();
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeInt(RudyMesh.MAGIC); // 0x52554459
            out.writeInt(parts.size());
            for (MeshPart part : parts) {
                out.writeInt(part.part());
                out.writeInt(part.material());
                out.writeInt(part.vertices().length / RudyMesh.VERTEX_FLOATS);
                for (float f : part.vertices()) {
                    out.writeFloat(f);
                }
            }
        }
    }
}
