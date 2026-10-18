# HANDOFF — эстафета между чатами

> Методология: этот файл обновляется в конце КАЖДОЙ задачи. Следующий чат читает его первым и продолжает с «Следующей задачи». Детали решений — `MERGE_DECISIONS.md`, скоуп — `ROADMAP.md`.

## Состояние на 2026-10-18 (чат №1, открыт)

### Сделано
1. Git-репозиторий инициализирован в корне `C:\Users\gameg\Desktop\StrangeVisuals-1.21.11` (remote: `https://github.com/KurumaOfficial/LinkenVisuals`). Baseline-коммит `2e15f5e` = v1.0.32 до переименования.
2. Единый README/LICENSE/.gitignore в корне; добавлены `.gitattributes`, `.opencodeignore` (D1).
3. **Переименование по приказу пользователя**: mod id/package/assets/lang/README → LinkenVisuals; пакет `dev.linkenvisuals`; класс `LinkenVisuals`; версия 1.0.33. Миграция легаси-файлов `strangevisuals*` → `linkenvisuals*` (класс `LegacyConfigMigration`, копирование без удаления оригинала). Коммит `7a5b638`.
4. `build.gradle`: хардкод `D:/MultiMC/instances/1.21.111` удалён → opt-in `-PmultiMcModsDir` (локально прописан в `%USERPROFILE%\.gradle\gradle.properties`); добавлен JUnit 5 (bom 5.11.4 + jupiter + platform-launcher), `test { useJUnitPlatform() }`. Коммит `568c692`.
5. `docs/`: `AUDIT_BASELINE.md` (из §10 мастер-промта), `ROADMAP.md`, `PORT_STATUS.md`, `MERGE_DECISIONS.md`, `HANDOFF.md` (этот файл).
6. Тестовый набор Фазы 0: 27 тестов в 9 классах (см. D5) — зелёные. Микро-рефакторинг тестируемости: `util/ProfileNames`, `util/AtomicFiles`, `util/ColorMath`, `clickgui/ConfigJson`, `hands/HandPoseMath`; публичные константы UBO.
7. CI `.github/workflows/build.yml` (JDK 21 temurin, build+test). Полный `build` зелёный; jar `source/build/libs/linkenvisuals-1.0.33.jar` собран и задеплоен в `D:\MultiMC\instances\1.21.111\.minecraft\mods`.

### Следующая задача (старт нового чата)
**Фаза 1 — один пакетный вопрос пользователю** (без него нельзя удалять/переносить ассеты и код):
(а) подтвердить авторство донорских шейдеров/ассетов/звуков (см. `THIRD_PARTY_ASSETS.md` донора) и панорам/звуков проекта перед публичным релизом;
(б) список «дребедени» к удалению в проекте (кандидаты: редактор главного меню и пресеты стилей, `MainMenuScreen`, `CustomMultiplayerScreen`, `AltManagerScreen`, `MacroScreen`);
(в) белый список модулей/косметик к переносу (Фаза 5).
До ответа ничего не удалять и не переносить ассеты. После ответа — Фаза 2 (чистка, каждое удаление отдельным коммитом с миграцией конфигов), затем Фаза 3 (фиксы).

### Открытые решения (ждём пользователя)
- LICENSE не менялся: в копирайт-строке осталось «StrangeVisuals Team & Contributors» — решить, обновлять ли держателя (правило «лицензию не менять» пока в силе).
- `README.md`/`README_RU.md` перебрендированы механически; полноценная переписка (дрейф: адрес конфига, категории, косметика «Bandana») — Фаза 3.9.
- `source/tools/*` — скрипты ассет-пайплайна: не трогали, проверить их актуальность при Фазе 2.

### Проверенные факты (для следующего чата)
- Сборка: `cd source; $env:JAVA_HOME='C:\Program Files\Java\jdk-21'; cmd /c ".\gradlew.bat build --console=plain 2>&1" | Select-Object -Last 40`.
- `git push` работает (GIT_TERMINAL_PROMPT=0, credential helper настроен).
- Кириллица в консоли: `chcp 65001`; в PowerShell 5.1 `[System.IO.File]::ReadAllText('relative')` **не работает** — только абсолютные пути.
- В инстансе MultiMC лежит fabric-api 0.141.6 (сборка против 0.141.3 — совместимо).
