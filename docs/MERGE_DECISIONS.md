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

## 2026-10-18 · D4 · Тестируемость Фазы 0 (микро-рефакторинг без смены поведения)
- Причина: юнит-тесты обязаны идти без bootstrap Minecraft; `ConfigManager`/`HandPose`/`Material` тянут `FabricLoader`/GPU-статику в clinit.
- План (commit с тестами): чистые классы `util.ProfileNames` (санитайз имени), `util.AtomicFiles` (tmp + ATOMIC_MOVE + fallback), `clickgui.ConfigJson` (build/apply модулей), `hands.HandPoseMath` (клэмпы), `util.ColorMath` (mix/argb/…); константы размеров UBO сделать `public static final` для сверки тестом std140.
- Экспозиция: `public` у констант — только видимость, значение не меняется; инлайнится javac, GPU-класс в тестах не инициализируется.
