# MERGE_DECISIONS — журнал решений

Методология (закон для всех чатов):
- Запись на каждое решение: дата · решение · доказательство (код/байты/лог/замер) · почему так.
- Источник истины по скоупу — приказы пользователя; по технологии — этот журнал.
- «Должно работать» запрещено: только подтверждённые факты.

---

## 2026-10-18 · D1 · Git-корень — корень `StrangeVisuals-1.21.11`
- Evidence: `README.md`, `README_RU.md`, `LICENSE` в корне и в `source/` были байт-идентичны (SHA-256 совпадают); корневой `.gitignore` уже был написан с путями `source/...`.
- Решение: единственный канонический набор — в корне. Дубликаты `source/README.md`, `source/README_RU.md`, `source/LICENSE`, `source/.gitignore` удалены (commit 89d03b2), правила из `source/.gitignore` влиты в корневой.
- Добавлены `.gitattributes` (`* text=auto eol=lf`, `*.bat` → CRLF, бинарь-типы) и `.opencodeignore`.
- Исключение: `*.jar` игнорируется, но `!source/gradle/wrapper/gradle-wrapper.jar` — обёртка обязана быть в репо.

## 2026-10-18 · D2 · Переименование в LinkenVisuals (приказ пользователя)
- Приказ: «название будет LinkenVisuals а не strange visuals, и все упоминание в коде про strange надо убрать» + push в `https://github.com/KurumaOfficial/LinkenVisuals`.
- Evidence замены: 371 файл, 1672/1551 строк; `dev.strangevisuals`→`dev.linkenvisuals`, mod id/assets/mixins/keys/README; остаток `(?i)strange` в `source/src` — ноль (проверено сплошным грепом; 3 ланг-ключа `*_switch` дочищены вручную).
- Миграция сохранёнок: новый класс `dev.linkenvisuals.LegacyConfigMigration` копирует старые файлы `config/strangevisuals*.{json,txt,png}` и каталоги `config/strangevisuals/`, `<game>/strangevisuals/` в новые пути, только если новых ещё нет; легаси-оригиналы не удаляются. Вызов — до `ConfigManager.load()` в `LinkenVisuals#onInitializeClient`.
- НЕ переименованы (осознанно): `LICENSE` (правило «лицензию не менять», копирайт-строка сохраняет исходного держателя) и `MASTER_PROMPT.md` (исторический промт, не код).
- Техдолг: тест миграции добавлен в commit с тестами Фазы 0.

## 2026-10-18 · D3 · Автодеплой jar — только opt-in свойство
- Evidence: в `source/build.gradle:62-75` был хардкод `D:/MultiMC/instances/1.21.111/...`.
- Решение: деплой выполняется только при `-PmultiMcModsDir=<path>`; локальный путь живёт в `%USERPROFILE%\.gradle\gradle.properties` (`multiMcModsDir=D:/MultiMC/instances/1.21.111/.minecraft/mods`), не в репозитории. Путь подтверждён пользователем.

## 2026-10-18 · D5 · Первые тесты и CI (Фаза 0 закрыта по коду)
- 27 тестов, все зелёные: `ConfigJsonTest` (round-trip всех 4 типов настроек + легаси-ключ «Фильтры»), `ProfileNamesTest` (CWE-22), `AtomicFilesTest`, `ColorMathTest`, `HandPoseMathTest`, `LangParityTest` (en/ru/uk паритет ключей + запрет легаси-бренда в значениях/ключах), `Std140LayoutTest` (10 std140-блоков сверены с Java-константами: kill 224, hit 416, rain 192, hand_outline 96, hand_trail 32, filter 64, ambience 64, bright_pass 64, menu_background 48, shader_sky 64 B), `KillEffectsPackingTest` (побайтовая упаковка 224 B, zero-pad, зона матрицы), `LegacyConfigMigrationTest` (копирование, идемпотентность, запрет перезаписи).
- Рефакторинг D4 выполнен: новые чистые классы `util/ProfileNames`, `util/AtomicFiles`, `util/ColorMath`, `clickgui/ConfigJson`, `hands/HandPoseMath`; `ConfigManager`/`HandPose`/`Material` делегируют — поведение не менялось (логика перенесена строка-в-строку). Константы размеров UBO стали `public static final` (видимость, не значение).
- Evidence тестируемости: `KillEffectsPackingTest` вызывает `ModuleRegistry.killLife()` без Fabric-bootstrap — clinit `ModuleRegistry` чист (MC-колбэки висят в лямбдах и не исполняются).
- CI: `.github/workflows/build.yml` (JDK 21 temurin, `gradlew build` на push/PR).

