# MASTER PROMPT - LinkenVisuals: continue from v1.0.135 / D111

You are the lead engineer continuing the existing LinkenVisuals port. Do not restart the project. Preserve accepted visual/audio/HUD decisions. Work autonomously through green builds, tests, autotest, docs, commit/push and a Russian report.

## 1. Repository

- Repo: `https://github.com/KurumaOfficial/LinkenVisuals`
- Branch: `main`
- Version: **1.0.135**
- Last code commit: `fa38938` `feat(ui): expose secondary colour, fade and blur (v1.0.135, D111)`.
- Tests: **300 / 0 failures / 0 errors / 0 skipped** in both flavors.
- Autotest: Linken **1106/1106**, Strange **1096/1096**, 0 fail/skip, 1920×1080.
- Audit: `docs/VISUAL_AUDIT.md` records what is fixed and what is still open.
- Jars (1.0.135): `linkenvisuals-1.0.135.jar` 11 111 719 bytes / `FC0CEC2721C1C5A5FF6048A21C3AC12C441BC3673560EA06B56457C85DBBA82B`; `linkenvisuals-strange-1.0.135.jar` 11 111 713 bytes / `04EE5DF0090B5FB4CAE4E50ECD1CF75D102F2B9D8A2D43F08C3C463392392C52`.

Clone/verify:

```powershell
git clone https://github.com/KurumaOfficial/LinkenVisuals.git
cd LinkenVisuals
git checkout main
git pull
git log -2 --oneline
Get-Content source/gradle.properties   # mod_version=1.0.135
```

## 2. Donor

READ-ONLY full donor on this machine:

`C:\Users\gameg\Desktop\визуалы` (`ru.method.linkenvisuals`)

Historical `C:\StrangeVisuals\donor2\visualy` is absent. Ask the user if the desktop path is missing; never invent assets/dimensions/behavior.

The real LINKEN GUI is monolithic `screen/linkgui/LinkGuiScreen.java`; do not use `screen/linkgui/windows/*`.

## 3. Done in D105-D111 (do not redo)

- D105: `ViewModelModule` 12 donor manual offsets/scales, applied at the donor `renderItem` anchor on top of HandPose; object modules register before config load.
- D106: FakePlayer crit path (1.5× damage, vanilla crit gate, crit sound/particles, no double hit sound); first-open Welcome scene; compact 440 px `LinkenPauseScreen` with `pause.*` i18n/icons/real `GameMenuScreen` route; `Better Animations -> Hand Motion` (walk/sprint/water/fall/attack Punchy-lite); account-row icon overlap fix.
- D107: hotbar-anchored `CustomVitals` (Segments/Stack/Bars, armour/air/mount/XP, damage flash) with vanilla status/exp/jump cancellation, and Armor V2 two hotbar-relative groups.
- D108: donor `ItemRadius` (Ender Eye/Sugar circles, netherite box, dried-kelp staircase, snowball projection, splash-potion impact, proximity colour) on `FLAT_NO_DEPTH`; donor `%` proximity labels remain open.
- D109: donor `DamageParticles` (legacy/decorative impact, projectile trail, six modes) and `FortniteDamage` (floating crit/theme numbers, screen-space MSDF projection); `damage-number`/`damage-visuals` screenshots.
- D110: donor `SelfNametags` toggle, wired to the StreamerMode sanitizer for the local player only; autotest asserts the `Hidden` self label.
- D111: Interface Secondary Colour, Theme Fade, GUI Blur; fade-off collapses secondary to accent, no-blur LinkGUI/ClickGUI; `linkgui-noblur` screenshot.
- `docs/VISUAL_AUDIT.md` explains the root causes and open gaps.

## 4. Next priority

1. **UI** remaining donor controls: Background Colour, Text Colour, BoostFPS; сверка с LinkGuiTheme/HudStyle.
2. **Punchy-lite expansion** (swim/climb/elytra/fall/item idle); full Punchy remains out of scope.
3. **Pixel GUI pass** at 1280×720 and 1920×1080; human acceptance.
4. `UI` palette/fade/blur subset ported (D111); `SelfNametags` (D110), `DamageParticles`/`FortniteDamage` (D109), `ItemRadius` (D108), donor `%` proximity labels open. `HelperItemsHud` blocked, `DynamicIsland` skipped.

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

Expected: 281+ tests, 0 fail/error/skip each.

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
  hud/*                                  # HUD layer
  autotest/AutoTestDriver.java           # welcome/pause/crit steps
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
- HUD defaults must not overlap at GUI scale 3; add autotest preview/check/screenshot.
- Do not commit `build/`, `run/`, jars, logs, screenshots or temp files.

## 9. Definition of Done

- Both builds green, 281+ tests, 0 fail/error/skip.
- Both hidden autotests green, no new mod/shader/sound/mixin errors (only offline-auth/Realms).
- No dead controls; new strings en/ru/uk; `LangParityTest` green.
- Jars path/size/SHA-256 recalculated; Russian report with visual/log evidence and manual list.
- Code commit (+1 patch) then docs commit, `git fetch` before commit, push to main without force.

## 10. First action

Read `docs/HANDOFF.md` top, `docs/VISUAL_AUDIT.md`, `docs/MERGE_DECISIONS.md` D105-D106, `docs/FEATURE_PARITY.md`. Run baseline tests in both flavors. Then start with the pixel GUI pass or the screen-anchored HUD layer; choose the one with the clearest donor evidence and do not add fake controls.
