# Feature parity — donor LinkenVisuals 2.0 vs our port

> Snapshot: **v1.0.127**, 2026-10-23. This audit is deliberately explicit: it is
> not a claim that every donor class is present. It records what is ported,
> what is covered by an equivalent implementation, and what remains open.

## Scope

The port targets the donor's **client-side visual/UI/audio/HUD** layer. The
following donor subsystems are intentionally outside the port (D6, D19, D52
and the original master prompt): network/`UserSyncManager`, Crasher, Discord
RPC, IAS/account backend, cosmetics catalog/remote pets, legacy
`WindowGuiRender`/`Render2D`/raw GL, and the Lottie welcome screen.

## Build flavors

- **Default build**: Linken-only — Linken main menu, Link GUI, all 8 Linken HUD styles.
- **`-Pstrange`**: StrangeVisuals-only — Vanilla/Beta menu (default Vanilla), Classic/Beta ClickGUI (default Classic), legacy Strange HUD.
- Both are compile-time locks: saved configs, hotkeys and editor buttons cannot enable the other family. See `StyleFlavor` / `BuildFlavor`.

## Screens and UI

| Area | State | Notes |
|---|---|---|
| Title/main menu | ✅ ported | Donor `CustomMainMenuScreen`: wordmark, 3 rows, account chip, social tiles, media card/footer; D90 donor secondary/social-gradient material and footer spacing; D91 donor `icons2` main-menu icon set; D92 donor media-cover gradient/artist baseline. |
| Splash/loading | ✅ ported | LINKEN scene replaces the Mojang splash immediately (D93); a vanilla-only bootstrap progress runs until shaders/atlases are ready, then the full `linken.` scene. Handwritten Lottie `welcome.json` is not ported (Skija out of scope). |
| LinkGUI | ✅ ported | 1090×830 main window, 3×102 cards, dock, profile, theme/interface, search, configs, friends/waypoints, media strip, pinned cards. |
| LinkGUI Interface settings | ✅ ported + scroll | Scale, transparency, cursor tracker, close animation, corner rounding, accent/tints, **HUD Style/Shape**, **custom scoreboard**, **custom tab**. The section is scrollable via mouse wheel. |
| Pinned cards | ✅ ported | Left below Profile / right when Theme is closed; per-card toggle, unpin, settings, truncation marker. |
| Style lock | ✅ preserved | Linken → Classic/Beta is blocked; `GUI Style` is hidden in Linken settings/pinned cards. |
| Settings hub/subpages | ✅ ported | Language, keybinds, resource packs, skin, graphics, credits where applicable. |
| Multiplayer / Alt Manager / Select World | ✅ ported | Donor server browser 4b: 1000×272 list window, selected/hover rows, favicon/letter tiles, players/ping, search/refresh, donor 460 px Add/Edit/Direct/Delete forms with resource-pack choice (D89); D92 window hatching and CPU-rounded favicons. Backend pinned servers are not ported. Select-world uses vanilla `WorldListWidget` as backend. |
| Pause menu | ✅ ported | Donor-style pause screen. |
| Inventory / Create/Edit World / death | ⚪ vanilla by design | Donor also uses vanilla for these paths. |

## HUD

