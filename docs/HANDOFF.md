# HANDOFF — эстафета между чатами

> Методология: этот файл обновляется в конце КАЖДОЙ задачи. Следующий чат читает его первым и продолжает с «Следующей задачи». Детали решений — `MERGE_DECISIONS.md`, скоуп — `ROADMAP.md`.

## Состояние на 2026-10-18 (чат №3)

### Сделано
1. **Тест Ф3.1 (1.0.35) подтверждён логом** (`D:\MultiMC\instances\1.21.111\.minecraft\logs\latest.log`, 16:37): запуск `linkenvisuals 1.0.35`, вход в мир «New World», выход («Stopping singleplayer server as player logged out» → «Saving worlds»); `wrong thread` = **0** (было 9), всего WARN/ERROR = 0. Критерий закрыт — регрессии «Rendersystem called from wrong thread» на DISCONNECT-пути нет.
2. **Ф3.2 закрыта (v1.0.36, D8)**: config-окно `ClickGuiScreen` (бывшие строки 473–804) вынесено в новый `clickgui/ConfigPanel.java` (486 строк); `ClickGuiScreen` 1794 → 1394 строк; 16 RU-строк → ключи `gui.config.*` (en/ru/uk), резолв через `dev.linkenvisuals.i18n.I18n.get`; заодно локализован placeholder поиска (`gui.search.placeholder`). Поведение не менялось: статические поля остались статическими, скроллбар — колбэк, шрифты биндятся в `init()`.
3. Гигиена: lang-файлы нормализованы в LF (в git-блобах лежал CRLF с прежних коммитов) — CRLF-шум ушёл.
4. `build` зелёный (27 тестов, LangParityTest с новыми ключами), jar `linkenvisuals-1.0.36.jar` задеплоен в MultiMC (старый удалён деплоем).

### Следующая задача (старт нового чата)
**Ф3.3 (ROADMAP п.2)** — миксины: убрать эвристики по локализованному тексту (`TitleScreenMixin`, `GameMenuScreenMixin`, `VanillaStylePipeline`), ordinal-анкеры (`CameraMixin`, `TextFieldWidgetMixin`) → именованные. Дальше по порядку: Ф3.4 единый `ColorUtil` (заодно убрать дубли `centredTop`/`argb`, заведённые D8) → Ф3.5 атомарные записи + `configVersion` → Ф3.6 пер-кадровые аллокации → Ф3.7 MediaWatcher → Ф3.8 HudManager edit-mode → Ф3.9 README-дрейф → Ф3.10 секреты/Yggdrasil/Sodium-Iris.

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
