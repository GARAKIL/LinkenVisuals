# NEXT CHAT вЂ” LinkenVisuals: РїРѕР»РЅС‹Р№ РїРµСЂРµРЅРѕСЃ Linken GUI 1:1

## РљРѕРЅС‚РµРєСЃС‚
- Р РµРїРѕР·РёС‚РѕСЂРёР№: https://github.com/KurumaOfficial/LinkenVisuals
- Р’РµС‚РєР°: `main`
- HEAD РїСЂРё РїРµСЂРµРґР°С‡Рµ: СЃРј. `git log -1` (РїРѕСЃР»РµРґРЅРёР№ docs-РєРѕРјРјРёС‚), РїРѕСЃР»РµРґРЅРёР№ РєРѕРґ: `f76016d`
- Р’РµСЂСЃРёСЏ РјРѕРґР°: **1.0.95**
- РўРµСЃС‚С‹: **207 / 0 РїР°РґРµРЅРёР№**
- РСЃС…РѕРґРЅРёРєРё: `<repo>/source`
- Р”РѕРєСѓРјРµРЅС‚Р°С†РёСЏ: `<repo>/docs`
- Р”РѕРЅРѕСЂ (READ-ONLY РЅР° СЃС‚Р°СЂРѕРј СѓСЃС‚СЂРѕР№СЃС‚РІРµ): `C:\StrangeVisuals\donor2\visualy`
  - РќР° РЅРѕРІРѕРј СѓСЃС‚СЂРѕР№СЃС‚РІРµ РїСѓС‚СЊ РјРѕР¶РµС‚ РѕС‚Р»РёС‡Р°С‚СЊСЃСЏ. Р•СЃР»Рё РґРѕРЅРѕСЂР° РЅРµС‚ вЂ” **Р·Р°РїСЂРѕСЃРё Сѓ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ РїСѓС‚СЊ/РєРѕРїРёСЋ**, РЅРµ РІС‹РґСѓРјС‹РІР°Р№ С„Р°Р№Р»С‹ Рё РЅРµ РїСЂР°РІСЊ РґРѕРЅРѕСЂ.
- РђРІС‚РѕСЂСЃС‚РІРѕ РґРѕРЅРѕСЂСЃРєРёС… Р°СЃСЃРµС‚РѕРІ РїРѕРґС‚РІРµСЂР¶РґРµРЅРѕ РїРѕР»СЊР·РѕРІР°С‚РµР»РµРј (D6), Р»РёС†РµРЅР·РёСЏ РґРѕРЅРѕСЂР° вЂ” ARR.
- Р Р°Р±РѕС‡РµРµ РґРµСЂРµРІРѕ РЅР° РјРѕРјРµРЅС‚ РїРµСЂРµРґР°С‡Рё С‡РёСЃС‚РѕРµ, `origin/main` СЃРёРЅС…СЂРѕРЅРёР·РёСЂРѕРІР°РЅ.

## РџРµСЂРІС‹Рј РґРµР»РѕРј
1. `git clone https://github.com/KurumaOfficial/LinkenVisuals.git`
2. `git checkout main && git pull`
3. РџСЂРѕРІРµСЂСЊ: РїРѕСЃР»РµРґРЅРёР№ РєРѕРјРјРёС‚ вЂ” docs СЃ СЌС‚РёРј С„Р°Р№Р»РѕРј; РїРѕСЃР»РµРґРЅРёР№ РєРѕРґ `f76016d` (`git log --oneline -5`); `source/gradle.properties` в†’ `mod_version=1.0.95`.
4. РџСЂРѕС‡РёС‚Р°Р№:
   - `docs/HANDOFF.md` вЂ” РІРµСЂС…РЅСЏСЏ СЃРµРєС†РёСЏ В«С‡Р°С‚ в„–26 вЂ” donor card scale + media controlsВ» Рё РІСЃРµ M8-СЃРµРєС†РёРё;
   - `docs/MERGE_DECISIONS.md` вЂ” D52вЂ“D72, РѕСЃРѕР±РµРЅРЅРѕ D66вЂ“D72;
   - `docs/ROADMAP.md` вЂ” Р±Р»РѕРє LINKEN, M8.1вЂ“M8.8;
   - `docs/PORT_STATUS.md` вЂ” LINKEN M8;
   - `MASTER_PROMPT_STYLE3.md`, РµСЃР»Рё РѕРЅ РµСЃС‚СЊ Сѓ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ.
