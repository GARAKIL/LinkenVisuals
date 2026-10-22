# HANDOFF — эстафета между чатами

> Методология: этот файл обновляется в конце КАЖДОЙ задачи. Следующий чат читает его первым и продолжает с «Следующей задачи». Детали решений — `MERGE_DECISIONS.md`, скоуп — `ROADMAP.md`.

## Состояние на 2026-10-22 (чат №35 — shader warm-up deferral, D81) — v1.0.104

### Сделано
1. **D81/v1.0.104** (`1713548`): `Pipelines.warmUp()` убран из `Fonts.ensure`/`LinkenFonts.ensure`; `LinkenMainMenuScreen.init` больше не прогревает пайплайны; `render` не рисует меню, пока `client.getOverlay() != null`, и вызывает `warmUp` только после снятия splash/resource-overlay. Кандидат-фикс shader-source race; в runClient **не проверен**.
2. **Strict 1:1**: зафиксированы отклонения — pinned settings cards (донорский `drawPinnedCard`) не портированы (пины только всплывают в сетке); HUD style/shape и custom scoreboard/tab отсутствуют в backend, мёртвые контролы не рисуются.
3. **Сборка**: **216 тестов, 0 падений**; clean build зелёный; jar `linkenvisuals-1.0.104.jar` (10 657 617 байт, SHA-256 `179E5F2623AA41B49171D5397CA99EC7F5730011CB3E724FC3399AE7FE04F4D2`). Minecraft не запускался.

### Следующая задача
- **Явно запросить разрешение на runClient** и провести:
  - приёмку боевых звуков D80 (kill меч/стрела, streak 2–5, totem, death, low HP, HitSound Bell/Crime/Nya/Uwu/Only Crit, UI/ClickGUI);
  - проверку `latest.log` на `Unable to play unknown soundEvent`, vanilla-звуки и shader-source race;
  - визуальную приёмку 1280×720/1920×1080 (pinned cards/HUD/scoreboard/tab и точные отступы).
- После приёмки — либо портировать pinned cards + HUD/scoreboard/tab, либо оставить зафиксированное отклонение.
- Style/Editor в LINKEN Settings не добавлять; style lock сохранять.

### Риски
- Shader-фикс не проверен в игре; возможно, ошибки останутся или проявятся на другом этапе загрузки.
- Боевые cues и media-трек не проверены живьём в этом чате.
- Pinned cards / HUD style/shape / scoreboard/tab — открытый визуальный долг.
- Точная пиксельная сверка на 1920×1080 ещё не выполнена.

## Состояние на 2026-10-21 (чат №34 — donor sounds + menu track, D80) — v1.0.103

### Сделано
1. **D80/v1.0.103**: перенесены все донорские звуки — **35 событий** в `sounds.json`, включая `mainmenu/evilshit.ogg`, kill/streak/totem/low-HP/death cues, HitSound Bell/Crime/Nya/Uwu, cosmetic/pet. Все OGG проверены тестом; `ui.*` перенаправлены на донорские `clickgui_open`/`disable`/`enable`.
2. **Vanilla sounds removed**: все клики UI (ConfigPanel, LinkGuiScreen, CustomMultiplayer, MainMenu, MainMenuEditor, AltManager, Hub, Multiplayer, SelectWorld) используют `LinkenSounds.UI_CLICK`; `KillEffects` и `FakePlayer` используют донорские cues; в Java-исходниках мода не осталось `SoundEvents.*`.
3. **Sounds module**: `SoundsModule` (Player, default ON) — Volume 130 %, Kills/Kill Streak/Totem Pop/Death/Low Health/Low Health At 6 HP; melee + `EntityDamageS2CPacket` (включая свои стрелы), streak 2–5 за 9 с, totem pop, death, low HP.
4. **Main menu**: донорский трек `EVILSHIT` / `ARLEKIN 40 000` (131,3 с, volume 0.4) запускается один раз, продолжается между меню, останавливается при входе в мир, заменяет vanilla menu music; карточка рисует `EVILSHIT by ARLEKIN 40 000`, progress и play/pause.
5. **Сборка/тесты**: **216 тестов, 0 падений**; clean build зелёный; jar `linkenvisuals-1.0.103.jar` (10 657 799 байт, SHA-256 `64494B17AE61674037DD9588ED5EA4DC20DEDE941CB27808FF971B9B679F26F3`). Release-jar 1920×1080: title с донорским треком и версией 1.0.103, в логе нет `Unable to play unknown soundEvent`.

### Следующая задача
- Ручная проверка боевых cues на сервере/в мире: kill, streak 2–5, totem pop, low HP, death, HitSound Bell/Crime/Nya/Uwu.
- Остаток strict 1:1: pinned cards, donor-only Interface groups (HUD style/shape, scoreboard/tab), точная сверка шрифтов/отступов.
- Устранить shader-source race (`Couldn't find source` для `linkenvisuals:core/*` / `minecraft:core/position_color`).
- Style/Editor в LINKEN Settings не добавлять; style lock сохранять.

### Риски
- Аудиовоспроизведение проверено по состоянию/progress и отсутствию unknown-event, но не измерялось инструментально; боевые cues требуют живого прогона.
- `LinkenMenuMusic.pause()` использует `pauseAllExcept()` — пауза трека глушит все звуки, как в доноре.
- `SoundsModule` считает kill по исчезновению цели при HP ≤ 6 (мобы/игроки) и по damage-пакетам; скрытое сервером HP может дать пропуск, как и в доноре.
- `LinkenSelectWorldScreen` использует vanilla `WorldListWidget` через off-screen `renderWidget`; при изменении vanilla API это хрупко.
- Create/Edit/Re-Create открывают vanilla `CreateWorldScreen` — осознанный остаток.


## Состояние на 2026-10-21 (чат №32 — MSDF/icon pixelation fix, D78) — v1.0.101

### Сделано
1. **D78/v1.0.101**: найдена причина «пиксельности» — `core/icon.fsh` клампил `pixelRange` в 1.0, из-за чего MSDF-переход был почти бинарным; плюс категории/утилиты рисовались PNG 48×48 на 17 px с неверным UV 128×128. Исправлено: донорская формула `Range * inversesqrt((dFdx*W)^2 + (dFdy*H)^2)`, `Range=8`, `smoothstep(-0.5,0.5)`, baseline без округления, `aa` скруглений 0.6→1.0.
2. **Иконки**: добавлены донорские MSDF-атласы `iconsnew` и `iconsfa` (+mcmeta blur/clamp); категории P/S/R/V/H, утилиты FontAwesome (search, gear, users, map-pin, sliders, user, droplet). PNG-иконки 48×48 больше не используются в LinkGUI. TTF/шрифты не импортировались — только атласы.
3. **Сборка/тесты**: **212 тестов, 0 падений**; clean build зелёный; jar `linkenvisuals-1.0.101.jar` (7 585 612 байт, SHA-256 `FA4181AAD894515C6630D788B8E892E65E0C15D331A2FB9C969944C8FD34C5C8`).
4. **Визуал**: при client 1280×720 текст/иконки/dock/Theme/Profile гладкие; окно 1264×681 (нестандартный клиент) даёт дробный canvas-scale и субъективно усиливает пиксельность — это property canvas-подхода.

### Следующая задача
- Остаток strict 1:1: pinned settings cards, donor media track/audio, donor-only Interface groups (HUD style/shape, custom scoreboard/tab), точная сверка шрифтов/отступов на 1920×1080.
- Устранить shader-source race при старте.
- Style/Editor в LINKEN Settings не добавлять; style lock сохранять.

### Риски
- Media strip без донорского трека показывает реальное состояние MediaWatcher.
- Theme-панель короче донорской: нет HUD style/shape, scoreboard/tab настроек.
- Pinned cards не отрисовываются.


## Состояние на 2026-10-21 (чат №31 — donor visual diff, D77) — v1.0.100

### Сделано
1. **D77/v1.0.100**: собран и запущен донорский release-jar `linkenvisuals-2.0.jar` на MC 1.21.4 (только запуск, исходники не менялись) и снят эталон 1280×720 (title + LinkGUI). Найдены и исправлены системные расхождения: canvas не центрировался (`offsetX/offsetY` — сдвиг ~40/24 px), Profile/Theme были закрыты по умолчанию, dock был неполным (5 категорий + 4 утилиты), 3D profile не рисовался.
2. **LinkGUI**: `LinkGuiLayout.Metrics.offsetX/offsetY`; матрица и мышь через `translate(ox,oy)+scale`; backdrop в screen-space. Profile/Theme открыты сразу. Dock 9 тайлов: Combat/Movement/Visual/Player/Misc + Configs/Friends/Waypoints/Elements. Friends/Waypoints — рабочие side-панели 340×600; Elements — HUD edit + чат (fallback).
3. **3D profile**: `InventoryScreen.drawEntity` получает screen-space (`offset + design*scale`) и размер `eh/3.2`; модель внутри viewer, 2D-голова — fallback.
4. **Текст**: title-case кнопки, локализованный `gui.header.all`, keycap `None`.
5. **Сборка/тесты**: **212 тестов, 0 падений**; clean build зелёный; jar `linkenvisuals-1.0.100.jar` (7 384 704 байт, SHA-256 `F32C5338B4F3B17F0624E4A97B0D6D2BED66E45D5ACA44942FF25EA7DC4159C0`). Визуально проверены title, LinkGUI с Profile/Theme, 3D profile, dock, смена темы.

### Следующая задача
- Остаток strict 1:1: pinned settings cards, donor media track/audio, donor-only Interface groups (HUD style/shape, custom scoreboard/tab), точная сверка шрифтов/отступов на 1920×1080.
- Устранить shader-source race при старте.
- Style/Editor в LINKEN Settings не добавлять; style lock сохранять.

### Риски
- Media strip без донорского трека показывает реальное состояние MediaWatcher (часто «ничего не играет») — визуально отличается от донора.
- Theme-панель короче донорской: в моде нет HUD style/shape, scoreboard/tab настроек.
- Pinned cards не отрисовываются.


## Состояние на 2026-10-21 (чат №30 — main menu + LinkGUI re-base, D76) — v1.0.99

### Сделано
1. **D76/v1.0.99**: сверка с фактически используемым донорским `LinkGuiScreen` (а не неиспользуемыми `windows/*`) показала настоящую раскладку 1920×1080: main 1090×830, 3 колонки card 102, theme 340×920, profile 330×358, dock 52. `LinkGuiLayout`/`LinkGuiScreen` переписаны под неё; header 62 (wordmark, avatar 34, ALL, search, sliders, close), bottom dock с раскрывающимся активным табом, вызываемые Profile и Theme/Interface, карточки с иконкой 17/двумя строками описания/keycap/32×18 toggle/«···», media strip 76.
2. **Главное меню 1:1 donor**: центрированный `linken.` 23, часы PlexMono 44, welcome-колонка 56/300/340, кнопки **Multiplayer → Singleplayer → Settings + Quit**, inline-аккаунт с «сменить», 4 соц-плитки (TG/Discord/LV/Code), media 320 по центру внизу, footer 2×11. Убраны лишний Alt Manager, карточка аккаунта, левый wordmark и правая media-карточка.
3. **Настройки GUI**: добавлены `Interface Scale`, `GUI Transparency`, `Cursor Tracker`; theme-панель 2×4 chips 28 + Interface-секция со всеми настройками ClickGUI. `GUI Style` скрыт/заблокирован (D69).
4. **Сборка/тесты**: **211 тестов, 0 падений**; `clean build --no-daemon` зелёный; jar `linkenvisuals-1.0.99.jar` (7 376 475 байт, SHA-256 `8E9F08D5FDD5AB41C4507723D7BA9845289A2B0DF81496BC7E18DCFBB4E8E8EE`).
5. **Визуал**: release-jar 1280×720 — title, LinkGUI, Theme, Profile проверены; при старте остаётся известный shader-source race, но UI/мир рисуются.

### Следующая задача
- Доводка strict 1:1 остатка: pinned settings cards (right-click pin сейчас без панели), 3D profile preview (2D-голова — зафиксированный fallback), donor media/volume, точная сверка шрифтов/отступов на 1920×1080.
- Устранить shader-source race при старте (отдельный дефект: `Couldn't find source` для `linkenvisuals:core/*` и `minecraft:core/position_color`).
- Style/Editor в LINKEN Settings не добавлять; style lock сохранять.

### Риски
- Pinned-карточки и 3D-профиль не 1:1; это записано в D76.
- Шрифтовые метрики MSDF могут давать субпиксельные отличия от donor `Render2D`.
- Донорский `donor2`/`Главная` отсутствуют; использован `C:\Users\gameg\Desktop\визуалы` (read-only).

## Состояние на 2026-10-21 (чат №29 — strict dock/theme, M8.9b-2a) — v1.0.98

### Сделано
1. **D75/v1.0.98** (`1c529bc`): dock donor ×1.5 — pad 9, contiguous tabs 54px, sliding indicator inset 3/radius 13.5, icons 22.5, active/hover lift 6.75, label 10.2 alpha; theme — donor 2×4 swatches 45px/gap 7.5/radius 12, preset backgrounds (Midnight…Frost), active outline 1.8, lower ACTIVE PALETTE info.
2. **Сборка/тесты**: **210 тестов, 0 падений**; `clean build --no-daemon` зелёный; jar `linkenvisuals-1.0.98.jar` (7 374 430 байт, SHA-256 `E64C6D748DC3CE2698CF5762960EEFA3ADB0D33A8EDC5D7AB8B867B7A8755CE0`).
3. **Визуал**: dev-run ранее упал в shader-source race; release-jar через `MultiMC.exe --launch 1.21.111` не создал окно (процесс без MainWindowHandle, остановлен). Визуально dock/theme **не приняты**.

### Следующая задача
- **M8.9b-2b**: InterfaceWindow (interface scale/cursor tracker/transparency/Apply-Default), donor Theme Interface section, media strip typography/volume, theme button; volume/system volume — реальный контрол или зафиксировать отклонение; close overlay — docs-only (no-op).
- **Release-jar visual acceptance** 1280×720/4K в чистом профиле; dev shader-source race обходить.
- Style/Editor в LINKEN Settings не добавлять; style lock сохранять.

