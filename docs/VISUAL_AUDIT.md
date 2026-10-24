# VISUAL_AUDIT — Linken 1.21.11 vs donor 1.21.4

> Date: 2026-10-24 · Target build: **v1.0.139 / D115** · Donor: `C:\Users\gameg\Desktop\визуалы` (READ-ONLY, `linkenvisuals-2.0.jar`, MC 1.21.4)
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

**Excluded by scope (2026-10-24):** full Punchy item animation set (bedrock
item models, third-person animations, per-item clips, swim/elytra/crawl poses)
is not ported. `HandMotion` is the final accepted subset; Punchy is not an
open task.

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

From `FEATURE_PARITY.md`, the remaining items are human checks only:
live multiplayer splash/team-prefix acceptance and screen-by-screen pixel
acceptance. `DynamicIsland` is closed by the donor Link-style rule; helper
mouse bindings shipped in D115; `BoostFPS` is closed because the donor getter
is hard-wired to `DEFAULT` and its UI control is hidden.
**Punchy and Punchy-dependent ViewModel Animated are excluded by user
(2026-10-24); HandMotion is final.**

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

## 6b. World D108 — ItemRadius (fixed; labels fixed in D115)

- **Ender Eye/Sugar**: radius-14 ring at the player's feet.
- **Netherite Scrap**: 5×5×5 box (7×7×7 with Dragon Trap).
- **Dried Kelp**: 5×2×5 plate under/above the player when looking steeply
  up/down, donor staircase wall (5 long, 4 high) when looking sideways.
- **Snowball**: projected terrain center, radius 12.
- **Splash Potion**: 200-step impact simulation (speed 0.5, pitch −20°,
  gravity 0.03, water/air drag, player velocity, block raycast), radius 1–30.
- **Proximity colour**: magenta when another player is inside the circle/box.
- **Splash labels (D112, fixed D115)**: `NN%` over each player inside the
  simulated splash radius, donor `1 − dist/radius` formula, screen-space MSDF
  with black outline. D115 fixed the camera basis (`Camera.getRotation()`
  instead of a near-zero `right × up`), added the client-side FakePlayer to
  the scan and added screen-bounds + a 4–10 px size cap. Autotest screenshot
  `item-radius-labels` shows `92%` over the FakePlayer. A live multiplayer
  case remains a human check.

## 6c. Combat D109 — DamageParticles + FortniteDamage (fixed)

- **DamageParticles**: legacy impact burst (30, 2.1–2.8 s, bounce/friction/LOS,
  glow/core/sparkle layers) plus decorative attack burst (35) and projectile
  trail (4 per moving arrow/trident/ender pearl), six donor modes.
- **FortniteDamage**: HP-difference sampling up to 750 ms, kill estimate from
  attack damage, 850 ms lifetime, 0.42 rise, crit #FCFE3C, theme/custom colour.
- **Difference:** the donor 3D billboard is replaced by a camera-projected
  screen-space MSDF pass, so digits stay readable and ignore terrain depth.
- **Open:** exact donor 3D billboard parity, server HP-sync limitation, pixel
  diff/human acceptance.

## 6d. Identity D110 — SelfNametags (fixed)

- Donor `SelfNametags` is a toggle, not a renderer: it applies the StreamerMode
  self-name sanitizer to the local player's `getDisplayName()`.
- Requires StreamerMode + `Custom Name`; without them the donor and this port
  both leave the label unchanged.
- Autotest sets `Hidden`, asserts the local display name, then restores.
- **Open:** multiplayer/team-prefix human check.

## 6e. UI D111 — palette/fade/blur (subset, open remainder)

- **Secondary Colour**: `Theme.secondary()` now uses the configured secondary
  for HUD gradients.
- **Theme Fade off**: secondary collapses to accent, so MINIMAL/AURORA/GRADIENT
  HUD surfaces become single-colour.
