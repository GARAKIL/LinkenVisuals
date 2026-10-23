# MASTER PROMPT - LinkenVisuals: continue from v1.0.128 / D105

You are the lead engineer continuing the existing LinkenVisuals port. Do not restart the project. Work autonomously, preserve accepted visual/audio/HUD decisions, and finish with green builds, tests, autotest, docs, commit/push and a Russian report.

## 1. Repository

- Repository: `https://github.com/KurumaOfficial/LinkenVisuals`
- Branch: `main`
- Version: **1.0.128**
- Last code commit: `aa6e107` `feat(hands): port donor ViewModel offsets (v1.0.128, D105)`
- Latest docs commit: check `git log -1` after clone; it records D105.
- Tests: **270 / 0 failures / 0 errors / 0 skipped** in both flavors.
- Autotest: Linken **938/938**, Strange **928/928**, 0 fail/skip, 1920×1080. Strange log has 3 pre-existing offline-auth/Realms ERROR lines; no mod/shader/sound errors.
- Jars (not committed; rebuild locally):
  - `source/build/libs/linkenvisuals-1.0.128.jar` - 11 034 570 bytes, SHA-256 `37E19B641CBFD7F488D92874CDE596B34054A472C86170A9BB92C28F50FCE710`
  - `source/build/libs/linkenvisuals-strange-1.0.128.jar` - 11 034 567 bytes, SHA-256 `4277ED2DA154083FB40C12AD4985DA91ED3083A6ADBF4B3A8F2C1D3673216173`

Clone/verify:

```powershell
git clone https://github.com/KurumaOfficial/LinkenVisuals.git
cd LinkenVisuals
git checkout main
git pull
git log -2 --oneline
Get-Content source/gradle.properties   # mod_version=1.0.128
```

## 2. Donor

Donor is **READ-ONLY**. On this machine the full decompiled donor is:

`C:\Users\gameg\Desktop\визуалы` (package `ru.method.linkenvisuals`)

The historical path `C:\StrangeVisuals\donor2\visualy` does not exist here. If the desktop path is missing on a new machine, ask the user; never invent assets, dimensions or behavior.

The actually used LINKEN GUI is the monolithic `screen/linkgui/LinkGuiScreen.java`. Do not use `screen/linkgui/windows/*` as a reference.

## 3. Done in D105 (do not redo)

- `ViewModelModule` (`viewmodel`, Visual, default OFF) ports the donor manual ViewModel: `Main/Off X/Y/Z Offset` (-2..2, step 0.05, 0) and `Main/Off X/Y/Z Scale` (0..3, step 0.05, 1).
- `HeldItemRendererMixin` applies the delta at the donor anchor: `renderFirstPersonItem` -> `INVOKE renderItem(LivingEntity, ItemStack, ItemDisplayContext, MatrixStack, OrderedRenderCommandQueue, I)`, `require = 2` (normal + use-action). It runs after vanilla equip/swing and after `HandPose`, before the item-display transform.
- Donor sign rules: main-hand X inverted, off-hand X not, left-handed mirror; while eating/drinking X/Z offsets are dropped, Y and scale remain. HandEditor active -> ViewModel delta is skipped so accepted drag handles stay accurate.
- `Modules.init()` moved before `ConfigManager.load()`: object modules (D95-D105) now persist enabled state/settings. This also fixes the latent persistence bug for earlier object modules.
- `ViewModelModuleTest` (5) covers neutral defaults, main/off signs, left-hand mirror, consumable suppression, diagnostics.
- Autotest adds enable/pose/wait/check/screenshot/restore and asserts `ViewModelModule.lastAppliedVisible()` after a real first-person frame.
- The donor `Animated`/Punchy mode is **not** ported (Punchy absent); no dead toggle is exposed.

## 4. Next task priority

