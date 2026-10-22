# MASTER PROMPT — LinkenVisuals: continue from v1.0.115 / D92

You are the lead engineer continuing the existing LinkenVisuals port. Do not restart the project. Work autonomously, preserve the existing visual/audio decisions, and finish with a green build, tests, docs, commit/push and a Russian report.

## 1. Repository

- Repository: `https://github.com/KurumaOfficial/LinkenVisuals`
- Branch: `main`
- Version at handoff: **1.0.115**
- Tests at handoff: **223 / 0 failures / 0 errors / 0 skipped**
- Autotest at handoff: **697/697**, no new ERROR/exception
- Jar at handoff: `source/build/libs/linkenvisuals-1.0.115.jar`
- Size: **10 743 565 bytes**
- SHA-256: `41553852CAF77D13BF3EE824890BB7C325D100815A696F8947BFCB46CEC35251`
- HEAD: use `git log -1` after clone; the latest docs commit describes D92.

Clone and verify:

```powershell
git clone https://github.com/KurumaOfficial/LinkenVisuals.git
cd LinkenVisuals
git checkout main
git pull
git log -1
Get-Content source/gradle.properties   # mod_version=1.0.115
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

- `docs/HANDOFF.md` — top section (chat #46, D92);
- `docs/MERGE_DECISIONS.md` — D80–D92;
- `docs/ROADMAP.md` — M8.10–M8.18c;
- `docs/PORT_STATUS.md` — M8.14–M8.18c;
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
- Title/main menu, loading/splash, LinkGUI, settings hub/subpages, multiplayer, Alt Manager, Select World, pause, HUD editor, pinned cards, friends/waypoints, configs, media, style lock are in place.
- Shader-source race: no `Couldn't find source` / `Couldn't compile` in the final autotest.

Do not redo these from scratch. Use the docs and current code as the baseline.

## 5. What remains — next code slices

Use `docs/FEATURE_PARITY.md` as the source of truth. Recommended order:

1. **HUD elements**, port donor behavior/geometry one by one:
   - `CooldownsHud`
   - `ArmorHud`
   - `InventoryHud`
   - `HelperItemsHud`
   - `LowArmorNotificationsHud`
   - `DynamicIsland` (only after the simpler elements)
   Donor sources: `hud/impl/*.java`, `hud/HudElement.java`, `hud/LinkHudStyle.java`, `managers/system/HudManager.java`.
   Our HUD architecture: `hud/HudElement.java`, `hud/HudManager.java`, `hud/HudStyle.java`, `hud/HudSurface.java`, layout persistence in `hud/HudLayoutStorage.java`.
   Add each element to `HudManager.ELEMENTS`, preserve edit-mode drag/snap, add a working enable/disable setting if needed, and add an autotest screenshot/behavior step.

2. **Remaining visual modules** from `FEATURE_PARITY.md`:
   - `CameraSettings`, `AspectRatio`, `CustomHitBox`, `CustomVitals`, `DamageParticles`, `FortniteDamage`, `HitRange`, `ItemRadius`, `NoRender`, `Optimization`, `Particles`, `Trajectories`, visual `KillStreak`, full `ViewModel`/Hands.
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

Expected baseline: **223 tests, 0 failures/errors/skips**.

Hidden/automated smoke test (requires explicit user permission):

```powershell
# run/options.txt: fullscreen:false, overrideWidth:1920, overrideHeight:1080
.\gradlew.bat runAutotest -PautotestWorld=AutoTestWorld -PautotestQuit --console=plain --no-daemon
```

- Autotest opens both GUIs, LinkGUI, title, inventory, attacks mobs, sweeps every module/setting, takes screenshots and writes a report.
- Check `run/logs/latest.log` and `run/logs/linkenvisuals-autotest-*.log`.
- Expected at handoff: **697/697**, no new ERROR/exception, no `Couldn't find source`/`Couldn't compile`, no unknown sound events. `pauseOnLostFocus` must be `false` for an unfocused/minimized hidden run; restore `options.txt` afterwards.
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
  ui/linkmenu/LinkenMenuChrome.java
  ui/linkmenu/LinkenMenuLayout.java
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
  audio/SoundAssetsTest.java
```

## 8. Current important decisions

- **Linken style lock**: `ModuleRegistry.isGuiStyleLocked`, `cycleGuiStyle`, `setGuiStyle`; `GUI Style` hidden in Linken Interface/pinned cards.
- **Shader readiness**: `Pipelines.resourcesReady` set after `ResourceReloaderKeys.Client.SHADERS` + tick fallback; custom splash/loading/title gated.
- **HUD style**: all variants use `HudStyle`; donor border smoothness uses `RoundedRectPipeline.drawBorder`.
- **TargetHud**: `TargetHud` in `HudManager.ELEMENTS`; `ModuleRegistry.targetHud()` gates rendering, edit/chat shows self-preview; favours crosshair over aim-cone and holds the last target for 1 s.
- **CoordinatesHud**: `CoordinatesHud` in `HudManager.ELEMENTS`; `ModuleRegistry.coordinatesHud()` (default off) gates rendering, edit/chat shows self-preview; BPS = horizontal velocity ×20 (1.21.11 has no public `prevX/prevZ`).
- **HUD animation**: `HudTween` is the shared time-based linear/ease-out helper; do not duplicate per-element tween classes.
- **Server browser**: `LinkenMultiplayerScreen` mirrors donor 4b list geometry (1000×272, topbar 68/row 64/footer 76); Add/Edit/Direct/Delete use a 460 px donor-style modal with resource-pack policy; window hatching and CPU-rounded favicons. Backend pinned servers are not ported.
- **Main-menu icons**: use `LinkenFonts.icons2()` / `MENU_ICON_*`; do not swap the global `ICON_*` constants used by ClickGUI and auxiliary screens.
- **Custom scoreboard**: `ScoreboardHud`; vanilla sidebar cancelled while enabled.
- **Custom tab**: `PlayerListHudMixin`; multiplayer-only visual acceptance.
- **Pinned cards**: left below Profile/right when Theme closed; style-lock hides `GUI Style`.
- **Media strip** reads `MediaWatcher`, not the title music track; title track belongs to `LinkenMenuMusic`.
- **Close overlay** donor is a no-op; do not invent a new behavior.

## 9. Definition of Done for the next task

- `clean build` green, **223+ tests** (or more) with zero failures/errors/skips.
- No new shader/sound errors in hidden/runClient log.
- Every visible control works; no fake controls.
- Beta/Vanilla unchanged; Linken style lock preserved.
- New strings in en/ru/uk; `LangParityTest` green.
- Feature/fix commit **+1 patch**, docs commit without bump.
- `git fetch`, commit, push; no force push.
- Jar path, size, SHA-256 and Russian report with what was verified visually/in the log and what remains manual.

## 10. First action

Read the docs and current code, run `test`, then continue the **pixel-diff pass on the menu**: compare `LinkenMainMenuScreen` and `LinkenMultiplayerScreen` against the donor 1280×720/1920×1080 screenshots, fix remaining hover/press/server-row differences, and only then move to **CooldownsHud**. Keep the existing HUD style/border pipeline and do not regress title/LinkGUI/pinned/scoreboard/tab/TargetHud/CoordinatesHud.
