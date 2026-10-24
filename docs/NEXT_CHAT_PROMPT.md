# ПРОМТ ДЛЯ СЛЕДУЮЩЕГО ЧАТА — LinkenVisuals 1.21.11: pixel-perfect доработка визуала

Ты продолжаешь существующий порт **LinkenVisuals 2.0 (MC 1.21.4) → LinkenVisuals 1.21.11**. Не начинай проект заново и не переписывай принятые решения. Этот чат — **только чистая визуальная доработка и сверка пиксель в пиксель**. Новые фичи, настройки, модули, экраны и «улучшения» геймплея запрещены.

Пользователь считает, что различия при запуске и мелкие визуальные дефекты всё ещё есть. Твоя задача — найти их объективно, починить то, что относится к порту, и доказать результат скриншотами + числовым diff. Не заявляй «1:1», если это не подтверждено.

---

## 1. Репозиторий и текущее состояние

- Repo: `https://github.com/KurumaOfficial/LinkenVisuals`
- Branch: `main`
- Version: **1.0.140** (не поднимать без прямого приказа пользователя)
- Последний code commit: `a87d04b` — `fix(ui): restore pre-D117 LV logo with SDF edges (v1.0.140, D117b)`
- Предыдущие: `ee65d37` — D117 Create World/loading; `155645e` — docs D117
- Docs-коммит с этим промтом идёт следующим; проверь `git log -3 --oneline` после `git pull`.

Текущие проверки:

- **82 набора / 324 теста**, 0 failures / 0 errors / 0 skipped в обоих flavors.
- Linken autotest: **1158/1158**, 0 fail/skip, 0 новых ERROR/exception (1920×1080, прогон после D117b).
- Strange autotest: **1148/1148**, 0 fail/skip; 3 ERROR — только offline-auth/Realms (прогон D117; D117b меняет только PNG и ветку `drawLogo`).
- Linken jar: `linkenvisuals-1.0.140.jar`, **11 180 156 байт**, SHA-256 `34282DFE9ABD6874B63580083941EEFBA4D678AB5620FDC8720869999C3A2259`.
- Strange jar: `linkenvisuals-strange-1.0.140.jar`, **11 180 144 байта**, SHA-256 `586D71642799253C4822088C6702931F529AA476596214AFA64FADDC585C341B`.
- Linken jar развёрнут в `D:\MultiMC\instances\1.21.111\.minecraft\mods`; SHA-256 совпадает.

Что уже принято и не переделывается:

- D117: loading screen переложен под donor 9d; Create World полностью перерисован `LinkenCreateWorldSkin`; ESC-tile `LINKENVISUALS HUD` удалён.
- D117b: возвращён **прежний** LV-монограмм из `6c6919a`; края пересобраны SDF-мастером 2048; `drawLogo` для `size <= 128` использует 120 px `logo.png`, для крупных — 512 px `lv_logo.png`.
- В текстовом wordmark главного меню/welcome/pause LV-бейджа нет; LV остаётся в LinkGUI header/footer и HUD. Strange использует только `strange_logo.png` и только в `-Pstrange`.

Evidence: `source/run/screenshots/evidence-d117b/` — `linkgui-header.png`, `linkgui-header-zoom.png`, `logo-compare-old-new.png`, `pause.png`, `report.log`.

---

## 2. Где donor и где порт

Donor (READ-ONLY, ничего не менять):

- Исходники: `C:\Users\gameg\Desktop\визуалы` (`ru.method.linkenvisuals`, MC 1.21.4)
- Запуск donor-клиента: `D:\MultiMC\instances\1.21.4` (`linkenvisuals-2.0.jar`, `fabric-api-0.119.4-1.21.4.jar`)
- Команда: `D:\MultiMC\MultiMC.exe --launch 1.21.4`

Порт:

- Исходники: `C:\Users\gameg\Desktop\VS\LinkenVisuals\source`
- Запуск: `D:\MultiMC\instances\1.21.111`
- Команда: `D:\MultiMC\MultiMC.exe --launch 1.21.111`

Donor-классы-эталоны:

- `screen/overlay/LinkenLoadingScreen.java`
- `screen/mainmenu/CustomMainMenuScreen.java`
- `screen/settings/LinkenPauseScreen.java`
- `screen/linkgui/LinkGuiScreen.java`
- `screen/mainmenu/MenuChrome.java`

Порт-классы:

- `ui/linkmenu/LinkenLoadingScreen.java`
- `ui/linkmenu/LinkenMainMenuScreen.java`
- `ui/linkmenu/LinkenPauseScreen.java`
- `ui/linkmenu/LinkenCreateWorldSkin.java`
- `ui/linkmenu/LinkenMenuChrome.java`
- `clickgui/link/LinkGuiScreen.java`
- `mixin/SplashOverlayMixin.java`, `mixin/CreateWorldScreenMixin.java`

