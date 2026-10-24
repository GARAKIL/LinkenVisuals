# MASTER PROMPT - LinkenVisuals: continue from v1.0.139 / D115

You are the lead engineer continuing the existing LinkenVisuals port. Do not restart the project. Preserve accepted visual/audio/HUD decisions. Work autonomously through green builds, tests, autotest, docs, commit/push and a Russian report.

> **SCOPE:** full Punchy and Punchy-dependent `ViewModel Animated` are **explicitly excluded** by the user (2026-10-24). `HandMotion` is final; do not list Punchy as open work.

## 1. Repository

- Repo: `https://github.com/KurumaOfficial/LinkenVisuals`
- Branch: `main`
- Version: **1.0.139**
- Last code commit: `610a03d` `feat(helper): port mouse binds, fix splash labels, split LV branding (v1.0.139, D115)`.
- Tests: **315 / 0 failures / 0 errors / 0 skipped** in both flavors.
- Autotest: Linken **1147/1147** (1280×720 and 1920×1080), Strange **1137/1137** (1920×1080), 0 fail/skip.
- Audit: `docs/VISUAL_AUDIT.md` records what is fixed and what is still open.
- Jars (1.0.139): `linkenvisuals-1.0.139.jar` 11 165 138 bytes / `DE5528B70BC094AA7F6FEFFFA2D35D1D6775055C67675889500FCB503EA198F0`; `linkenvisuals-strange-1.0.139.jar` 11 165 146 bytes / `72E618F708D35D0661E9D568B348244E53BB67AFC01CEAEA92D43C992BE63353`.

Clone/verify:

```powershell
git clone https://github.com/KurumaOfficial/LinkenVisuals.git
cd LinkenVisuals
git checkout main
git pull
git log -2 --oneline
Get-Content source/gradle.properties   # mod_version=1.0.139
```

## 2. Donor

READ-ONLY full donor on this machine:

`C:\Users\gameg\Desktop\визуалы` (`ru.method.linkenvisuals`)

Historical `C:\StrangeVisuals\donor2\visualy` is absent. Ask the user if the desktop path is missing; never invent assets/dimensions/behavior.

The real LINKEN GUI is monolithic `screen/linkgui/LinkGuiScreen.java`; do not use `screen/linkgui/windows/*`.

## 3. Done in D105-D115 (do not redo)

- D105: `ViewModelModule` 12 donor manual offsets/scales, applied at the donor `renderItem` anchor on top of HandPose; object modules register before config load.
- D106: FakePlayer crit path (1.5× damage, vanilla crit gate, crit sound/particles, no double hit sound); first-open Welcome scene; compact 440 px `LinkenPauseScreen` with `pause.*` i18n/icons/real `GameMenuScreen` route; `Better Animations -> Hand Motion` (walk/sprint/water/fall/attack Punchy-lite); account-row icon overlap fix.
- D107: hotbar-anchored `CustomVitals` (Segments/Stack/Bars, armour/air/mount/XP, damage flash) with vanilla status/exp/jump cancellation, and Armor V2 two hotbar-relative groups.
- D108: donor `ItemRadius` (Ender Eye/Sugar circles, netherite box, dried-kelp staircase, snowball projection, splash-potion impact, proximity colour) on `FLAT_NO_DEPTH`; donor `%` proximity labels remain open.
- D109: donor `DamageParticles` (legacy/decorative impact, projectile trail, six modes) and `FortniteDamage` (floating crit/theme numbers, screen-space MSDF projection); `damage-number`/`damage-visuals` screenshots.
- D110: donor `SelfNametags` toggle, wired to the StreamerMode sanitizer for the local player only; autotest asserts the `Hidden` self label.
- D111: Interface Secondary Colour, Theme Fade, GUI Blur; fade-off collapses secondary to accent, no-blur LinkGUI/ClickGUI; `linkgui-noblur` screenshot.
- D112: ItemRadius splash proximity `NN%` labels (donor 1−dist/radius formula) via screen-space MSDF; `ItemRadiusRulesTest`.
- D113: Custom Theme Colours master + Background/Text colours, LinkGUI palette and HUD base; `linkgui-custom-colours` screenshot.
- D114: HelperItemsHud + Ft/Hw/Rw helper modules, key dispatch, `helper-items` screenshot.
- D115: real helper LMB/RMB binds (`KeybindSetting.mouseAllowed`, `MOUSE1`/`MOUSE2`, donor `M1`/`M2`, `MouseMixin` → `Modules.onHelperMouse`, ClickGUI capture, HUD `LMB`/`RMB`, autotest slot assertions); ItemRadius splash labels fixed (camera quaternion basis, FakePlayer scan, screen bounds, `item-radius-labels`); SelfNametags team-prefix tests; DynamicIsland/BoostFPS closed by donor rule; LV/Strange branding split (`lv_logo.png`, `strange_logo.png`, `BrandingAssetsTest`).
- `docs/VISUAL_AUDIT.md` explains the root causes and open gaps.

