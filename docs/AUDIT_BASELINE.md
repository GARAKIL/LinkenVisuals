# AUDIT_BASELINE — база знаний аудита (перенесено из мастер-промта, §10)

> Статус: живой документ. Строки/номера — стартовые точки, сверяй с кодом.
> Репозиторий после Фазы 0 переименован в LinkenVisuals (mod id linkenvisuals, пакет dev.linkenvisuals), исторические имена strangevisuals/StrangeVisuals ниже относятся к базовому снапшоту до переименования.

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
