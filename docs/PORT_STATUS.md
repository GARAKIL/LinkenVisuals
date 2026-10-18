# PORT_STATUS — статус переноса из донора (LinkenVisuals 2.0 / MC 1.21.4 → LinkenVisuals 1.21.11)

Донор: `C:\Users\gameg\Desktop\визуалы` (READ-ONLY, код `ru.method.linkenvisuals`, лицензия ARR). Перенос — только после подтверждения скоупа (Фаза 1). Донорский легаси-стек (`ShaderProgram`, `setShader`, GL11/GL20, JSON-шейдеры) не переносится никогда.

Обозначения: ⛔ не начато · 🔄 в работе · ✅ перенесено и проверено.

| # | Объект донора | Статус | Комментарий |
|---|---|---|---|
| 5.1 | MSDF: `api/render/msdf/MsdfFont,GlyphAtlas,FontData` | ⛔ | сверить с нашими `MsdfText`/`GlyphAtlas`; атласы — по нужде |
| 5.2 | BlockOverlay + `shaders/block_overlay_*` (10 эффектов) | ⛔ | GLSL как текст → `RenderPipeline.builder()` |
| 5.3 | Liquid glass: `LiquidGlassNewBuilder`, `BuiltLiquidGlassNew`, capture/reflection | ⛔ | сначала сверка с `GlassPanelPipeline`/`HudGlassCapture`/`ScreenCapture` |
| 5.4 | MotionBlur: `MotionBlurRenderer`, `shaders/motionblur.*` | ⛔ | без `glUseProgram(0)` |
| 5.5 | Оптимизации-идеи: `Render2D.drawDotGrid/drawHatch`, `Render3D` LRU VoxelShape | ⛔ | применять точечно, где у нас аналогичные циклы |
| 5.6a | Косметика-инфраструктура (bedrock/gecko, FeatureRenderer, tools-скрипты) | ⛔ | spike, затем решение по списку |
| 5.6b | Wave-плащ (`CapeSimulation`) | ⛔ | без сетевого синка |
| 5.6c | Питомцы (`RemotePets`/`PetWanderController`/Rudy) | ⛔ | проверить зависимость от их сети — вероятно адаптировать |
| 5.6d | ChinaHat, Nimb, Naruto, CosmeticEffects, Spheres | ⛔ | каждая отдельным подтверждённым пунктом |
| 5.7 | Модули: WayPoints/GPS, ItemPhysics, ItemScroller, ChatHelper, MineHelper, FastExp, ElytraSwap, HitSound, HandSwap, SwingAnimation, Trails, JumpCircles, Crosshair, HitColor, TargetEsp, Zoom, Freelook, Fullbright, TimeChanger, TotemCounter/Tracker, UseTracker, StreamerMode | ⛔ | пачками только с подтверждением скоупа |
| 5.8 | ViewModel-паритет (`Hands`, `ViewModel`) | ⛔ | референс параметров → `HandEditor`/`HandPose` |
| — | Идеи compat: soft-dep рефлексия, Iris-детект, Pojav/gl4es | ⛔ | паттерны, не код |
| — | Антипаттерны донора | 🚫 | 184 проглоченных catch, сырые GL-вызовы, god-классы, жёсткие IP — не переносим |

Священные фичи (§7 мастер-промта) — не регрессировать: KillEffects (UBO 224 B) · HitEffects + HitBubbleRenderer (416 B) · Filter (64 B) · хендсы (HandOutline 96 B, hand_trail 32 B) · Rain (192 B) · процедурные косметики · ClickGUI (modern + classic) · i18n en/ru/uk. На каждую — тест или проверка.
