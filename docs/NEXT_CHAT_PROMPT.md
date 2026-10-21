# NEXT CHAT — LinkenVisuals: полный перенос Linken GUI 1:1

## Контекст
- Репозиторий: https://github.com/KurumaOfficial/LinkenVisuals
- Ветка: `main`
- HEAD при передаче: см. `git log -1` (последний docs-коммит), последний код: `f76016d`
- Версия мода: **1.0.95**
- Тесты: **207 / 0 падений**
- Исходники: `<repo>/source`
- Документация: `<repo>/docs`
- Донор (READ-ONLY на старом устройстве): `C:\StrangeVisuals\donor2\visualy`
  - На новом устройстве путь может отличаться. Если донора нет — **запроси у пользователя путь/копию**, не выдумывай файлы и не правь донор.
- Авторство донорских ассетов подтверждено пользователем (D6), лицензия донора — ARR.
- Рабочее дерево на момент передачи чистое, `origin/main` синхронизирован.

## Первым делом
1. `git clone https://github.com/KurumaOfficial/LinkenVisuals.git`
2. `git checkout main && git pull`
3. Проверь: последний коммит — docs с этим файлом; последний код `f76016d` (`git log --oneline -5`); `source/gradle.properties` → `mod_version=1.0.95`.
4. Прочитай:
   - `docs/HANDOFF.md` — верхняя секция «чат №26 — donor card scale + media controls» и все M8-секции;
   - `docs/MERGE_DECISIONS.md` — D52–D72, особенно D66–D72;
   - `docs/ROADMAP.md` — блок LINKEN, M8.1–M8.8;
   - `docs/PORT_STATUS.md` — LINKEN M8;
   - `MASTER_PROMPT_STYLE3.md`, если он есть у пользователя.
5. `cd source`
6. `.\gradlew.bat test --console=plain --no-daemon` → ожидаемо 207/0.

## Главная цель
Довести LINKEN ClickGUI до визуального **1:1** с донорским `screen/linkgui`: компоновка, панели, карточки, шрифты, отступы, радиусы, тени, hover/active, анимации. Сейчас перенесён весь основной каркас, но строгая пиксельная сверка ещё не выполнена.

## Жёсткие требования пользователя
- Пока активен **Linken GUI**, переключение на Classic/Beta **запрещено**. Не возвращай эту возможность.
- Кнопку смены стилей GUI и кнопку редактора главного меню в **LINKEN Settings не добавлять**.
- Minecraft сейчас **не запускать** без нового явного разрешения: пользователь просил «без окна Minecraft». Работай кодом, компиляцией и тестами.
- Для финальной визуальной приёмки отдельно спроси разрешение на `runClient` (1280×720 и 4K-framebuffer).

## Что уже сделано (M1–M8.8)
- M1–M7: LINKEN main menu, splash/loading/connect, Link GUI shell, темы/пины/конфиг, pause/settings hub, multiplayer browser, account manager, settings sub-pages, keybinds, language, skin, resource packs.
- M8.1: ThemeWindow, 8 палитр.
- M8.2: донорская компоновка 1280×720×1.5 на 1920×1080:
  - main 1080×720;
  - dock 540×72;
  - Interface 330×282;
  - Profile 330×360;
  - Theme 360×855.
- M8.3: 2D player head, card badges с иконками категорий, media strip.
- M8.4: light-темы LIGHT/FROST, полный token-pass (`LinkGuiTheme`: TEXT/PANEL/ROW/HOVER/ACTIVE/FIELD/TRACK/BACKDROP/ON_DARK).
- M8.5: style lock + Interface/Profile split.
- M8.6: draggable module settings window 330×440 (header drag, close X, clamp, scroll).
- M8.7: cursor trail — 64 точки/780 мс, glow-орбы + ядро.
- M8.8: card scale — 129, gap 15, header 78, media block 126, strip 94; media prev/play/next через системные media-клавиши (`java.awt.Robot`).

## Ключевые файлы
```
source/src/main/java/dev/linkenvisuals/clickgui/link/
  LinkGuiScreen.java      — shell, hit-зоны, карточки, настройки, конфиг, media
  LinkGuiLayout.java      — чистая геометрия 1920×1080
  LinkGuiTheme.java       — 8 палитр и токены, включая light
  LinkGuiConfig.java      — linkenvisuals_linkgui.json (тема/пины)
  LinkGuiCursorTrail.java — cursor trail
  LinkGuiMediaKeys.java   — системные media-клавиши
source/src/test/java/dev/linkenvisuals/clickgui/link/
  LinkGuiLayoutTest.java
  LinkGuiThemeTest.java
```
Донорские файлы для сверки:
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