- **GUI Blur off**: LinkGUI and Beta ClickGUI override `renderBackground` and
  skip vanilla post-process blur; the Link backdrop also skips `applyBlur`.
- Screenshot `linkgui-noblur` at 1920×1080 proves the no-blur path.
- **Remaining:** donor per-offset fade interpolation is simplified. Background/
  Text shipped in D113; BoostFPS is closed by donor rule in D115.

## 6f. UI D113 — custom background/text colours

- **Custom Theme Colours** master + `Background Colour` / `Text Colour`.
- LinkGUI panel/row/field/text/backdrop are recoloured; `HudStyle.base()` uses
  the custom background for HUD panels.
- Screenshot `linkgui-custom-colours` at 1920×1080.
- **Closed in D115:** BoostFPS is not exposed because donor `getBoostFpsMode()`
  always returns DEFAULT and its UI control is hidden; semantic HUD colours
  (health/armour) keep their own colours.

## 6g. HUD D114 — HelperItemsHud + Ft/Hw/Rw (mouse added in D115)

- 18 px item/key tiles for enabled Ft/Hw/Rw helper bindings; item must be in
  the hotbar (preview shows sample tiles in HUD edit mode).
- D115: LMB/RMB binds (`MOUSE1`/`MOUSE2`, donor `M1`/`M2`), `MouseMixin`
  dispatch, ClickGUI capture and `LMB`/`RMB` tile labels. Autotest asserts
  both buttons select the same slots as key binds.
- Default position moved to the hotbar's right (`0.77, 0.86` design fraction):
  at 1280×720 the old centre-left slot overlapped chat and the hotbar.
- Screenshot `helper-items` at 1920×1080 and 1280×720; labels are fully visible.
- 1280×720 autotest: Linken **1147/1147**; LinkGUI/custom-vitals/helper-items
  layouts fit at that resolution.
- **Open:** human pixel acceptance only. DynamicIsland is closed by donor rule.
- **Punchy is out of scope** (user decision 2026-10-24); HandMotion is final.

## 6h. Branding D115 — LV vs Strange

- Linken uses the user-provided LV monogram (`lv_logo.png`, transparent
  512×512) in the main menu wordmark, welcome scene, loading scene, pause
  topbar, HUD watermark, KeybindPanel, LinkGUI header and footer.
- Strange uses `strange_logo.png` in `MainMenuScreen`, `ClickGuiScreen`, the
  Strange HUD watermark/KeybindPanel and the Beta loading scene.
- `BrandingAssetsTest` guards the packaged assets. Linken screenshots show
  only LV; the Strange world screenshot shows the infinity mark.
- The LinkGUI screen is a Linken-family shell and keeps the LV mark even when
  the autotest opens it from a Strange build; normal Strange routing never
  exposes it.

## 7. Verification for D101-D115

- `test` / `clean build`: 315 tests, 0 failures/errors/skips in both flavors.
- Autotest Linken **1147/1147** at 1280×720 and 1920×1080; Strange
  **1137/1137** at 1920×1080, 0 fail/skip. `welcome`, `pause`,
  `custom-vitals`, `armor-hud-v2`, `item-radius`, `item-radius-labels`,
  `damage-number`, `damage-visuals`, `check self nametag`, `check UI palette`,
  `check custom UI colours`, `check helper hud`, helper LMB/RMB selection and
  the fake-player crit assertion are part of the plan. No new
  shader/mixin/sound errors; only offline-auth/Realms.
- Screenshots: `lv-autotest-*-welcome.png`, `lv-autotest-*-pause.png`,
  `lv-autotest-*-linken-mainmenu.png`, `lv-autotest-*-linkgui.png`,
  `lv-autotest-*-helper-items.png`, `lv-autotest-*-item-radius-labels.png`;
  the pause screenshot shows the compact 440 px panel with `icons2` glyphs,
  the `PAUSED` title and the LV mark.
- Reported resolutions are the screenshot framebuffer sizes: 1280×720 and
  1920×1061 (client area of the 1920×1080 override window).