1. **Screen-anchored HUD layer** - unblocks two explicit open items:
   - `ArmorHud` V2 (hotbar-relative two groups);
   - donor `CustomVitals` (HP/food/XP bars above the hotbar, donor `hud/CustomVitalsBar.java`, 246 lines, styles Bars/Stack/Segments, armor row, air, mount, poison/wither/hunger).
   Current `HudElement` is center/fraction-based. Add a screen/hotbar-anchored layer without breaking `HudManager` drag/snap/edit-mode or the 8 `HudStyle` variants.
2. If that layer is too large for one slice, implement `CustomVitals` as a direct `InGameHudMixin` overlay (donor order: before DrawContext flush), not as a `HudElement`; add a real setting and autotest screenshot. Do not add an empty HUD element for it.
3. Then `ItemRadius` (donor 333 lines: ender eye/sugar circles, netherite scrap box, dried-kelp staircase wall, snowball projection, splash-potion impact simulation, proximity color/percent labels) or `DamageParticles` (legacy impact + decorative feature; inspect its helper classes first).
4. Remaining open parity: `FortniteDamage`, `SelfNametags` line-by-line, full UI colour/fade/blur/BoostFPS. `HelperItemsHud` stays blocked until donor `FtHelper/HwHelper/RwHelper` exist. `DynamicIsland` stays skipped because Link style hides it and media is covered.
5. Human acceptance items from D104 still stand: live KillStreak 2-5 kills, RU/UA/AZERTY/QWERTZ, live cooldown ticks, low-armor in combat, multiplayer custom tab, both flavor jars, ViewModel with food/potions/left hand.

## 5. Architecture that must not break

- HUD: `hud/HudElement.java` (center/fraction), `hud/HudManager.java` (`ELEMENTS`, drag/snap/edit-mode), `hud/HudStyle.java`/`HudSurface.java` (8 donor styles/3 shapes), `hud/HudLayoutStorage.java`, `hud/HudTween.java`. New HUD element -> `HudManager.ELEMENTS`, real `BooleanSetting` in Interface, preview/check/screenshot, default position non-overlapping at GUI scale 3.
- World render: `END_MAIN` + `BlockOverlayPipelines.FLAT` / `FLAT_NO_DEPTH`, byte-packed `POSITION_COLOR`; do not leave RenderSystem state/transforms damaged; validate with a hidden 1920×1080 screenshot.
- Mixins: every `*Mixin.java` must be listed in `linkenvisuals.mixins.json`; `MixinRegistrationTest` enforces it. No removed 1.21.11 names (`ParticleManager.renderParticles`).
- Flavors: default build = Linken-only; `-Pstrange` = Strange-only. `StyleFlavor` owns allowed styles; configs are sanitized. Never add cross-family style buttons.
- Linken style lock and absence of Style/Editor buttons in LINKEN Settings must stay.
- New visible strings go to en/ru/uk; `LangParityTest` must stay green. Module/setting labels use the existing raw-English convention; do not add half-localized controls.
- No Lombok, donor Module/Setting, legacy Render2D/WindowGuiRender, raw GL, Lottie welcome, network/UserSyncManager, cosmetics catalog, crashers, Discord RPC, IAS. No empty catches/TODO/FIXME/System.out.
- Every visible control must work. No dead controls.

## 6. Build and verification

```powershell
cd source
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'   # local JDK 21
.\gradlew.bat test --console=plain --no-daemon
.\gradlew.bat clean build --console=plain --no-daemon

.\gradlew.bat test -Pstrange --console=plain --no-daemon
.\gradlew.bat build -Pstrange --console=plain --no-daemon
```

Expected baseline: **270+ tests**, 0 failures/errors/skips in both flavors.

Hidden autotest (requires explicit user permission):

```powershell
.\gradlew.bat runAutotest -PautotestWorld=AutoTestWorld -PautotestQuit --console=plain --no-daemon
.\gradlew.bat runAutotest -Pstrange -PautotestWorld=AutoTestWorld -PautotestQuit --console=plain --no-daemon
```

