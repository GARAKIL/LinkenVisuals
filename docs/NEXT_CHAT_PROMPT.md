# MASTER PROMPT - LinkenVisuals: continue from v1.0.126 / D103

You are the lead engineer continuing the existing LinkenVisuals port. Do not restart the project. Work autonomously, preserve the existing visual/audio decisions, and finish with a green build, tests, docs, commit/push and a Russian report.

## 1. Repository

- Repository: `https://github.com/KurumaOfficial/LinkenVisuals`
- Branch: `main`
- Version at handoff: **1.0.126**
- Tests at handoff: **265 / 0 failures / 0 errors / 0 skipped** in both flavors
- Autotest at handoff: Linken **862/862**, Strange **852/852**, no new ERROR/exception
- Jars at handoff:
  - `source/build/libs/linkenvisuals-1.0.126.jar` — **11 020 288 bytes**, SHA-256 `F83D8CA8EFC95FC7446FD16D4E90FC2857464D5041B1AEA463F873DCD72ABE84`
  - `source/build/libs/linkenvisuals-strange-1.0.126.jar` — **11 020 285 bytes**, SHA-256 `38DA975FCB6C74E6CB5A32CE8F6958828A0F55FE3C7FE5471809052C6503344D`
- HEAD: use `git log -1` after clone; the latest docs commit describes D103.

Clone and verify:

```powershell
git clone https://github.com/KurumaOfficial/LinkenVisuals.git
cd LinkenVisuals
git checkout main
git pull
git log -1
Get-Content source/gradle.properties   # mod_version=1.0.126
```

## 2. Donor

The donor is **READ-ONLY**. On the previous machine it was:

`C:\StrangeVisuals\donor2\visualy`

Release jar may be at:

`C:\Users\gameg\Desktop\визуалы\build\libs\linkenvisuals-2.0.jar`

If the path is missing on the new machine, **ask the user for the donor path/copy**. Never invent assets, dimensions or behavior. Do not modify donor files.

Important: the actually used donor screen is the monolithic
`src/main/java/ru/method/linkenvisuals/screen/linkgui/LinkGuiScreen.java`.
Do **not** use `screen/linkgui/windows/*` as the reference; those classes are unused.

Read first:

