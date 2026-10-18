# LinkenVisuals 🌌

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-brightgreen.svg?style=flat-square)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Loader%200.19.1+-blue.svg?style=flat-square)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg?style=flat-square)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg?style=flat-square)](LICENSE)

**LinkenVisuals** is a visual enhancement client mod for Minecraft Fabric 1.21.11. Engineered with Mojang's modern Blaze3D rendering backend and high-performance GLSL shaders, LinkenVisuals delivers custom post-processing, screen-space effects, first-person hand customizations, and an in-game media player with synchronized lyrics.

[🇷🇺 Читать на русском (README_RU.md)](README_RU.md)

---

## ✨ Features

### 🎨 Advanced Post-Processing Pipelines
- **Ambience & Bloom**: Multi-stage downsampled bloom pyramid (1/2, 1/4, 1/8 resolution passes), customizable exposure, contrast, saturation, and cinematic vignette.
- **Procedural Skybox**: Custom 36-vertex inverted sky cube rendering with celestial time tracking, star density, and dual-color gradient accents.
- **Screen-Space Rain**: Physical refraction and distortion shaders responding to weather intensity.
- **Hit Bubbles & Kill Effects**: Screen-space radial distortion and pulse shaders on combat impacts.

### ✋ First-Person Hand Customization & VFX
- **Hand Outlines**: Depth-buffer delta extraction creating crisp first-person silhouettes (`Line`, `Glow`, `Aura`, `Energy`).
- **Motion & Smoke Trails**: Temporal frame retention for fluid smoke and speed trails trailing your weapon or hands.
- **Hand Editor**: Interactive offsets, rotation, and scaling for mainhand and offhand items.

### 🎵 In-Game Media Player & Live Lyrics
- **Windows Media Integration**: Seamlessly communicates with the Windows Media Control system (WinRT) to detect active playback from Spotify, Apple Music, YouTube Music, Deezer, Tidal, Foobar2000, and browsers.
- **Synchronized LRC Lyrics**: Real-time lyric synchronization powered by the lrclib API, showing upcoming lines directly on your HUD.
- **Album Artwork**: Asynchronous cover art lookup via iTunes Search API with procedural gradient fallback generation.

### 🖥️ 100% Custom UI Suite, Menu Editor & Presets
- **Interactive Main Menu Editor**: Full in-game visual editor (accessible via Settings -> Style tab). Drag and drop buttons, scale elements with mouse wheel or resize handles, customize background photos (PNG/JPG), and toggle custom GLSL shader overlays.
- **3 Visual Presets**: Easily apply presets **`Beta`** (flagship dark-slate frosted glass), **`Modern`** (centered translucent glass dashboard), and **`Classic`** (sleek top navigation bar with compact vertical rows).
- **Custom Background Photos & Shader Overlays**: Drop your wallpaper into `.minecraft/linkenvisuals/background.png` or pick a custom image path, with live dimming and shaders (Bloom Pulse, Starfield particles, Radial waves, Film grain).
- **Custom Main Menu**: Fully styled interactive dashboard with user skin rendering, spring-physics hover animations, and direct Mod Menu integration.
- **Custom Multiplayer Browser**: Asynchronous server list with live pinging, latency indicators, MOTD rendering, smooth scrolling, and inline modals for Direct Connect and Add/Edit Server.
- **Unified Settings Hub**: Clean tabbed interface covering **Style** (presets & menu editor launcher), **Video** (Render distance, FPS, FOV, VSync), **Audio** (Master & channels), **Modules** (quick visual toggles & ClickGUI launcher), and **System** (resource packs, keybinds, vanilla settings bridge, language switcher).
- **Custom Loading Screens**: Replaced vanilla dirt background with live GLSL `BackgroundPipeline`, pulsing brand mark, and style-adaptive progress indicators for server connection, task progress, and world loading.

