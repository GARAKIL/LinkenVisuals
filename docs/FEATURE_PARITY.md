# Feature parity — donor LinkenVisuals 2.0 vs our port

> Snapshot: **v1.0.113**, 2026-10-22. This audit is deliberately explicit: it is
> not a claim that every donor class is present. It records what is ported,
> what is covered by an equivalent implementation, and what remains open.

## Scope

The port targets the donor's **client-side visual/UI/audio/HUD** layer. The
following donor subsystems are intentionally outside the port (D6, D19, D52
and the original master prompt): network/`UserSyncManager`, Crasher, Discord
RPC, IAS/account backend, cosmetics catalog/remote pets, legacy
`WindowGuiRender`/`Render2D`/raw GL, and the Lottie welcome screen.

## Screens and UI

| Area | State | Notes |
|---|---|---|
| Title/main menu | ✅ ported | Donor `CustomMainMenuScreen`: wordmark, 3 rows, account chip, social tiles, media card/footer; D90 donor secondary/social-gradient material and footer spacing. |
| Splash/loading | ✅ ported | LINKEN scene + deterministic fallback to vanilla until shaders are ready. |
| LinkGUI | ✅ ported | 1090×830 main window, 3×102 cards, dock, profile, theme/interface, search, configs, friends/waypoints, media strip, pinned cards. |
| LinkGUI Interface settings | ✅ ported + scroll | Scale, transparency, cursor tracker, close animation, corner rounding, accent/tints, **HUD Style/Shape**, **custom scoreboard**, **custom tab**. The section is scrollable via mouse wheel. |
| Pinned cards | ✅ ported | Left below Profile / right when Theme is closed; per-card toggle, unpin, settings, truncation marker. |
| Style lock | ✅ preserved | Linken → Classic/Beta is blocked; `GUI Style` is hidden in Linken settings/pinned cards. |
| Settings hub/subpages | ✅ ported | Language, keybinds, resource packs, skin, graphics, credits where applicable. |
| Multiplayer / Alt Manager / Select World | ✅ ported | Donor server browser 4b: 1000×272 list window, selected/hover rows, favicon/letter tiles, players/ping, search/refresh, donor 460 px Add/Edit/Direct/Delete forms with resource-pack choice (D89). Backend pinned servers are not ported. Select-world uses vanilla `WorldListWidget` as backend. |
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
| CooldownsHud | ❌ open |
| ArmorHud / InventoryHud | ❌ open |
| HelperItemsHud / LowArmorNotificationsHud | ❌ open |
| DynamicIsland (full) | ❌ open |
| Custom Hotbar | ✅ `HotbarHud` |
| HUD Style (8 variants) | ✅ all donor variants ported (D85); donor border smoothness ported (D86) |
| HUD Shape (Rounded/Pill/Sharp) | ✅ implemented centrally in `HudStyle`; geometry test |
| HUD element enable/disable list | ◐ our edit mode supports layout/drag; donor per-element toggles are not fully exposed |

## Modules

### Ported or covered by equivalent implementations

`BlockOverlay`, `Crosshair`, `Fullbright`, `JumpCircles`, `MotionBlur`,
`TargetEsp`, `TimeChanger`, `Trails`, `Zoom`, `Freelook`, `HitColor`,
`HitSound`, `SwingAnimation`, `ItemPhysics`, `NameTags` (donor
SelfNametags), `KillEffects`, `Hands`/`HandPose`, `Shader Sky` (donor
SkyChanger), `ChatHelper`, `FastExp`, `ItemScroller`, `MineHelper`,
`StreamerMode`, `TotemTracker`, `UseTracker`, `Waypoints`, `ElytraSwap`,
`HandSwap`, `Sounds`, cosmetics/pets subset, filters/ambience subset.

### Donor modules not ported (visual/client, still open)

`CooldownsHud`, `ArmorHud`,
`InventoryHud`, `HelperItemsHud`, `LowArmorNotificationsHud`,
`DynamicIsland`, `CameraSettings`, `AspectRatio`, `CustomHitBox`,
`CustomVitals`, `DamageParticles`, `FortniteDamage`, `HitRange`,
`ItemRadius`, `NoRender`, `Optimization`, `Particles`, `Trajectories`,
`KillStreak` (visual module; sound streak is implemented), `ViewModel`
(only HandPose subset), `SelfNametags` (our NameTags is not a line-by-line
port), full `UI` colour/fade/blur/BoostFPS controls.

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

- `clean build`: **223 tests / 0 failures / 0 errors / 0 skips**.
- Hidden `runAutotest` (RU profile): **697/697 steps**, no new ERROR/exception,
  no shader source errors, no unknown sound events.
- Screenshots at 1280×720 and 1920×1080 for title, LinkGUI, pinned cards,
  custom scoreboard and all eight HUD styles; TargetHUD/CoordinatesHud
  self-previews and the donor server browser + Add modal were captured at
  1920×1080. Custom-tab path renders/injects without mixin errors, but a
  singleplayer list does not show entries, so its visual check needs a
  multiplayer server.
- Beta/Classic GUI shells were opened/closed by autotest; style lock remains.

## Bottom line

The **LinkGUI + title + settings + HUD material/shape + TargetHud +
CoordinatesHud + donor server browser + pinned cards + custom scoreboard/tab**
slice is now in place. Full donor parity is **not** closed: the remaining visual/HUD modules above and the excluded
network/crasher/catalog subsystems are explicit open scope. The next parity
slice should be chosen from that list rather than adding fake controls.