**Options workaround (important):** a full `run/options.txt` with `key_key.*` lines currently trips `OptionsKeyLwjgl3Fix` (NumberFormatException on e.g. `key.keyboard.g`), the game drops the file and resets `pauseOnLostFocus` to true. Before a hidden run, replace `run/options.txt` with the minimal file below; after the run restore the original. This is a dev-run/options issue, not a mod mixin issue.

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

Run the game minimized (or on a separate Windows desktop); do not leave it on the active desktop. Check `run/logs/latest.log` and `run/logs/linkenvisuals-autotest-*.log`. Expected only offline-auth/Realms errors; no `Couldn't find source`/`Couldn't compile`, no mixin apply/injection errors, no unknown sound events.

Jar hashes:

```powershell
Get-ChildItem build\libs\linkenvisuals-*.jar |
  ForEach-Object { "$($_.Name) | $($_.Length) | $((Get-FileHash $_.FullName -Algorithm SHA256).Hash)" }
```

## 7. Key files

```text
source/src/main/java/dev/linkenvisuals/
  module/impl/ViewModelModule.java      # D105 12 donor sliders + resolve/apply
  mixin/HeldItemRendererMixin.java      # HandPose HEAD + ViewModel renderItem anchor
  module/Modules.java                   # viewModel() accessor
  LinkenVisuals.java                    # Modules.init() before ConfigManager.load()
  autotest/AutoTestDriver.java          # viewmodel steps + screenshot
  hud/HudManager.java, HudElement.java, HudStyle.java, HudSurface.java
  hud/ArmorHud.java, CooldownsHud.java, InventoryHud.java
  hud/CustomVitals*                     # does not exist yet; donor is hud/CustomVitalsBar.java
  render/Pipelines.java, BlockOverlayPipelines.java
  module/impl/ItemRadiusModule.java     # does not exist yet
  util/optimization/*, util/particles/*, util/trajectories/*
source/src/test/java/dev/linkenvisuals/
  module/impl/ViewModelModuleTest.java
  hud/*Test.java, render/*GeometryTest.java, MixinRegistrationTest.java
```

## 8. Known traps

- Do not re-add the full `run/options.txt` key lines before a hidden run; use the minimal file above.
- ViewModel must stay at the `renderItem` anchor; moving it to HEAD changes axes relative to vanilla equip/swing.
- Do not apply ViewModel while `HandEditor.active()`; otherwise drag handles no longer match the rendered hand.
- Object modules are now registered before config load. Do not move `Modules.init()` back after `ConfigManager.load()`, or their state stops persisting again.
- `HandPose` persists separately in `linkenvisuals_hands.json`; ViewModel persists in the normal module profile. Do not merge the stores without a migration.
- Particles billboards: raw pass only `DrawMode.TRIANGLES` (6 vertices), never `QUADS`.
- Trajectory marker is procedural; do not restore donor `hit.png` raw sampling.
- HUD defaults must not overlap at GUI scale 3; add autotest preview/check/screenshot.
- Beta/Vanilla behavior and the compile-time family lock must not change.

## 9. Definition of Done

- Both `clean build` and `build -Pstrange` green, 270+ tests, 0 fail/error/skip.
- Hidden autotest in both flavors with no new mod/shader/sound/mixin errors (only offline-auth/Realms).
- New functionality really works; no dead controls.
- New strings in en/ru/uk; `LangParityTest` green.
- Jar path/size/SHA-256 recalculated locally; Russian report with visual/log evidence and manual checks.
- Code commit (+1 patch) then docs commit, `git fetch` before commit, push to main without force.

## 10. First action

Read `docs/HANDOFF.md` top, `docs/MERGE_DECISIONS.md` D95-D105, `docs/FEATURE_PARITY.md`, then inspect donor `hud/CustomVitalsBar.java` and `modules/impl/render/CustomVitals.java`. Decide whether to add the screen-anchored HUD layer (preferred, also unblocks Armor V2) or implement CustomVitals as a direct InGameHud overlay. Run baseline tests in both flavors before editing. Do not add dead settings.