### Риски
- Визуал не подтверждён: dev shader-source race и неудачный MultiMC launch.
- Interface/Theme donor section и media volume ещё не портированы.
- Донор `donor2` отсутствует; используется локальный донор с теми же 1280×720-константами.

## Состояние на 2026-10-21 (чат №28 — strict header/grid, M8.9b-1) — v1.0.97

### Сделано
1. **D74/v1.0.97** (`1d1e876`): donor header ×1.5 — logo chip 60, player head 42 на `+87`, `ALL` 51×42, search `+192`, close 36/right−60; grid side pad 63/126, media strip 120. Ник/счётчик категории убраны из шапки (в доноре их нет); добавлен `searchAll`/ALL.
2. **Сборка/тесты**: **209 тестов, 0 падений**; `clean build --no-daemon` зелёный; jar `linkenvisuals-1.0.97.jar` (7 373 888 байт, SHA-256 `BECF12FB2DDB48D41B9C0BDF997AFB123F65E5D1E23CEA3A2B4E90103B6161DB`).
3. **Визуальный прогон**: пользователь дал разрешение; dev-клиент запустился, title screen отрисовался, но при входе в мир/загрузке возник shader-source resource race — `Couldn't find source` для `linkenvisuals:core/*` и `minecraft:core/position_color`, world start не завершился. Header/карточки визуально **не приняты**; это dev-артефакт, release-jar не проверялся.

### Следующая задача
- **M8.9b-2**: theme button, dock indicator slide, Interface/Profile/Theme spacing, media strip typography/volume; volume/system volume — реальный контрол или зафиксировать отклонение; close overlay (no-op) — docs.
- **Release-jar visual acceptance** 1280×720/4K: запускать не dev `runClient`, а собранный `linkenvisuals-1.0.97.jar` в чистом профиле, чтобы обойти shader-source race.
- Style/Editor в LINKEN Settings не добавлять; style lock сохранять.

### Риски
- Dev-визуал заблокирован shader-source resource race; release-jar ещё не проверялся.
- Header/карточки не проверены глазами; возможны нюансы chip/head/All/search.
- Донор `donor2` на этом устройстве отсутствует; использован локальный старый донор с теми же 1280×720-константами.

## Состояние на 2026-10-21 (чат №27 — strict card 1:1, M8.9a) — v1.0.96

### Сделано
1. **D73/v1.0.96** (`a081296`): строгая геометрия `ModuleCard` из донора ×1.5 — badge 51/pad 18, title 14.1 на +81/+34.5, description 11.85 на +61.5, bind pill +90/h21, toggle 54×27 (right margin 15), radius 15, hover lift 4.5. Чистая `LinkGuiLayout.CardMetrics` + `cardContentUsesDonorScaledGeometry`.
2. **Сборка/тесты**: **208 тестов, 0 падений**; `clean build --no-daemon` зелёный; jar `linkenvisuals-1.0.96.jar` (7 373 730 байт, SHA-256 `139A07F5B48329270D64D774F3D6C1CC7F1C34A3CF7F1ACB518DD4F6A0DC0BFB`). Minecraft не запускался.
3. **Донор**: `C:\StrangeVisuals\donor2\visualy` отсутствует на этом устройстве; использован локальный донор `C:\Users\gameg\Desktop\Главная` (`screen/linkgui`, те же 1280×720-константы). Донор не изменялся.

### Следующая задача
- **M8.9b**: main header (logo chip 60, head 42 на +87, All+search, close 36, theme), dock indicator slide, Interface/Profile/Theme spacing, media strip typography/volume; volume/system volume — только реальный контрол или зафиксировать отклонение.
- Close overlay: в доноре no-op, новый не выдумывать; зафиксировать в D-решении.
- Визуальная приёмка 1280×720/4K — только после явного разрешения пользователя.

### Риски
- Карточки не проверены глазами после изменения; нужна визуальная сверка.
- `pin` — наша M4-фича, не донорская; оставлена в нижнем правом углу.
- Строгий 1:1 остаётся неполным: header/dock/panels/media ещё не сверены.

## Состояние на 2026-10-21 (чат №26 — donor card scale + media controls, M8.8) — v1.0.95

### Сделано
1. **D72/v1.0.95** (`f76016d`): карточки приведены к донорскому масштабу — высота 129, gap 15, header 78, grid top +80, media 126; media strip 94. Добавлены `<<` / `>||` / `>>`, отправляющие системные media-клавиши через `java.awt.Robot` (`LinkGuiMediaKeys`), ошибки в debug.
2. **Сборка/тесты**: 207 тестов, 0 падений; `clean build --no-daemon` зелёный; Minecraft не запускался. Jar `linkenvisuals-1.0.95.jar` (7 371 851 байт, SHA-256 `886EF652F5FDEE6298178C0B15916412816CBEE780A8BE66C66D5D8480B65081`).
3. **M8**: layout, dock, Interface/Profile, draggable settings, 8 тем, player head, badges, media strip/controls, style lock, cursor trail.

### Следующая задача
- Остаток строгого 1:1: volume slider/system volume (донорский audio backend не портирован), close overlay (no-op в доноре), точная пиксельная сверка шрифтов/отступов.
- Визуальная приёмка 1280×720/4K — только с разрешения пользователя.
- Style/Editor в LINKEN Settings не добавлять; style lock сохранять.

### Риски
- System media keys через Robot могут не сработать на некоторых Windows/Java конфигурациях (debug-лог, без краша).
- Volume/prev/play/next и light/theme/cursor не проверены глазами.
- Масштаб карточек изменён по донорским константам, но визуальная сверка ещё нужна.

## Состояние на 2026-10-21 (чат №25 — cursor trail, M8.7) — v1.0.94

### Сделано
1. **D71/v1.0.94** (`166869a`): `LinkGuiCursorTrail` — 64 точки/780 мс/min-step 2.4, мягкие glow-орбы + белое ядро через `GlowPipeline`/`CirclePipeline`; очистка в `init`, рендер поверх GUI. Minecraft не запускался (только код/тесты).
2. **Сборка/тесты**: 207 тестов, 0 падений; `clean build --no-daemon` зелёный; jar `linkenvisuals-1.0.94.jar` (7 370 108 байт, SHA-256 `FC4974DB090D25907B91B2FE055F8056EECA80F172975ACBA0B9AF6C64E48991`).
3. **M8 итог**: донорская компоновка, 8 тем (LIGHT/FROST), Interface/Profile, draggable settings window, player head, card badges, media strip, style lock, cursor trail.

### Следующая задача
- Строгий 1:1 остаток: управление музыкой/громкостью (донорский playback-бэкенд не портирован, strip read-only), close overlay (в доноре no-op), точная пиксельная сверка шрифтов/отступов с донором.
- Визуальная приёмка 1280×720/4K — только с разрешения пользователя; сейчас код-компиляция.
- Style/Editor в LINKEN Settings не добавлять; style lock сохранять.

### Риски
- Cursor trail/light/drag-окно не проверены глазами.
- Read-only media strip и отсутствие volume/prev/play/next — осознанное отклонение без donor audio backend.
- Строгий пиксель-в-пиксель не подтверждён без визуального сравнения.

## Состояние на 2026-10-21 (чат №24 — draggable module settings, M8.6) — v1.0.93

### Сделано
1. **D70/v1.0.93** (`1d66355`): клик по карточке модуля (кроме switch/pin) открывает плавающее окно настроек 330×440; header перетаскивается, `X` закрывает, позиция клампится в canvas. Внутри — имя/описание, toggle, bind, прокручиваемые настройки. Клик по ClickGUI оставляет Interface/Profile.
2. **Сборка/тесты**: 207 тестов, 0 падений; `clean build --no-daemon` зелёный; jar `linkenvisuals-1.0.93.jar` (7 366 303 байта, SHA-256 `B7B6F14481432FDF571D96CDFD3C7DE9E1A5ACAEF28C5111D0495B70569296C7`). Minecraft не запускался — только компиляция/тесты.
3. **Уже в M8**: донорская компоновка, 8 тем включая LIGHT/FROST, player head, card badges, media strip, Interface/Profile, style lock.

### Следующая задача
- Строгий 1:1 остаток: cursor trail (донорский `LinkGuiCursorTrail`), close overlay (в доноре no-op), управление музыкой/громкостью (read-only), точная визуальная сверка отступов/шрифтов с донором.
- Визуальная приёмка 1280×720/4K — после следующего code-среза; Minecraft не запускать без разрешения.
- Style/Editor в LINKEN Settings не добавлять; style lock сохранять.

### Риски
- Draggable-окно и split не проверены глазами; возможны пересечения при малом окне.
- Cursor trail/close overlay/music controls пока не перенесены.
- Строгий пиксель-в-пиксель без запуска клиента не подтверждён.

## Состояние на 2026-10-21 (чат №23 — LINKEN GUI style lock + Interface/Profile, M8.5) — v1.0.92

### Сделано
1. **D69/v1.0.92** (`5638ef7`): пока активен Linken GUI, стиль заблокирован — `ModuleRegistry.isGuiStyleLocked`, `cycleGuiStyle` no-op, `setGuiStyle` принимает только Linken, Ctrl+Left/Right не переключает, строка `GUI Style` скрыта в Interface-панели. +1 тест.
2. **Interface/Profile split**: левая колонка теперь `INTERFACE` 330×282 + `PROFILE` 330×360 (донорская компоновка). Верх — глобальные настройки ClickGUI, низ — активный профиль/список профилей либо настройки выбранного модуля. `LinkGuiLayoutTest` обновлён.
3. **Сборка/тесты**: **207 тестов, 0 падений**; `clean build --no-daemon` зелёный; jar `linkenvisuals-1.0.92.jar` (7 365 384 байта, SHA-256 `728C7BAA069D9BFE37F423E72EE4958DFAB5DFDCFDF054AE406F1368E350B5D0`). Minecraft по просьбе пользователя не запускался.

### Следующая задача
- Строгий 1:1 остаток: перетаскиваемые окна настроек модуля (donor `LinkGuiSettingsWindow`), cursor trail/close overlay, 3D `ClickGuiPlayer`, управление музыкой/громкостью, точная сверка отступов/шрифтов по скриншоту донора.
- Визуальная приёмка 1280×720/4K после следующего code-среза.
- Style/Editor в LINKEN Settings не добавлять (приказ пользователя); style lock сохранять.

### Риски
- Стиль заблокирован намертво: вернуть Classic/Beta можно только правкой конфига/кода — это требование пользователя.
- Light-темы/player head/interface split не проверены глазами; компиляция и тесты только.
- Строгий пиксель-в-пиксель не подтверждён без визуального сравнения.

## Состояние на 2026-10-21 (чат №22 — LINKEN ClickGUI: player/card/light, M8.3–M8.4) — v1.0.91

### Сделано
1. **D68/v1.0.91** (`fa107f1`): в шапке главного окна — 2D-голова активного скина (`PlayerSkinDrawer`, fallback), ник/категория; ModuleCard получил badge 28×28 с иконкой категории и переработанные отступы; добавлены донорские light-темы **LIGHT** и **FROST** (8 палитр), полный набор токенов (`TEXT/PANEL/ROW/HOVER/ACTIVE/FIELD/TRACK/BACKDROP/ON_DARK`), светлый backdrop, светлые панели/строки, тёмный текст; theme-панель показывает 8 карточек.
2. **Тесты/сборка**: `LinkGuiThemeTest` +2 — **206 тестов, 0 падений**; `clean build --no-daemon` зелёный, Java-ворнингов 0; jar `linkenvisuals-1.0.91.jar` (7 363 633 байта, SHA-256 `79DD285FA99E729BB64559C68B10033FA3960B34403979A2A961D56BD90573D9`). Minecraft по просьбе пользователя не запускался — только компиляция/тесты.
3. **Уже перенесено в M8**: донорская компоновка (main 1080×720, dock 540×72, левый inspector 330×600, правое theme-окно 360×855), 8 палитр, read-only media strip, player head, card badges, поиск/категории/настройки/конфиги/пины.

### Следующая задача
- **M8.5 (если нужен строгий 100%)**: глобальный `InterfaceWindow` (interface scale, GUI style, cursor tracker, Apply/Default), отдельные перетаскиваемые окна настроек модуля, cursor trail/close overlay, 3D player preview, управление музыкой.
- После этого — визуальная приёмка 1280×720/4K и финальная полировка. Style/Editor в LINKEN Settings не добавлять (приказ пользователя).

### Риски
- Light-темы не проверены глазами: часть secondary-элементов остаётся тёмной (`ON_DARK`), контраст на светлых панелях нужно проверить.
- Player head 2D вместо донорского 3D; card badge — иконка категории, не индивидуальная иконка модуля.
- Media strip read-only, без управления и громкости.
- Строгий 1:1 с донором ещё не закрыт (см. M8.5).

## Состояние на 2026-10-21 (чат №21 — LINKEN ClickGUI donor window layout, M8.2) — v1.0.90

### Сделано
1. **D67/v1.0.90 — донорская компоновка LinkGUI** (`ea26c9c`): 1280×720 композиция ×1.5 на 1920×1080 — главное окно 1080×720, горизонтальный док категорий 540×72, левый inspector 330×600, правое постоянное Theme-окно 360×855. `LinkGuiLayout` ужимает главное окно на узких virtual-width, чтобы панели не пересекались. `LinkGuiLayoutTest` обновлён.
2. **Визуал**: ник/категория/счётчик/поиск/close в шапке главного окна; док с иконками+подписями; левая панель модуля; правая панель 6 палитр (ACTIVE/CLICK TO APPLY) без модала; read-only media strip внизу (NOW PLAYING/MEDIA, title/artist/progress/equalizer). Модальный `LinkGuiThemeWindowLayout` удалён.
3. **Evidence**: 1280×720 visual — новое окно/док/панели читаются, пересечений нет; media strip добавлен после снимка, глазами не перепроверен. `clean build --no-daemon` — **204 теста, 0 падений**; jar `linkenvisuals-1.0.90.jar` (7 361 749 байт, SHA-256 `3531B024BED331B6496E931DD73231536BCD6D88AC79530E681D437526C015AE`).

