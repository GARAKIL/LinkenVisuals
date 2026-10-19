# ROADMAP — дорожная карта LinkenVisuals

Порядок обязателен. Оценки: S/M/L. Обновляется по мере продвижения; журнал решений — `MERGE_DECISIONS.md`.

## Фаза 0 — гигиена (закрыта)
- [x] Git-корень (корень репо), `.gitattributes`, `.opencodeignore`, baseline-коммит, remote `KurumaOfficial/LinkenVisuals`, push. → D1
- [x] Переименование StrangeVisuals → LinkenVisuals + миграция легаси-данных (приказ пользователя). → D2
- [x] `build.gradle`: убран хардкод MultiMC, JUnit 5 (bom + jupiter + platform-launcher), `useJUnitPlatform()`. → D3
- [x] `docs/`: HANDOFF, MERGE_DECISIONS, ROADMAP, PORT_STATUS, AUDIT_BASELINE (этот коммит).
- [x] Первые тесты: ConfigJson round-trip, ProfileNames, AtomicFiles, HandPoseMath clamp, ColorMath, lang-паритет en/ru/uk, std140 ↔ константы Java (kill 224 B, hit 416 B, rain 192 B, hand_outline 96 B, hand_trail 32 B, filter 64 B, ambience 64 B, background 48 B, sky 64 B), миграция LegacyConfigMigration. → 27 тестов зелёные, D5.
- [x] CI `.github/workflows/build.yml` (JDK 21, build+test).

## Фаза 1 — один пакетный вопрос пользователю (S) — [x] закрыта, D6
Ответы: авторство донорских ассетов и своих панорам/звуков подтверждено; к удалению — только панорамы + `PanoramaManager`; редактор меню, `MainMenuScreen`, `CustomMultiplayerScreen`, `AltManagerScreen`, `MacroScreen` остаются; белый список модулей и вся косметика приняты.

## Фаза 2 — чистка по списку пользователя (S–M) — [x] закрыта, D7
- [x] Панорамы + `PanoramaManager`: удалены (commit `2d5adf4`, v1.0.34), ванильный фолбэк, jar 50.5 → 1.5 МБ.
- Остальные кандидаты НЕ удаляются (решение D6б): редактор меню и его `CustomTextureLoader` (гонку чиним в Фазе 3 как фикс, не удаление), `MainMenuScreen`, `CustomMultiplayerScreen`, `AltManagerScreen`/`AccountStorage`, `MacroScreen`, эвристики `TitleScreenMixin`/`GameMenuScreenMixin`/`VanillaStylePipeline` — живут и чинятся в Фазе 3.