5. `cd source`
6. `.\gradlew.bat test --console=plain --no-daemon` в†’ РѕР¶РёРґР°РµРјРѕ 207/0.

## Р“Р»Р°РІРЅР°СЏ С†РµР»СЊ
Р”РѕРІРµСЃС‚Рё LINKEN ClickGUI РґРѕ РІРёР·СѓР°Р»СЊРЅРѕРіРѕ **1:1** СЃ РґРѕРЅРѕСЂСЃРєРёРј `screen/linkgui`: РєРѕРјРїРѕРЅРѕРІРєР°, РїР°РЅРµР»Рё, РєР°СЂС‚РѕС‡РєРё, С€СЂРёС„С‚С‹, РѕС‚СЃС‚СѓРїС‹, СЂР°РґРёСѓСЃС‹, С‚РµРЅРё, hover/active, Р°РЅРёРјР°С†РёРё. РЎРµР№С‡Р°СЃ РїРµСЂРµРЅРµСЃС‘РЅ РІРµСЃСЊ РѕСЃРЅРѕРІРЅРѕР№ РєР°СЂРєР°СЃ, РЅРѕ СЃС‚СЂРѕРіР°СЏ РїРёРєСЃРµР»СЊРЅР°СЏ СЃРІРµСЂРєР° РµС‰С‘ РЅРµ РІС‹РїРѕР»РЅРµРЅР°.

## Р–С‘СЃС‚РєРёРµ С‚СЂРµР±РѕРІР°РЅРёСЏ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ
- РџРѕРєР° Р°РєС‚РёРІРµРЅ **Linken GUI**, РїРµСЂРµРєР»СЋС‡РµРЅРёРµ РЅР° Classic/Beta **Р·Р°РїСЂРµС‰РµРЅРѕ**. РќРµ РІРѕР·РІСЂР°С‰Р°Р№ СЌС‚Сѓ РІРѕР·РјРѕР¶РЅРѕСЃС‚СЊ.
- РљРЅРѕРїРєСѓ СЃРјРµРЅС‹ СЃС‚РёР»РµР№ GUI Рё РєРЅРѕРїРєСѓ СЂРµРґР°РєС‚РѕСЂР° РіР»Р°РІРЅРѕРіРѕ РјРµРЅСЋ РІ **LINKEN Settings РЅРµ РґРѕР±Р°РІР»СЏС‚СЊ**.
- Minecraft СЃРµР№С‡Р°СЃ **РЅРµ Р·Р°РїСѓСЃРєР°С‚СЊ** Р±РµР· РЅРѕРІРѕРіРѕ СЏРІРЅРѕРіРѕ СЂР°Р·СЂРµС€РµРЅРёСЏ: РїРѕР»СЊР·РѕРІР°С‚РµР»СЊ РїСЂРѕСЃРёР» В«Р±РµР· РѕРєРЅР° MinecraftВ». Р Р°Р±РѕС‚Р°Р№ РєРѕРґРѕРј, РєРѕРјРїРёР»СЏС†РёРµР№ Рё С‚РµСЃС‚Р°РјРё.
- Р”Р»СЏ С„РёРЅР°Р»СЊРЅРѕР№ РІРёР·СѓР°Р»СЊРЅРѕР№ РїСЂРёС‘РјРєРё РѕС‚РґРµР»СЊРЅРѕ СЃРїСЂРѕСЃРё СЂР°Р·СЂРµС€РµРЅРёРµ РЅР° `runClient` (1280Г—720 Рё 4K-framebuffer).