### Следующая задача
- **M8.3** — раскрывающиеся `ModuleCard` (настройки в карточке), `ClickGuiPlayer` preview в шапке, донорский `MusicPlayerStrip` (read-only основа уже внизу главного окна), доводка левого inspector под ширину 330.
- **M8.4** — light-темы LIGHT/FROST и плавный переход палитр.
- Прогнать 4K-framebuffer после M8.2: проверить, что док/панели не обрезаны и media strip читается.
- Shader-source ERROR caveat и ручной keybind rebind — без изменений.

### Риски
- Левая панель 330 px тесная: длинные названия настроек/значения могут выглядеть плотно (добавлен ellipsis, но глазами проверить).
- Media strip не проверен глазами; права/анимация только от `MediaWatcher`, без управления.
- Полный M8 ещё не закрыт: карточки пока без inline-настроек, player preview отсутствует.

## Состояние на 2026-10-21 (чат №20 — LINKEN ClickGUI ThemeWindow, M8.1) — v1.0.89

### Сделано
1. **D66/v1.0.89 — первый срез переноса донорского LinkGUI** (`43031da`): `LinkGuiThemeWindowLayout` (чистая геометрия 568-панели, 3 карточки в ряд 160×110) + модальное THEME-окно в `LinkGuiScreen`. 6 текущих палитр, selected/hover, клик применяет и сохраняет тему; закрытие X/клик вне/Esc. Theme-chip теперь открывает окно вместо циклинга.
2. **Тесты**: `LinkGuiThemeWindowLayoutTest` (+3) — итого **207**, 0 падений. `clean build --no-daemon` зелёный; jar `linkenvisuals-1.0.89.jar` (7 363 796 байт, SHA-256 `BD03E44F5DB2E06F1F7906BA2271FA4525A52F1378FD67F523B70336E0C77C5F`).
3. **Визуальный прогон ThemeWindow не выполнен**: автоматическая отправка Right Shift в dev-окно не открыла ClickGUI (фокус/raw input). Разметка покрыта unit-тестами; глазами проверить при следующем runClient.

### Следующая задача
- **M8.2** — донорская компоновка плавающих окон: `MainModuleWindow` (720×480), `CategoryDockWindow` (нижний док), `InterfaceWindow` (левая панель), `ThemeWindow` справа; перенести геометрию 1280×720 + fit-scale, сохранить текущий ModuleRegistry/Setting.
- **M8.3** — `ModuleCard` с раскрытием настроек, `MusicPlayerStrip` (read-only через `MediaWatcher`), `ClickGuiPlayer` preview.
- **M8.4** — light-темы LIGHT/FROST из донора + плавный переход палитр.
- Style/Editor в LINKEN Settings не добавлять (приказ пользователя). Shader-source ERROR caveat и ручной keybind rebind — по-прежнему открыты.

### Риски
- ThemeWindow не проверен глазами; возможны визуальные нюансы карточек при 4K.
- Полный M8 — крупная переработка `LinkGuiScreen` (сейчас 74 КБ); дробить по окнам, не ломать Beta/Classic.

## Состояние на 2026-10-21 (чат №19 — LINKEN keybind refresh) — v1.0.88

### Сделано
1. **D65/v1.0.88 — keybind refresh fix** (`23ce1d0`): `LinkenKeybindsScreen` после rebind/right-click reset/RESET ALL теперь вызывает `KeyBinding.updateKeysByCode()` и `refresh()`. До фикса новая клавиша сохранялась в `options.txt` (проверено `key_key.forward:key.keyboard.b`), но строка показывала старую, а карта клавиш в сессии не обновлялась. Listening-состояние и запись в options подтверждены; ручной end-to-end UI rebind всё ещё рекомендуется. `clean build --no-daemon` — 204 теста, 0 падений; jar `linkenvisuals-1.0.88.jar` (7 358 704 байта, SHA-256 `3B7583E3CE18616DBE34E650BAD7B0C7172AFB0FBBDA512864495021DB779860`).

### Следующая задача
- Ручной end-to-end keybind rebind (новая клавиша сразу в строке и в игре).
- Shader-source ERROR caveat: в dev-прогонах при startup/reload логируются `Couldn't find source for … shader` (включая `minecraft:core/position_color`), экраны рендерятся; на релизном jar/реальном клиенте не перепроверено.
- `OPEN FOLDER`, соцссылки и медиа-трек — по желанию.
- Финальное «принято» M2–M7; Style/Editor в LINKEN Settings не добавлять (приказ пользователя).

### Риски
- Shader-source ERRORs в dev — вероятный resource race; не воспроизведён/не проверен на релизной сборке.
- Физического 4K нет: только framebuffer 3840×2160.

## Состояние на 2026-10-21 (чат №18 — M7b-4 acceptance + splash reload fix) — v1.0.87

### Сделано
1. **D63/v1.0.86 — Skin и Resource Packs** (`62cb4e1`): `LinkenSkinScreen` (превью головы, 7 model parts, main arm, account action) + `LinkenResourcePacksScreen` (AVAILABLE/SELECTED, поиск, enable/disable, приоритет, pinned/locked/incompatible, confirm-modal, open folder, apply/reload); роутинг из Settings + `MinecraftClientMixin`; i18n en/ru/uk; `LinkenPackSearchTest` (+5), итого 204 теста.
2. **D64/v1.0.87 — критический fix, найденный на приёмке** (`7f181cc`): `SplashOverlayMixin` отменял vanilla `render` до инициализации `reloadStartTime`; при ручном reload ресурсов оверлей зависал навсегда (чёрный экран после применения пака). Теперь mixin сам ставит `reloadStartTime`, если он `<= 0`; после `Enable Anyway`/закрытия паков возврат в Settings hub за ~3 с. 204 теста, 0 падений; jar `linkenvisuals-1.0.87.jar` (7 358 660 байт, SHA-256 `9A2E4D53378564D1B10CFB06CAA1FDA0A018126FD02CABE2391CB96D4D3769CC`).
3. **Визуальная приёмка M7b-4**: 1280×720/GUI 2 — Skin (превью, 7 частей, ON/OFF пишутся в `options.txt`, Main Arm Left/Right, account manager), Resource Packs (две колонки, поиск по `p`, enable/disable, порядок 01/02, incompatible-confirm, DONE/ESC → apply/reload → hub), Language (автопрокрутка к активному `English (US)`), Keybinds (60 биндов, conflict-маркеры). 4K-framebuffer F2 PNG **3840×2160**: `run/screenshots/2026-10-21_10.48.11.png` (Resource Packs), `2026-10-21_10.51.10.png` (Skin); обрезки/перекрытий нет.
4. **Локальный `options.txt` восстановлен** из `options.txt.lvaccept.bak` (resourcePacks `[]`, mainHand right, model parts true); тестовый `IncompatibleTest` пак удалён.

### Следующая задача
- **Ручная приёмка keybind rebind** (listening-состояние «PRESS A KEY / ESC CANCELS» подтверждено; фактический ввод клавиши автоматизация не подтвердила) и, при желании, `OPEN FOLDER`/соцссылки/медиа-трек.
- **Style/Editor в LINKEN Settings не добавлять** — приказ пользователя от 21.10.2026; переключение Beta/Vanilla и редактор остаются вне LINKEN Settings, это принято.
- **Проверить shader-source ERROR caveat**: в dev-прогонах при startup/reload логировались `Couldn't find source for … shader` (включая `minecraft:core/position_color`), экраны работали. Guard в `Pipelines.warmUp` не помог и откатан; перепроверить на релизном jar/реальном клиенте, при воспроизведении — отдельный фикс.
- Финальное «принято» M2–M7 — за пользователем; после него M7 закрыть и выбрать следующий объём.

### Риски
- Keybind rebind визуально не подтверждён (только listening-состояние); conflict-маркеры и экран отображаются корректно.
- Shader-source ERRORs при startup/reload в dev — возможен resource race; на релизной сборке не проверено.
- Style/Editor недоступны из LINKEN Settings по приказу пользователя — принято, не дефект.
- Физического 4K нет: только framebuffer 3840×2160.

## Состояние на 2026-10-21 (чат №17 — LINKEN M7b-4) — v1.0.86

### Сделано
1. **D63 — M7b-4: Skin и Resource Packs, v1.0.86, `62cb4e1`.** `LinkenSkinScreen`: LINKEN-окно 1060×740, живой 2D-превью головы из `PlayerSkinProvider`/`DefaultSkinHelper` (fallback `?`), все 7 `PlayerModelPart` как ON/OFF-строки, `Main Arm` Left/Right, `ACCOUNT & SKIN` → существующий `LinkenAltManagerScreen`. В 1.21.11 у `SkinOptionsScreen` нет отдельного skin chooser для offline-аккаунтов — это осознанное отклонение (см. D63), а не пропущенная кнопка.
2. **`LinkenResourcePacksScreen`**: две колонки AVAILABLE/SELECTED на `ResourcePackOrganizer`, поиск по имени/описанию/id, включение/выключение кликом, стрелки приоритета, `PINNED`/`LOCKED`/`INCOMPATIBLE`, подтверждение включения несовместимого пака, `OPEN FOLDER`, DONE/ESC. Применение — `GameOptions.refreshResourcePacks(manager)` (запись options + reload при изменении) и возврат в Settings. Иконки — LINKEN-монограммы, без vanilla texture-loader.
3. **Роутинг**: Settings → Skin/Resource Packs ведут в новые экраны; `MinecraftClientMixin` дополнительно перехватывает vanilla `SkinOptionsScreen`/`PackScreen` в LINKEN. Beta/Vanilla не перерисовываются.
4. **i18n**: en/ru/uk — skin (title/subtitle/preview/account/model parts/main arm/on/off) и packs (title/subtitle/available/selected/open folder/none/hint/locked/pinned/conflict/incompatible modal/cancel/enable anyway); `LangParityTest` зелёный.
5. **Тесты/сборка**: `LinkenPackSearchTest` (+5) — итого **204 теста, 0 падений/ошибок/скипов**; `clean build --no-daemon` зелёный, Java-ворнингов 0; jar `source/build/libs/linkenvisuals-1.0.86.jar` (7 358 636 байт).
6. **Визуальная приёмка не выполнялась**: в этом чате не было явного разрешения на `runClient`; 1280×720/4K, Skin-превью, Resource Packs enable/disable/move/apply/reload, incompatible-confirm и Open Folder — за следующим шагом.

### Следующая задача
- **Финальная визуальная приёмка M7** (нужно явное разрешение на `runClient`): 1280×720 и 4K-framebuffer — Skin (превью головы, все 7 частей, main arm, account action), Resource Packs (поиск, enable/disable, порядок, apply/reload, incompatible-confirm, open folder), Language (активная строка после автопрокрутки), keybind rebind, Beta/Vanilla regression. После «принято» — закрыть M7 и решить следующий объём.
- Открытые решения без изменений: LICENSE copyright, `MASTER_PROMPT.md` в публичном репо, UK-глифы MSDF, waypoints UX, StreamerMode nametag-санитизация.

### Риски
- Физического 4K нет: проверка только framebuffer 3840×2160; окно может уезжать на второй монитор/терять фокус (как в M6).
- Skin: offline-аккаунты не имеют in-game skin chooser в 1.21.11; показывается голова активного скина и переход в account manager — осознанное отклонение.
- Resource Packs: иконки — монограммы, не оригинальные pack.png; несовместимый пак можно включить только через подтверждение. `ResourcePackOrganizer` — vanilla API; поведение apply/reload нужно проверить в игре.
- Keybind rebind и Language активная строка всё ещё не подтверждены визуально после изменений M7b-2/M7b-3a.

## Состояние на 2026-10-21 (чат №16 — LINKEN M7a/M7b-1/M7b-2/M7b-3a) — v1.0.85