### 🌐 100% Multi-Language Localization
- **Automatic Game Language Detection**: Adapts dynamically to Minecraft's language.
- **Native Supported Languages**: Complete translations for **English** (`en_us`), **Russian** (`ru_ru`), and **Ukrainian** (`uk_ua`).
- **MSDF Font Engine Fallbacks**: Seamless Cyrillic and Ukrainian letter support in the vector glyph renderer.

### 🎛️ Modern Fluent ClickGUI & Presets
- **Intuitive Categories**: `Render`, `Visuals`, `HUD`, `Player`, and `Config`.
- **Spring-Physics Animations**: Fluid, responsive ease animations across menus and container dialogs.
- **Color Pickers & Sliders**: Fine-grained color palettes (HSV/RGB) and numeric sliders.
- **Full JSON Persistence**: Module states, keybindings, menu style, language, and layout are automatically saved to `config/`.
- **Mod Menu Integration**: Seamless button integrated into both the custom title screen and system settings.

---

## ⌨️ Controls & Keybindings

| Action | Default Key | Configurable |
| :--- | :---: | :---: |
| **Open / Close ClickGUI** | `Right Shift` | Yes (Minecraft Controls) |
| **Bind Module Key** | Click module in GUI & press key | Yes |
| **Clear Module Key** | Right-click binding button | Yes |

---

## 📂 Project Structure

```text
LinkenVisuals/
├── source/
│   ├── src/main/java/dev/linkenvisuals/
│   │   ├── LinkenVisuals.java       # Mod initializer & lifecycle events
│   │   ├── account/                  # Account switcher & session cache
│   │   ├── clickgui/                 # ClickGUI engine, widgets & ConfigManager
│   │   │   └── setting/              # Boolean, Slider, Mode, and Color settings
│   │   ├── cosmetic/                 # Custom cosmetic items (Wings, Halos, Bandanas)
│   │   ├── hands/                    # HandEditor, HandPose, HandOutline pipelines
│   │   ├── hud/                      # HUD elements, potion tracker, notifications
│   │   ├── media/                    # MediaWatcher, Lyrics (LRC), MediaArt
│   │   ├── mixin/                    # Clean Fabric bytecode mixins
│   │   ├── player/                   # Target marks, kill effects, fake player
│   │   ├── render/                   # Blaze3D pipelines, UBO buffers, GLSL shaders
│   │   └── ui/                       # Main menu, custom buttons, spring animations
│   └── src/main/resources/
│       ├── assets/linkenvisuals/
│       │   ├── shaders/core/         # Vertex & Fragment GLSL shaders
│       │   └── textures/             # Fonts, icons, and UI assets
│       ├── fabric.mod.json           # Mod metadata
│       └── linkenvisuals.mixins.json# Mixin configuration
├── build.gradle                      # Loom build script
├── settings.gradle                   # Gradle settings
└── LICENSE                           # MIT License
```

---

## 🛠️ Building from Source

### Prerequisites
- [Java Development Kit (JDK) 21](https://adoptium.net/) or higher.
- Git.

### Build Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/LinkenVisuals.git
   cd LinkenVisuals/source
   ```

2. **Compile and assemble the mod JAR:**
   ```bash
   # On Windows:
   .\gradlew build

   # On Linux / macOS:
   ./gradlew build
   ```

3. **Locate the compiled JAR:**
   The output artifact will be placed in:
   ```text
   source/build/libs/LinkenVisuals-1.0.0.jar
   ```

4. **Launch the Minecraft client for testing:**
   ```bash
   .\gradlew runClient
   ```

---

## ⚙️ Configuration File

Configuration is saved in standard JSON format in your Minecraft profile directory:
`.minecraft/config/linkenvisuals_config.json`

Example structure:
```json
{
  "modules": {
    "Ambience": {
      "enabled": true,
      "keyCode": -1,
      "settings": {
        "Exposure": 1.15,
        "Contrast": 1.05,
        "Saturation": 1.20,
        "Vignette": 0.35,
        "Fog": true
      }
    }
  }
}
```

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for complete details.