## Р§С‚Рѕ СѓР¶Рµ СЃРґРµР»Р°РЅРѕ (M1вЂ“M8.8)
- M1вЂ“M7: LINKEN main menu, splash/loading/connect, Link GUI shell, С‚РµРјС‹/РїРёРЅС‹/РєРѕРЅС„РёРі, pause/settings hub, multiplayer browser, account manager, settings sub-pages, keybinds, language, skin, resource packs.
- M8.1: ThemeWindow, 8 РїР°Р»РёС‚СЂ.
- M8.2: РґРѕРЅРѕСЂСЃРєР°СЏ РєРѕРјРїРѕРЅРѕРІРєР° 1280Г—720Г—1.5 РЅР° 1920Г—1080:
  - main 1080Г—720;
  - dock 540Г—72;
  - Interface 330Г—282;
  - Profile 330Г—360;
  - Theme 360Г—855.
- M8.3: 2D player head, card badges СЃ РёРєРѕРЅРєР°РјРё РєР°С‚РµРіРѕСЂРёР№, media strip.
- M8.4: light-С‚РµРјС‹ LIGHT/FROST, РїРѕР»РЅС‹Р№ token-pass (`LinkGuiTheme`: TEXT/PANEL/ROW/HOVER/ACTIVE/FIELD/TRACK/BACKDROP/ON_DARK).
- M8.5: style lock + Interface/Profile split.
- M8.6: draggable module settings window 330Г—440 (header drag, close X, clamp, scroll).
- M8.7: cursor trail вЂ” 64 С‚РѕС‡РєРё/780 РјСЃ, glow-РѕСЂР±С‹ + СЏРґСЂРѕ.
- M8.8: card scale вЂ” 129, gap 15, header 78, media block 126, strip 94; media prev/play/next С‡РµСЂРµР· СЃРёСЃС‚РµРјРЅС‹Рµ media-РєР»Р°РІРёС€Рё (`java.awt.Robot`).

## РљР»СЋС‡РµРІС‹Рµ С„Р°Р№Р»С‹
```
source/src/main/java/dev/linkenvisuals/clickgui/link/
  LinkGuiScreen.java      вЂ” shell, hit-Р·РѕРЅС‹, РєР°СЂС‚РѕС‡РєРё, РЅР°СЃС‚СЂРѕР№РєРё, РєРѕРЅС„РёРі, media
  LinkGuiLayout.java      вЂ” С‡РёСЃС‚Р°СЏ РіРµРѕРјРµС‚СЂРёСЏ 1920Г—1080
  LinkGuiTheme.java       вЂ” 8 РїР°Р»РёС‚СЂ Рё С‚РѕРєРµРЅС‹, РІРєР»СЋС‡Р°СЏ light
  LinkGuiConfig.java      вЂ” linkenvisuals_linkgui.json (С‚РµРјР°/РїРёРЅС‹)
  LinkGuiCursorTrail.java вЂ” cursor trail
  LinkGuiMediaKeys.java   вЂ” СЃРёСЃС‚РµРјРЅС‹Рµ media-РєР»Р°РІРёС€Рё
source/src/test/java/dev/linkenvisuals/clickgui/link/
  LinkGuiLayoutTest.java
  LinkGuiThemeTest.java
```
Р”РѕРЅРѕСЂСЃРєРёРµ С„Р°Р№Р»С‹ РґР»СЏ СЃРІРµСЂРєРё:
```
screen/linkgui/LinkGuiScreen.java
screen/linkgui/LinkGuiLayout.java
screen/linkgui/LinkGuiChrome.java
screen/linkgui/windows/MainModuleWindow.java
screen/linkgui/windows/CategoryDockWindow.java
screen/linkgui/windows/InterfaceWindow.java
screen/linkgui/windows/ThemeWindow.java
screen/linkgui/components/ModuleCard.java
screen/linkgui/components/MusicPlayerStrip.java
screen/linkgui/theme/*
```