### Сделано
1. **M2 — splash/loading/connect, v1.0.77, D53, `bef9569`.** `SplashOverlayMixin`, `LinkenLoadingScreen`, `SplashProgress` + `SplashProgressTest`; LINKEN-ветки `LevelLoadingScreenMixin`, `ConnectScreenMixin`, `ProgressScreenMixin`, `MessageScreenMixin`; `LinkenFonts.retryIfUnavailable()`. Кастомный `SAVING WORLD` пойман, hand-off на title стабилен, чёрного застревания нет. Сам splash через F2 не снимается (ввод во время overlay не обрабатывается) — тайминги покрыты тестом.
2. **M3/M4 — Link GUI, v1.0.78/1.0.79, D54/D55, `21cd4d1`/`f639d69`.** `clickgui/link/` (shell, layout, theme, config), Classic/Beta/Linken и Ctrl+Left/Right, поиск/скролл, темы `MIDNIGHT/GRAPHITE`, пины, создание/удаление профилей, compact `Auto` 190. Темы/пины/конфиг вошли в M3 из-за compile-зависимости; M4 — полировка. Beta/Vanilla не затронуты.
3. **M5 — pause/settings hub, v1.0.80, D56, `ec52c1e`.** `LinkenHubScreen`, `LinkenPauseScreen`, `LinkenSettingsScreen`, `returnScreen`, blur-fix. **Критический hang `Save and Quit` исправлен:** вместо `disconnectWithSavingScreen()` вызывается `client.disconnect(ClientWorld.QUITTING_MULTIPLAYER_TEXT)`, поэтому соединение закрывается, сервер пишет `Stopping server`/`All chunks are saved` и возвращает title. По приказу пользователя из Settings убрана кнопка `LinkenVisuals` — hub содержит только игровые опции (8 карточек).
4. **M6 — multiplayer, v1.0.81, D57, `c3a328c`.** `LinkenMultiplayerScreen` на `ServerList`/pinger/`ConnectScreen`: сетка 3×3, paging, статусы, Add/Edit/Delete/Direct modal, toolbar, double-click/Enter. Проверены Add/Edit/Delete, `LocalServer` → `INCOMPATIBLE`, пагинация `1 / 1`; 4K главное меню и Settings сняты. Direct Connect modal/actual connect — на финальную приёмку.
5. **Сборка/тесты:** `.\gradlew.bat test --console=plain` — **199 тестов, 0 падений/ошибок** на v1.0.85. Коммиты M2–M7b-3a созданы отдельно, версии `+1 patch`; рабочее дерево после docs-коммита должно быть чистым.
6. **Визуальные evidence (1280×720, GUI 2):** pause `20.44.54.png`; settings `20.45.11.png`; Link GUI `20.45.28.png`; config `20.46.22.png`; создание профиля `20.46.49.png`; Save/Quit title `20.59.04.png`; M6 main `20.59.22.png`; Add modal `20.59.43.png`; M6 add `21.00.08.png`; Edit `21.24.16.png`; Delete `21.30.11.png`; M6 after delete `21.31.27.png`; 4K menu `21.53.13.png`; 4K Settings `21.57.22.png`. Все — в `source/run/screenshots/`, не коммитить.
7. **Docs:** D53–D60 в `MERGE_DECISIONS.md`; ROADMAP/PORT_STATUS обновлены. Тестовое состояние (`linkenvisuals_linkgui.json` с `GRAPHITE`+`Shaders`, `servers.dat` с `LocalServer`/`M6Test`, `linkenvisuals_accounts.json`) очищено; `run/options.txt` вернуть в исходное (`ao:true`, если тестовый toggle его выключил).
8. **M7a — LINKEN account manager, v1.0.82, D59, `c04ae31`.** `LinkenAltManagerScreen`: 1920×1080 chrome, список аккаунтов с ACTIVE-бейджем, поиск, Add/Use/Delete и confirm-delete; backend — существующий `AccountStorage` + `MinecraftClientAccessor`, без IAS. Роутинг из main menu/account-chip и `MinecraftClientMixin`. Проверены пустое состояние, добавление `LinkenTest`, применение, поиск, удаление; `AltAccountFilterTest` (+3). PrintWindow evidence: `alt-state.png`, `alt-add.png`, `alt-added.png`, `alt-used.png`, `alt-search.png`, `alt-del.png`, `alt-deleted.png` (temp).
9. **M7b-1 — LINKEN settings sub-pages, v1.0.83, D60, `a129b8f`.** `LinkenSubSettingsScreen` (search, двухколоночные строки, toggle/slider/cycle, DONE/ESC), `LinkenSettingsOption`/`LinkenSettingsCatalog`/`LinkenSettingsFilter`; Video/Controls/Sound/Chat/Accessibility используют vanilla `SimpleOption` через LINKEN chrome. Проверены Video, toggle Smooth Lighting, Sound, Chat, поиск `opacity`, Controls; PrintWindow `video-settings.png`, `video-toggle.png`, `settings-hub-after-video.png`, `sound-settings2.png`, `chat-settings.png`, `chat-search.png`, `controls-settings.png`. Language/Skin/Packs и keybinds list — следующий срез.
10. **M7b-2 — LINKEN keybinds, v1.0.84, D61, `2d569c8`.** `LinkenKeybindsScreen`: категории, поиск, capture через `InputUtil.fromKeyCode`, conflict-маркеры, right-click reset, RESET ALL, `options.write()`. Controls → `Keybinds → Open` теперь LINKEN. Evidence `keybinds2.png`, capture-состояние `rebind-state.png`; автоматический ввод клавиши не сработал из-за фокуса окна на втором мониторе — сам rebind проверить руками.
11. **M7b-3a — LINKEN language list, v1.0.85, D62, `f9dbed3`.** `LinkenLanguageScreen`: 143 языка, поиск, ACTIVE, клавиатурная навигация, Apply через `LanguageManager`/`reloadResources`, автопрокрутка к текущему. Settings → Language больше не открывает vanilla. Evidence `language.png`; после добавления автопрокрутки визуально не перепроверено.

### Следующая задача
- **Финальное «принято» M2–M6 пользователем.** Повторить Direct Connect modal и actual connect (например, на localhost), проверить splash глазами при желании.
- **M7b-4:** Skin/Resource Packs list screens; затем визуальная приёмка M7 1280×720/4K. Singleplayer/Select World в доноре ванильный, отдельный экран не нужен; Alt Manager закрыт M7a, keybinds — M7b-2, Language — M7b-3a. Beta/Vanilla не перерисовывать.
- Открытые решения: LICENSE copyright, `MASTER_PROMPT.md` в публичном репо, UK-глифы MSDF, waypoints UX, StreamerMode nametag.

### Риски
- Физического 4K-дисплея нет: 4K проверен framebuffer 3840×2160; на реальном 4K/другом DPI возможны отличия.
- 4K-окно растягивалось на два 1920×1080-монитора и периодически минимизировалось/теряло фокус — automation F2 нестабильна; 4K Settings снят, 4K Multiplayer/Link GUI — нет.
- Splash визуально не пойман (F2 не работает во время overlay); логика покрыта тестом.
- Direct Connect и actual connect не перепроверены после modal-фиксов; код использует общий layout и `ConnectScreen`.
- Skin/Resource Packs пока используют vanilla chrome — это M7b-4, не регрессия M5/M7b-1.
- Keybind rebind-клик/ESC проверены визуально, но автоматическая отправка клавиши не сработала из-за фокуса 4K-окна на втором мониторе; ручной rebind и conflict-поведение — на приёмку.
- Account manager работает только с offline-именами (`AccountStorage`), без IAS/лицензионного login и без скинов; это осознанный backend проекта.

## Состояние на 2026-10-20 (чат №14 — LINKEN M1) — v1.0.76

### Сделано
1. **D52 — третий визуальный стиль LINKEN, M1: главное меню и роутинг.** Прочитан `MASTER_PROMPT_STYLE3.md`; M1 сделан одним фич-коммитом `feat(ui): add LINKEN main menu style (v1.0.76, D52)`.
2. **Меню** — `ui/linkmenu/`: `LinkenMainMenuScreen` (виртуальный холст 1920×1080, `scale = screenHeight/1080`), `LinkenMenuChrome` (фон `bg-tower` cover + parallax + scrim/виньетка, wordmark `linken.`/`VISUALS`, часы/дата, приветствие, аккаунт-чип, соцсети, живая медиа-карточка, футер, fit/ellipsis длинных строк), `LinkenMenuLayout` (чистая геометрия), `LinkenFonts` (MSDF-атласы Golos/Golos Semi/Unbounded/Unbounded Light/PlexMono + shared `wtmico`/`brands`). Акцент `#7EDFB4`; Esc не закрывает, Quit — повторный клик 3 с; кнопки Singleplayer/Multiplayer/Settings/Alt Manager/Quit и соцсети рабочие.
3. **Роутинг** — `MenuStyle.LINKEN` (алиасы `linken`/`link`), чистые правила `StyleRouting` (`nextStyle`, `shouldApplyPreset`, `usesVanillaChrome`), цикл Video Settings **Beta → Vanilla → Linken → Beta**; `TitleScreenMixin`/`MinecraftClientMixin` — явные `switch`; все прежние бинарные проверки переведены на `usesVanillaChrome`. LINKEN **не вызывает** `MainMenuConfig.applyPreset` — элементы Beta/Vanilla сохраняются, `menuStyle` переживает рестарт (схема/`configVersion` не менялись).
4. **Редактор** — третий пункт `Linken`, в LINKEN только переключатель стиля + `Save & Exit` + пояснение фиксированной раскладки; тулбар расширен до 840, Reset/Save не пересекаются.
5. **Fallback'и M1 (без мёртвых кликов)** — Multiplayer → ванильный `MultiplayerScreen` (M4/M6), Settings → существующий `OptionsScreen` (M5), Alt Manager → наш `AltManagerScreen`; unported-экраны — vanilla chrome; Link GUI остаётся Classic/Beta до M3.
6. **Ассеты/provenance** — copy: `textures/linkmenu/bg-tower.png` + MSDF-атласы `fonts/link/*` (upstream SIL OFL 1.1, только атласы, без TTF); reuse: `wtmico`/`brands`; skip: донорский `icons2`, Lottie `welcome.json`, донорские play/pause/next/previous и аудио; новых шейдеров нет (фон/скрим — существующие `GlowPipeline`/`RoundedRectPipeline`). Авторство донорских ассетов — D6.
7. **i18n** — en/ru/uk: приветствия, welcome/subtitle, account/licensed/offline, quit, media, disclaimers, `video.style_linken`, `editor.linken_fixed/help`; `LangParityTest` зелёный.
8. **Тесты/сборка** — `MenuStyleTest` (4), `StyleRoutingTest` (3), `LinkenMenuLayoutTest` (5: 1280×720/1920×1080/2560×1440/3840×2160, вложенность, отсутствие пересечений), `ConfigRoundTripTest` (4: LINKEN round-trip, сохранение Beta-элементов, Beta/Vanilla пресеты). `clean build --no-daemon` — **181 тест, 0 падений/ошибок/скипов, Java-ворнингов 0**; jar `source/build/libs/linkenvisuals-1.0.76.jar` (7 192 589 байт, SHA-256 `39702CC7587E7D1C60C290E1DED8E62288D690E547C66982A185CDAE0A241069`).
9. **Docs** — D52 в `MERGE_DECISIONS.md`; ROADMAP/PORT_STATUS/README/README_RU обновлены (LINKEN M1 + M2–M6).
10. **Визуальная приёмка M1 (завершена 20.10.2026)** — `runClient` разрешён пользователем; проверены 4K-framebuffer **3840×2160** (F2 PNG; физического 4K нет — 2×1920×1080, окно выведено за экран) и **1280×720/GUI 2**. LINKEN-меню без обрезки/перекрытий: wordmark, часы/дата, приветствие, аккаунт-чип, кнопки, соцсети, медиа-карточка, футер. Esc не закрывает (два 4K-снимка побайтно равны), Quit показывает `Confirm` и через 3 с возвращается к `Quit`, повторный клик штатно завершает игру. Singleplayer → Select World, Multiplayer → ванильный `MultiplayerScreen`, Settings → `OptionsScreen` → Video Settings, аккаунт-чип/Alt Manager → наш `AltManagerScreen`. Соцсети открывают браузер (Telegram и Discord). Медиа-карточка с реальным SMTC-треком `Linken Acceptance Track` / `LinkenVisuals QA`, прогресс `0:03 / 1:15`; read-only — осознанно.
11. **Редактор/стили/регрессия** — LINKEN-редактор 1280×720 и 3840×2160: тулбар `Beta/Vanilla/Linken` + `SAVE` без пересечений, fixed-notice и help-бар; на 4K тулбар уже экрана из-за клампа 840 — не дефект. SAVE вернул в Video Settings. Цикл **Linken → Beta → Vanilla → Linken** отработал; Beta-меню и Vanilla-редактор не перекрашены LINKEN-слоем. После рестарта с `menuStyle=LINKEN` сразу открылся LINKEN-титул (`Active Style: Linken`), после рестарта с `VANILLA` — ванильный титул без перекраски.
12. **Лог/локальный options.txt** — в сессиях 1–2 было 186× `Skipping bad option:` (WARN). Это не дефект мода: предупреждения шли из старого локального `source/run/options.txt`; после перезаписи файла игрой (186 валидных строк, UTF-8/CRLF) сессия 3 дала **0** таких строк. `ERROR`/`Exception` в логах — 0 (только известное `Realms ... SignedJWT: FabricMC` в INFO). Все три сессии завершились штатным `Stopping!`, `BUILD SUCCESSFUL`. Код после v1.0.76 не менялся — этот шаг docs-only, без бампа.

### Следующая задача
- **Финальное «принято» M1 — за пользователем.** Визуальная приёмка 1280×720/4K, кнопок, соцсетей, медиа, редактора, цикла стилей и логов выполнена; M2 не начинать до этого слова.
- После «принято» — **M2 (splash/loading/connect)**, ориентир v1.0.77/D53, затем M3 (Link GUI shell), M4 (themes/config), M5 (pause/settings), M6 (multiplayer/parity) по ROADMAP.
- Открытые решения без изменений: LICENSE copyright, `MASTER_PROMPT.md` в публичном репо, UK-глифы MSDF, waypoints UX, StreamerMode nametag-санитизация.

### Риски
- 4K проверен в framebuffer 3840×2160; физического 4K-дисплея нет — остаётся риск отличий на реальном 4K/другом DPI.
- Редактор: тулбар фиксированной ширины 840 логических единиц; при 1280×720/GUI 2 и 4K пересечений нет, но экстремально высокий GUI scale не перепроверялся.
- MSDF-атласы донора: UK-глифы `і/ї/є/ґ` по-прежнему без нативных глифов (общий известный риск, фолбэк).
- Медиа-карточка read-only (нет донорского playback-бэкенда) — это осознанное отклонение, не баг.
- M2 не стартует до финального «принято» пользователя.

## Состояние на 2026-10-20 (чат №13 — TargetEsp) — v1.0.75

### Сделано
1. **D51 — TargetEsp**: 4 режима донора (Original/Ghosts/Circles/Crystals) на `AFTER_ENTITIES`: выбор цели по прицелу + retention 1 с, радиус 30; рейкаст видимости (кэш 50 мс); NPC-фильтр (armor stand / ping 0 + нулевой UUID, `Show NPCs`); инвиз скрывается только без видимой экипировки. Все билборды depth-tested — сквозь стены не видно. Original — world-billboard с пересчётом донорского экранного размера в мировые единицы (единственное визуальное отклонение). Ассеты `textures/esp/{marker,ghost_bloom}.png`, пайплайны sprite/glow/body. +11 тестов правил (итого 165).
2. **Evidence**: `clean build` — **165 тестов, 0 падений**; hands-free `runAutotest -PautotestWorld=AutoTestWorld -PautotestQuit=true` — **583 шага, 580 ok, 0 fail, 3 skip** (зомби в Peaceful + нет прав на `/setblock`), новых `ERROR`/`Exception` — **0**, клиент закрылся сам; отчёт `run/logs/linkenvisuals-autotest-20261020-094258.log`, 3 скриншота. Первый прогон поймал реальный краш Crystals (`BufferBuilder: Not building!` — смена слоёв в одном кадре), исправлено раздельными проходами по слоям.
3. **Jar**: `source/build/libs/linkenvisuals-1.0.75.jar`.