---

## 3. Жёсткие ограничения

1. **Никаких новых фич, экранов, настроек, модулей и контролов.** Только исправление уже существующего визуала.
2. **Punchy и Punchy-зависимый `ViewModel Animated` исключены** решением пользователя (2026-10-24). `HandMotion` — финальный принятый subset. Не открывать их и не упоминать как «остаток».
3. **LV-логотип только в Linken-сборке**, Strange — только `strange_logo.png` в `-Pstrange`. Никаких cross-family стилей.
4. **Без мёртвых контролов.** Если элемент виден, он должен работать.
5. **Не менять версию** `1.0.140` без прямого приказа.
6. **Create World:** у donor 1.21.4 нет собственного экрана создания мира. Точный «1:1» невозможен; сверять только язык стиля (фон, карточка, типографика, отступы, состояния). Ванильную сетку Game/More не перестраивать.
7. **`run/` и артефакты сборки не коммитить.** Jar, логи, скриншоты, `build/` — вне git.
8. Любое изменение подтверждать сборкой, тестами и скриншотом. «Должно работать» запрещено.

---

## 4. Методология pixel-diff

### Шаг 0. Baseline

```powershell
cd C:\Users\gameg\Desktop\VS\LinkenVisuals\source
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\gradlew.bat test --console=plain
.\gradlew.bat build --console=plain
.\gradlew.bat test -Pstrange --console=plain
.\gradlew.bat build -Pstrange --console=plain
```

Ожидание: 324 теста, 0 fail/error/skip в каждом flavor. Если baseline красный — сначала чинишь его, потом визуал.

### Шаг 1. Одинаковые условия съёмки

Сравнивать можно только кадры с одинаковыми:

- разрешением клиента (1280×720 и 1920×1080);
- GUI scale (2 для 1280×720, 3 для 1920×1080);
- языком (сначала `en_us`, потом `ru_ru` — отдельными сериями);
- набором ресурспаков (vanilla only);
- состоянием экрана (анимация завершена, курсор в одной и той же точке, тултипов нет);
- фазой анимации (для loading — фиксированный stage/percent, иначе сравнивать только геометрию, не текст).

Практика:

1. В donor `D:\MultiMC\instances\1.21.4\.minecraft\options.txt` сейчас `fullscreen:true`, `guiScale:2`. Для сравнения выставь **те же** `fullscreen`, `overrideWidth`, `overrideHeight`, `guiScale`, что и у порта, иначе кадры не совпадут.
2. У порта — `D:\MultiMC\instances\1.21.111\.minecraft\options.txt`; не забывай вернуть значения после съёмки.
3. Снимай **F2** (PNG). Проверь фактический размер PNG: при оконном режиме клиентская область может быть 1920×1061 из-за рамки. Если размеры отличаются — либо снимай в fullscreen, либо обрезай оба кадра до общего client-rect и фиксируй это в отчёте.
4. Для loading/splash снимай серию кадров (F2 несколько раз подряд) или запись экрана; выбирай кадр с одинаковым progress stage. Не сравнивай разные моменты анимации.
5. Для hover/press — отдельные пары кадров: normal / hover / pressed / focus.

### Шаг 2. Числовой diff

В системе есть Python 3.14, Pillow 11.3, numpy 2.4.6, scipy 1.17.1. ImageMagick нет — используй Pillow/numpy.

Скрипт складывай в `C:\Users\gameg\AppData\Local\Temp\opencode\pixel-audit\` (не в repo). Минимальный набор:

```python
from PIL import Image, ImageChops
import numpy as np

a = Image.open(donor_path).convert("RGB")
b = Image.open(port_path).convert("RGB")
assert a.size == b.size, (a.size, b.size)

na = np.asarray(a).astype(np.int16)
nb = np.asarray(b).astype(np.int16)
d = np.abs(na - nb)

print("mean", d.mean(), "p95", np.percentile(d, 95),
      "pct>8", (d.max(axis=2) > 8).mean() * 100,
      "pct>16", (d.max(axis=2) > 16).mean() * 100,
      "pct>32", (d.max(axis=2) > 32).mean() * 100)

# side-by-side + heatmap + 50% onion
side = Image.new("RGB", (a.width * 3, a.height))
side.paste(a, (0, 0)); side.paste(b, (a.width, 0))
heat = Image.fromarray(np.clip(d.max(axis=2) * 4, 0, 255).astype(np.uint8)).convert("RGB")
side.paste(heat, (a.width * 2, 0))
side.save(out_side_by_side)