## Что осталось (приоритет)
1. **Строгий 1:1**
   - Сверь наши константы/отступы/шрифты/альфы/радиусы/анимации с донором.
   - Точки: main header (player head, search, close), card badge/name/description/bind/toggle/pin,
     hover lift/enter animation/stripe fill, dock indicator slide, Interface/Profile/Theme spacing,
     media strip.
   - Не копируй Lombok, донорские Module/Setting, legacy `Render2D`/`WindowGuiRender`, Gif/Lottie,
     сеть, косметику, аудио. Используй наши `LinkenMenuChrome`, `LinkenFonts`, `Spring`,
     `MsdfText`, `RoundedRectPipeline`, `GlowPipeline`, `CirclePipeline`, `PanelPipeline`.
   - При изменении layout обновляй `LinkGuiLayoutTest`; добавляй тесты на геометрию.
2. **Volume / system volume**
   - Media prev/play/next уже работают через `LinkGuiMediaKeys` (`java.awt.Robot`).
   - Громкость не портирована. Добавляй только реально работающий контрол; без декоративных мёртвых кнопок.
   - Если system volume нельзя сделать без новой зависимости — зафиксируй отклонение в D-решении.
3. **Close overlay**
   - В доноре `LinkGuiCloseOverlay` — пустой no-op стаб. Не выдумывай новый оверлей; зафиксируй в docs.
4. **Пиксельная сверка**
   - Без runClient можно сверять только код/константы.
   - Если пользователь даст скриншот донорского GUI — используй как reference.
   - В конце попроси разрешение на `runClient` для 1280×720/4K.
5. **M8.9+**
   - Доводка InterfaceWindow (interface scale / cursor tracker / Apply-Default), если это есть в доноре и не конфликтует.
   - Проверь style lock во всех путях: `ModuleRegistry.cycleGuiStyle`, `setGuiStyle`,
     `Ctrl+Left/Right`, строка `GUI Style` скрыта в Interface-панели.
   - Beta/Classic не должны перекрашиваться/ломаться.

## Правила
- Feature/fix-коммит: `+1 patch` от фактической версии. Docs-only — без бампа.
- Перед коммитом: `git fetch origin`. Force push запрещён.
- Не коммитить `build/`, `run/`, jar, логи, скриншоты, временные файлы.
- Не добавлять `TODO/FIXME/System.out`, пустые catch, проглоченные исключения.
  Fallback/ошибки — через `LOGGER.warn/debug`.
- Не тащить донорские: cosmetics, network, Kotlin, Lombok, legacy shaders,
  старый Panel GUI, Lottie `welcome.json`, донорское аудио.
- Все видимые кнопки должны работать; мёртвые клики запрещены.
- i18n: en/ru/uk для новых строк; `LangParityTest` зелёный.
- После зелёной сборки: commit + push, jar в `source/build/libs`, SHA-256, отчёт на русском.
- В конце: обновить `docs/HANDOFF.md`, `docs/MERGE_DECISIONS.md` (новое D-решение),
  `docs/ROADMAP.md`, `docs/PORT_STATUS.md`; docs-коммит отдельно.

## Команды
```powershell
cd source
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot'
.\gradlew.bat clean build --console=plain --no-daemon
.\gradlew.bat runClient --no-daemon   # ТОЛЬКО после явного разрешения пользователя
```

## Текущие артефакты
- Jar: `source/build/libs/linkenvisuals-1.0.95.jar`
- SHA-256: `886EF652F5FDEE6298178C0B15916412816CBEE780A8BE66C66D5D8480B65081`
- Размер: 7 371 851 байт
- HEAD: последний docs-коммит; код: `f76016d`
- Предыдущие M8-коммиты: `166869a` (v1.0.94), `1d66355` (v1.0.93),
  `5638ef7` (v1.0.92), `fa107f1` (v1.0.91), `ea26c9c` (v1.0.90).

## Известные риски
- Строгий пиксель-в-пиксель не подтверждён без reference/runClient.
- `java.awt.Robot` media-клавиши могут не работать на некоторых Windows/Java — ошибка в debug, без краша.
- Light-темы, cursor trail, draggable settings и Interface/Profile не проверены глазами.
- `LinkGuiScreen.java` большой; правки точечные, не ломать hit-зоны.
- `LinkGuiThemeWindowLayout` удалён в D67 — не возвращать.
- В `LinkGuiLayout` уже донорские 129/15/78/126; менять только осознанно и с тестами.
- На новом устройстве донора может не быть — без него не выдумывать ассеты/размеры, запросить у пользователя.

## Формат первого ответа
Кратко подтверди:
- HEAD/версию/число тестов;
- что прочитал HANDOFF/D52–D72/ROADMAP M8;
- план первого code-среза;
- что Minecraft не запускается без разрешения.
Затем переходи к работе.
