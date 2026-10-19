# HANDOFF — эстафета между чатами

> Методология: этот файл обновляется в конце КАЖДОЙ задачи. Следующий чат читает его первым и продолжает с «Следующей задачи». Детали решений — `MERGE_DECISIONS.md`, скоуп — `ROADMAP.md`.

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