## Фаза 3 — фиксы базового кода (M)
- [x] Wrong-thread fix из лога запуска: DISCONNECT-путь (8 GPU-пайплайнов) → render-тред (`ce852de`, v1.0.35); проверка в игре по логу — за пользователем.
1. [x] `ClickGuiScreen` (1794 → 1394 стр.): config-окно (строки 473–804) вынесено в `ConfigPanel.java` (486 стр.); RU-строки → `I18n` (`gui.config.*`, en/ru/uk) — v1.0.36, D8.
2. [x] Миксины: локализованные эвристики убраны (новый `util/TextKeys`; `TitleScreenMixin`/`GameMenuScreenMixin`/`VanillaStylePipeline` — ключи перевода вместо `contains`); `TextFieldWidgetMixin` и `CameraMixin.setRotation` — slice-анкеры по байткоду (javap build.4), два булевых аргумента `Camera.update` — ordinal с обоснованием (в проде нет LVT-имён аргументов) — v1.0.37, D9.
3. [x] Четыре клона loading-миксинов → общий `render/LoadingOverlay` (статические хелперы; `withAlpha` ×4 → `ColorMath.argb`; кэш MSDF-шрифтов на все экраны): 645 → 317 стр. миксинов + 165 стр. хелпер (net −163) — v1.0.38, D10.
- (хотфикс 1.0.38→1.0.39) [x] Крэш при лоаде: неремапленная цель `getScale` в `CameraMixin` → `clipToSpace`; введён обязательный javap-скан member-ref'ов всех миксинов (0 yarn-имён, 24/24 intermediary) — v1.0.39, D11.
4. [x] Единый цветовой util: `ColorMath.withAlpha(rgb, 0..255)`; клоны `argb`×13 / `withAlpha`×3 удалены (302 вызова), `mix`×3 → `ColorMath.mix` (`MainMenuScreen.mix` — делегат с непрозрачной семантикой), `centredTop`×2 → `MsdfText.topFor` (17 вызовов); единый `render/Fonts` вместо MSDF-обвязки в `LoadingOverlay`/`WidgetStylePipeline`/3 экранах — v1.0.40, D12.
4b. [x] Шрифтовые константы 13 файлов → `Fonts` (18 load-сайтов; поля-алиасы сохранены, `FontDraw.getFont` упрощён) — v1.0.41, D13.
5. [x] Атомарные записи `HudLayoutStorage`, `PotionLayoutMode` + 4 файла с ручными копиями (AccountStorage, HandPose, MacroManager, MainMenuConfig) → `util.AtomicFiles` (укреплён cleanup'ом tmp при сбое); `ConfigManager.CONFIG_VERSION` пишется в 3 файла-источника и проверяется warn'ом на «файл новее сборки» — v1.0.42, D14.
6. [x] Пер-кадровые аллокации: `KeybindPanel` (кэш по новому `ModuleInfo.revision()`, uppercase-ключ в `Entry`), `Cosmetics` (переиспользуемый `CosmeticContext.set()` вместо 6 аллокаций/кадр), `HudManager` (drag-путь без аллокаций: `SNAP_OUT` + `CURSOR_X/Y`); `Pipelines.warmUp()` из кадра → в одноразовый блок первого кадра — v1.0.43, D15.
7. [x] `MediaWatcher`: `-ExecutionPolicy Bypass` убран (политика не гейтит stdin-команды — доказано прогоном под форс-`Restricted`); SCRIPT → 9 строк, каждая законченный statement (построчный stdin-режим PS 5.1 подтверждён A/B-стендами Java/.NET); не-Windows → один INFO «unsupported platform»; внешний скрипт `config/linkenvisuals_media.cmd|.sh` (формат в javadoc); `MediaWatcherTest` (5) — v1.0.44, D16.
8. [x] `HudManager`: edit-mode через явный флаг (`ChatScreenMixin` выставляет `setEditing` на `init`/`removed`; `instanceof ChatScreen` убран); тихий catch `applyBlur()` → `LOGGER.warn` один раз — v1.0.45, D17.
9. [x] README-дрейф устранён (категории ClickGUI → факт `Category.java`; 8 файлов конфига вместо одного; косметика → 9 реальных носимых, «Bandana» удалена; плюс clone-URL и имя jar) — v1.0.46, D18.
10. [x] Проверено (код не тронут): секретов в `AccountStorage` нет (только ники + offline-UUID); миксины `Yggdrasil*` (2 шт.) — нормализация неподписанных текстур, обхода аутентификации нет, риск минимальный; Sodium/Iris — 0 упоминаний в коде и `fabric.mod.json`; пост-цепочка через `WorldRenderEvents.END_MAIN` + глубина из главного фреймбуфера: с Sodium конфликтов архитектуры нет (не тестировалось), с шейдерами Iris пост-эффекты/небо в финальный кадр, скорее всего, не попадут — v1.0.47, D19.

## Фаза 4 — каркас модулей (M) — [x] закрыта, D20 (дизайн) + D21 (реализация), v1.0.49
`module/Module` (id/info/enabled, `onEnable`/`onDisable` через `onToggle`), `module/Modules` (фасад, `init()`), `ModuleRegistry.registerModule`/`module(id)`; legacy-статики не тронуты; тест `ModuleLifecycleTest` (3). Без этого п.5.7 не стартует — теперь стартует.

## Фаза 5 — переносы из донора (по фиче на коммит; перед каждой — таблица «параметр → реализация на 1.21.11»)
- [ ] 5.1 MSDF (S): сверка с донорскими `MsdfFont/FontData/MsdfGlyph`; брать только доказанные выигрыши (fastutil, кэш ширин, `recordRenderCall`, thread-asserts). → [x] закрыта, D22, v1.0.50: примитивные `IntObjectMap`/`IntFloatMap` вместо fastutil (без новой зависимости), LRU-кэш ширин (1024), thread-asserts в `write()`/`width()`; фильтрация текстуры не тронута (визуальный риск); 9 новых тестов (итого 45).
- [ ] 5.2 BlockOverlay + 10 шейдерных эффектов (M) → `RenderPipeline.builder()`. → [x] закрыта, D23, v1.0.51: 9 эффектов (10-й `.fsh` донора мёртвый — не покрыт FillMode); `BEFORE_BLOCK_OUTLINE`+`END_MAIN` (старого `BLOCK_OUTLINE` нет на 1.21.11 — extraction-модель); UBO 48 Б; первый `Module` (`BlockOverlayModule`); 4 новых теста (итого 49). Глазной контроль: матрицы на END_MAIN, окклюзия.
- [x] 5.3 Liquid Glass (L) → D24, v1.0.54: `liquidglassnew.fsh` портирован (SDF+fresnel+distortion+radial blur), UBO 96 Б, `LiquidGlassPipeline` (свой encoder/pass, переиспользует `HudGlassCapture`); `LiquidGlassCaptureBlur`/`ReflectionCache`/билдеры НЕ портированы (дубли) — см. D24.
- [x] 5.4 MotionBlur (M) → D25, v1.0.55: кадровый бленд (linear-space), UBO 16 Б, `MotionBlurRenderer` (без сырого GL, `BlitPipeline` для копий), `MotionBlurModule` (strength 5-90%, default OFF), хук `END_MAIN` (мир смазывается, HUD нет). 3 новых теста (итого 53).
- [x] 5.5 Оптимизации (S) → D26, v1.0.56: кэш `BlockPos→Box` в `BlockOverlayRenderer` (не пересчитывать shape при том же блоке); int-линии/scratch/dot-grid — уже оптимально или отсутствуют (см. D26).
- [ ] 5.6 Косметика в настройках модуля Cosmetics (L): спайк ✅ + ChinaHat ✅ (D27, v1.0.57), WaveCape ✅ (D28, v1.0.58 + фикс 18b2f3b), Nimb ✅ (D30, v1.0.59), Spheres ✅ (D31, v1.0.60). Питомцы/Rudy — пропущены (D29: существует только UserSyncManager-путь, запрещён п.3). Остаток-вопрос: Naruto/CosmeticEffects — не портированы (Naruto — 28-строчный энейблер настроек; CosmeticEffects — зонтичный модуль донора с ghost-bloom ветками) — ждёт решения пользователя. Каталог не переносим.
- [x] 5.7 Модули по белому списку, пачками с подтверждением (M каждая): WayPoints/GPS, ItemPhysics, ItemScroller, ChatHelper, MineHelper, FastExp, ElytraSwap, HitSound, HandSwap, SwingAnimation, Trails, JumpCircles, Crosshair, HitColor, TargetEsp, Zoom, Freelook, Fullbright, TimeChanger, TotemCounter/Tracker, UseTracker, StreamerMode (таблицу вынести в ресурс). → Пачка 1 закрыта (D34, v1.0.63): SwingAnimation, HitColor, Freelook, ItemPhysics — код + хуки + 26 новых тестов. Пачка 2 закрыта (D35, v1.0.64): ItemScroller, ElytraSwap, TotemTracker (событие + HUD TotemCounter/UseTracker), 11 проглоченных catch → лог; 104 теста. Пачка 3 закрыта (D36, v1.0.65): StreamerMode — таблица в ресурсе, чистое ядро `StreamerRules`/`StreamerTextSanitizer`, 7 хуков (чат/таб/скорборд/actionbar/поле ввода), 119 тестов. Все 7 заглушек D33 (SwingAnimation, HitColor, Freelook, ItemPhysics, ItemScroller, ElytraSwap, StreamerMode) закрыты пачками 1–3. Ф5.7 закрыта (D37, v1.0.66): WayPoints активирован клиентской командой `/way` (алиас `/waypoint`; add me / add xyz / remove / list / clear) + персистентность `linkenvisuals_waypoints.json` с configVersion; 127 тестов.
- [x] 5.8 ViewModel-паритет (S) → закрыта, D32 (commit fa0fefa): `HandPose` — per-axis scale (`scaleX/Y/Z`) как у донора + миграция старого uniform scale; `HeldItemRendererMixin` использует per-axis.

## Фаза 6 — финал (S)
Перф-профилирование (аллокации, draw calls), инвентарь ресурсов, README/доки, релизная сборка.
