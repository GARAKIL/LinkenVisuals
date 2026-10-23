# VISUAL_AUDIT — Linken 1.21.11 vs donor 1.21.4

> Date: 2026-10-23 · Target build: **v1.0.131 / D107** · Donor: `C:\Users\gameg\Desktop\визуалы` (READ-ONLY, `linkenvisuals-2.0.jar`, MC 1.21.4)
>
> This is an explicit gap audit, not a claim of pixel parity. Items marked
> **fixed** are in D106; the rest is open scope.

## 1. FakePlayer crits

**Symptom:** fake player could be hit, but never received a critical hit.

**Root causes found:**
1. `MinecraftClientAttackMixin` cancelled `doAttack` at HEAD for the fake player, so the vanilla crit particle path in `PlayerEntity.attack` was not reached from the normal attack route.
2. `FakePlayer.damageOf` applied only the charge curve; it never multiplied by the vanilla `1.5x` crit factor.
3. Both fake paths called `HitSoundModule.tryPlay(client, false)`, so the configured hit sound always used the non-crit branch.
4. `HitSoundModule.isCrit` did not require `getAttackCooldownProgress(0.5f) > 0.9f` or `!isSprinting()`.

**Fixed in D106:**
- Vanilla-shaped crit gate: full charge, falling, airborne, not climbing,
  not in water, no blindness, no vehicle, not sprinting.
- `FakePlayer.hit` detects the crit once, adds the 1.5x damage, passes the
  crit flag to `HitSoundModule.tryPlay`, scales the hit effect and keeps the
  crit sound/particle path.
- `ClientPlayerInteractionManagerMixin` plays the hit sound only after the
  fake-player branch (no duplicate sound).
- **D106a:** damage uses the current `getAttackCooldownProgress(0.5f)` instead
  of `sinceHit`, so the first full-charge hit no longer deals only 20%.
- Pure tests: `FakePlayerTest` (damage curve) and `HitSoundModuleTest`
  (crit rules).

**Manual check:** jump, wait for full charge, hit the dummy; red crit
particles, crit sound, bigger number/health drop. Sprinting or standing on
the ground must not crit. In water/vehicle must not crit.

## 2. Welcome screen

**Symptom:** the donor plays a Lottie welcome animation on the first title
open; 1.21.11 opened straight into the menu.

**Donor:** `utils/client/MainMenuLottiePlayer` + `mainmenu/welcome.json`
(428×123, 493 frames, 60 fps) rendered through Skija/Skottie. Skija is
explicitly outside this port.

**Fixed in D106:** `LinkenMainMenuScreen` now has a first-open scene built
from the existing MSDF/chrome stack: `linken.` wordmark, localized
`Welcome back, <nick>`, accent glow, progress rail, click/key skip, then the
normal 450 ms menu intro. Music starts after the scene, like the donor.
`welcomePlayed` is static, so the scene plays once per process, not on every
settings return. Autotest captures `welcome` and then skips to `linken-mainmenu`.

**Open:** exact Lottie path/gradient stroke is not reproduced; the donor JSON
is kept as a reference only. A Skija/Skottie dependency remains out of scope.

## 3. ESC / pause menu

**Symptom:** 1.21.11 ESC opened the wide 1040 px settings-style hub, not the
donor 440 px pause panel.

**Donor:** `screen/settings/LinkenPauseScreen` (440 wide, 56 px topbar, 44 px
rows, 8 px gap, 2-column tiles, divider, save/quit row).

**Fixed in D106:** `LinkenPauseScreen` rewritten as a compact donor-style
panel:
- title `PAUSED` / `ПАУЗА` / `ПАУЗА`;
- world/server line;
- primary Resume row;
- 2-column Settings / LinkenVisuals / Advancements / Statistics / Open to LAN;
- divider;
- Save & Quit / Quit Game;
- hover springs, open slide, close X, ESC-to-resume, UI click sound.
- uses the existing `pause.*` i18n keys in en/ru/uk.

Autotest captures `pause` at 1920×1080. Remaining manual check: click every
row, resize to 1280×720, compare side-by-side with donor.

