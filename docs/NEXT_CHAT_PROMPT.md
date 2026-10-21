# NEXT CHAT — LinkenVisuals: звуки донора + остаток strict 1:1

## Контекст
- Репозиторий: https://github.com/KurumaOfficial/LinkenVisuals
- Ветка: `main`
- HEAD при передаче: см. `git log -1` (последний docs-коммит), последний код: `D80`
- Версия мода: **1.0.103**
- Тесты: **216 / 0 падений**
- Исходники: `<repo>/source`
- Документация: `<repo>/docs`
- Донор (READ-ONLY): `C:\Users\gameg\Desktop\визуалы`; release-jar — `build/libs/linkenvisuals-2.0.jar`.
- Авторство донорских ассетов подтверждено пользователем (D6), лицензия донора — ARR.

## Первым делом
1. `git clone https://github.com/KurumaOfficial/LinkenVisuals.git`
2. `git checkout main && git pull`
3. Проверь: последний код `D80`; `source/gradle.properties` → `mod_version=1.0.103`.
4. Прочитай `docs/HANDOFF.md` (верхняя секция, чат №34), `docs/MERGE_DECISIONS.md` D76–D80, `docs/ROADMAP.md` M8.10–M8.14, `docs/PORT_STATUS.md`.
5. `cd source`
6. `.\gradlew.bat test --console=plain --no-daemon` → ожидаемо 216/0.

## Главная цель
Довести visual-слой LINKEN до строгого 1:1 с донорским `LinkGuiScreen` и принять звуковую часть: боевые cues (kill / streak 2–5 / totem pop / low HP / death / HitSound Bell/Crime/Nya/Uwu) на сервере или в мире. Визуальный и звуковой слой уже перенесены; остались ручная приёмка и точечные расхождения.

## Жёсткие требования пользователя
- Пока активен **Linken GUI**, переключение на Classic/Beta **запрещено**. Не возвращай эту возможность.
- Кнопку смены стилей GUI и кнопку редактора главного меню в **LINKEN Settings не добавлять**.
- Beta/Vanilla не должны ломаться или перерисовываться.
- Все видимые кнопки должны работать; мёртвые клики запрещены.
- i18n: en/ru/uk для новых строк; `LangParityTest` зелёный.
- Не коммитить `build/`, `run/`, jar, логи, скриншоты, временные файлы.
- Feature/fix-коммит: `+1 patch` от фактической версии. Docs-only — без бампа.
- Перед коммитом: `git fetch origin`, force push запрещён. После зелёной сборки — commit + push, jar в `source/build/libs`, SHA-256, отчёт на русском.
- Новый запуск Minecraft — только после явного разрешения пользователя (в этой задаче разрешение было выдано на release-jar приёмку).

## Что уже сделано (D80 / v1.0.103)
- 35 донорских sound-событий в `assets/linkenvisuals/sounds.json`; все OGG на месте, включая `mainmenu/evilshit.ogg` и `cosmetic/sounds/*`.
- `audio/LinkenSounds` — реестр и playback helpers; `audio/LinkenMenuMusic` — title-трек.
- UI-клики всех LINKEN-экранов → `LinkenSounds.UI_CLICK` (`enable.ogg`); тумблеры → `enable`/`disable`.
- `HitSoundModule` — донорские Bell/Crime/Nya/Uwu; `KillEffects` и `FakePlayer` — только донорские cues.
- `SoundsModule` (Player, default ON): Volume 130 %, Kills, Kill Streak, Totem Pop, Death, Low Health, Low Health At 6 HP; melee + `EntityDamageS2CPacket` (свои стрелы).
- Главное меню: `EVILSHIT` / `ARLEKIN 40 000`, 131,3 с, volume 0.4; запускается один раз, живёт между меню, останавливается при входе в мир, заменяет vanilla menu music; карточка показывает `EVILSHIT by ARLEKIN 40 000`.
- `SoundAssetsTest` (35 событий + OGG + `ui.click` → `enable`), `SoundsModuleTest`, config round-trip для Sounds.
- Release-jar 1920×1080: title с треком/версией 1.0.103, в логе нет `Unable to play unknown soundEvent`; чёрные области слева совпадают с донорским reference (не регрессия).
- Jar: `source/build/libs/linkenvisuals-1.0.103.jar`, 10 657 799 байт, SHA-256 `64494B17AE61674037DD9588ED5EA4DC20DEDE941CB27808FF971B9B679F26F3`.