## 4. Next priority

1. **Human multiplayer acceptance**: real second player for ItemRadius splash `NN%` and team-prefix SelfNametags. Deterministic tests already exist; do not claim live acceptance from singleplayer.
2. **Human pixel acceptance**: 1280×720 and 1920×1080 screenshots exist; side-by-side donor comparison and hover/press checks remain.
3. **Punchy is out of scope** — do not start it. `HandMotion` is the final accepted animation subset.
4. **DynamicIsland / BoostFPS / helper mouse are closed** — do not reopen them.

## 5. Architecture that must not break

- HUD: `HudElement` (center/fraction), `HudManager` (`ELEMENTS`, drag/snap/edit-mode), `HudStyle`/`HudSurface` (8 styles/3 shapes), `HudLayoutStorage`, `HudTween`. New HUD element -> `HudManager.ELEMENTS`, real Interface `BooleanSetting`, preview/check/screenshot, non-overlapping default at GUI scale 3.
- Render: world passes on `END_MAIN` via `BlockOverlayPipelines.FLAT`/`FLAT_NO_DEPTH`, byte-packed `POSITION_COLOR`; never leave RenderSystem state/transforms damaged; hidden 1920×1080 screenshot required.
- Mixins: each `*Mixin.java` must be in `linkenvisuals.mixins.json`; `MixinRegistrationTest` enforces. Do not target removed 1.21.11 names.
- Flavors: default = Linken-only; `-Pstrange` = Strange-only. `StyleFlavor` is the single source. No cross-family style controls. Linken style lock and no Style/Editor buttons in LINKEN Settings.
- i18n: new visible strings in en/ru/uk; `LangParityTest` green. Existing module/setting labels are raw English.
- No Lombok, donor Module/Setting, legacy Render2D/WindowGuiRender, raw GL, Lottie/Skija, network/UserSyncManager, cosmetics catalog, crashers, Discord RPC, IAS. No empty catch/TODO/FIXME/System.out. No dead controls.

## 6. Build/run

```powershell
cd source
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\gradlew.bat test --console=plain --no-daemon
.\gradlew.bat clean build --console=plain --no-daemon
.\gradlew.bat test -Pstrange --console=plain --no-daemon
.\gradlew.bat build -Pstrange --console=plain --no-daemon
```

Expected: 315 tests, 0 fail/error/skip each.

Hidden autotest (explicit permission only):

```powershell
.\gradlew.bat runAutotest -PautotestWorld=AutoTestWorld -PautotestQuit --console=plain --no-daemon
.\gradlew.bat runAutotest -Pstrange -PautotestWorld=AutoTestWorld -PautotestQuit --console=plain --no-daemon
```

**Options workaround:** full `run/options.txt` with `key_key.*` currently trips `OptionsKeyLwjgl3Fix` and resets focus pause. Before a hidden run use:

```text
version:4671
fullscreen:false
pauseOnLostFocus:false
overrideWidth:1920
overrideHeight:1080
guiScale:3
renderDistance:8
onboardAccessibility:false
skipMultiplayerWarning:true
tutorialStep:none
```