Image.blend(a, b, 0.5).save(out_onion)
```

Дополнительно:

- сохраняй **zoom-кропы** 4×/8× по якорям (wordmark, кнопки, rail, footer, tabs, поля);
- считай diff по областям (header / body / footer / конкретная кнопка), а не только по всему кадру;
- если есть `skimage` — добавь SSIM; если нет, не добавляй зависимость, хватит MAE/p95/процентов.

### Шаг 3. Классификация различий

Каждый hotspot относи к одной из категорий:

- **A. Баг порта** — неверный offset/size/цвет/alpha/шрифт/радиус/порядок слоёв. Это чинишь.
- **B. Donor-only** — Lottie, Punchy, донорский шрифт, донорский blur и т.п. Не имитировать; задокументировать.
- **C. Разница версий MC** — метрики ванильных виджетов 1.21.4 vs 1.21.11. Задокументировать, не ломать ванильную логику.
- **D. Шум съёмки** — курсор, тултип, другая фаза анимации, другой кадр параллакса. Переснять.

### Шаг 4. Цикл исправления

Один экран за раз:

1. снял donor + порт;
2. посчитал diff и сохранил evidence;
3. нашёл причину в коде (не «на глаз»);
4. поправил;
5. пересобрал;
6. снял заново теми же условиями;
7. сравнил до/после и записал числа.

Не делай «пакетных» правок нескольких экранов без промежуточных скриншотов — иначе не докажешь, что стало лучше.

### Шаг 5. Итоговый отчёт

Таблица по каждому экрану и разрешению:

| Экран | Разрешение | mean abs diff | p95 | % >16 | Главные остатки | Причина | Статус |
|---|---|---|---|---|---|---|---|

Плюс перед/после для каждого исправления. Если разница осталась — честно укажи, почему (B/C/D), а не прячь.

---

## 5. Чеклист экранов (приоритет сверху вниз)

1. **Самый первый запуск / splash**
   - первый кадр: чем заменяется Mojang splash, есть ли донорский wordmark/логотип, нет ли пустого/чёрного кадра;
   - progress bar, stage text, footer, фон;
   - нет ли мерцания/подмены кадра между splash и loading.

2. **Loading screen**
   - градиент `BG_TOP/MID/BOTTOM`, эллиптический glow, угловые скобки;
   - wordmark-блок `linken.` + `VISUALS` на 44% высоты;
   - rail 460 px на 200 px от низа, толщина/цвет/заливка;
   - stage/percent, stacked footer `Version ..., build ...` + disclaimer;
   - поведение при 1280×720 и 1920×1080.

3. **Главное меню**
   - фон `bg-tower`: кадр/масштаб/параллакс, затемнение;
   - wordmark, menu rows (Singleplayer / Multiplayer / Options / Quit), иконки, hover/press;
   - account chip: иконка/текст/статус, отсутствие overlap;
   - social tiles, media card/footer, отступы и baseline;
   - отсутствие LV-бейджа в текстовом wordmark (это принятое решение D117).

4. **Pause**
   - панель 440 px, topbar 56, rows 44, gap 8, 2-column tiles, divider, Save & Quit;
   - title `PAUSED`/`ПАУЗА`, world/server line;
   - hover/open/close, ESC;
   - отсутствие tile `LINKENVISUALS HUD`.

5. **Create World**
   - backdrop, glass-карточка 1024 design px, header, footer;
   - tab-pills Game/World/More и их хитбоксы (реальный клик);
   - поля, cycling-строки, чекбоксы, кнопки, slider, WORLD SUMMARY;
   - mint-рамка поля имени, folder hint;
   - normal/hover/focus/disabled;
   - помни: donor-экрана нет, сравнивай только стиль.

6. **LinkGUI**
   - header: LV 24 px (теперь 120 px текстура), wordmark, search;
   - карточки 3×102, dock, profile/theme, footer LV;
   - hover/press, анимации (не сравнивай разные фазы);
   - отсутствие HUD за экраном.

7. **HUD watermark / KeybindPanel**
   - LV 10 px (120 px текстура), читаемость, отсутствие «лесенок»;
   - Strange-сборка не показывает LV.

Для каждого экрана — минимум 1280×720 и 1920×1080. Для loading — ещё и ранний/средний/поздний stage.

---

## 6. Команды

### Сборка и тесты

```powershell
cd C:\Users\gameg\Desktop\VS\LinkenVisuals\source
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'