| Donor element | Our state |
|---|---|
| Watermark | ✅ `Watermark` |
| KeyBindsHud | ✅ `KeybindPanel` |
| Media/Dynamic Island media | ◐ media strip + `MediaPlayerHud`; full Dynamic Island not ported |
| TotemCounter / UseTracker | ✅ our equivalents |
| PotionHud / status effects | ✅ `PotionSlot`/`PotionBlock` |
| ScoreboardHud | ✅ **ported** (`ScoreboardHud`, vanilla sidebar cancelled while enabled) |
| TargetHud | ✅ **ported** (`TargetHud`, D87): donor 104×30 card + armour/offhand row, crosshair/aim-cone + LOS pick with 1 s hold, head/HP/absorption/durability/hit flash, `Target HUD` setting |
| CoordinatesHud | ✅ **ported** (`CoordinatesHud`, D88): 17 px card, `iconsnew` compass, integer XYZ, two-decimal BPS, animated width/fade, `Coordinates HUD` setting (default off) |
| CooldownsHud | ✅ **ported** (`CooldownsHud`, `CooldownTracker`, D95): reflection over 1.21.11 cooldown entries + inventory fallback, preview, item/name/time rows |
| ArmorHud / InventoryHud | ✅ **ported** D95 + D107: Armor V1 100×39 card with durability %; Armor V2 two hotbar-relative 49 px groups (boots/leggings left, chest/helmet right) selectable via `Armor HUD Mode`; Inventory 27/36 slots with optional hotbar row |
| HelperItemsHud / LowArmorNotificationsHud | ✅ LowArmor D95 (50/25 thresholds, 3.5 s, preview, en/ru/uk); ⛔ HelperItems blocked on absent donor Ft/Hw/Rw helper modules |
| CustomVitals | ✅ **ported** D107: hotbar-anchored 182 px bars, Segments/Stack/Bars, armor row, air, mount HP/jump, XP/level, damage flash; vanilla status/exp/jump layers cancelled |
| DynamicIsland (full) | ⛔ intentionally not ported: donor hides it when Link style is active; `MediaPlayerHud` + `LyricsHud` cover media/lyrics |
| Custom Hotbar | ✅ `HotbarHud` |
| HUD Style (8 variants) | ✅ all donor variants ported (D85); donor border smoothness ported (D86) |
| HUD Shape (Rounded/Pill/Sharp) | ✅ implemented centrally in `HudStyle`; geometry test |
| HUD element enable/disable list | ◐ our edit mode supports layout/drag; donor per-element toggles are not fully exposed |

## Modules

### Ported or covered by equivalent implementations

`BlockOverlay`, `Crosshair`, `Fullbright`, `JumpCircles`, `MotionBlur`,
`TargetEsp`, `TimeChanger`, `Trails`, `Zoom`, `Freelook`, `HitColor`,
`HitSound`, `SwingAnimation`, `ItemPhysics`, `NameTags` (donor
SelfNametags), `KillEffects`, `Hands`/`HandPose`, `ViewModel` (manual
offsets/scale, D105), `Shader Sky` (donor
SkyChanger), `ChatHelper`, `FastExp`, `ItemScroller`, `MineHelper`,
`StreamerMode`, `TotemTracker`, `UseTracker`, `Waypoints`, `ElytraSwap`,
`HandSwap`, `Sounds`, `HitRange`, `CustomHitBox`, `Optimization`, `Particles`,
`Trajectories`, `KillStreak` (visual card D104), `ViewModel` manual offsets
(D105), `Hand Motion` Punchy-lite subset (D106), `CustomVitals`/Armor V2 (D107),
`ItemRadius` (D108; splash proximity labels D112), `DamageParticles`/`FortniteDamage` (D109),
`SelfNametags` toggle (D110), Interface palette/fade/blur/background/text (D111/D113),
`HelperItemsHud`/Ft/Hw/Rw helpers (D114), cosmetics/pets subset,
filters/ambience subset.

### Donor modules not ported (visual/client, still open)

`DynamicIsland`,
`ViewModel` animated Punchy mode (the twelve manual donor
offsets/scale settings are ported in D105; D106 adds only a lightweight
Hand Motion subset), remaining `UI` BoostFPS control.

Covered: `CameraSettings` is implemented by `Better Animations` →
`Animations.perspective`; `AspectRatio` is ported in D96; `NoRender` is ported in D97 and its
bossbar/fire/particle mixins were registered in D99 (8 donor switches, all applied);
`HitRange` and `CustomHitBox` are ported in D98 (world-render triangle/box passes, geometry tests).

### Intentionally excluded donor modules

Crasher (`Crasher*`), network/`RemotePets`/`UserSyncManager`, Discord RPC,
IAS, server helpers (`AutoBuy`, `AutoDuel`, `AutoRelist`, `AutoResell`,
`AuctionHelper`, `PartyModule`, etc.), and the cosmetics catalog. These are
not silently faked; no dead controls are exposed for them.

## Audio

