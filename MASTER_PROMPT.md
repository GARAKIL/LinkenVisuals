# МАСТЕР-ПРОМТ: StrangeVisuals → идеальный мод (порт лучшего из LinkenVisuals + тотальные фиксы)

Привет, продолжаем разработку StrangeVisuals — клиентского визуального мода Minecraft на Fabric (якорь версии: Minecraft 1.21.11, Fabric Loom 1.15.5, loader 0.19.1, yarn 1.21.11+build.4, Java 21).

- Репозиторий-цель: `C:\Users\gameg\Desktop\StrangeVisuals-1.21.11\source` (далее «проект»).
- Донор кода и ассетов (READ-ONLY): `C:\Users\gameg\Desktop\визуалы` (LinkenVisuals 2.0, MC 1.21.4, пакет `ru.method.linkenvisuals`).

Миссия: перенести из донора всё лучшее — оптимизации, косметику, функции, эффекты — в проект **на современном рендер-стеке 1.21.11**, и параллельно вылечить все известные болячки StrangeVisuals из аудита (список в §10), чтобы получился один идеальный мод. Донор — это склад референсов, а не рабочая копия: его не собираем, не коммитим, не изменяем ни на байт.

---

## 0. Правила игры (нарушение = провал)
1. Донор read-only. Любая запись в `C:\Users\gameg\Desktop\визуалы` запрещена.
2. В проект не попадает легаси-стек 1.21.4: только `RenderPipeline`/`RenderPipelines.register`, `CommandEncoder`/`RenderPass`, `GpuBuffer`, `SimpleFramebuffer`, GUI render states (`context.state.addSimpleElement`). Никаких `ShaderProgram`, `RenderSystem.setShader`, старых JSON-программ — даже если в доноре это работает.
3. Запрещённый к переносу код (см. §10): `punchy/**` (декомпилированный сторонний мод), `.prime-decomp/**`, `tools/reverse-audit/**`, `player/Crasher*/**` и любые читерские combat/anticheat-обходные модули. Каталог `CosmeticsScreen` и сервис синка донора по умолчанию НЕ переносим: косметика живёт в настройках модуля Cosmetics.
4. Решение о переносе любого модуля/ассета — только после моего подтверждения (скоуп). Технические решения (как реализовать) принимаешь сам.
5. Все отчёты — по-русски, без воды и лести. Не поддакиваешь: если что-то не сходится с моими ожиданиями — говоришь честно с доказательствами.
6. Каждое утверждение — с evidence (код/байты/лог/замер), потом синтез. Запрещено «должно работать».

## 1. Погружение в контекст (обязательно первым делом)
- Прочитай ВСЮ документацию проекта на 100%, каждую строку: `docs/MERGE_DECISIONS.md` (журнал решений), `docs/ROADMAP.md`, `docs/PORT_STATUS.md`, `docs/AUDIT_BASELINE.md`, `README.md` / `README_RU.md`. Если каких-то из этих файлов ещё нет — создай их из §10 этого промта (это твоя Фаза 0).
- Проверь `git log` и `git status` — пойми, на чём остановился прошлый чат, и продолжай оттуда, а не начинай заново. Если git ещё не инициализирован — Фаза 0, пункт 1.
- Методология из шапок документов — закон, а не пожелание.
- В первом же ответе дай короткий «контекст-репорт»: статус репозитория, что сделано, что делаешь дальше (3-6 строк), и сразу начинай работу.