.\gradlew.bat test --console=plain
.\gradlew.bat build --console=plain
.\gradlew.bat test -Pstrange --console=plain
.\gradlew.bat build -Pstrange --console=plain
```

`build` включает `test`; отдельный `test` нужен, если хочешь быстро проверить без jar.

### Autotest

Перед прогоном в `source/run/options.txt` поставь:

```text
pauseOnLostFocus:false
```

Иначе окно уходит в паузу и прогон ломается/зависает. После прогона верни `true`.

```powershell
.\gradlew.bat runAutotest -PautotestWorld=AutoTestWorld -PautotestQuit=true -PautotestWidth=1920 -PautotestHeight=1080 --console=plain
```

Ожидание: `steps=1158 ok=1158 fail=0 skip=0`, `New ERROR/exception lines in latest.log: 0`.

Strange (в temp-каталог, чтобы не заменить развёрнутый Linken-jar):

```powershell
.\gradlew.bat clean build -Pstrange -PmultiMcModsDir="C:\Users\gameg\AppData\Local\Temp\opencode\mods-deploy" --console=plain
```

После Strange обязательно пересобери и разверни Linken обратно:

```powershell
.\gradlew.bat build --console=plain
```

### Запуск donor и порта

```powershell
& "D:\MultiMC\MultiMC.exe" --launch 1.21.4
& "D:\MultiMC\MultiMC.exe" --launch 1.21.111
```

Скриншоты: F2. У порта — `source/run/screenshots/`; у donor — `D:\MultiMC\instances\1.21.4\.minecraft\screenshots\`.

### Хеши jar

```powershell
Get-ChildItem build\libs\linkenvisuals-*.jar |
  ForEach-Object { "$($_.Name) | $($_.Length) | $((Get-FileHash $_.FullName -Algorithm SHA256).Hash)" }
```

---

## 7. Критерии готовности

- Оба flavor собираются, **324 теста**, 0 fail/error/skip.
- Linken autotest **1158/1158**, Strange **1148/1148**, 0 fail/skip; новых mod/shader/sound/mixin ERROR нет (только offline-auth/Realms).
- Для каждого затронутого экрана есть **до/после** скриншоты и числовой diff; остатки классифицированы (A/B/C/D).
- Нет новых фич, контролов, настроек; версия `1.0.140`.
- Обновлены `docs/HANDOFF.md` (верхний раздел), `docs/MERGE_DECISIONS.md`, `docs/PORT_STATUS.md`, `docs/FEATURE_PARITY.md`, `docs/VISUAL_AUDIT.md`; `docs/NEXT_CHAT_PROMPT.md` заменён промтом для следующего чата.
- Отдельный code-коммит и отдельный docs-коммит; `git fetch` перед коммитом; push в `main` без force.
- Финальный отчёт пользователю — по-русски, с таблицей diff, ссылками на evidence и списком ручных проверок.

---

## 8. Известные ловушки

- `pauseOnLostFocus` — см. выше; после autotest вернуть `true`.
- Полный `run/options.txt` с `key_key.*` может словить `OptionsKeyLwjgl3Fix` и сбросить focus pause. Для autotest используй минимальный набор, после — восстанови оригинал.
- `LinkenMenuChrome.drawLogo`: `size <= 128` → `logo.png` 120 px (UV 120), иначе `lv_logo.png` 512 px (UV 512). Не возвращай 512-only: это даёт ~21× minification в LinkGUI header/footer.
- `rgba(int rgb, int alpha)` — alpha 0..255; `rgba(int rgb, float fade)` — fade 0..1. Int-литерал без перегрузки трактовался как fade.
- `I18n` fallback использует `Language.getInstance().get(key)`; `I18n.translate(key)` без аргументов ломает `%s`-ключи (`Format error`).
- `alignTabs` переставляет ванильные `TabButtonWidget`; клики идут через те же кнопки, не подменяй хитбоксы.
- HUD скрыт за экранами (кроме ChatScreen/edit mode) — не «чини» это обратно.
- Donor 1.21.4 и порт 1.21.11 — разные версии; не сравнивай байты кадра там, где отличается ванильная геометрия.
- F2 при оконном режиме может дать 1920×1061; фиксируй фактический размер и обрезай одинаково.
- Не сравнивай разные фазы анимаций (параллакс, glow, progress) — сначала стабилизируй кадр.
- ImageMagick нет; используй Pillow/numpy/scipy.
- Не коммить `run/`, `build/`, jar, логи, скриншоты, temp-файлы.

---

## 9. Первый шаг

1. Прочитай `docs/HANDOFF.md` (верхний раздел D117b), `docs/VISUAL_AUDIT.md`, `docs/MERGE_DECISIONS.md` (D117/D117b), `docs/FEATURE_PARITY.md`.
2. Прогони baseline-тесты в обоих flavors.
3. Запусти donor 1.21.4 и порт 1.21.111 при **одинаковых** `fullscreen`/`guiScale`/разрешении.
4. Сними главное меню на 1920×1080, построй side-by-side + diff + heatmap, запиши числа.
5. Дальше иди по чеклисту: splash → loading → main menu → pause → Create World → LinkGUI → HUD.
6. Исправляй только то, что классифицировано как **A. Баг порта**. Всё остальное — в отчёт с причиной.

Главное: не добавляй ничего нового. Только доводи уже существующий визуал и доказывай каждое изменение.