## 4. Animations

**Symptom:** 1.21.11 feels static compared to the donor.

**Cause:** the donor 1.21.4 instance runs the separate **Punchy** engine
(`punchy_config.json`: `enableMod:true`, sprint arm swing, swim/climb/elytra,
custom walk, fall arm animation). Our port intentionally did not copy its
181 classes / 2.7 MB animation stack.

**Fixed in D106 (lightweight subset):**
- `Better Animations → Hand Motion` (default true): walk bob, stronger sprint
  sway, lower hand in water, fall lift and a small attack recoil.
- Applied at the donor `renderItem` anchor, on top of HandPose/ViewModel and
  skipped while HandEditor is active.
- Pure tests in `HandMotionTest`.

**Open:** full Punchy item animation set (bedrock item models, third-person
animations, per-item clips, swim/elytra/crawl poses) is not ported. Do not
claim full Punchy parity.

## 5. GUI / main menu small differences

**Fixed in D106:**
- Account row: the `icons2` person glyph no longer overlaps
  “Current account · Licensed” (icon moved to the donor `tx`, text to `tx+10`).
- First-open welcome has a proper background scrim/glow instead of a flat
  black frame.
- ESC panel now matches the donor width/row rhythm.

**Checked but left as-is:**
- LinkGUI card grid, Profile/ClickGUI cards, Theme/Interface panes and dock
  match the D76–D95 geometry; no pixel diff is claimed.
- Main-menu button hover lift/colors follow the donor 130 ms / 1 px rule.
- `useVanillaTitleScreen` in a stale profile does not hide the Linken title:
  `MinecraftClientMixin` routes both `TitleScreen` and `MainMenuScreen` to
  `LinkenMainMenuScreen` while the style is Linken.

**Still open (needs a later pixel pass):**
- exact donor text metrics/kerning on non-Latin strings;
- screen-by-screen hovers/press states on Select World, Alt Manager,
  multiplayer modal and sub-settings;
- real server rows/favicon (singleplayer has no listed tab entries);
- pixel diff at 1280×720 and 1920×1080 for every screen.

## 6. Other explicit parity gaps

From `FEATURE_PARITY.md`, still open:
`HelperItemsHud` (blocked on donor Ft/Hw/Rw helper modules),
`DynamicIsland` (donor hides it for Link style; media covered),
`ItemRadius`, `DamageParticles`, `FortniteDamage`, line-by-line
`SelfNametags`, full Punchy, and full `UI` colour/fade/blur/BoostFPS.

## 6a. HUD D107 — CustomVitals + Armor V2 (fixed)

- **CustomVitals**: module with Segments/Stack/Bars, Segments 3–10,
  Armor Row; 182 px hotbar-anchored bar above the hotbar with HP/food,
  armor/air, XP/level, mount health/jump, damage flash/ghost HP.
- **Vanilla layers**: `renderStatusBars`, `renderMountHealth`,
  `ExperienceBar.renderBar/renderAddons` and `JumpBar.renderBar/renderAddons`
  are cancelled only while CustomVitals is enabled.
- **Armor V2**: two 49 px hotbar-relative groups (boots/leggings left,
  chest/helmet right), durability percentages; `Armor HUD Mode` in Interface.
- Screenshots `custom-vitals` and `armor-hud-v2` at 1920×1080.
- Still manual: live mount/air/hotbar-offhand clearance, 1280×720 preview,
  exact donor damage-flash timing.

## 7. Verification for D101-D107

- `test` / `clean build`: 284 tests, 0 failures/errors/skips in both flavors.
- Autotest Linken **977/977**, Strange **967/967**, 0 fail/skip, 1920×1080;
  `welcome`, `pause`, `custom-vitals`, `armor-hud-v2` and the fake-player crit
  assertion are part of the plan.
  No new shader/mixin/sound errors; only offline-auth/Realms.
- Screenshots: `lv-autotest-*-welcome.png`, `lv-autotest-*-pause.png`,
  `lv-autotest-*-linken-mainmenu.png`; the pause screenshot shows the compact
  440 px panel with `icons2` glyphs and the `PAUSED` title.