## 2026-10-18 · D4 · Тестируемость Фазы 0 (микро-рефакторинг без смены поведения)
- Причина: юнит-тесты обязаны идти без bootstrap Minecraft; `ConfigManager`/`HandPose`/`Material` тянут `FabricLoader`/GPU-статику в clinit.
- План (commit с тестами): чистые классы `util.ProfileNames` (санитайз имени), `util.AtomicFiles` (tmp + ATOMIC_MOVE + fallback), `clickgui.ConfigJson` (build/apply модулей), `hands.HandPoseMath` (клэмпы), `util.ColorMath` (mix/argb/…); константы размеров UBO сделать `public static final` для сверки тестом std140.
- Экспозиция: `public` у констант — только видимость, значение не меняется; инлайнится javac, GPU-класс в тестах не инициализируется.

## 2026-10-18 · D6 · Фаза 1: пакетный ответ пользователя
- (а) Авторство/права на донорские ассеты (LinkenVisuals 2.0) подтверждены — переносим (Caramel Frappuccino CC0; MoreCosmetics-каталог; breda/kintsugi/shrek). Условие: при переносе Rudy (CC BY 4.0, автор amogusstrikesback2) добавить атрибуцию в docs/README.
- (а2) Авторство собственных панорам/звуков/текстур проекта подтверждено (код уже в публичном KurumaOfficial/LinkenVisuals по приказу пользователя).
- (б) Фаза 2: к удалению назначены ТОЛЬКО панорамы + PanoramaManager. Редактор главного меню, `MainMenuScreen`, `CustomMultiplayerScreen`, `AltManagerScreen`/`AccountStorage`, `MacroScreen` остаются (их не режем и сейчас не чиним).
- (в) Фаза 5.7: белый список модулей из ROADMAP принят целиком; (в2) косметика — вся (wave-плащ, питомцы/Rudy, ChinaHat, Nimb, Naruto, CosmeticEffects, Spheres).

## 2026-10-18 · D7 · Удаление панорам (исполнение D6б) — v1.0.34, commit `2d5adf4`
- Удалено: `assets/linkenvisuals/textures/gui/panoramas/**` (6 наборов × 6 граней PNG, 42.4 МБ), перекрытие `assets/minecraft/textures/gui/title/background/panorama_*.png` (6 PNG, 6.5 МБ), `render/PanoramaManager.java` (234 строки), кейбинд `key.linkenvisuals.next_panorama` (INSERT) + ланг-ключи en/ru/uk.
- Evidence: jar 50.5 МБ → 1.5 МБ; греп `PanoramaManager|next_panorama|triggerTransition|PANORAMA_POOL` в `source/src` = 0; `build` (тесты, включая LangParityTest) зелёный; jar задеплоен в MultiMC.
- Фолбэк: новый `VanillaStylePipeline.renderVanillaPanorama` — стоковый `RotatingCubeMapRenderer` игры, при недоступности — градиент. Использование: `ScreenMixin` (settings-подобные экраны без мира), `renderOptionsBackground`, `renderDirtBackground`, `CustomMultiplayerScreen` (VANILLA-стиль).
- Loading-миксины (Connect/Message/Progress/LevelLoading) НЕ правились: они звали `Screen.renderPanoramaBackground`, который обёртка `ScreenMixin`-`WrapOperation` подменяла на наш менеджер; без обёртки они автоматически рендерят стоковую панораму MC.
- Конфиг: `MainMenuConfig.BackgroundMode.PANORAMA` удалён; Gson неизвестное имя enum → null → существующая null-миграция переводит в `SHADER` и `save()` перезаписывает файл (краш-путь закрыт и на случай исключения парсинга). Редактор меню: цикл Shader → Photo → Both → Shader.
- Деплой-гигиена: `remapJar.doLast` теперь удаляет старые `linkenvisuals-*.jar` из mods-каталога перед копированием (иначе два jar = «Duplicate mod» на запуске; факт зафиксирован в этой итерации: в mods лежали 1.0.33 и 1.0.34 одновременно).