Restore the original `options.txt` afterwards. Run minimized or on a separate desktop; never leave the game on the active desktop. Expected log: only offline-auth/Realms; no shader-source/compile, mixin apply/injection, or unknown sound events.

Jar hashes:

```powershell
Get-ChildItem build\libs\linkenvisuals-*.jar |
  ForEach-Object { "$($_.Name) | $($_.Length) | $((Get-FileHash $_.FullName -Algorithm SHA256).Hash)" }
```

## 7. Key files

```text
source/src/main/java/dev/linkenvisuals/
  player/FakePlayer.java                 # crit detection/damage/lastCrit
  module/impl/HitSoundModule.java        # vanilla crit gate + critTrace
  ui/linkmenu/LinkenMainMenuScreen.java  # welcome intro, account row, menu
  ui/linkmenu/LinkenPauseScreen.java     # compact donor ESC panel
  ui/linkmenu/LinkenMenuChrome.java      # shared chrome/icons
  hands/HandMotion.java                  # Punchy-lite hand motion
  hands/HandPose.java, HandEditor.java   # accepted free-form hand editor
  mixin/HeldItemRendererMixin.java       # HandPose + ViewModel + HandMotion anchors
  clickgui/ModuleRegistry.java           # Better Animations settings, HUD/i18n gates
  module/impl/ViewModelModule.java       # 12 donor manual settings
  hud/*                                  # HUD layer incl. HelperItemsHud
  clickgui/setting/KeybindSetting.java   # key + LMB/RMB bind representation
  mixin/MouseMixin.java                  # helper mouse dispatch
  ui/linkmenu/*                          # LV-branded Linken menu/pause/loading
  clickgui/link/LinkGuiScreen.java       # LV header/footer, bind capture
  autotest/AutoTestDriver.java           # welcome/pause/crit/helper/radius steps
source/src/test/java/dev/linkenvisuals/
  player/FakePlayerTest.java
  module/impl/HitSoundModuleTest.java
  hands/HandMotionTest.java
  module/impl/ViewModelModuleTest.java
  ...
```

## 8. Known traps

- `HandMotion` and `ViewModel` both anchor at `renderFirstPersonItem -> INVOKE renderItem`; do not move them to HEAD, and skip both while `HandEditor.active()`.
- FakePlayer crit must use the same gate for damage/sound/particles; don't force crit in production, only via the test overload.
- `LinkenPauseScreen` must only replace `GameMenuScreen` in the Linken style. Strange/Vanilla must keep the vanilla pause.
- `LinkenFonts.icons2()` glyphs are `MENU_ICON_*` (A-H), not legacy `ICON_*` (P/Q/...).
- Welcome is procedural, not donor Lottie; full Punchy is not ported.
- HUD defaults must not overlap at GUI scale 3; add autotest preview/check/screenshot. `HelperItemsHud` default is `0.77, 0.86` (hotbar's right) to avoid chat.
- Helper mouse binds persist as `MOUSE1`/`MOUSE2`; key-only `KeybindSetting` must never show a mouse label (dead bind).
- Linken branding uses `lv_logo.png`; `strange_logo.png` is Strange-only. Do not reference `link_icon.png` from Linken screens.
- Do not commit `build/`, `run/`, jars, logs, screenshots or temp files.

## 9. Definition of Done

- Both builds green, 315 tests, 0 fail/error/skip.
- Both hidden autotests green, no new mod/shader/sound/mixin errors (only offline-auth/Realms).
- No dead controls; new strings en/ru/uk; `LangParityTest` green.
- Jars path/size/SHA-256 recalculated; Russian report with visual/log evidence and manual list.
- Code commit (+1 patch) then docs commit, `git fetch` before commit, push to main without force.

## 10. First action

Read `docs/HANDOFF.md` top, `docs/VISUAL_AUDIT.md`, `docs/MERGE_DECISIONS.md` D105-D115, `docs/FEATURE_PARITY.md`. Run baseline tests in both flavors. The port is feature-complete for the accepted scope; do not invent new controls. Only human multiplayer/pixel acceptance remains.