### Следующая задача
- Игровая приёмка v1.0.75: включить Target ESP, навести прицел на игрока/моба — Original (вращающийся маркер), Ghosts, Circles, Crystals; уйти за стену — эффект должен пропасть; невидимый игрок без брони/предметов — эффекта нет, с предметом в руке/бронёй — есть; `Show NPCs` на стойках/NPC.
- Пачка этого чата: WayPoints-II (чат-события + квикбинд + 2D-маркеры) → v1.0.76, GPS (`/gps` + HUD-стрелка) → v1.0.77.
- Открытые решения без изменений: LICENSE copyright, `MASTER_PROMPT.md` в публичном репо, UK-глифы MSDF, waypoints UX, StreamerMode nametag-санитизация.

### Риски
- Original — world-billboard вместо 2D-прохода: экранный размер по формуле донора, но при экстремальном FOV возможны субпиксельные отличия — проверить глазами.
- NPC-фильтр опирается на ping 0 + нулевой UUID; на серверах с NPC-плагинами возможны ложные срабатывания (отключается `Show NPCs`).

## Состояние на 2026-10-19 (чат №12 — пачка визуальных фиксов по отчёту пользователя) — v1.0.74

### Сделано
1. **D46 — «фейковые скругления» («и так на всём»)**: glass-шейдеры считали радиус угла без GUI-масштаба (`1.2×level`, кап 12 px), а тела/обводки — `level×guiScale` → из-под скруглённых панелей торчали квадратные полупрозрачные углы стекла. Теперь UV несёт радиус в пикселях устройства, шейдеры трактуют его напрямую (формула 1:1 с `rounded_rect.fsh`). Файлы: `GlassPanelPipeline.java`, `GlassWindowPipeline.java`, `glass_panel.fsh`, `glass_window.fsh`.
2. **D47 — Trails/JumpCircles/BlockOverlay/Waypoints не работали**: их собственные encoder-пассы не биндили `DynamicTransforms` (в 1.21.11 `RenderSystem.bindDefaultUniforms` ставит только Projection/Fog/Globals/Lighting) → геометрия вырождалась молча. Двухфазный хелпер: `Pipelines.writeWorldTransforms()` **до** открытия пасса + `bindWorldUniforms(pass, slice)` внутри (write мапит UBO — внутри пасса краш, пойман первым прогоном `/lvtest`); заменено 6 вызовов, включая `ShaderSkyPipeline`. Сигнатура `write(...)` сверена javap по yarn build.4.
3. **D48 — HitSound**: RETURN-гейт `if (!doAttack().getReturnValue()) return;` давал звук на блоках и тишину на мобах. Звук перенесён в `ClientPlayerInteractionManagerMixin.attackEntity` (после реального удара) + ванильное крит-условие для `Only Crit`; фейк-игрок озвучен в cancel-ветке `doAttack`.
4. **D49 — бинды клавиш (Zoom и др.)**: `KeySetting` не имел UI-бинда (оба GUI рисуют бинды только для `KeybindSetting`) — удалён; Zoom/Freelook/HandSwap/ElytraSwap на `KeybindSetting` (`code()`; дефолты C/LALT/NONE/NONE). `ConfigJson` читает бинд только из строки — legacy-числа игнорируются; +1 тест (`keybindIgnoresLegacyNumericValues`).
5. **D50 — вкладка Configs**: шапка категории (иконка/заголовок/описание/подчёркивание), stagger при входе + hover-пружины карточек, пустое состояние, векторный «×» вместо глифа U+2715 (нет в атласах), статусы на двойных кавычках (апострофа U+0027 тоже нет), 4 новых i18n-ключа en/ru/uk. Поведение/клики не менялись.
6. **Jar**: `linkenvisuals-1.0.74.jar` (автодеплой в MultiMC). Evidence: `clean build` — **154 теста, 0 падений**; hands-free `/lvtest` — **562 шага, 562 ok, 0 fail, 0 skip, новых ERROR/исключений — 0**, клиент закрылся сам.

### Следующая задача
- Игровая приёмка пользователем v1.0.74: углы HUD/ClickGUI при масштабах GUI 2/3/4 (не должно быть полупрозрачных квадратов), Trails/JumpCircles/BlockOverlay/Waypoints/Shader Sky (должны быть видны), HitSound (моб/крит/«только крит»/фейк-игрок), бинд клавиш в modern+classic GUI (Zoom дефолт C), вкладка Configs (шапка, hover, «×», статусы).
- **Остаток заказа этой пачки** (следующие чаты): (а) прицелы — улучшить встроенные + пиксельный редактор собственных; (б) 3-й стиль GUI «LINK» из донора (донорский «ClickGUI 3a», ~6k строк + ассеты + несвободные шрифты) — крупная отдельная работа, перед стартом нужен скоуп/решение по шрифтам и лицензии.
- Открытые решения без изменений: LICENSE copyright, `MASTER_PROMPT.md` в публичном репо, UK-глифы MSDF (і/ї/є/ґ), waypoints UX, StreamerMode nametag-санитизация.
- Тег/релиз GitHub — только по явному слову пользователя.

### Риски
- Glass-радиус теперь зависит от GUI-масштаба и без потолка 12 px (кламп — по половине квада, как у `RoundedRectPipeline`): при масштабе 4 радиус до 40 px — выглядит одинаково со телами по построению, но глазами проверить все три масштаба.
- Тексты без апострофов в статусах Configs — осознанно (атлас без U+0027); если понадобятся апострофы — регенерация атласа (тот же пункт, что UK-глифы).

## Состояние на 2026-10-19 (чат №11 — лог-фикс + in-game автотест) — v1.0.73

### Сделано
1. **D44 — двойной blur ClickGUI**: по логу пользователя (1.0.72) единственное наше предупреждение — `Can only blur once per frame`; причина — `ClickGuiScreen.render` вызывал `context.applyBlur()` после ванильного `Screen#renderBackground`. Повторный вызов и поле `blurUnsupportedLogged` удалены. В прогоне 1.0.73 — 0 таких строк.
2. **D45 — автотест в игре**: `dev.linkenvisuals.autotest` (`AutoTestDriver`, `AutoTestCommand`, `AutoTestReport` + 4 теста). `/lvtest start|stop|status` (алиас `/lvautotest`), автостарт `-Dlinkenvisuals.autotest=true`, авто-выход `.quit=true`; Gradle `runAutotest`, `-PautotestWorld=<save>`, `-PautotestQuit=true`. Свип всех модулей/настроек (boolean/slider/mode/color; keybind/text не трогаются), modern+classic ClickGUI, инвентарь, 4 моба (2 cow/2 zombie, NoAI, 1 HP) и атаки, jump/sneak/sprint, use item, place/break в креативе, restore конфига/позиции/слота, скан новых ERROR в `latest.log`, отчёт `logs/linkenvisuals-autotest-*.log` + 3 скриншота.
3. **Evidence**: `clean build` — 153 теста, 0 падений. Hands-free прогон на копии мира (читы вкл.): **564 шага, 564 ok, 0 fail, 0 skip**, новых ERROR/Exception — 0, blur-warning — 0, клиент сам закрылся. Первый прогон (557/7 skip) выявил, что `Tags` не синхронизируются на клиент; детекция мобов — по снимку entity id (тег остался для `/kill`). Скриншоты: `run/screenshots/lv-autotest-{1-world,2-clickgui,3-final}.png`.
4. **Jar**: `source/build/libs/linkenvisuals-1.0.73.jar` (автодеплой в MultiMC).

### Следующая задача
- Визуальная приёмка пользователем: в мире с читами выполнить `/lvtest start`, дождаться чат-отчёта; проверить размытие ClickGUI и эффекты. Для dev-рана: `.\gradlew runAutotest` (автостарт после входа в мир) или `-PautotestWorld=<save> -PautotestQuit=true`.
- Открытые решения без изменений: LICENSE copyright, MASTER_PROMPT в публичном репо, UK MSDF (і/ї/є/ґ), waypoints UX, StreamerMode nametag-санитизация.
- Тег/релиз GitHub — только по явному слову пользователя.

### Риски
- Автотест не заменяет визуальную проверку: он ловит исключения/варнинги и проверяет жизненный цикл модулей. В мультиплеере без op мобы/`tp`/`setblock` пропускаются (skip), свип модулей выполняется.

## Состояние на 2026-10-19 (чат №10 — Фаза 6 закрыта) — v1.0.72

### Сделано
1. **Ф6.1 — аудит пер-кадровых аллокаций (D42).** `pet/PetUltimate`: горячий путь ULT-сцены переведён на примитивы — `lastBodyPoint: Vec3d` → `lastBodyX/Y/Z` (double), физика иллюзий и туман перьев по компонентам, `yawFromDir`/`pitchFromDir` — double-перегрузки (Vec3d-делегаты для возврата). Было ~10 Vec3d/птица/кадр + 588 Vec3d/кадр в тумане → стало 2 Vec3d/птица/кадр (неизменяемые `pos`/`vel`, принято) и 0 в тумане; порядок float-операций сохранён построчно. `render/VanillaStylePipeline`: `getIconForKey` больше не аллоцирует на виджет каждый кадр — `ICON_CACHE` + `resolveIcon`. Остальное проверено и принято осознанно (см. D42).
2. **Ф6.2 — аудит draw-calls.** 0 per-frame регистраций пайплайнов/слоёв: все `RenderPipelines.register`/`RenderLayer.of` — static final или одноразовый `Pipelines.warmUp()` (guard `warmed`; `HudManager` под `loaded`, `MainMenuScreen.init`). 29 сайтов `createBuffer` — lazy-guard (uniform/dummy) либо динамическая геометрия под try-with-resources; суммарно единицы GPU-буферов на кадр. Сабмиты ограничены константами (ULT ≤43 raven + 2 слоя билбордов ≤294 перьев/42 glow; BlockOverlay — только target; вэйпоинты/трейлы — по спискам).
3. **Ф6.3 — инвентарь ресурсов.** Скрипт-сверка в обе стороны (id-литералы ↔ файлы, sounds.json ↔ .ogg, обратный поиск): 0 битых ссылок; 5 «нерезолвленных» id — рантайм-текстуры (screen_capture, hud_glass_capture/sharp, media_art, custom_menu_bg.png). sounds.json ↔ .ogg — чисто. **Удалены 45 файлов без ссылок** (8 легаси-шейдеров blur/border/shadow/texture + 36 донорских png + `ico_avatar.png.mcmeta`); `characters/rudy.json` оставлен как носитель CC-BY-4.0-атрибуции. Джар: 538 записей / 3.5 МБ.
4. **Ф6.4 — README/доки.** Оба README: раздел «Cosmetics, Pets & Waypoints» (11 скинов, ULT, Rudy, `/way`), строка «Pet ULT» в таблице управления, примечание о клиентских командах, структура (`pet/`, `waypoint/`, `characters/`, `lang/`, `sounds/`), `linkenvisuals_waypoints.json` в конфигах; в RU поправлен уровень заголовка UI-раздела. ROADMAP/PORT_STATUS синхронизированы.
5. **Ф6.5 — релизная сборка.** `clean build` — **149 тестов, 0 падений**; `linkenvisuals-1.0.71.jar` задеплоен в MultiMC; dev-прогон: 1 «Reloading ResourceManager», «Sound engine started», 0 «Failed to load»/«Couldn't find»/«cannot add it to event»/mixin/shader/«deferred»; 2 ERROR — ожидаемый dev-шум. Миксин-скан не требуется (новых mixin-целей нет).
6. **Ф6.6 — DoD-свип (D43, v1.0.72).** 0 `TODO`/`FIXME`/`System.out`/javac-ворнингов по нашему коду. Аудит «проглоченных» catch: исправлено 3 (`PetUltimate` play/stop звука → `LOGGER.debug`; `LoadingOverlay` fallback панорамы → warn-один-раз), 8 оставлены осознанно (fallback-move, `Result.failures`, sentinel `-1`, `interrupt()`, UI-статусы). LICENSE (MIT) ↔ README-бейдж ↔ `fabric.mod.json` согласованы; строка копирайта «StrangeVisuals Team & Contributors» (старое имя) — в открытых вопросах.

### Следующая задача
Игровая приёмка за пользователем: v1.0.68 (Rudy/Trails), v1.0.69 (11 скинов), v1.0.70 (ULT), v1.0.71 (аудит-правки: картинка не менялась, ULT — тот же сценарий), v1.0.72 (только логи в catch + warn-один-раз — визуал не тронут); проверка, что ничего не сломалось. Тег/релиз GitHub — только по слову пользователя (проект к нему готов: сборка/доки/инвентарь/DoD закрыты).

### Проверенные факты (для следующего чата)
- Ф6 закрыта; код-правки аудита — только `PetUltimate.java` (примитивы) и `VanillaStylePipeline.java` (кэш иконок); изменения v1.0.71 — аудит + 45 удалённых ассетов + README/доки.
- Удалённые ассеты восстановимы из истории git (последний коммит с ними — v1.0.70, `8c7d297`).
- `source/tools/*` — repo-only артефакты (превью + исходник шрифта), в jar не попадают; релевантность подтверждена как «оставить».
- Инвентарь-скрипт (одноразовый, вне репо): id-литералы `LinkenVisuals.id(...)`/`Identifier.of("linkenvisuals", ...)` → файлы; sounds.json → .ogg; обратный поиск; динамические семейства — Category/ClassicCategory iconName, ClassicRenderTheme, lang/<code>, MSDF, `"core/" + name`.

### Открытые решения (ждём пользователя)
- Игровая приёмка v1.0.68/1.0.69/1.0.70/1.0.71 — за пользователем.
- LICENSE: копирайт-строка — держатель не менялся (правило «лицензию не менять» в силе).
- `MASTER_PROMPT.md` — убирать ли из публичного репо.
- UK-глифы MSDF (фолбэк і/ї→i, є→э, ґ→г) — нужна ли регенерация атласа.
- WayPoints: дополнительный UX (кейбинд/GUI) — нужен ли.
- StreamerMode: nametag-санитаризация — нужна ли отдельной задачей.