- `docs/HANDOFF.md` — top section (chat #51, D97);
- `docs/MERGE_DECISIONS.md` — D80–D97;
- `docs/ROADMAP.md` — M8.10–M8.24;
- `docs/PORT_STATUS.md` — M8.14–M8.24;
- `docs/FEATURE_PARITY.md` — explicit ported/open/excluded matrix.

## 3. Hard requirements

- While **Linken GUI** is active, switching to Classic/Beta is forbidden. Do not restore that path.
- Do not add Style/Editor buttons to LINKEN Settings.
- Beta and Vanilla must not be broken, restyled or behaviorally changed.
- Every visible control must work. Dead clicks/fake controls are forbidden.
- New strings go to i18n **en/ru/uk**; `LangParityTest` must stay green.
- Feature/fix commits bump **+1 patch** from the actual version.
- Docs-only commits do not bump.
- Before commit: `git fetch origin`. Force push is forbidden.
- Do not commit `build/`, `run/`, jars, logs, screenshots or temp files.
- Errors/fallbacks via `LOGGER.warn/debug`; no empty catch, `TODO`, `FIXME`, `System.out`.
- Do not add Lombok, donor `Module`/`Setting`, legacy `Render2D`/`WindowGuiRender`, raw GL, Lottie `welcome.json`, network, cosmetics catalog, crashers, Discord RPC or IAS. These are intentionally out of scope (D6/D19/D52).
- Minecraft may be launched only with the user's explicit permission. If the user asks for a hidden run, use a separate Windows desktop/minimized window; do not leave the window on their active desktop.

## 4. What is already done

- D80/v1.0.103: 35 donor sound events, all OGGs, `LinkenSounds`, `LinkenMenuMusic`, `SoundsModule`, UI/combat cues no longer use vanilla sounds.
- D81/v1.0.104: shader warm-up deferral; later verified and replaced by readiness gating.
- D82/v1.0.105: pinned settings cards, verified shader readiness after `ResourceReloaderKeys.Client.SHADERS` + tick fallback.
- D83/v1.0.106: HUD Style/Shape settings, custom scoreboard, custom tab, Interface scroll, HUD edge clamp.
- D84/v1.0.107: donor **Minimal** HUD style exact.
- D85/v1.0.108: **all 8 donor HUD styles** (Card, Minimal, Neon, Aurora, Glass, Solid, Gradient, Outline) with per-vertex rounded gradients.
- D86/v1.0.109: donor **border smoothness** via `rounded_rect_border` pipeline (`POSITION_TEXTURE_COLOR_NORMAL`; thickness/internal/external through normals, radius through Position.z).
- D87/v1.0.110: donor **TargetHud** — 104×30 card + armour/offhand row, crosshair/aim-cone + LOS target pick with 1 s hold, head/HP/absorption/durability/hit flash, `Target HUD` setting, `target-hud.png` autotest.
- D88/v1.0.111: donor **CoordinatesHud** — 17 px card, `iconsnew` compass, integer XYZ, two-decimal BPS, animated width/fade, `Coordinates HUD` setting (default off), `coordinates-hud.png` autotest; shared `HudTween`.
- D89/v1.0.112: donor **server browser 4b** — 1000×272 list window, donor chrome, search/refresh, selected/hover rows, favicon/letter tiles, players/ping, smooth scroll, double-click join, donor 460 px Add/Edit/Direct/Delete forms with resource-pack choice; `multiplayer.png` + `multiplayer-add.png`.
- D90/v1.0.113: donor **secondary/social gradient** material, account-avatar gradient, lowercase RU meta, donor disclaimer line height; RU title/server/modal autotest.
- D91/v1.0.114: donor **icons2** atlas + `MENU_ICON_*` for main-menu globe/person/gear/power/Telegram/Discord/LV/code.
- D92/v1.0.115: donor **media cover** gradient/artist baseline, server window **-45° hatching**, CPU-rounded **favicon** mask 6/32.
- D93/v1.0.116: **LINKEN splash replaces the Mojang logo from the first frame**; vanilla-only bootstrap progress until shaders/atlases are ready, then full `linken.` scene.
- D94/v1.0.117: **layout-aware text shortcuts** — `LayoutKeys` + `TextInput`; Ctrl+A/C/V/X/Z/Y, word deletion and caret movement in all vanilla fields and LINKEN/ClickGUI text fields.
- D95/v1.0.118: **HUD batch** - `CooldownsHud` + reflective `CooldownTracker`, `ArmorHud` V1, `InventoryHud` 27/36 slots, `LowArmorNotificationsHud`, five Interface settings, previews/screenshots; `HelperItems`/`DynamicIsland` intentionally blocked/skipped.
- D96/v1.0.119: **Aspect Ratio** - Visual/World module, projection `m00 = m11 / ratio`, `ProjectionMathTest`; CameraSettings is covered by Better Animations (`Camera (F5)`).
- D97/v1.0.120: **NoRender** - 8 donor switches (hurt camera, fire, totem, scoreboard, bossbar, particles, armor, inventory background); mixins and autotest sweep.
- D98/v1.0.121: **HitRange + CustomHitBox** - triangle-list ring at `END_MAIN` (3.32 radius, 72 sides, donor thickness/rotation/depth, red near visible players) and entity boxes (128-block cap, frustum + 120 ms LOS cache, fill/outline, self/items toggles); geometry tests and clean `hitrange`/`custom-hitbox` screenshots.
- D99/v1.0.122: **NoRender mixin registration fix** - BossBar/Fire/Particle mixins were missing from the config; `ParticleManager.addToBatch` and `renderFireOverlay` signatures corrected for 1.21.11; `MixinRegistrationTest` added; remap warning gone.
- D100/v1.0.123: **compile-time Strange/Linken flavors** - `build` is Linken-only; `build -Pstrange` is Strange-only (Vanilla/Beta menu default Vanilla, Classic/Beta GUI default Classic, legacy Strange HUD). `BuildFlavor` + `StyleFlavor`; saved configs and editor/hotkeys cannot cross families.
- D101/v1.0.124: **Optimization** - Tier Low/Medium/Ultra, `Entity Culling` with donor DDA OcclusionCuller, render-distance cap 12/8, particle radius 48/32/22, Ultra cloud culling; four registered mixins; 257 tests, Linken 805/805, Strange 795/795.
- D102/v1.0.125: **Particles** - Bloom/Star/Snow/Firefly/Dollar textured billboards + Cube wireframe/glow, donor spawn/life/LOS, copied assets; 261 tests, Linken 847/847, Strange 837/837.
- D103/v1.0.126: **Trajectories** - 300-step projectile simulation, Bow/Multishot/throwables, procedural impact marker + optional explosive radius; 265 tests, Linken 862/862, Strange 852/852.
- Title/main menu, loading/splash, LinkGUI, settings hub/subpages, multiplayer, Alt Manager, Select World, pause, HUD editor, pinned cards, friends/waypoints, configs, media, style lock are in place.
- Shader-source race: no `Couldn't find source` / `Couldn't compile` in the final autotest.

Do not redo these from scratch. Use the docs and current code as the baseline.

## 5. What remains — next code slices

Use `docs/FEATURE_PARITY.md` as the source of truth. Recommended order:

1. **HUD elements**:
   - ✅ D95: `CooldownsHud`, `ArmorHud` V1, `InventoryHud`, `LowArmorNotificationsHud` — in `HudManager.ELEMENTS`, Interface settings, drag/snap/edit-mode, preview/autotest.
   - ⛔ `HelperItemsHud`: blocked — donor `FtHelper/HwHelper/RwHelper` modules and their bind settings are absent; do not fake data. Port the helper modules first or leave it explicitly open.
   - ⛔ `DynamicIsland`: donor skips it while `LinkHudStyle.active()`; our style is Link-locked, media is already covered by `MediaPlayerHud` and `LyricsHud`. Do not add a duplicate island.
   - ⛔ `ArmorHud` V2: needs screen-anchored/hotbar-relative HUD geometry; current `HudElement` is center/fraction based.
   Donor sources: `hud/impl/*.java`, `hud/HudElement.java`, `hud/LinkHudStyle.java`, donor HUD manager.
   Our HUD architecture: `hud/HudElement.java`, `hud/HudManager.java`, `hud/HudStyle.java`, `hud/HudSurface.java`, layout persistence in `hud/HudLayoutStorage.java`.
   Add each new element to `HudManager.ELEMENTS`, preserve edit-mode drag/snap, add a working enable/disable setting, and an autotest screenshot/behavior step. Keep default positions non-overlapping at GUI scale 3.

2. **Remaining visual modules** from `FEATURE_PARITY.md`:
   - visual `KillStreak`, full `ViewModel`/Hands. `CameraSettings` is covered by Better Animations; `AspectRatio`/`NoRender` in D96/D97; `HitRange`/`CustomHitBox` in D98; `Optimization` in D101; `Particles` in D102; `Trajectories` in D103.
   - For new world modules, follow the proven `END_MAIN` + `BlockOverlayPipelines.FLAT` path, byte-packed `POSITION_COLOR`, and validate with a hidden 1920×1080 screenshot before committing.
   - Do not add dead settings for modules that have no backend.
   - Port only donor client/visual behavior; leave server helpers/network/crashers out.

3. **Human acceptance**:
   - Sound cues: kill melee/arrow, streak 2–5, totem, death, low HP, HitSound Bell/Crime/Nya/Uwu/Only Crit, UI/ClickGUI.
   - Check `latest.log` for `Unable to play unknown soundEvent`, vanilla sounds, shader-source errors.
   - Custom Tab needs a multiplayer server; singleplayer has no listed entries.
   - Visual pixel diff at 1280×720 and 1920×1080.

4. **Final polish**:
   - Any remaining donor border/geometry differences, hover animations, exact font metrics.
   - Keep `FEATURE_PARITY.md` updated; do not claim 100% parity until every open item is closed.

## 6. Build and verification

```powershell
cd source
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\gradlew.bat test --console=plain --no-daemon
.\gradlew.bat clean build --console=plain --no-daemon
```

Expected baseline: **265 tests, 0 failures/errors/skips** in both flavors. Build both:
`.\gradlew.bat clean build` and `.\gradlew.bat build -Pstrange`.

Hidden/automated smoke test (requires explicit user permission):

```powershell
# run/options.txt: fullscreen:false, overrideWidth:1920, overrideHeight:1080
.\gradlew.bat runAutotest -PautotestWorld=AutoTestWorld -PautotestQuit --console=plain --no-daemon
```

- Autotest opens both GUIs, LinkGUI, title, inventory, attacks mobs, sweeps every module/setting, takes screenshots and writes a report.
- Check `run/logs/latest.log` and `run/logs/linkenvisuals-autotest-*.log`.
- Expected at handoff: Linken **862/862**, Strange **852/852**, no new ERROR/exception, no `Couldn't find source`/`Couldn't compile`, no unknown sound events. `pauseOnLostFocus` must be `false` for an unfocused/minimized hidden run; restore `options.txt` afterwards.
- Restore `run/options.txt` after the run.

Jar check:

```powershell
Get-ChildItem build\libs\linkenvisuals-*.jar |
  ForEach-Object { Get-FileHash $_.FullName -Algorithm SHA256 }
```

## 7. Key files

```text
source/src/main/java/dev/linkenvisuals/
  clickgui/ModuleRegistry.java
  clickgui/link/LinkGuiScreen.java
  clickgui/link/LinkGuiLayout.java
  clickgui/link/LinkGuiConfig.java
  clickgui/link/LinkGuiTheme.java
  hud/HudManager.java
  hud/HudElement.java
  hud/HudStyle.java
  hud/HudSurface.java
  hud/ArmorHud.java
  hud/CooldownTracker.java
  hud/CooldownsHud.java
  hud/InventoryHud.java
  hud/LowArmorNotificationsHud.java
  hud/TargetHud.java
  hud/CoordinatesHud.java
  hud/HudTween.java
  hud/ScoreboardHud.java
  ui/linkmenu/LinkenMultiplayerScreen.java
  hud/Watermark.java
  hud/KeybindPanel.java
  hud/HotbarHud.java
  hud/MediaPlayerHud.java
  hud/PotionSlot.java
  hud/PotionBlock.java
  hud/TotemCounterHud.java
  hud/UseTrackerHud.java
  render/RoundedRectPipeline.java
  render/Pipelines.java
  render/HitRangeRenderer.java
  render/HitRangeGeometry.java
  render/CustomHitBoxRenderer.java
  render/CustomHitBoxGeometry.java
  render/CustomHitBoxRules.java
  module/impl/HitRangeModule.java
  module/impl/CustomHitBoxModule.java
  module/impl/OptimizationModule.java
  module/impl/ParticlesModule.java
  util/optimization/OcclusionCuller.java
  util/optimization/OptimizationRules.java
  util/particles/ParticlesRules.java
  util/particles/ParticleVisibilityHelper.java
  render/ParticlesPipelines.java
  render/ParticlesRenderer.java
  render/TrajectoriesRenderer.java
  module/impl/TrajectoriesModule.java
  util/trajectories/TrajectoryRules.java
  ui/style/StyleFlavor.java
  ui/style/StyleManager.java
  ui/linkmenu/LinkenMenuChrome.java
  ui/linkmenu/LinkenMenuLayout.java
  util/input/LayoutKeys.java
  util/input/TextInput.java
  mixin/TextFieldWidgetMixin.java
  mixin/InGameHudMixin.java
  mixin/PlayerListHudMixin.java
  autotest/AutoTestDriver.java
  ui/linkmenu/*
source/src/main/resources/assets/linkenvisuals/
  shaders/core/rounded_rect_border.{vsh,fsh}
  sounds.json
  sounds/*
source/src/test/java/dev/linkenvisuals/
  clickgui/link/LinkGuiLayoutTest.java
  hud/HudStyleTest.java
  render/HitRangeGeometryTest.java
  render/CustomHitBoxGeometryTest.java
  mixin/MixinRegistrationTest.java
  ui/style/StyleFlavorTest.java
  util/optimization/OptimizationRulesTest.java
  util/particles/ParticlesRulesTest.java
  util/trajectories/TrajectoryRulesTest.java
  audio/SoundAssetsTest.java
```

## 8. Current important decisions

- **Compile-time flavor lock (D100)**: default = Linken-only; `-Pstrange` = Strange-only. `BuildFlavor` is generated by Gradle; `StyleFlavor` owns the allowed GUI/menu/HUD lists. `ModuleRegistry`, `StyleManager`, `ConfigManager`, `GameOptionsScreenMixin` and `MainMenuEditorScreen` sanitize every entry path. Never add a cross-family style button.
- **Linken style lock**: `ModuleRegistry.isGuiStyleLocked`, `cycleGuiStyle`, `setGuiStyle`; `GUI Style` hidden in Linken Interface/pinned cards.
- **Shader readiness**: `Pipelines.resourcesReady` set after `ResourceReloaderKeys.Client.SHADERS` + tick fallback; custom splash/loading/title gated.
- **HUD style**: all variants use `HudStyle`; donor border smoothness uses `RoundedRectPipeline.drawBorder`.
- **TargetHud**: `TargetHud` in `HudManager.ELEMENTS`; `ModuleRegistry.targetHud()` gates rendering, edit/chat shows self-preview; favours crosshair over aim-cone and holds the last target for 1 s.
- **CoordinatesHud**: `CoordinatesHud` in `HudManager.ELEMENTS`; `ModuleRegistry.coordinatesHud()` (default off) gates rendering, edit/chat shows self-preview; BPS = horizontal velocity ×20 (1.21.11 has no public `prevX/prevZ`).
- **HUD animation**: `HudTween` is the shared time-based linear/ease-out helper; do not duplicate per-element tween classes.
- **HUD batch (D95)**: `CooldownsHud` + `CooldownTracker` (reflection over 1.21.11 cooldown entries, inventory fallback); `ArmorHud` V1 only; `InventoryHud` optional hotbar row; `LowArmorNotificationsHud` 50/25 thresholds. Do not expose Armor V2 until the HUD layer supports hotbar-relative anchoring. `HelperItemsHud` has no data source; `DynamicIsland` is hidden by Link style and covered by `MediaPlayerHud`/`LyricsHud`.
- **Server browser**: `LinkenMultiplayerScreen` mirrors donor 4b list geometry (1000×272, topbar 68/row 64/footer 76); Add/Edit/Direct/Delete use a 460 px donor-style modal with resource-pack policy; window hatching and CPU-rounded favicons. Backend pinned servers are not ported.
- **Main-menu icons**: use `LinkenFonts.icons2()` / `MENU_ICON_*`; do not swap the global `ICON_*` constants used by ClickGUI and auxiliary screens.
- **Splash**: `SplashOverlayMixin` cancels vanilla from frame 0 and drives the reload itself; bootstrap must use only vanilla `fill/fillGradient` (no text/custom shaders) until `Pipelines.resourcesReady()`. Donor Lottie `welcome.json` is not ported (Skija out of scope); a lightweight welcome alternative is optional.
- **Text input**: never compare Ctrl shortcuts to raw US keycodes only; use `LayoutKeys.matches(input, letter)` or `TextInput`. Vanilla fields are covered by `TextFieldWidgetMixin`; custom fields must use `TextInput`.
- **Custom scoreboard**: `ScoreboardHud`; vanilla sidebar cancelled while enabled.
- **Custom tab**: `PlayerListHudMixin`; multiplayer-only visual acceptance.
- **HitRange (D98)**: triangle-list ribbon at `END_MAIN` via `BlockOverlayPipelines.FLAT`; 72 sides, 3.32 radius, donor thickness/rotation/depth. Never use GL lines; `FLAT_NO_DEPTH` is the separate no-depth variant.
- **CustomHitBox (D98)**: donor entity set (all alive except XP orbs; items behind toggle), 128-block cap, frustum + 120 ms LOS cache, 36 fill/144 outline vertices per entity. Keep fill/outline in one flat pass.
- **Optimization (D101)**: Tier Low/Medium/Ultra; entity culling goes through `OcclusionCuller` from `EntityRenderManager.shouldRender`; render cap 12/8 from `GameOptions.getClampedViewDistance`; particle radius 48/32/22 from `ParticleManager.addParticle(Particle)`; clouds cancel only on Ultra. Do not expose `Limit Render Distance` at Low or donor fast-transparency (removed 1.21.11 getter).
- **Particles (D102)**: textured billboards use `POSITION_TEXTURE_COLOR` + `ParticlesPipelines.BILLBOARD` with `DrawMode.TRIANGLES` (6 vertices per particle). Never use raw `QUADS` — it produced long stray lines. Cube mode uses 12 thick edges on `BlockOverlayPipelines.FLAT`; assets live in `textures/particles/`, Bloom reuses `textures/esp/ghost_bloom.png`.
- **Trajectories (D103)**: 300-step simulation in `TrajectoriesRenderer`; bow power `TrajectoryRules.bowPower`, water/air drag + gravity from `TrajectoryRules.step`. Impact marker is procedural (ring + four prongs), not the donor `hit.png` (raw texture sampling produced an atlas square). Explosive radius is a flat `HitRangeGeometry` ring; bossbar heuristic is not ported.
- **Mixin config**: every `*Mixin.java` under `dev/linkenvisuals/mixin` must be listed in `linkenvisuals.mixins.json`; `MixinRegistrationTest` enforces this. Do not target removed 1.21.11 names such as `ParticleManager.renderParticles`; use `addToBatch`.
- **Pinned cards**: left below Profile/right when Theme closed; style-lock hides `GUI Style`.
- **Media strip** reads `MediaWatcher`, not the title music track; title track belongs to `LinkenMenuMusic`.
- **Close overlay** donor is a no-op; do not invent a new behavior.

## 9. Definition of Done for the next task

- Both `clean build` and `build -Pstrange` green, **265+ tests** (or more) with zero failures/errors/skips.
- No new shader/sound errors in hidden/runClient log.
- Every visible control works; no fake controls.
- Beta/Vanilla behavior unchanged within their flavor; compile-time family lock preserved (no Linken controls in Strange and vice versa).
- New strings in en/ru/uk; `LangParityTest` green.
- Feature/fix commit **+1 patch**, docs commit without bump.
- `git fetch`, commit, push; no force push.
- Jar path, size, SHA-256 and Russian report with what was verified visually/in the log and what remains manual.

## 10. First action

Read the docs and current code, run `test` in both flavors, then continue with the **remaining visual modules** in `FEATURE_PARITY.md` (visual `KillStreak`, full `ViewModel`/Hands). `CameraSettings` is covered by Better Animations; `AspectRatio`/`NoRender` in D96/D97; `HitRange`/`CustomHitBox` in D98; the compile-time Strange/Linken flavor lock is D100; `Optimization` is D101; `Particles` is D102; `Trajectories` is D103. Do not add dead settings; skip `HelperItemsHud` until its donor helper modules exist, and do not add `DynamicIsland` because Link style hides it. Keep HUD drag/snap/edit-mode and the existing style/border pipeline; do not regress title/LinkGUI/pinned/scoreboard/tab/TargetHud/CoordinatesHud/Cooldowns/Armor/Inventory/LowArmor/HitRange/CustomHitBox/Optimization/Particles/Trajectories, the LINKEN splash or the input shortcuts. Live acceptance still needs a real run of **both** jars, RU/UA/AZERTY/QWERTZ, a real multiplayer tab/cooldown run, the HitRange red state with a second player, CustomHitBox LOS/self/items toggles, in-world Optimization culling, all six Particles modes, and Bow/Multishot/pearl trajectories.