## Р§С‚Рѕ РѕСЃС‚Р°Р»РѕСЃСЊ (РїСЂРёРѕСЂРёС‚РµС‚)
1. **РЎС‚СЂРѕРіРёР№ 1:1**
   - РЎРІРµСЂСЊ РЅР°С€Рё РєРѕРЅСЃС‚Р°РЅС‚С‹/РѕС‚СЃС‚СѓРїС‹/С€СЂРёС„С‚С‹/Р°Р»СЊС„С‹/СЂР°РґРёСѓСЃС‹/Р°РЅРёРјР°С†РёРё СЃ РґРѕРЅРѕСЂРѕРј.
   - РўРѕС‡РєРё: main header (player head, search, close), card badge/name/description/bind/toggle/pin,
     hover lift/enter animation/stripe fill, dock indicator slide, Interface/Profile/Theme spacing,
     media strip.
   - РќРµ РєРѕРїРёСЂСѓР№ Lombok, РґРѕРЅРѕСЂСЃРєРёРµ Module/Setting, legacy `Render2D`/`WindowGuiRender`, Gif/Lottie,
     СЃРµС‚СЊ, РєРѕСЃРјРµС‚РёРєСѓ, Р°СѓРґРёРѕ. РСЃРїРѕР»СЊР·СѓР№ РЅР°С€Рё `LinkenMenuChrome`, `LinkenFonts`, `Spring`,
     `MsdfText`, `RoundedRectPipeline`, `GlowPipeline`, `CirclePipeline`, `PanelPipeline`.
   - РџСЂРё РёР·РјРµРЅРµРЅРёРё layout РѕР±РЅРѕРІР»СЏР№ `LinkGuiLayoutTest`; РґРѕР±Р°РІР»СЏР№ С‚РµСЃС‚С‹ РЅР° РіРµРѕРјРµС‚СЂРёСЋ.
2. **Volume / system volume**
   - Media prev/play/next СѓР¶Рµ СЂР°Р±РѕС‚Р°СЋС‚ С‡РµСЂРµР· `LinkGuiMediaKeys` (`java.awt.Robot`).
   - Р“СЂРѕРјРєРѕСЃС‚СЊ РЅРµ РїРѕСЂС‚РёСЂРѕРІР°РЅР°. Р”РѕР±Р°РІР»СЏР№ С‚РѕР»СЊРєРѕ СЂРµР°Р»СЊРЅРѕ СЂР°Р±РѕС‚Р°СЋС‰РёР№ РєРѕРЅС‚СЂРѕР»; Р±РµР· РґРµРєРѕСЂР°С‚РёРІРЅС‹С… РјС‘СЂС‚РІС‹С… РєРЅРѕРїРѕРє.
   - Р•СЃР»Рё system volume РЅРµР»СЊР·СЏ СЃРґРµР»Р°С‚СЊ Р±РµР· РЅРѕРІРѕР№ Р·Р°РІРёСЃРёРјРѕСЃС‚Рё вЂ” Р·Р°С„РёРєСЃРёСЂСѓР№ РѕС‚РєР»РѕРЅРµРЅРёРµ РІ D-СЂРµС€РµРЅРёРё.
3. **Close overlay**
   - Р’ РґРѕРЅРѕСЂРµ `LinkGuiCloseOverlay` вЂ” РїСѓСЃС‚РѕР№ no-op СЃС‚Р°Р±. РќРµ РІС‹РґСѓРјС‹РІР°Р№ РЅРѕРІС‹Р№ РѕРІРµСЂР»РµР№; Р·Р°С„РёРєСЃРёСЂСѓР№ РІ docs.
4. **РџРёРєСЃРµР»СЊРЅР°СЏ СЃРІРµСЂРєР°**
   - Р‘РµР· runClient РјРѕР¶РЅРѕ СЃРІРµСЂСЏС‚СЊ С‚РѕР»СЊРєРѕ РєРѕРґ/РєРѕРЅСЃС‚Р°РЅС‚С‹.
   - Р•СЃР»Рё РїРѕР»СЊР·РѕРІР°С‚РµР»СЊ РґР°СЃС‚ СЃРєСЂРёРЅС€РѕС‚ РґРѕРЅРѕСЂСЃРєРѕРіРѕ GUI вЂ” РёСЃРїРѕР»СЊР·СѓР№ РєР°Рє reference.
   - Р’ РєРѕРЅС†Рµ РїРѕРїСЂРѕСЃРё СЂР°Р·СЂРµС€РµРЅРёРµ РЅР° `runClient` РґР»СЏ 1280Г—720/4K.