## Состояние на 2026-10-19 (чат №9, продолжение 2) — v1.0.70

### Сделано
1. **Фаза 5.6 (остаток): ULT-анимация воронов (D41, v1.0.70).** Донорская 19-с сцена — `pet/PetUltimate.java` (константы 1:1: 2.4 s подъём → 2.6 s стягивание → 9.2 s удержание → 2.8 s разлёт → 3.0 s дисперсия, обрезка на 19 s). Hero + 42 иллюзии — `PetRig.renderUltRaven` (`entityTranslucent(textures/pet/voron.png)` + ARGB); перья (7/птицу, `dens = 1.05+1.55·attach`) и свечение — билборды пайплайнами `position_tex_color` (перья TRANSLUCENT, свечение `BlendFunction.LIGHTNING`; `NO_DEPTH_TEST`, без depth-write/cull); возврат 850 мс (`snapFlyTo`). Триггер — бинд `Pet ULT` (Cosmetics, дефолт NONE) + GLFW edge-poll (`PetRig.frame`; гейты «экран закрыт» + «скин Raven»; мид-сцена — игнор, во время возврата — перезапуск). Настройки: `Pet Alpha` 82 %, `Pet Glow` 65 %, `Pet Glow Colour` 0xFF141414. Звук `linkenvisuals:pet/ult` → `sounds/pet/itachi.ogg`. `oblok.png` не импортирован (0 ссылок в доноре); остальные отклонения — в D41.
2. **Находка dev-прогона (исправлена до выдачи jar)**: звуковой путь резолвился как `sounds/pet/sounds/itachi.ogg` (двойной `sounds/`) — файл переложен в `sounds/pet/`, имя в `sounds.json` — `linkenvisuals:pet/itachi`; заодно ассеты питомцев приведены к общему layout `textures/pet/*`. Повторный прогон чистый.
3. **Тесты**: **149, 0 падений** (+7: `PetUltimateTest` — окна фаз/перекрытие дисперсии, attach/detach, альфа птиц, smoothstep; `KeyNamesTest` — round-trip, NONE/−1, «KEY n»). Build зелёный; автодеплой `linkenvisuals-1.0.70.jar` в MultiMC.
4. **Dev-прогон**: 1 «Reloading ResourceManager», 0 «Failed to load»/«Couldn't find source», 0 ошибок миксинов, 0 звуковых предупреждений; клиент дошёл до титульного экрана, закрыт штатно (2 ERROR в логе — дев-шум: user properties/Realms offline).

### Следующая задача
Игровая приёмка пользователем: v1.0.68 (Rudy — поза/крылья/хват; Trails), v1.0.69 (11 скинов), **v1.0.70 (ULT)**: в Cosmetics забиндить `Pet ULT`, выбрать скин Raven, нажать в мире — 19-с сцена (подъём → стягивание/удержание → разлёт, перья/свечение) со звуком темы Itachi, в конце возврат на плечо за 850 мс; повторное нажатие мид-сцены игнорируется (во время возврата — перезапуск). Затем Фаза 6 в утверждённом порядке: 1) аудит пер-кадровых аллокаций, 2) аудит draw-calls, 3) инвентарь ресурсов, 4) README/доки, 5) релизная сборка. Тег/релиз — только по слову пользователя.

### Проверенные факты (для следующего чата)
- **Yarn-кэш**: проект собирается против `yarn 1.21.11+build.4`; в кэше fabric-loom лежат лишние `build.3` и `build.6` — javap по ним **врёт**. Пример: `PositionedSoundInstance.master(SoundEvent, float)` в build.4 переименован в `ui(...)`.
- **Ресурсы**: id-путь текстур — точный путь под `assets/<ns>/` (префикс `textures/` не добавляется автоматически); у нас вся графика — `textures/…` и в id указывается `textures/…`. Для `sounds.json`: имя `[<ns>:]<path>` ↔ файл `assets/<ns>/sounds/<path>.ogg`.
- **ULT-рендер**: `Camera.getRotation(): Quaternionf`; `camera.getHorizontalPlane()/getVerticalPlane()` — билборды; `RenderPipelines.POSITION_TEX_COLOR_SNIPPET` + `RenderPipelines.register` + `RenderLayer.of(...RenderSetup.texture("Sampler0", id)...)` (паттерн `Material.java`); `RenderLayers.entityTranslucent(id)` + цвет-аргумент `submitModel(..., color)` — альфа без шейдера.
- **ULT-константы (locked тестами)**: 19000/2400/2600/9200/2800/3000 мс; 42 иллюзии (сид `now ^ 0x9E3779B97F4A7C15L`); спавн птицы `520 + 14·i` мс + фейд 860 мс; detach после break = 0.95; постоянный фейд `i·0.0085`.
- **KeybindSetting**: ярлык + код; `KeyNames.codeOf` (обратный маппинг); персистенция в `ConfigJson`.

### Открытые решения (ждём пользователя)
- Игровая приёмка v1.0.68 (Rudy, Trails), v1.0.69 (11 скинов) и v1.0.70 (ULT) — за пользователем.
- LICENSE: копирайт-строка — держатель не менялся (правило «лицензию не менять» в силе).
- `MASTER_PROMPT.md` — убирать ли из публичного репо.
- UK-глифы MSDF (фолбэк і/ї→i, є→э, ґ→г) — нужна ли регенерация атласа.
- `source/tools/*` — проверить актуальность при переносах Ф5.
- WayPoints: дополнительный UX (кейбинд/GUI) — нужен ли.
- StreamerMode: nametag-санитаризация — нужна ли отдельной задачей.

## Состояние на 2026-10-19 (чат №9, продолжение) — v1.0.69

### Сделано
1. **Фаза 5.6 (остаток): питомцы — 11 скинов (D40, v1.0.69).** Новый пакет `dev.linkenvisuals.pet`: `PetSkins` (None + Bat, Parrot, Raven, Fairy, Bee, Vex, Fox, Pig, Frog, Pufferfish, Slime), `PetGroundSampler`, `PetWanderController` (фикс-тик 20 Гц, инжектируемый `GroundProbe` — тестируется без мира), `PetRig` (рантайм + рендер). Рендер — `AFTER_ENTITIES` → camera-relative `manager.render` (паттерн KillEffects), поза/свет/варианты — патчи render state после `getAndUpdateRenderState`; raven — `submitModel` с `pet/voron.png`; без UserSyncManager/сети, Naruto не портирован. Настройки в Cosmetics: `Pet` + Radius/Lift/Speed/Size/First Person (дефолты донора, скин None). Весь кадр под защитным `catch` (warn с троттлингом 30 с + пауза 5 с).
2. **Тесты**: **142, 0 падений** (+7: `PetWanderControllerTest` — seed/догон/телепорт/лиши/reset; `PetSkinsTest` — round-trip меток). Build зелёный; автодеплой в MultiMC сработал (`linkenvisuals-1.0.69.jar`).
3. **Dev-прогон**: 1 «Reloading ResourceManager», 0 «Failed to load»/«Couldn't find source», 0 ошибок миксинов; dev-клиент закрыт штатно.

### Следующая задача
**ULT-анимация воронов (v1.0.70)** — донорский `Raven.java` 458–1008 (beginReturn/renderReturn/renderUltimate/renderMainAscendRaven/billboards/playUltSound) + импорт ассетов `pero.png`, `oblok.png`, `sounds/itachi.ogg` (одобрено) + бинд. Затем игровая приёмка пользователем (питомцы v1.0.69 + Rudy/Trails из v1.0.68). Затем Фаза 6 по порядку: перф-аудит аллокаций → draw-calls → инвентарь ресурсов → README/доки → релизная сборка. Тег/релиз — только по слову пользователя.

### Открытые решения (ждём пользователя)
- Игровая приёмка v1.0.68 (Rudy, Trails) и v1.0.69 (11 скинов) — за пользователем.
- LICENSE: копирайт-строка — держатель не менялся (правило «лицензию не менять» в силе).
- `MASTER_PROMPT.md` — убирать ли из публичного репо.
- UK-глифы MSDF (фолбэк і/ї→i, є→э, ґ→г) — нужна ли регенерация атласа.
- `source/tools/*` — проверить актуальность при переносах Ф5.
- WayPoints: дополнительный UX (кейбинд/GUI) — нужен ли.
- StreamerMode: nametag-санитаризация — нужна ли отдельной задачей.

### Проверенные факты (для следующего чата)
- **Питомцы**: `Entity.getEntityWorld()` (НЕ `getWorld()`) — в 1.21.11; `EntityDimensions.height()` — доступор; `ParrotEntity.lastFlapProgress/lastMaxWingDeviation` публичны (крылья считаются аналитически); `setVariant`/`setSleeping` НЕ публичны — варианты только через render state (`ParrotEntityRenderState.variant/parrotPose`, `FoxEntityRenderState.type/sitting/sleeping/sleepingDirection/walking`); `LightmapTextureManager.MAX_LIGHT_COORDINATE`, `OverlayTexture.DEFAULT_UV`, `RenderLayers.entityCutoutNoCull(id)`; `queue.submitModel(model, state, matrices, layer, light, overlay, color, null)` — 8-арг default-оверлоад; `manager.getAndUpdateRenderState(ent, pt)` + `manager.render(state, cameraState, 0,0,0, matrices, queue)`.
- **Dev-прогон обязателен** перед выдачей jar (см. секцию v1.0.68 ниже): reload ловит битые шейдеры/ресурсы; закрытие — kill `java.exe` с `devlaunchinjector` в CommandLine.

## Состояние на 2026-10-19 (чат №9) — v1.0.68

### Сделано
1. **Хотфикс D38 (v1.0.67): пайплайн Trails ронял загрузку шейдеров.** `TrailsRenderer` (с коммита b5bcf1e, «v1.0.62») ссылался на `linkenvisuals:core/position_color`, файлов не было → любая сборка 1.0.62–1.0.66 падала на reload (`Failed to load required shader programs` → сброс ресурспаков; каскадом терялись и ванильные шейдеры), т.к. пайплайн регистрируется на старте в `Pipelines.warmUp()`. Добавлены `assets/linkenvisuals/shaders/core/position_color.vsh/.fsh` (семантика ванильной пары 1.21.11). Evidence: повторный dev-запуск — 1 reload, 0 ошибок, `precompile deferred` = 0. У пользователя не проявлялся: последняя сессия MultiMC — v1.0.45 (лог 18.10).
2. **Фаза 5.6: Rudy — порт персонажа (D39, v1.0.68).** Ассеты `characters/rudy.*` (CC-BY-4.0) + чистое ядро `RudyMesh` (парсер, 8 тестов) + `RudyCharacter` (загрузка/текстуры/submit/эмиссия/поза) + 4 новых миксина и WrapOperation над `submitModel` в `LivingEntityRendererMixin`. Архитектура: `submitCustom` по одному на материал, `setAngles` перевызывается в draw-лямбде (поза на submit не финальна), пивоты/хват — из донора, броня скрыта, только локальный игрок. UI: Cosmetics → «Character» (Default/Rudy).
3. **Тесты**: 135, 0 падений (+8 RudyMeshTest); build зелёный; jar `linkenvisuals-1.0.68.jar`, автодеплой в MultiMC сработал (запись «путь 1.21.111 не существует» в разделе v1.0.66 неактуальна).
4. **Ремап-скан jar 1.0.68**: 53 миксин-класса (включая 4 новых), 0 yarn-имён в member-рефах — dev-запуск ремап НЕ проверяет (named-маппинги), скан обязателен.

### Следующая задача
**Питомцы (5.6c-остаток = CosmeticEffects-задача)**: локальная адаптация донорского `Raven` (≈2950 строк: симуляция, 11 скинов ванильных мобов, ULT-анимация воронов) + `PetWanderController`/`PetGroundSampler` — **без UserSyncManager** (п.3); перед стартом уточнить у пользователя состав (какие скины, ULT — да/нет). После — Ф6 по утверждённому порядку: 1) перф-аудит аллокаций, 2) draw-calls, 3) инвентарь ресурсов, 4) README/доки, 5) релизная сборка.

