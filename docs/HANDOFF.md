# HANDOFF — эстафета между чатами

> Методология: этот файл обновляется в конце КАЖДОЙ задачи. Следующий чат читает его первым и продолжает с «Следующей задачи». Детали решений — `MERGE_DECISIONS.md`, скоуп — `ROADMAP.md`.

## Состояние на 2026-10-18 (чат №4)

### Сделано
1. **Тест Ф3.1 (1.0.35) подтверждён логом** (`D:\MultiMC\instances\1.21.111\.minecraft\logs\latest.log`, 16:37): запуск `linkenvisuals 1.0.35`, вход в мир «New World», выход («Stopping singleplayer server as player logged out» → «Saving worlds»); `wrong thread` = **0** (было 9), всего WARN/ERROR = 0. Критерий закрыт — регрессии «Rendersystem called from wrong thread» на DISCONNECT-пути нет.
2. **Ф3.2 закрыта (v1.0.36, D8)**: config-окно `ClickGuiScreen` (бывшие строки 473–804) вынесено в новый `clickgui/ConfigPanel.java` (486 строк); `ClickGuiScreen` 1794 → 1394 строк; 16 RU-строк → ключи `gui.config.*` (en/ru/uk), резолв через `dev.linkenvisuals.i18n.I18n.get`; заодно локализован placeholder поиска (`gui.search.placeholder`). Поведение не менялось: статические поля остались статическими, скроллбар — колбэк, шрифты биндятся в `init()`.
3. Гигиена: lang-файлы нормализованы в LF (в git-блобах лежал CRLF с прежних коммитов) — CRLF-шум ушёл.
4. `build` зелёный (27 тестов, LangParityTest с новыми ключами), jar `linkenvisuals-1.0.36.jar` задеплоен в MultiMC (старый удалён деплоем).
5. **Ф3.3 закрыта (v1.0.37, D9)**: идентификация виджетов — только по ключам перевода (новый `util/TextKeys`); `TitleScreenMixin`/`GameMenuScreenMixin`/`VanillaStylePipeline` — ключи вместо `contains(...)`, 12 вызовов `getIconFor` → `getIconForKey`; `TextFieldWidgetMixin` и `CameraMixin.setRotation` переведены с ordinal на slice-анкеры по байткоду (javap yarn build.4: format@234/drawText@258/getWidth@269; yaw@258/setRotation@269/getScale@294); ремап целей подтверждён дампом аннотаций собранного jar (промежуточные `class_*`, refmap не нужен). Видимое изменение — семантические иконки кнопок под-экранов настроек (раньше из-за порядка проверок почти все получали шестерёнку) — ждёт игровой проверки вместе с визуалом 1.0.36.
6. **Ф3.4 (п.3) закрыта (v1.0.38, D10)**: 4 loading-миксина (645 стр.) сведены к общему `render/LoadingOverlay` (165 стр.); миксины 645 → 317 стр.; удалены 4 копии `withAlpha` (→ `ColorMath.argb`), 4 копии lazy-загрузки MSDF-шрифтов (теперь статический кэш `LoadingOverlay.titleFont()/textFont()` — один на все 4 экрана) и 4 копии beta/vanilla-сцен; `Screen#renderPanoramaBackground` (protected) передаётся через `@FunctionalInterface PanoramaDrawer`.
7. **Хотфикс краша 1.0.38 → 1.0.39 (D11)**: игра падала на лоаде (`CameraMixin`, «Invalid name: getScale:») — цель slice `to` осталась yarn-именем при ремапе (owner ремапнут, имя нет). Заменена на `Camera.clipToSpace(F)F` (класс, чьи рефы доказанно ремапятся); javap jar 1.0.39 — все цели intermediary; скан всех 40 mixin-классов jar — 0 yarn-имён в member-ref'ах аннотаций (контроль на 1.0.38 находит ровно `class_1309;getScale:()F`).
8. **Ф3.4 (п.4) закрыта (v1.0.40, D12)**: единый цветовой свод — 16 клонов `argb`/`withAlpha` удалены, 302 вызова → `ColorMath.withAlpha(rgb, 0..255)`; `mix`-клоны ClickGui/NameTags/WidgetStylePipeline → `ColorMath.mix` (WidgetStylePipeline.mix удалён, 4 внешних вызова обновлены), `MainMenuScreen.mix` — делегат с прежней непрозрачной семантикой; `centredTop` (ClickGui+ConfigPanel) ≡ `MsdfText.topFor` — 17 вызовов, методы/константы удалены; новый единый `render/Fonts` вместо копий MSDF-обвязки в `LoadingOverlay`/`WidgetStylePipeline`/`CustomMultiplayerScreen`/`MainMenuScreen`/`MainMenuEditorScreen` (+ ConnectScreenMixin); javap-скан jar 1.0.40 — 24 member-ref'а, 0 yarn-имён.

### Следующая задача (старт нового чата)
**Ф3.5 (ROADMAP п.5)** — атомарные записи `HudLayoutStorage`, `PotionLayoutMode` + общий persistence-хелпер (`util/AtomicFiles` уже есть — обобщить) и `configVersion` в конфигах. Затем п.4b (шрифтовые константы 12 HUD-файлов + ClickGuiScreen → `Fonts`; нужен пофайловый проход), дальше по порядку: Ф3.6 пер-кадровые аллокации → Ф3.7 MediaWatcher → Ф3.8 HudManager edit-mode → Ф3.9 README-дрейф → Ф3.10 секреты/Yggdrasil/Sodium-Iris.

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
- Цвет/шрифты после D12: единый источник — `util.ColorMath` (`withAlpha(rgb, byte)` clamp, `argb(rgb, float 0..1)`, `mix` дропает альфу) и `render.Fonts` (`attempted`-флаг; `GlyphAtlas.load` кэширует по Identifier — дублировалась только обвязка). Массовые своды делать скриптом с пофайловыми count-проверками и абортом до записи (шаблон: `%TEMP%\opencode\refactor_34p4.ps1`); удаление методов — балансным сканом скобок, не regex'ом до первой `}`-строки (тела с try/catch ломают наивный regex); после сводов проверять тройные пустые строки (в этот раз 3 штуки, схлопнуты; 2 в MainMenuScreen — пре-существующие).
- Повторная сборка в той же фазе — БЕЗ `-PbumpVersion` (флаг бампает на каждом вызове; повтор даст лишний бамп).