- 35 donor events and all OGGs are present; `SoundAssetsTest` is green.
- Autotest reports no `Unable to play unknown soundEvent`.
- Mod source has no `SoundEvents.*` usage.
- Audible acceptance of every cue/HitSound mode is still a human task.

## Verification used for this snapshot

- `clean build`: **304 test / 0 failures / 0 errors / 0 skips** in both flavors.
- Hidden `runAutotest`: Linken **1136/1136**, Strange **1126/1126**, no new
  ERROR/exception, no shader
  source errors, no unknown sound events; title screenshot includes the donor
  `icons2` main-menu set. D98 adds clean `hitrange` and `custom-hitbox`
  screenshots at 1920×1080. D99 fixed and registered the bossbar/fire/particle
  NoRender mixins; `MixinRegistrationTest` now guards the mixin config. D100
  adds the compile-time flavor lock; the Strange run shows the vanilla
  `STRANGE VISUALS` title and legacy HUD, while the Linken run keeps the
  Linken menu/HUD. D101 ports Optimization; all four culling mixins apply in
  both hidden runs. D102 adds textured particle billboards and Cube wireframe
  screenshots with clean geometry. D103 adds a clean procedural trajectory
  marker screenshot. D104 adds the KillStreak card preview and a real
  post-attack streak assertion. D105 adds the twelve donor ViewModel offsets
  on top of HandPose, a live `lastAppliedVisible()` assertion and the
  `viewmodel` screenshot. D106 adds the first-open `welcome` scene, the
  compact donor `pause` panel (with real `GameMenuScreen` routing), the
  FakePlayer crit assertion and the `Better Animations -> Hand Motion`
  Punchy-lite subset; D107 adds hotbar-anchored `CustomVitals` and Armor V2
  with `custom-vitals`/`armor-hud-v2` screenshots; D108 adds donor `ItemRadius`
  with the `item-radius` screenshot; D109 adds `DamageParticles`/`FortniteDamage`
  with `damage-number`/`damage-visuals` screenshots; D110 adds the donor
  `SelfNametags` self-label toggle with the `check self nametag` assertion;
  D111 adds Secondary Colour/Theme Fade/GUI Blur with the `linkgui-noblur`
  screenshot; D112 adds the donor splash proximity `NN%` labels; D113 adds
  Custom Theme Colours with the `linkgui-custom-colours` screenshot; D114 adds
  HelperItemsHud/Ft/Hw/Rw with the `helper-items` screenshot. A separate
  frame-capture 1280×720 confirms the LINKEN splash replaces the Mojang logo
  from the first frame.
- Text shortcuts: layout-aware `Ctrl+A/C/V/X/Z/Y`, word deletion and caret
  movement are covered by `TextInputTest`/`LayoutKeysTest`; live RU/UA/AZERTY/
  QWERTZ acceptance remains a human check.
- Screenshots at 1280×720 and 1920×1080 for title, LinkGUI, pinned cards,
  custom scoreboard and all eight HUD styles; TargetHUD/CoordinatesHud
  self-previews and the donor server browser + Add modal were captured at
  1920×1080. Custom-tab path renders/injects without mixin errors, but a
  singleplayer list does not show entries, so its visual check needs a
  multiplayer server.
- Beta/Classic GUI shells were opened/closed by autotest; style lock remains.

## Bottom line

The **LinkGUI + title + welcome + settings + HUD material/shape + TargetHud +
CoordinatesHud + CustomVitals + Armor V2 + donor server browser + pinned cards +
custom scoreboard/tab + HitRange + CustomHitBox + Optimization + Particles +
Trajectories + KillStreak + ViewModel + compact LINKEN pause + CustomVitals/Armor V2 +
ItemRadius (with splash labels) + DamageParticles/FortniteDamage + SelfNametags + UI palette/fade/blur/background/text + HelperItemsHud/Ft/Hw/Rw + Strange/Linken build flavors** slice is now in place. Full donor parity is **not** closed: the remaining visual/HUD modules above and the excluded
network/crasher/catalog subsystems are explicit open scope. The next parity
slice should be chosen from that list rather than adding fake controls.