5. **M8.9+**
   - Р”РѕРІРѕРґРєР° InterfaceWindow (interface scale / cursor tracker / Apply-Default), РµСЃР»Рё СЌС‚Рѕ РµСЃС‚СЊ РІ РґРѕРЅРѕСЂРµ Рё РЅРµ РєРѕРЅС„Р»РёРєС‚СѓРµС‚.
   - РџСЂРѕРІРµСЂСЊ style lock РІРѕ РІСЃРµС… РїСѓС‚СЏС…: `ModuleRegistry.cycleGuiStyle`, `setGuiStyle`,
     `Ctrl+Left/Right`, СЃС‚СЂРѕРєР° `GUI Style` СЃРєСЂС‹С‚Р° РІ Interface-РїР°РЅРµР»Рё.
   - Beta/Classic РЅРµ РґРѕР»Р¶РЅС‹ РїРµСЂРµРєСЂР°С€РёРІР°С‚СЊСЃСЏ/Р»РѕРјР°С‚СЊСЃСЏ.

## РџСЂР°РІРёР»Р°
- Feature/fix-РєРѕРјРјРёС‚: `+1 patch` РѕС‚ С„Р°РєС‚РёС‡РµСЃРєРѕР№ РІРµСЂСЃРёРё. Docs-only вЂ” Р±РµР· Р±Р°РјРїР°.
- РџРµСЂРµРґ РєРѕРјРјРёС‚РѕРј: `git fetch origin`. Force push Р·Р°РїСЂРµС‰С‘РЅ.
- РќРµ РєРѕРјРјРёС‚РёС‚СЊ `build/`, `run/`, jar, Р»РѕРіРё, СЃРєСЂРёРЅС€РѕС‚С‹, РІСЂРµРјРµРЅРЅС‹Рµ С„Р°Р№Р»С‹.
- РќРµ РґРѕР±Р°РІР»СЏС‚СЊ `TODO/FIXME/System.out`, РїСѓСЃС‚С‹Рµ catch, РїСЂРѕРіР»РѕС‡РµРЅРЅС‹Рµ РёСЃРєР»СЋС‡РµРЅРёСЏ.
  Fallback/РѕС€РёР±РєРё вЂ” С‡РµСЂРµР· `LOGGER.warn/debug`.
- РќРµ С‚Р°С‰РёС‚СЊ РґРѕРЅРѕСЂСЃРєРёРµ: cosmetics, network, Kotlin, Lombok, legacy shaders,
  СЃС‚Р°СЂС‹Р№ Panel GUI, Lottie `welcome.json`, РґРѕРЅРѕСЂСЃРєРѕРµ Р°СѓРґРёРѕ.
- Р’СЃРµ РІРёРґРёРјС‹Рµ РєРЅРѕРїРєРё РґРѕР»Р¶РЅС‹ СЂР°Р±РѕС‚Р°С‚СЊ; РјС‘СЂС‚РІС‹Рµ РєР»РёРєРё Р·Р°РїСЂРµС‰РµРЅС‹.
- i18n: en/ru/uk РґР»СЏ РЅРѕРІС‹С… СЃС‚СЂРѕРє; `LangParityTest` Р·РµР»С‘РЅС‹Р№.
- РџРѕСЃР»Рµ Р·РµР»С‘РЅРѕР№ СЃР±РѕСЂРєРё: commit + push, jar РІ `source/build/libs`, SHA-256, РѕС‚С‡С‘С‚ РЅР° СЂСѓСЃСЃРєРѕРј.
- Р’ РєРѕРЅС†Рµ: РѕР±РЅРѕРІРёС‚СЊ `docs/HANDOFF.md`, `docs/MERGE_DECISIONS.md` (РЅРѕРІРѕРµ D-СЂРµС€РµРЅРёРµ),
  `docs/ROADMAP.md`, `docs/PORT_STATUS.md`; docs-РєРѕРјРјРёС‚ РѕС‚РґРµР»СЊРЅРѕ.

## РљРѕРјР°РЅРґС‹
```powershell
cd source
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot'
.\gradlew.bat clean build --console=plain --no-daemon
.\gradlew.bat runClient --no-daemon   # РўРћР›Р¬РљРћ РїРѕСЃР»Рµ СЏРІРЅРѕРіРѕ СЂР°Р·СЂРµС€РµРЅРёСЏ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ
```