### Открытые решения (ждём пользователя)
- Питомцы: состав порта (скины, ULT-анимация воронов).
- Trails: был неработоспособен с 1.0.62 — в игровой приёмке посмотреть визуал (упрощённые billboard'ы).
- LICENSE: копирайт-строка — держатель не менялся (правило «лицензию не менять» в силе).
- `MASTER_PROMPT.md` — убирать ли из публичного репо.
- UK-глифы MSDF (фолбэк і/ї→i, є→э, ґ→г) — нужна ли регенерация атласа.
- `source/tools/*` — проверить актуальность при переносах Ф5.
- WayPoints: дополнительный UX (кейбинд/GUI) — нужен ли.
- StreamerMode: nametag-санитаризация — нужна ли отдельной задачей.

### Проверенные факты (для следующего чата)
- **Dev-прогон перед выдачей jar — обязателен** (после D38): `runClient` (окно), лог `source/run/logs/latest.log`, DEBUG — в `debug.log` (`precompilation deferred`); ресурсы — из `build/resources/main`; закрытие — kill `java.exe` с `devlaunchinjector` в CommandLine. Ловит битые шейдеры/ресурсы на reload; ремап целей — НЕ проверяет (named-маппинги).
- **Шейдеры в пайплайнах**: ванильные — `Identifier.of("minecraft", "core/…")` (пример: CirclePipeline); свои — файлы `assets/linkenvisuals/shaders/core/<name>.vsh|.fsh` (`#version 330` — как в большинстве; `#moj_import <minecraft:dynamictransforms.glsl>`/`projection.glsl`); сверка ссылок: `Select-String -Pattern '"core/([a-zA-Z_0-9]+)"'` по java × список файлов.
- **Rudy-факты**: draw-лямбда `submitCustom` обязана сама звать `model.setAngles(state)`; `matrices.peek().copy()` — снимок матриц для лямбды; «невидим для игрока» = `invisible && !invisibleToPlayer`; свои текстуры — `NativeImage.read` + `NativeImageBackedTexture(id::toString, image)` + `registerTexture`; флаги по render state — duck-интерфейс + миксин на state (`@Unique` поле; паттерн HitColor/Rudy).

## Состояние на 2026-10-19 (чат №8) — v1.0.66

### Сделано
1. **Пачка 3 Ф5.7 (D36, v1.0.65): StreamerMode — полный порт движка без god-класса.** Таблица → ресурс `assets/linkenvisuals/streamer/patterns.json` (донат/монеты/анархия/гриф/26 парольных команд); чистый движок `util/text/StreamerRules` (предфильтр одним regex, whole-token ник, пары «друг=ник», донат/монеты, номера 999/666, маска `/login` с сохранением пробелов, §/&-коды и hex сохраняются); `util/text/StreamerTextSanitizer` (обход Text-компонент со стилями, без аллокаций на «мимо»; маска поля ввода той же длины); `util/text/StreamerPatterns` (Gson-загрузчик, громкий фейл). Хуки (7): ChatHud.addMessage ×2, PlayerListHud.getPlayerName/setHeader/setFooter, InGameHud.setOverlayMessage, ScoreboardObjective.getDisplayName, ScoreboardEntry.name/display/formatted, TextFieldWidget (маска в существующем WrapOperation).
2. **Модуль**: `StreamerModeModule` — кеш `StreamerRules` с инвалидацией по настройкам/нику сессии, `rules()` (null при выключенном); мёртвый `shouldHideAnarchy()` удалён; FriendManager заменён строкой «real=nick» (отклонение, D36).
3. **Тесты**: 119, 0 падений (+15: StreamerRulesTest 12, StreamerPatternsTest 3). BUILD SUCCESSFUL, jar `source/build/libs/linkenvisuals-1.0.65.jar` (1 786 339 байт).
4. **Ремап-скан 1.0.65**: 6 затронутых миксинов — 0 yarn-остатков; все таргеты — методы по `minecraft-merged-intermediary`; `comp_2129()` — легальный метод-аксессор record-компонента (не дефект).
5. **WayPoints активирован (D37, v1.0.66)**: клиентская команда `/way` (алиас `/waypoint`) — `add <name> me`, `add <name> x y z`, `remove`, `list`, `clear` (донорские семантика и отказ на дубликат); персистентность `config/linkenvisuals_waypoints.json` (AtomicFiles + configVersion); `WaypointsRenderer` переведён на общий стор, тоггл модуля больше не стирает точки; тесты 127 (+8 `WaypointStoreTest`). **Ф5.7 закрыта.**

### Следующая задача
**Игровая приёмка пользователем** пачек 1–3 + WayPoints (jar 1.0.66; автодеплой в MultiMC недоступен — путь `D:\MultiMC\instances\1.21.111` на машине не существует, jar выдаётся вручную). Затем — решение по Naruto/CosmeticEffects (5.6) и Ф6 (перф-профилирование, инвентарь ресурсов, релизная сборка).

### Открытые решения (ждём пользователя)
- LICENSE: копирайт-строка — держатель не менялся (правило «лицензию не менять» в силе).
- `MASTER_PROMPT.md` — убирать ли из публичного репо.
- UK-глифы MSDF (фолбэк і/ї→i, є→э, ґ→г) — нужна ли регенерация атласа.
- `source/tools/*` — проверить актуальность при переносах Ф5.
- WayPoints: реализована команда `/way` (D37); нужен ли дополнительный UX (кейбинд/GUI для добавления) — скажи.
- Naruto/CosmeticEffects (5.6): пропустить или портировать (Naruto — энейблер настроек; CosmeticEffects — зонтичный модуль, ghost-bloom ветки)?
- Питомцы/Rudy (5.6c): D29 пропустил (только UserSyncManager-режим) — подтверди пропуск.
- StreamerMode-остаток: nametag-санитаризация (подписи игроков в мире) отложена — нужна ли отдельной задачей.

## Состояние на 2026-10-19 (чат №7) — v1.0.64

### Сделано
1. **Пачка 2 Ф5.7 (D35, v1.0.64)**: `ItemScroller` (drag по модификаторам: shift/alt+shift — стаки, alt — по совпадению, ctrl — по одному; колесо — переносы одного предмета/стаков/всего; хуки `HandledScreenMixin.mouseClicked/mouseDragged/mouseReleased/mouseScrolled` + `HandledScreenAccessor` из пачки 1; чистая логика `util/ItemScrollerMath` + MoveBudget, анти-кик пол 50 мс), `ElytraSwap` (фазовый своп через слот 6, 1 клик/тик; временный InventoryScreen; `util/InventoryMath.pickBest`; без Instant и `/fly` — D35), `TotemTracker` (хук `ClientPlayNetworkHandlerMixin.onEntityStatus` + чат-сообщение с меткой зачарованности; дедуп `util/PopDedupe`), HUD `TotemCounterHud`/`UseTrackerHud` в `HudManager.ELEMENTS` (видны в edit-mode).
2. **Гигиена catch закрыта**: не 6, а 11 проглоченных исключений (ещё 5 — в `CustomTextureLoader`) → LOGGER (warn / warn-once для пер-кадровых путей / debug для best-effort cleanup). TODO/FIXME/System.out/catch-ignored по `src/main/java` = 0.
3. **Тесты**: 104, 0 падений (+21: ItemScrollerMath 11, InventoryMath 5, PopDedupe 5; −1: `inventoryIndexToSlot` переехал в `InventoryMathTest`, `DragPath` — только сэмплирование).
4. **Ремап-скан** jar 1.0.64: 47 миксинов, 0 yarn-имён; аннотации новых инжектов ремапнуты (`method_25394`, `method_11113`; `getStatus` → `method_11470:()B`). Стабильные `rotation(float)`/`getString()`/`comp_*` валидированы по `minecraft-merged-intermediary` (не method_* — так и должно быть).

### Следующая задача
**StreamerMode** (остаток пачки 2 Ф5.7) — отдельной сессией: `StreamerModeUtils` (3048 строк) + `StreamerModePatterns`/`StreamerModeState` + FriendManager-зависимость; таблицу паттернов в ресурс; фильтры ChatHud/PlayerListHud/scoreboard. Перед стартом уточнить объём у пользователя (полный донорский фильтр vs минимальный). Затем `WayPoints/GPS` (нет команды добавления точек — открытый вопрос).

### Открытые решения (ждём пользователя)
- LICENSE: копирайт-строка «StrangeVisuals Team & Contributors» — обновлять ли держателя (правило «лицензию не менять» пока в силе).
- `MASTER_PROMPT.md` — убирать ли из публичного репо.
- UK-глифы: в MSDF-атласах нет нативных і/ї/є/ґ (фолбэк і/ї→i, є→э, ґ→г) — нужна ли регенерация атласа.
- `source/tools/*` — проверить актуальность скриптов при переносах Фазы 5.
- `WaypointsRenderer.clear()` намеренно ничего не делает (комментарий «Keep waypoints on disable») — решить: чистить при disable или нет.
- `WaypointsModule` — нет команды добавления точек (`addWaypoint` нигде не вызывается). Нужен ли `/wp add`?
- StreamerMode: объём порта (см. «Следующая задача»).

## Состояние на 2026-10-19 (чат №6) — v1.0.63

### Сделано
1. **Пачка 1 Ф5.7 закрыта (D34, v1.0.63)**: `SwingAnimation`, `HitColor`, `Freelook`, `ItemPhysics` — полные порты вместо заглушек D33. Хуки: `HeldItemRendererMixin.swingArm` (RETURN, guard main arm), `LivingEntityRenderStateMixin` + `LivingEntityRendererMixin` (`updateRenderState` TAIL, `getMixColor` RETURN), `MouseMixin.updateMouse` (HEAD/RETURN), `CameraMixin` (`@ModifyExpressionValue` на `Entity.getYaw/getPitch`), `ItemEntityRenderStateMixin` + `ItemEntityRendererMixin` (`updateRenderState` TAIL, `render` HEAD cancellable).
2. **Чистая логика вынесена и покрыта тестами**: `util/HitColorMath` (7), `util/ItemPhysicsMath` (6), `util/DragPath` (6), `SwingAnimationModule.curve/xAngle` (7). Тесты: 83, 0 падений (было 50).
3. **Конфиг-гэп закрыт**: `ConfigJson` теперь сохраняет/читает `KeySetting` и `StringSetting` (бинды Freelook/ElytraSwap/HandSwap/Zoom и ники StreamerMode раньше терялись); round-trip-тест в `ConfigJsonTest`.
4. **Убрана инертная настройка** `HitColorModule.colorArmor`: armor-тинт через `EquipmentRenderer` → `submitModel` (color — index 6) не реализован, настройка ничего не делала. Причина и путь возврата — D34.
5. **Ремап-скан**: 47 миксинов собранного jar, 0 yarn-имён в member-ref'ах (узкий и широкий проходы); `HandledScreenAccessor` зарегистрирован в `linkenvisuals.mixins.json`.
6. Мелочи: выравнивание отступа `@Inject` в `HeldItemRendererMixin`; `mod_version` 1.0.62 → 1.0.63.

### Следующая задача
**Пачка 2 Ф5.7** (D34-остаток): `ItemScroller` (drag-перемещение через `HandledScreenAccessor` + `DragPath`, `clickSlot`), `ElytraSwap` (клик по слоту 6), `StreamerMode` (паттерны в ресурс + фильтры ChatHud/PlayerListHud/scoreboard), `TotemTracker` (`ClientPlayNetworkHandler.onEntityStatus` + `EntityStatuses.USE_TOTEM_OF_UNDYING`), HUD-элементы `TotemCounter`/`UseTracker` в `HudManager.ELEMENTS`. Затем — 6 оставшихся `catch (... ignored)` → лог, `WayPoints/GPS`.

### Открытые решения (ждём пользователя)
- LICENSE: копирайт-строка «StrangeVisuals Team & Contributors» — обновлять ли держателя (правило «лицензию не менять» пока в силе).
- `MASTER_PROMPT.md` — убирать ли из публичного репо.
- UK-глифы: в MSDF-атласах нет нативных і/ї/є/ґ (фолбэк і/ї→i, є→э, ґ→г) — нужна ли регенерация атласа.
- `source/tools/*` — проверить актуальность скриптов при переносах Фазы 5.
- `WaypointsRenderer.clear()` намеренно ничего не делает (комментарий «Keep waypoints on disable») — решить: чистить при disable или нет.
- `WaypointsModule` — нет команды добавления точек (`addWaypoint` нигде не вызывается): модуль визуально инертен без команды. Нужен ли `/wp add`?

## Состояние на 2026-10-18 (чат №5, Muse)

### Сделано
1. **Архивы разобраны**: оба скачаны через Git LFS из ветки `стщеуте1018` (пользователь сам запушил: коммит `chore(archives)`), SHA256 сошлись, распакованы в `~/workspace/mcmerge/archives/`. `StrangeVisuals-1.21.11.zip` (253 МБ) — локальная копия на том же HEAD v1.0.44, реального дрейфа от main нет (только CRLF-шум в 9 ресурсных файлах + 2 пустые папки, `git status` чистый). `визу34алы.rar` (790 МБ) — донор LinkenVisuals 2.0: 779 java-файлов, punchy 181 файл (не трогаем), все цели переноса из PORT_STATUS на месте (MSDF, BlockOverlay, MotionBlurRenderer, Render2D/3D, ViewModel/Hands, косметика 74 файла, модули белого списка сверены точечно).
2. **Ф3.8 закрыта (v1.0.45, D17)** — решение пользователя: триггер edit-mode остаётся T (чат), но через явный флаг. `HudManager`: `isEditScreen`/`instanceof ChatScreen` удалены, новый `editing` + `setEditing`/`isEditing`; `ChatScreenMixin`: инъекции `init`→`setEditing(true)`, `removed`→`setEditing(false)`; тихий `catch (IllegalStateException ignored)` вокруг `applyBlur()` → `LOGGER.warn` один раз (`blurUnsupportedLogged`). Тест `HudManagerEditModeTest` (round-trip флага). Вне скоупа остались `instanceof ChatScreen` в `Animations.chat()`, `HandEditor:45`, `PotionSlot:31`.
3. **Игровой тест Ф3.8 (v1.0.45) подтверждён логом** (`D:\MultiMC\instances\1.21.111\.minecraft\logs\latest.log`, 22:06): запуск `linkenvisuals 1.0.45`, вход в мир «New World», выход; режим редактирования HUD (T / Esc), перетаскивание элементов — спама в логе нет, WARN/ERROR = 0. Ветка `f3.8-hud-edit-flag` смержена в `main` и запушена в `origin/main`.
4. **Ф3.9 закрыта (v1.0.46, D18)** — README-дрейф устранён (en/ru зеркально): категории ClickGUI → `Combat, Movement, Visual, Player, Misc, Config` (факт `Category.java`); конфиг → 8 реальных файлов в `.minecraft/config/` вместо одного; косметика → 9 носимых (`Embers, Halo, Kagune, Orbit, Runes, Seraph, Shards, Spires, Tendrils`), «Bandana» не существует в коде (`grep -rni bandana` = 0); попутно clone-URL → `KurumaOfficial`, jar → `linkenvisuals-<version>.jar`.
5. **Ф3.10 закрыта (v1.0.47, D19)** — аудит без изменения кода: секретов в `AccountStorage` нет (только ники + offline-UUID, токенов/паролей нет); миксины `Yggdrasil*` (2 шт.) — нормализация неподписанных текстур, обхода аутентификации нет, ToS-риск минимальный; Sodium/Iris — 0 упоминаний в коде/`fabric.mod.json`: с Sodium конфликтов архитектуры нет (пост-цепочка через `WorldRenderEvents.END_MAIN`, чанки не трогаются; не тестировалось), с шейдерами Iris пост-эффекты и кастомное небо в финальный кадр, скорее всего, не попадут. Workflow изменён по приказу пользователя: пачки коммитов прямо в `main`, без веток на фичу; тестирование — пачкой после.
6. **Фаза 4 закрыта (v1.0.49, D20+D21)** — каркас модулей: `module/Module` (id/info/enabled, `onEnable`/`onDisable`), `module/Modules` (фасад + `init()`), `ModuleRegistry.registerModule`/`module(id)`; legacy-статики не тронуты; тест `ModuleLifecycleTest` (3 кейса: регистрация, lifecycle на тоггле, молчание на `toggleSilently`).

### Следующая задача (старт нового чата)
**Фаза 5 (ROADMAP)** — переносы из донора по одной фиче на коммит; перед каждой — таблица «параметр → реализация на 1.21.11». Ближайшая: **5.5 Оптимизации** (LRU VoxelShape, int-ядро линий, scratch-буферы, batched dot-grid/hatch — только где есть циклы). Внимание: п.5.7 (модули) — пачками с подтверждением пользователя; скоуп переносов решает пользователь.
1. **Тест Ф3.1 (1.0.35) подтверждён логом** (`D:\MultiMC\instances\1.21.111\.minecraft\logs\latest.log`, 16:37): запуск `linkenvisuals 1.0.35`, вход в мир «New World», выход («Stopping singleplayer server as player logged out» → «Saving worlds»); `wrong thread` = **0** (было 9), всего WARN/ERROR = 0. Критерий закрыт — регрессии «Rendersystem called from wrong thread» на DISCONNECT-пути нет.
2. **Ф3.2 закрыта (v1.0.36, D8)**: config-окно `ClickGuiScreen` (бывшие строки 473–804) вынесено в новый `clickgui/ConfigPanel.java` (486 строк); `ClickGuiScreen` 1794 → 1394 строк; 16 RU-строк → ключи `gui.config.*` (en/ru/uk), резолв через `dev.linkenvisuals.i18n.I18n.get`; заодно локализован placeholder поиска (`gui.search.placeholder`). Поведение не менялось: статические поля остались статическими, скроллбар — колбэк, шрифты биндятся в `init()`.
3. Гигиена: lang-файлы нормализованы в LF (в git-блобах лежал CRLF с прежних коммитов) — CRLF-шум ушёл.
4. `build` зелёный (27 тестов, LangParityTest с новыми ключами), jar `linkenvisuals-1.0.36.jar` задеплоен в MultiMC (старый удалён деплоем).
5. **Ф3.3 закрыта (v1.0.37, D9)**: идентификация виджетов — только по ключам перевода (новый `util/TextKeys`); `TitleScreenMixin`/`GameMenuScreenMixin`/`VanillaStylePipeline` — ключи вместо `contains(...)`, 12 вызовов `getIconFor` → `getIconForKey`; `TextFieldWidgetMixin` и `CameraMixin.setRotation` переведены с ordinal на slice-анкеры по байткоду (javap yarn build.4: format@234/drawText@258/getWidth@269; yaw@258/setRotation@269/getScale@294); ремап целей подтверждён дампом аннотаций собранного jar (промежуточные `class_*`, refmap не нужен). Видимое изменение — семантические иконки кнопок под-экранов настроек (раньше из-за порядка проверок почти все получали шестерёнку) — ждёт игровой проверки вместе с визуалом 1.0.36.
6. **Ф3.4 (п.3) закрыта (v1.0.38, D10)**: 4 loading-миксина (645 стр.) сведены к общему `render/LoadingOverlay` (165 стр.); миксины 645 → 317 стр.; удалены 4 копии `withAlpha` (→ `ColorMath.argb`), 4 копии lazy-загрузки MSDF-шрифтов (теперь статический кэш `LoadingOverlay.titleFont()/textFont()` — один на все 4 экрана) и 4 копии beta/vanilla-сцен; `Screen#renderPanoramaBackground` (protected) передаётся через `@FunctionalInterface PanoramaDrawer`.
7. **Хотфикс краша 1.0.38 → 1.0.39 (D11)**: игра падала на лоаде (`CameraMixin`, «Invalid name: getScale:») — цель slice `to` осталась yarn-именем при ремапе (owner ремапнут, имя нет). Заменена на `Camera.clipToSpace(F)F` (класс, чьи рефы доказанно ремапятся); javap jar 1.0.39 — все цели intermediary; скан всех 40 mixin-классов jar — 0 yarn-имён в member-ref'ах аннотаций (контроль на 1.0.38 находит ровно `class_1309;getScale:()F`).
8. **Ф3.4 (п.4) закрыта (v1.0.40, D12)**: единый цветовой свод — 16 клонов `argb`/`withAlpha` удалены, 302 вызова → `ColorMath.withAlpha(rgb, 0..255)`; `mix`-клоны ClickGui/NameTags/WidgetStylePipeline → `ColorMath.mix` (WidgetStylePipeline.mix удалён, 4 внешних вызова обновлены), `MainMenuScreen.mix` — делегат с прежней непрозрачной семантикой; `centredTop` (ClickGui+ConfigPanel) ≡ `MsdfText.topFor` — 17 вызовов, методы/константы удалены; новый единый `render/Fonts` вместо копий MSDF-обвязки в `LoadingOverlay`/`WidgetStylePipeline`/`CustomMultiplayerScreen`/`MainMenuScreen`/`MainMenuEditorScreen` (+ ConnectScreenMixin); javap-скан jar 1.0.40 — 24 member-ref'а, 0 yarn-имён.
9. **Ф3.4 (п.4b) закрыта (v1.0.41, D13)**: шрифтовые константы `TEXT_*/BOLD_*` ещё в 13 файлах (12 HUD + `clickgui/classic/util/FontDraw`) → `Fonts`; 18 load-сайтов заменены (`text`/`regular` → `Fonts.textFont()`, `bold`/`menuFont` → `Fonts.titleFont()`), поля-алиасы сохранены; `FontDraw.getFont` — тернарник без try/catch; 2 тройные пустые строки-регрессии схлопнуты; миксины не тронуты (javap-скан не требовался). 
10. **Ф3.5 закрыта (v1.0.42, D14)**: `HudLayoutStorage`/`PotionLayoutMode` → атомарные записи; найдены и переведены 4 ручные копии tmp+ATOMIC_MOVE (AccountStorage, HandPose, MacroManager, MainMenuConfig) — теперь 7/7 конфиг-писателей через `util.AtomicFiles` (укреплён: tmp удаляется best-effort при сбое); `ConfigManager.CONFIG_VERSION = 1` пишется в профиль/глобальный/legacy-миграцию, при чтении — warn «файл новее сборки».
11. **Ф3.6 закрыта (v1.0.43, D15)**: пер-кадровые аллокации убраны — `KeybindPanel` пересобирал `entries` из `width()` каждый кадр → кэш по новому `ModuleInfo.revision()` (4 точки мутаций кейбиндов: arm/cancel/resolve/clear; uppercase-ключ строится один раз в `Entry`); `Cosmetics` 6 × `new CosmeticContext`/кадр → переиспользуемый `CosmeticContext.set()` (конструктор делегирует `set`, прочие 4 создающих сайта не тронуты); `HudManager` drag-путь: `double[1]`×2 + `float[2]`×2/кадр → статические `CURSOR_X/Y`/`SNAP_OUT`; `Pipelines.warmUp()` из `render()` → одноразовый `if (!loaded)` первого кадра. Миксины не тронуты (javap-скан не требовался).
12. **Ф3.7 закрыта (v1.0.44, D16)**: `MediaWatcher` — `-ExecutionPolicy Bypass` убран (доказано: политика не гейтит stdin — прогон под форс-`Restricted` дал 6 строк/8 c, как и без флага); SCRIPT переписан в 9 строк, каждая законченный statement (PS 5.1 может исполнять stdin построчно: старый многострочный скрипт на .NET-стенде молча умирал за 0.5 c/0 байт, новый — жил 9 c/6 строк; Java-репродукция мода зелёная во всех прогонах); не-Windows → один INFO «unsupported platform» (раньше — молча); внешний скрипт `config/linkenvisuals_media.cmd|.sh` заменяет встроенный (тот же TAB-формат, описан в javadoc); `parse` + `MediaWatcherTest` (5 тестов).

### Следующая задача (старт нового чата)
**Ф3.8 (ROADMAP п.8)** — `HudManager`: edit-mode не через «is a ChatScreen»; убрать тихий catch. Дальше по порядку: Ф3.9 README-дрейф → Ф3.10 секреты/Yggdrasil/Sodium-Iris.

### Открытые решения (ждём пользователя)
- LICENSE: копирайт-строка всё ещё «StrangeVisuals Team & Contributors» — обновлять ли держателя (правило «лицензию не менять» пока в силе).
- `MASTER_PROMPT.md` — убирать ли из публичного репо.
- UK-глифы: в MSDF-атласах нет нативных і/ї/є/ґ (работает фолбэк і/ї→i, є→э, ґ→г) — нужна ли отдельная задача регенерации атласа.
- `source/tools/*` — проверить актуальность скриптов при переносах Фазы 5.

### Проверенные факты (для следующего чата)
- Сборка: `cd source; $env:JAVA_HOME='C:\Program Files\Java\jdk-21'; cmd /c ".\gradlew.bat build --console=plain 2>&1" | Select-Object -Last 40`; бамп версии — `-PbumpVersion` на ОДНОМ вызове Gradle (не на каждом).
- `git push` работает (GIT_TERMINAL_PROMPT=0); деплой в MultiMC автоматический (`multiMcModsDir` из `%USERPROFILE%\.gradle\gradle.properties`), старые `linkenvisuals-*.jar` удаляются перед копированием.
- Логи для игровых проверок: `D:\MultiMC\instances\1.21.111\.minecraft\logs\latest.log` (+ ротация `*.log.gz` рядом).
- Кириллица в консоли: `chcp 65001`; в PS 5.1 `[System.IO.File]::ReadAllText` — только абсолютные пути; lang-файлы теперь LF.
- i18n: собственный движок `dev.linkenvisuals.i18n.I18n.get(key, args)` — встроенные бандлы en/ru/uk из classpath, фолбэк en → vanilla → сам ключ; `%s`-формат безопасен (исключения логируются).
- Миксины и ремап: refmap не используется (`linkenvisuals.mixins.json` без поля `refmap`) — loom ремапит цели `@At`/slice на `remapJar`; проверка после сборки — `javap -v -p` по классу из jar: цели должны быть в промежуточных именах (`class_*`/`method_*`/`field_*`).
- Ремап-скан ОБЯЗАТЕЛЕН на каждой сборке с новыми целями: yarn-имя, оставшееся в `target=` (не `method_`/`field_`/`class_`), = гарантированный краш при лоаде («Invalid name: …»), даже если билд зелёный. Regex для скана всех mixin-классов: `Lnet/minecraft/[A-Za-z0-9_$/]+;(?!method_|field_|class_)[A-Za-z_][A-Za-z0-9_]*(?=:|[(])` — должно быть 0 совпадений (контроль 1.0.38: ровно `class_1309;getScale:()F`). Пример: D11.
- Байткод-эталоны yarn build.4 для анкеров Ф3.3 лежат в `%TEMP%\sv_audit\` (`tfw.txt`, `cam.txt`, `title.txt`, `menu.txt`, `en_us.json` — ванильные lang-ключи 1.21.11). Именованные jar-ы: `%USERPROFILE%\.gradle\caches\fabric-loom\minecraftMaven\...\build.4-v2\*.jar` (javap без декомпиляции).
- Цвет/шрифты после D12/D13: единый источник — `util.ColorMath` (`withAlpha(rgb, byte)` clamp, `argb(rgb, float 0..1)`, `mix` дропает альфу) и `render.Fonts` (`attempted`-флаг; `GlyphAtlas.load` кэширует по Identifier — дублировалась только обвязка; весь MSDF-свод охватывает LoadingOverlay, 3 экрана, WidgetStylePipeline, 12 HUD-файлов и FontDraw). Массовые своды делать скриптом с пофайловыми count-проверками и абортом до записи (шаблон: `%TEMP%\opencode\refactor_34p4.ps1`); удаление методов — балансным сканом скобок, не regex'ом до первой `}`-строки (тела с try/catch ломают наивный regex); после сводов проверять тройные пустые строки (в этот раз 3 штуки, схлопнуты; 2 в MainMenuScreen — пре-существующие).
- Ф3.6-факты: `ModuleInfo.revision()` — static-счётчик мутаций кейбиндов (`++` в `armBinding`/`cancelBinding`/`resolveBinding`/`clearBinding`); кэш `KeybindPanel` привязан к нему — новые мутации кейбиндов вводить только через эти методы, иначе панель не обновится. `CosmeticContext` теперь мутабельный и переиспользуемый: ссылка, полученная из `Cosmetics.context(...)`, валидна только до следующего `set(...)` — не сохранять её; `Cosmetics` держит один статический `CONTEXT`, остальные 4 создающих сайта (TargetMarks/HitEffects/KillEffects/RainFall) по-прежнему создают свои экземпляры. `HudManager` drag-скретчи (`SNAP_OUT`, `CURSOR_X/Y`) — render-thread-only. Вызовы `Pipelines.warmUp()`: `Fonts.ensure` + `init()` MainMenuScreen/Editor + одноразовый блок первого HUD-кадра.
- Повторная сборка в той же фазе — БЕЗ `-PbumpVersion` (флаг бампает на каждом вызове; повтор даст лишний бамп).
- Ф3.7-факты: `MediaWatcher.SCRIPT` — каждая строка обязана быть законченным PowerShell-statement'ом: `-Command -` может исполняться построчно (зависит от хоста), многострочные конструкции в этом режиме молча убивают весь stdin-вход (exit 0, 0 байт) — не возвращать многострочность. Java-репродукция транспорта мода: `%TEMP%\opencode\MediaProbe.java` (режимы stdin / stdin-bypass / stdin-restricted / encoded), извлечение SCRIPT из java-файла — по маркерам text-block (скрипт-тела: `%TEMP%\opencode\media_script*.txt`). Внешний скрипт: `config/linkenvisuals_media.cmd` (Windows, `cmd /c`) или `config/linkenvisuals_media.sh` (прочие, `sh`); формат строки: title TAB artist TAB app TAB playing(0|1) TAB pos_ms TAB len_ms. `-ExecutionPolicy` не передаётся (политика не гейтит stdin-команды — проверено под `Restricted`).