## Что осталось (приоритет)
1. **Звуковая приёмка в игре**
   - Убийство мечом/стрелой: `molodec_ubil` / `unichtozjen` / `zapen`.
   - Серия 2–5 за 9 с: `dota_double_kill` … `dota_rampage`.
   - Totem pop, смерть, low HP; HitSound все 4 режима.
   - Проверить, что vanilla-звуки мода не звучат (UI clicks, FakePlayer, KillEffects).
2. **Остаток strict 1:1**
   - Pinned settings cards.
   - Donor-only Interface groups: HUD style/shape, custom scoreboard/tab (в нашем моде этих настроек нет — панель короче; либо портировать, либо зафиксировать отклонение).
   - Точная сверка шрифтов/отступов/радиусов на 1280×720 и 1920×1080.
3. **Shader-source race** при старте (`Couldn't find source` для `linkenvisuals:core/*` и `minecraft:core/position_color`) — отдельный открытый дефект, не блокирует UI/мир.
4. **Media strip в LinkGUI** (in-game) по-прежнему читает `MediaWatcher`, а не menu-трек. Это осознанно: трек принадлежит title screen.

## Ключевые файлы
```
source/src/main/java/dev/linkenvisuals/audio/
  LinkenSounds.java        — 35 events, play/playAt/ui click, Cue enum
  LinkenMenuMusic.java     — title track state/progress/pause/resume
source/src/main/java/dev/linkenvisuals/module/impl/
  SoundsModule.java        — kill/streak/totem/low HP/death
  HitSoundModule.java      — Bell/Crime/Nya/Uwu
source/src/main/java/dev/linkenvisuals/mixin/
  ClientPlayerInteractionManagerMixin.java — melee hit + SoundsModule.noteHit
  ClientPlayNetworkHandlerMixin.java       — damage packet + totem pop
source/src/main/resources/assets/linkenvisuals/
  sounds.json              — 35 events
  sounds/mainmenu/evilshit.ogg
  sounds/{bell,crime,nya,uwu,...}.ogg
source/src/test/java/dev/linkenvisuals/audio/SoundAssetsTest.java
source/src/test/java/dev/linkenvisuals/module/SoundsModuleTest.java
source/src/main/java/dev/linkenvisuals/ui/linkmenu/LinkenMainMenuScreen.java
source/src/main/java/dev/linkenvisuals/ui/linkmenu/LinkenMenuChrome.java
```

## Правила работы
- Перед изменением layout обновляй соответствующий тест (`LinkGuiLayoutTest`, `LinkenMenuLayoutTest`).
- Не тянуть Lombok, донорские Module/Setting, legacy `Render2D`/`WindowGuiRender`, Gif/Lottie, сеть, косметику, лишнее аудио.
- Ошибки/fallback — через `LOGGER.warn/debug`, не пустые catch.
- После зелёной сборки: commit + push, SHA-256, отчёт на русском.
- В конце обновить `docs/HANDOFF.md`, `docs/MERGE_DECISIONS.md` (D81+), `docs/ROADMAP.md`, `docs/PORT_STATUS.md`; docs-коммит отдельно.

## Команды
```powershell
cd source
.\gradlew.bat clean build --console=plain --no-daemon
# bump patch:
.\gradlew.bat clean build -PbumpVersion --console=plain --no-daemon
```

## Известные риски
- `pauseAllExcept()` на паузе menu-трека глушит все звуки (как в доноре).
- Kill detection по исчезновению цели при HP ≤ 6: скрытое сервером HP может дать пропуск.
- `LinkenSelectWorldScreen` использует vanilla `WorldListWidget` через off-screen `renderWidget` — хрупко к API.
- Create/Edit/Re-Create World и death screen остаются vanilla (донор тоже).
- `LinkGuiScreen.java` большой; правки точечные, не ломать hit-зоны.
- Донорский `LinkGuiThemeWindowLayout` удалён в D67 — не возвращать.