## 2. Полная автономность
Ты всё делаешь сам:
- пишешь код;
- билдишь сам: `.\gradlew build --console=plain` (итеративно — `.\gradlew compileJava --console=plain`);
- тестишь сам: `.\gradlew test --console=plain` (Фаза 0 заводит JUnit 5; каждая новая фича приносит тесты на чистую логику);
- дебаг-клиент запускаешь сам только при необходимости проверить старт: `.\gradlew runClient` (это тяжёлый процесс — не гоняй его без нужды; визуальное поведение в игре проверяю я);
- читаешь логи сам: `source\run\logs\latest.log`, отчёты `build\reports\`;
- сверяешь шейдеры сам: любая новая Java-структура, отдаваемая в GLSL, обязана совпадать байт-в-байт со std140-блоком шейдера (в проекте уже есть 8 образцово сверенных пайплайнов — держи планку);
- проверяешь полный паритет ключей локализации (en_us/ru_ru/uk_ua) тестом.

Не спрашивай меня то, что можно узнать из сборки/теста/лога. Спрашивай только про решения, где нужен мой вкус/приказ: что удаляем, какие модули берём в скоуп, очередность больших пачек.

## 3. Качество
- Эталон — современный стек 1.21.11: pipeline-first рендер, батчинг через GUI render state, прекомпиляция пайплайнов (`Pipelines.warmUp`), корректный lifecycle GPU-ресурсов (освобождение буферов при disable/DISCONNECT/shutdown).
- Ноль ворнингов компиляции, ноль `TODO`/`FIXME`, ноль `System.out`, ноль проглоченных исключений без лога.
- Юнит-тесты на каждую инварианту: цветовая математика, сериализация/миграция конфигов, заполнение UBO, санитизация имён файлов, паритет lang-ключей.
- Временные дебаг-файлы и скрипты чисти за собой.
- Новые зависимости — только с моим подтверждением. Проект остаётся клиентским модом на fabric-loader + fabric-api.

## 4. Порядок работы
- Standing order: после каждого завершённого и зелёного шага — сразу `git add/commit/push`. Если remote не настроен — спроси у меня URL один раз и настрой (донорский remote `methoduzz/linken-visuals` не трогать: это другой проект).
- Версия бампится +patch по правилу репозитория (`mod_version` в `gradle.properties`, есть `-PbumpVersion`).
- Каждый фич-коммит сопровождается записью в `docs/MERGE_DECISIONS.md` (что взято из донора, откуда именно, почему так реализовано) и обновлением `docs/ROADMAP.md` и `docs/PORT_STATUS.md`.
- Свежий jar для моих тестов всегда в `source\build\libs\strangevisuals-<version>.jar` (полный `build`). Следи, чтобы файл не был занят запущенной игрой. Деплой копирования — только по моей опции `-PmultiMcModsDir`, дефолтный хардкод пути удалить (Фаза 0).
- Перед крупной пачкой работ (больше одного коммита) — короткий план мне на утверждение (3-6 строк), потом делаешь.

## 5. Формат отчётов
В конце каждой задачи — кратко и просто:
- что сделал (пункты, с путями файлов);
- что мне тестить (конкретные шаги в игре + точный путь к jar);
- что не получилось/риски — честно, с доказательствами.
Без воды, без лести, только факты.

## 6. Гигиена контекста (обязательные правила)
Ты — инструмент автономной разработки. Чтобы экономить токены, ускорять генерацию и не деградировать на длинных дистанциях, соблюдай:
1. Жёсткий лимит сессии: не раздувай диалог выше ~120-150k токенов. Как только логическая задача (фича/фикс) завершена вместе с тестами — сообщи мне, чтобы я открыл новый чат, перед переходом к следующей задаче. Разработку нескольких несвязанных фич в одной простыне не вести. Но недоделанную фичу не бросать: лучше выйти за рамки, но довести.
2. Quiet mode: не запускай сборку/тесты в шумном режиме. `--console=plain`, без `--info/--debug`, вывод обрезай (`| Select-Object -Last 40`). В контекст должен попадать только упавший ассерт или короткая ошибка компилятора.
3. Точечный доступ к файлам: не сканируй репозиторий целиком. Указывай конкретный путь к файлу. Убедись, что `.gitignore`/`.opencodeignore` исключают `build/`, `.gradle/`, `run/`, `logs/`, `*.jar`, временные файлы.
4. Circuit breaker: если чинишь один и тот же падающий тест/ошибку компиляции >5 раз подряд безуспешно — немедленно остановись, выдай отчёт о тупике и позови меня. Бесконечные циклы правок запрещены.

## 7. Дорожная карта

### Фаза 0 — гигиена (делать в этом же чате, первым делом)
1. `git init` (если нет), проверить `.gitignore` (см. §6.3), коммит-снапшот «baseline after audit», настроить remote, push.
2. Создать `docs/`: `MERGE_DECISIONS.md`, `ROADMAP.md`, `PORT_STATUS.md`, `AUDIT_BASELINE.md` (из §10).
3. `build.gradle`: убрать хардкод `D:/MultiMC/instances/1.21.111/...` → опция `-PmultiMcModsDir`.
4. Тестовая обвязка: JUnit 5 + первые тесты: round-trip `ConfigManager`, clamp'ы `HandPose`, цветовая математика `Material`, UBO-раскладки (`KillEffects.fillEvents` и аналоги), санитизация имён файлов, паритет lang-ключей en/ru/uk.
5. CI: `.github/workflows` — build + test на push.
6. Один `ColorUtil` (в проекте ~22 клона `mix`/`withAlpha`/`argb` — список в §10), все клоны удалить.

### Фаза 1 — фиксы StrangeVisuals (файлы и строки — стартовые точки, сверяй факты)
1. `clickgui/ClickGuiScreen.java`: вынести settings/config-окно (район 494-786) в отдельный класс; все русские строки — через существующий `I18n` (ключи уже есть в lang-файлах).
2. Миксины: убрать localized-string heuristics (`TitleScreenMixin` ~74-133, `GameMenuScreenMixin` ~52-112, `VanillaStylePipeline` 53-195) → стабильные идентификаторы/компоненты; ordinal-анкеры (`CameraMixin`, `TextFieldWidgetMixin`) — на именованные, где безопасно; политику `required` не менять слепо, обосновывать в decisions.
3. 4 почти идентичных loading-миксина (`ConnectScreenMixin`/`LevelLoadingScreenMixin`/`ProgressScreenMixin`/`MessageScreenMixin`) → общий базовый.
4. Атомарные записи: `HudLayoutStorage` (пишет напрямую), `PotionLayoutMode` (.txt → единый формат) — с совместимостью существующих файлов игроков.
5. Гонка `CustomTextureLoader` (non-volatile `cachedConfigPath`: запись из фонового потока ~:163, чтение из render-треда ~:140) — volatile/барьеры.
6. Пер-кадровые аллокации: `KeybindPanel.width()→refresh()`, `Cosmetics` (3 контекста/кадр), `HudManager.snap()` — кэши/пулы.
7. `MediaWatcher`: убрать PowerShell `-ExecutionPolicy Bypass` → graceful «unsupported platform» + опциональный внешний скрипт.
8. `HudManager` edit-mode (hack «is a ChatScreen», ~:141-143) → явный флаг; убрать тихий catch ~:91-95.
9. README-дрейф (адрес конфига, категории, несуществующие фичи) — привести в соответствие.

### Фаза 2 — перенос из донора (по одной фиче на коммит; очередность утверждай)
Метод обязательный: сначала читаешь донорский код, составляешь таблицу «параметр/поведение → как реализуем на новом стеке», потом пишешь.
1. **BlockOverlay**: 10 шейдерных эффектов (донор: `assets/linkenvisuals/shaders/block_overlay_*`, модуль `modules/impl/render/BlockOverlay`) → `RenderPipeline`.
2. **Liquid Glass**: донор `BuiltLiquidGlassNew` + `liquidglassnew.fsh` + capture/reflection-цепочки → современный pipeline.
3. **Motion Blur** (`motionblur.fsh` + модуль) → pipeline-композит.
4. **MSDF**: сверить свой `MsdfText`/`GlyphAtlas` с донорскими `MsdfFont`/`FontData`/`MsdfGlyph` (это лучший код донора): взять fastutil-лукапы, кэш ширин строк, init через `recordRenderCall`; перенести атласы шрифтов при необходимости.
5. **Оптимизации-идеи донора** (без копипаста): LRU-кэш `VoxelShape`, int-ядро линий без упаковки в `Color`, scratch-буферы без боксинга, batched dot-grid/hatch одним draw call — применить там, где в проекте циклы рисования.
6. **Косметика**: wave-плащ (`CapeSimulation`), питомцы (`RemotePets`/`PetWanderController` + модель Rudy), `ChinaHat`, `Nimb`, `Naruto`, `CosmeticEffects`, `Spheres` — всё встраивать **в настройки модуля Cosmetics** (без каталога!); атласы конвертировать донорскими tools-скриптами (`tools/import_rudy.py`, `convert_cosmetic_models.py`). Каждую косметику — отдельным подтверждённым пунктом.
7. **Избранные модули из донора** (белый список, пачки согласовывать): WayPoints/GPS, ItemPhysics, ItemScroller, ChatHelper, MineHelper, FastExp, ElytraSwap, HitSound, HandSwap, SwingAnimation, Trails, JumpCircles, Crosshair, HitColor, TargetEsp, Zoom, Freelook, Fullbright, TimeChanger, TotemCounter/Tracker, UseTracker, StreamerMode (таблицу из 3000-строчного класса вынести в ресурс!), UserSync/party-сервер (только по отдельному решению).
8. **ViewModel-паритет**: донорский `ViewModel` как референс параметров; перенести недостающие настройки в `HandEditor`/`HandPose` (после сверки списков параметров).

### Фаза 3 — чистка проекта (только по моему подтверждённому списку; с миграцией конфигов)
Кандидаты (обсуждаемы, НЕ удалять без подтверждения): редактор главного меню и пресеты стилей, `CustomMultiplayerScreen`, `AltManagerScreen`, `MacroScreen` и т.п. Каждое удаление — отдельным коммитом, с миграцией пользовательских конфигов (чтобы апдейт не ломал сохранёнки).

### Фаза 4 — финальный проход
Перф-профилирование (аллокации/дров-коллы), инвентаризация перенесённых ресурсов, финальный README/доки, релизная сборка.

## 8. Definition of Done (для любой фичи)
- build зелёный, ноль ворнингов; новые тесты зелёные;
- для шейдеров — сверка UBO-раскладки байт-в-байт;
- `docs/MERGE_DECISIONS.md` + `ROADMAP.md` + `PORT_STATUS.md` обновлены; версия бампнута; commit + push;
- мне выдан свежий jar и конкретные шаги теста в игре;
- никаких временных файлов и TODO в коде.

## 9. Стоящие приказы (сводка)
donor read-only · только современный стек · evidence-first · тесты на инварианты · ноль ворнингов · commit+push сразу · +patch и доки на каждый фич-коммит · jar свежий · отчёты по-русски и без воды · лимит сессии 120-150k и передача эстафеты после каждой завершённой задачи · circuit breaker 5 попыток.

## 10. БАЗА ЗНАНИЙ АУДИТА (перенеси это в `docs/AUDIT_BASELINE.md` Фазой 0)

### 10.1 Метрики
- Проект (StrangeVisuals): 165 Java-файлов / ~24 835 строк (~21.5k непустых); 40 миксинов/76 инъекций; 227 использований `RenderPipeline`/`GpuBuffer`, ноль легаси; MIT; версия 1.0.32; jar ~52.9 МБ; тестов и git не было (git init — Фаза 0).
- Донор (LinkenVisuals): 782 Java / ~150k непустых (~170k физических) строк, из них `punchy` — 181 файл / ~69k строк (декомпилированный сторонний код — исключён); 136 миксинов, 261 инъекция; легаси-стек (144× `ShaderProgram`, 48× `setShader`); лицензия ARR; рабочее дерево было грязным (38 незакоммиченных файлов).

### 10.2 Карта донора — где лежит «всё хорошее»
- MSDF-подсистема (лучший код): `ру\method\linkenvisuals\api\render\msdf\` + `assets\linkenvisuals\fonts\`.
- Block overlays: `modules\impl\render\BlockOverlay.java` + `assets\linkenvisuals\shaders\block_overlay_*`.
- Liquid glass: `api\render\builders\impl\LiquidGlassNewBuilder.java`, `...\renderers\impl\BuiltLiquidGlassNew.java`, `utils\render\LiquidGlassCaptureBlur.java`, `LiquidGlassReflectionCache.java`, шейдер `liquidglassnew.*`.
- Motion blur: `utils\render\MotionBlurRenderer.java`, шейдер `motionblur.*`, модуль `modules\impl\render\MotionBlur.java`.
- Blur/захват: `utils\render\FogBlurRenderer.java`, `captureblur.*`, `gaussian.*`.
- Оптимизации: `utils\render\Render2D.java` (drawDotGrid/drawHatch), `Render3D.java` (LRU VoxelShape, scratch widths).
- Косметика: `cosmetics\**` (включая `wave\CapeSimulation`), `modules\impl\render\cosmetic\**` (Raven, Naruto, Nimb, Spheres, ChinaHat, CosmeticEffects, RemotePets, PetWanderController), ассеты `assets\linkenvisuals\characters\rudy.*`, скрипты `tools\import_rudy.py`, `tools\convert_cosmetic_models.py`, `tools\generate_cape_textures.py`.
- Модули-кандидаты (белый список §7.2.7): `modules\impl\misc\` (WayPoints, ItemPhysics, ItemScroller, ChatHelper, MineHelper, FastExp, ElytraSwap, HitSound, TotemCounter, TotemTracker, UseTracker, StreamerMode…), `modules\impl\render\` (HandSwap, SwingAnimation, Trails, JumpCircles, Crosshair, HitColor, TargetEsp, Zoom, Freelook, Fullbright, TimeChanger, ViewModel, Hands), `modules\impl\client\DiscordRPC` (по решению).
- Platform-compat паттерны: Pojav/gl4es fallback (setShader==null → ванильный путь), soft-dep рефлексия (`IasBridge`, `BetterCombatCompat`, `SkinLayersCompat`, `EmfFirstPersonHandCompat`, `FirstPersonModelCompat`), Iris-детект.
- Конфиг-паттерны донора (перенять идеи, не код): версия формата, автосейв с локом, shutdown-hook, кэш автора конфига.

### 10.3 Известные проблемы проекта (детализированный список к Фазе 1)
- `ClickGuiScreen.java` — 1794 строки; русские строки 494-786 в обход I18n (в файле ноль упоминаний `I18n`).
- `TitleScreenMixin` :74-133, `GameMenuScreenMixin` :52-112, `VanillaStylePipeline` :53-195 — localized-string heuristics.
- Клоны loading-миксинов: одинаковый alpha-хелпер в `ConnectScreenMixin:185`, `LevelLoadingScreenMixin:144`, `ProgressScreenMixin:157`, `MessageScreenMixin:147`.
- Клоны color-хелперов (~22 класса): примеры `Watermark:144`, `HudManager:378`, `NameTags:292`, `HandPanel:172`, `ClickGuiScreen:1777/1781`, `MainMenuScreen:671/675`, `Material:186/208`, `WidgetStylePipeline:237`.
- Конфиг-хранилища (7 шт.): атомарные — `ConfigManager`, `MainMenuConfig`, `AccountStorage`, `MacroManager`, `HandPose`; НЕ атомарные — `HudLayoutStorage:39-48`, `PotionLayoutMode:34`.
- `CustomTextureLoader` — data race (`cachedConfigPath` non-volatile, запись ~:163 из фонового потока, чтение ~:140 из render-треда).
- Пер-кадровый churn: `KeybindPanel:57-86`, `Cosmetics:67-101`, `HudManager:279-314`; `HudManager` зовёт `Pipelines.warmUp()` каждый кадр (~:57).
- `MediaWatcher:120-121` — `powershell.exe -ExecutionPolicy Bypass` (скрипт inline через stdin); Windows-only guard `:99`.
- `build.gradle:62-75` — хардкод `D:/MultiMC/instances/1.21.111/...` (имя папки похоже на опечатку).
- `HudManager` edit-mode == «is a ChatScreen» (~:141-143); тихий catch ~:91-95.
- README-дрейф: адрес конфига, список категорий, несуществующая косметика.
- `PanoramaManager:135-153` — геттеры игнорируют параметр `Screen`; `TargetMarks:354-359` — неиспользуемый `tipped()`.
- Миксин-конфиг: `required:true` + `defaultRequire:1` (`strangevisuals.mixins.json:2,48`) — падать при переименовании цели; политику менять только с обоснованием.

### 10.4 Машина и окружение
- JDK 21: `C:\Program Files\Java\jdk-21`. Системный `java` по умолчанию — 25.x (для градла проекта не использовать; wrapper сам скачает нужный Gradle).
- Сборка проекта исторически зелёная: `compileJava` → BUILD SUCCESSFUL.
- Донор собирается только с override (`-Dorg.gradle.java.home=C:\PROGRA~1\Java\jdk-21`) — но собирать его НЕ надо.
- Донор в прошлом падал на `org.gradle.java.home=C:/Program Files/Java/jdk-21.0.5+11` в своём `gradle.properties` — просто знай, это не наша проблема.

### 10.5 Юридические исключения (повтор)
Не переносим: `punchy/**` (декомпилированный сторонний мод), `.prime-decomp/**`, `tools/reverse-audit/**` (vineflower/skidfuscator), `player/Crasher*/**`, все читерские combat-модули без прямого приказа, каталог `CosmeticsScreen`+синк-сервис (по умолчанию). Ассеты (звуки/текстуры/модели) переносим только при подтверждённом авторстве; при сомнении — спроси меня.