## РўРµРєСѓС‰РёРµ Р°СЂС‚РµС„Р°РєС‚С‹
- Jar: `source/build/libs/linkenvisuals-1.0.99.jar`
- SHA-256: `8E9F08D5FDD5AB41C4507723D7BA9845289A2B0DF81496BC7E18DCFBB4E8E8EE`
- Р Р°Р·РјРµСЂ: 7 371 851 Р±Р°Р№С‚
- HEAD: РїРѕСЃР»РµРґРЅРёР№ docs-РєРѕРјРјРёС‚; РєРѕРґ: `f76016d`
- РџСЂРµРґС‹РґСѓС‰РёРµ M8-РєРѕРјРјРёС‚С‹: `166869a` (v1.0.94), `1d66355` (v1.0.93),
  `5638ef7` (v1.0.92), `fa107f1` (v1.0.91), `ea26c9c` (v1.0.90).

## РР·РІРµСЃС‚РЅС‹Рµ СЂРёСЃРєРё
- РЎС‚СЂРѕРіРёР№ РїРёРєСЃРµР»СЊ-РІ-РїРёРєСЃРµР»СЊ РЅРµ РїРѕРґС‚РІРµСЂР¶РґС‘РЅ Р±РµР· reference/runClient.
- `java.awt.Robot` media-РєР»Р°РІРёС€Рё РјРѕРіСѓС‚ РЅРµ СЂР°Р±РѕС‚Р°С‚СЊ РЅР° РЅРµРєРѕС‚РѕСЂС‹С… Windows/Java вЂ” РѕС€РёР±РєР° РІ debug, Р±РµР· РєСЂР°С€Р°.
- Light-С‚РµРјС‹, cursor trail, draggable settings Рё Interface/Profile РЅРµ РїСЂРѕРІРµСЂРµРЅС‹ РіР»Р°Р·Р°РјРё.
- `LinkGuiScreen.java` Р±РѕР»СЊС€РѕР№; РїСЂР°РІРєРё С‚РѕС‡РµС‡РЅС‹Рµ, РЅРµ Р»РѕРјР°С‚СЊ hit-Р·РѕРЅС‹.
- `LinkGuiThemeWindowLayout` СѓРґР°Р»С‘РЅ РІ D67 вЂ” РЅРµ РІРѕР·РІСЂР°С‰Р°С‚СЊ.
- Р’ `LinkGuiLayout` СѓР¶Рµ РґРѕРЅРѕСЂСЃРєРёРµ 129/15/78/126; РјРµРЅСЏС‚СЊ С‚РѕР»СЊРєРѕ РѕСЃРѕР·РЅР°РЅРЅРѕ Рё СЃ С‚РµСЃС‚Р°РјРё.
- РќР° РЅРѕРІРѕРј СѓСЃС‚СЂРѕР№СЃС‚РІРµ РґРѕРЅРѕСЂР° РјРѕР¶РµС‚ РЅРµ Р±С‹С‚СЊ вЂ” Р±РµР· РЅРµРіРѕ РЅРµ РІС‹РґСѓРјС‹РІР°С‚СЊ Р°СЃСЃРµС‚С‹/СЂР°Р·РјРµСЂС‹, Р·Р°РїСЂРѕСЃРёС‚СЊ Сѓ РїРѕР»СЊР·РѕРІР°С‚РµР»СЏ.

## Р¤РѕСЂРјР°С‚ РїРµСЂРІРѕРіРѕ РѕС‚РІРµС‚Р°
РљСЂР°С‚РєРѕ РїРѕРґС‚РІРµСЂРґРё:
- HEAD/РІРµСЂСЃРёСЋ/С‡РёСЃР»Рѕ С‚РµСЃС‚РѕРІ;
- С‡С‚Рѕ РїСЂРѕС‡РёС‚Р°Р» HANDOFF/D52вЂ“D72/ROADMAP M8;
- РїР»Р°РЅ РїРµСЂРІРѕРіРѕ code-СЃСЂРµР·Р°;
- С‡С‚Рѕ Minecraft РЅРµ Р·Р°РїСѓСЃРєР°РµС‚СЃСЏ Р±РµР· СЂР°Р·СЂРµС€РµРЅРёСЏ.
Р—Р°С‚РµРј РїРµСЂРµС…РѕРґРё Рє СЂР°Р±РѕС‚Рµ.


