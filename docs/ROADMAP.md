# ROADMAP — дорожная карта LinkenVisuals

Порядок обязателен. Оценки: S/M/L. Обновляется по мере продвижения; журнал решений — `MERGE_DECISIONS.md`.

## Фаза 0 — гигиена (в работе)
- [x] Git-корень (корень репо), `.gitattributes`, `.opencodeignore`, baseline-коммит, remote `KurumaOfficial/LinkenVisuals`, push. → D1
- [x] Переименование StrangeVisuals → LinkenVisuals + миграция легаси-данных (приказ пользователя). → D2
- [x] `build.gradle`: убран хардкод MultiMC, JUnit 5 (bom + jupiter + platform-launcher), `useJUnitPlatform()`. → D3
- [ ] `docs/`: HANDOFF, MERGE_DECISIONS, ROADMAP, PORT_STATUS, AUDIT_BASELINE (этот коммит).
- [ ] Первые тесты: ConfigJson round-trip, ProfileNames, AtomicFiles, HandPoseMath clamp, ColorMath, lang-паритет en/ru/uk, std140 ↔ константы Java (kill 224 B, hit 416 B, rain 192 B, hand_outline 96 B, hand_trail 32 B, filter 64 B, ambience 64 B, background 48 B, sky 64 B), миграция LegacyConfigMigration.
- [ ] CI `.github/workflows` (JDK 21, build+test).

## Фаза 1 — один пакетный вопрос пользователю (S)
(а) авторство донорских шейдеров/ассетов/звуков + панорам/звуков проекта до публичного релиза; (б) список «дребедени» к удалению (кандидаты: редактор главного меню и пресеты, `MainMenuScreen`, `CustomMultiplayerScreen`, `AltManagerScreen`, `MacroScreen`); (в) белый список модулей и косметик к переносу. До ответа ничего не удалять и не переносить ассеты.

## Фаза 2 — чистка по списку пользователя (S–M), ДО фиксов
Каждое удаление отдельным коммитом, с миграцией конфигов. Код, назначенный к удалению, не чинится. Бонус: уходят гонка `CustomTextureLoader`, хак `PanoramaManager`, часть эвристик миксинов, ~51 МБ панорам в jar.

## Фаза 3 — фиксы базового кода (M)
1. `ClickGuiScreen` (1794 стр.): settings-окно (494–786) в отдельный класс; RU-строки через `I18n`.
2. Миксины: убрать эвристики по локализованному тексту (`TitleScreenMixin`, `GameMenuScreenMixin`, `VanillaStylePipeline` — если переживут Фазу 2); ordinal-анкеры (`CameraMixin`, `TextFieldWidgetMixin`) → именованные.
3. Четыре клона loading-миксинов → общий базовый.
4. Единый `ColorUtil` вместо ~22 клонов mix/withAlpha/argb.
5. Атомарные записи `HudLayoutStorage`, `PotionLayoutMode`; общий persistence-хелпер; `configVersion`.
6. Пер-кадровые аллокации: `KeybindPanel`, `Cosmetics`, `HudManager.snap`; убрать `Pipelines.warmUp()` из кадра.
7. `MediaWatcher`: PowerShell `-ExecutionPolicy Bypass` → «unsupported platform» + опциональный внешний скрипт.
8. `HudManager`: edit-mode не через «is a ChatScreen»; убрать тихий catch.
9. README-дрейф (адрес конфига, категории, несуществующая косметика «Bandana»).
10. Проверить (не удалять без приказа): секреты в `AccountStorage`, миксины `Yggdrasil*`, совместимость Sodium/Iris (описать конфликтные места).

## Фаза 4 — каркас модулей (M)
`ModuleRegistry` — статические поля. Предложить минимальный дизайн `Module`/`Setting` (без event-bus и зависимостей), получить подтверждение, затем вводить. Без этого п.5.7 не стартует.

## Фаза 5 — переносы из донора (по фиче на коммит; перед каждой — таблица «параметр → реализация на 1.21.11»)
- [ ] 5.1 MSDF (S): сверка с донорскими `MsdfFont/FontData/MsdfGlyph`; брать только доказанные выигрыши (fastutil, кэш ширин, `recordRenderCall`, thread-asserts).
- [ ] 5.2 BlockOverlay + 10 шейдерных эффектов (M) → `RenderPipeline.builder()`.
- [ ] 5.3 Liquid Glass (L): сверка с `GlassPanelPipeline`/`HudGlassCapture`/`ScreenCapture` от дублей цепочек.
- [ ] 5.4 MotionBlur (M): без сырого `glUseProgram(0)`.
- [ ] 5.5 Оптимизации-идеи (S): LRU `VoxelShape`, int-ядро линий, scratch-буферы, batched dot-grid/hatch — только где есть соответствующие циклы.
- [ ] 5.6 Косметика в настройках модуля Cosmetics (L): spike инфраструктуры, затем wave-плащ, питомцы, ChinaHat, Nimb, Naruto, CosmeticEffects, Spheres. Каталог не переносим.
- [ ] 5.7 Модули по белому списку, пачками с подтверждением (M каждая): WayPoints/GPS, ItemPhysics, ItemScroller, ChatHelper, MineHelper, FastExp, ElytraSwap, HitSound, HandSwap, SwingAnimation, Trails, JumpCircles, Crosshair, HitColor, TargetEsp, Zoom, Freelook, Fullbright, TimeChanger, TotemCounter/Tracker, UseTracker, StreamerMode (таблицу вынести в ресурс).
- [ ] 5.8 ViewModel-паритет (S): донорский `ViewModel` — референс; недостающее — в `HandEditor`/`HandPose`.

## Фаза 6 — финал (S)
Перф-профилирование (аллокации, draw calls), инвентарь ресурсов, README/доки, релизная сборка.
