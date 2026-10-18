# HANDOFF — эстафета между чатами

> Методология: этот файл обновляется в конце КАЖДОЙ задачи. Следующий чат читает его первым и продолжает с «Следующей задачи». Детали решений — `MERGE_DECISIONS.md`, скоуп — `ROADMAP.md`.

## Состояние на 2026-10-18 (чат №2)

### Сделано
1. Git-репозиторий инициализирован в корне `C:\Users\gameg\Desktop\StrangeVisuals-1.21.11` (remote: `https://github.com/KurumaOfficial/LinkenVisuals`). Baseline-коммит `2e15f5e` = v1.0.32 до переименования.
2. Единый README/LICENSE/.gitignore в корне; добавлены `.gitattributes`, `.opencodeignore` (D1).
3. **Переименование по приказу пользователя**: mod id/package/assets/lang/README → LinkenVisuals; пакет `dev.linkenvisuals`; класс `LinkenVisuals`; версия 1.0.33. Миграция легаси-файлов `strangevisuals*` → `linkenvisuals*` (класс `LegacyConfigMigration`, копирование без удаления оригинала). Коммит `7a5b638`.
4. `build.gradle`: хардкод `D:/MultiMC/instances/1.21.111` удалён → opt-in `-PmultiMcModsDir` (локально прописан в `%USERPROFILE%\.gradle\gradle.properties`); добавлен JUnit 5 (bom 5.11.4 + jupiter + platform-launcher), `test { useJUnitPlatform() }`. Коммит `568c692`.
5. `docs/`: `AUDIT_BASELINE.md` (из §10 мастер-промта), `ROADMAP.md`, `PORT_STATUS.md`, `MERGE_DECISIONS.md`, `HANDOFF.md` (этот файл).
6. Тестовый набор Фазы 0: 27 тестов в 9 классах (см. D5) — зелёные. Микро-рефакторинг тестируемости: `util/ProfileNames`, `util/AtomicFiles`, `util/ColorMath`, `clickgui/ConfigJson`, `hands/HandPoseMath`; публичные константы UBO.
7. CI `.github/workflows/build.yml` (JDK 21 temurin, build+test). Полный `build` зелёный; jar `source/build/libs/linkenvisuals-1.0.33.jar` собран и задеплоен в `D:\MultiMC\instances\1.21.111\.minecraft\mods`.
8. **Запуск в игре подтверждён (лог MultiMC, чат №2)**: `linkenvisuals 1.0.33` загружен, `Migrated 8 legacy strangevisuals data entries to linkenvisuals`, `Active Style: Vanilla`, ошибок миксинов нет. Из лога найден баг: при выходе из мира GPU-ресурсы глушатся не в render-треде («Rendersystem called from wrong thread» ×9) → первый пункт Фазы 3.
9. **Фаза 1 закрыта (D6)**: ответы пользователя — авторство донорских ассетов и своих панорам/звуков подтверждено; удаление — только панорамы; белый список модулей и вся косметика приняты.
10. **Фаза 2 закрыта (D7, commit `2d5adf4` = v1.0.34)**: панорамы + `PanoramaManager` + кейбинд INSERT удалены, ванильный фолбэк (`VanillaStylePipeline.renderVanillaPanorama`), jar 50.5 МБ → 1.5 МБ, тесты зелёные, jar задеплоен (старый 1.0.33 из mods удалён; деплой теперь чистит сам).
11. **Фаза 3.1 (wrong-thread), commit `ce852de` = v1.0.35**: GPU-освобождение в DISCONNECT-пути (`shutdownWorldGpu` — 8 пайплайнов) перенесено на render-тред (`client.isOnThread()` → напрямую, иначе `client.execute`). В игре **не проверено**: критерий — после выхода из мира 0 × «Rendersystem called from wrong thread» (было 9).

### Следующая задача (старт нового чата)
**Фаза 3 — фиксы базового кода** (пункт 3.1 закрыт, см. «Сделано» №11):
1. Пункты ROADMAP 3.1–3.10 по порядку (ClickGuiScreen-разрез, миксины-эвристики, ColorUtil, атомарные записи, аллокации и т.д.).
2. Пользователю: тест 1.0.35 — войти в мир и выйти; в `latest.log` после выхода должно быть 0 × «Rendersystem called from wrong thread» (было 9). Заодно проверить 1.0.34: loading-экраны и ванильный стиль — стоковая панорама MC; редактор меню — цикл Shader/Photo/Both; бинд INSERT отсутствует в управлении.

### Открытые решения (ждём пользователя)
- LICENSE не менялся: в копирайт-строке осталось «StrangeVisuals Team & Contributors» — решить, обновлять ли держателя (правило «лицензию не менять» пока в силе).
- `README.md`/`README_RU.md` перебрендированы механически; полноценная переписка (дрейф: адрес конфига, категории, косметика «Bandana») — Фаза 3.9.
- `source/tools/*` — скрипты ассет-пайплайна: не трогали, проверить их актуальность при чистках/переносах.
- Деплой: задача `remapJar` теперь сама удаляет старые `linkenvisuals-*.jar` из mods-каталога перед копированием (иначе «Duplicate mod» на запуске); проверено `.\gradlew.bat remapJar --rerun`.

### Проверенные факты (для следующего чата)
- Сборка: `cd source; $env:JAVA_HOME='C:\Program Files\Java\jdk-21'; cmd /c ".\gradlew.bat build --console=plain 2>&1" | Select-Object -Last 40`.
- `git push` работает (GIT_TERMINAL_PROMPT=0, credential helper настроен).
- Кириллица в консоли: `chcp 65001`; в PowerShell 5.1 `[System.IO.File]::ReadAllText('relative')` **не работает** — только абсолютные пути.
- В инстансе MultiMC лежит fabric-api 0.141.6 (сборка против 0.141.3 — совместимо).
- jar проекта: 50.5 МБ (1.0.33, с панорамами) → 1.5 МБ (1.0.34). Полный цикл: `.\gradlew.bat build --console=plain` (+ `-PbumpVersion` для патч-бампа; деплой — из `%USERPROFILE%\.gradle\gradle.properties`).