## 2026-10-18 · D8 · Ф3.2: разрез ClickGuiScreen + i18n — v1.0.36
- Сначала закрыт тест Ф3.1 по логу MultiMC (16:37, «New World» → выход): `linkenvisuals 1.0.35`, «wrong thread» = **0** (было 9), WARN/ERROR всего = 0. «Rendersystem called from wrong thread» в DISCONNECT-пути больше не воспроизводится.
- Вынос (без смены поведения): config-окно — `renderConfigManager`, `handleConfigManagerClick`, `createConfigFromInput`, `setConfigStatus`, `playUiSound`, класс `ConfigActionBounds`, поля `cfg*`/`config*` (бывшие строки 473–804) → новый пакет-приватный `clickgui/ConfigPanel.java` (486 строк). `ClickGuiScreen`: 1794 → 1394 строк (−430 изменённых строк в diff).
- Parity-решения: поля, бывшие `static` (текст ввода, цель скролла, статус), остались `static` в ConfigPanel — ввод переживает переоткрытие экрана как раньше; скроллбар — колбэк `Scrollbar` на общий `ClickGuiScreen.scrollbar`; шрифты/колбэк биндятся в `init()`. Изменена только видимость констант `RAIL_DIVIDER_X`/`CONTENT_BOTTOM` (private → package-private, значения не тронуты).
- Осознанные дубли до Ф3.4/Ф5.1: `centredTop` и int-alpha `argb` скопированы в ConfigPanel 1:1 (та же формула/константы; слияние — в задаче единого ColorUtil).
- i18n: 16 RU-строк панели → ключи `gui.config.*` в en/ru/uk; RU-значения байт-в-байт равны прежнему хардкоду, EN/UK — новые переводы. В lang этих ключей не существовало (тезис мастер-промта «ключи уже есть» не подтвердился). Резолв — через собственный `dev.linkenvisuals.i18n.I18n.get` (как в classic GUI), не через `Text.translatable`.
- Заодно той же строкой: placeholder поиска в modern ClickGUI («Search...») подключён к существующему `gui.search.placeholder` (классический рендер уже его использовал; в modern строка была захардкожена).
- UK-глифы: в MSDF-атласах 160 глифов, кириллица = 66 (RU-набор), і/ї/є/ґ отсутствуют; `GlyphAtlas.resolveFallback` (стр. 93–105) уже отображает і/ї→i/I, є→э, ґ→г — рендер UK-текста осознанным фолбэком, атлас не трогали (отдельная задача — при желании пользователя).
- Гигиена: lang-файлы были в git-блобах с CRLF с прежних коммитов → нормализованы в LF (рабочая копия, CR=0), CRLF-предупреждения git уходят.
- Evidence: `build` зелёный (27 тестов, включая LangParityTest с новыми ключами), `git diff --check` чистый, греп по ClickGuiScreen: `cfg|configInput|configScroll|configStatus|ConfigActionBounds|playUiSound` = 0, кириллица = 0; jar `linkenvisuals-1.0.36.jar` собран и задеплоен в MultiMC (старый 1.0.35 удалён деплоем).
