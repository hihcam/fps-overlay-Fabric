<div align="center">
  <img src="common/src/main/resources/assets/icon.png" width="128" alt="FPS Overlay Logo">
  <h1>FPS Overlay</h1>
  <p><strong>A clean, highly customizable performance overlay for Minecraft.</strong></p>
  
  [![Modrinth Downloads](https://img.shields.io/modrinth/dt/fps-overlay?style=flat-square&logo=modrinth&color=00AF5C)](https://modrinth.com/project/j0IQ4hjv)
  [![CurseForge Downloads](https://img.shields.io/curseforge/dt/1341801?style=flat-square&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/fpsoverlay)
  [![License](https://img.shields.io/github/license/hichamdev/fps-overlay-Fabric?style=flat-square&color=blue)](LICENSE)
</div>

<br>

FPS Overlay gives you exactly the performance metrics you need without cluttering your screen. It replaces the overwhelming F3 debug menu with a beautiful, lightweight heads-up display.

It is a **client-side only** mod built for both Fabric and NeoForge.

---

## Requirements

| Dependency | Loader | Required? |
|---|---|---|
| [YetAnotherConfigLib](https://modrinth.com/mod/yacl) | Fabric & NeoForge | ✅ Required |
| [Fabric API](https://modrinth.com/mod/fabric-api) | Fabric only | ✅ Required |
| [Mod Menu](https://modrinth.com/mod/modmenu) | Fabric only | ⬜ Optional |

---

## Features

* **Thirteen Trackable Metrics:** Display exactly what you care about.
  * FPS, Average FPS, 1% Low, Frame Time, Session Min/Max
  * Server Ping, TPS, MSPT
  * RAM Usage, Chunk Updates
  * Player Coordinates, Biome
  * Rolling FPS Graph
* **Beautiful Preset Themes:** Choose from Classic Dark, Light, or translucent Glass styles.
* **Fully Custom Layouts:** Use a compact horizontal navbar or a traditional vertical stack.
* **Adaptive Colors:** Metrics automatically change color based on game performance (e.g., green for 60+ FPS, red for < 30).
* **Drag-and-Drop Editor:** Press `F6` to freely reposition the overlay anywhere on your screen.
* **Smart Hiding:** Automatically conceals itself when the vanilla F3 menu is open.
* **Performance First:** Written to have zero noticeable impact on game rendering.

## Downloads

Grab the latest release for your preferred mod loader:

* [Modrinth (Recommended)](https://modrinth.com/project/j0IQ4hjv)
* [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fpsoverlay)

## Configuration

Press `P` while in-game (by default) to open the configuration hub, powered by YetAnotherConfigLib.

From here you can:
* Enable or disable individual stats.
* Reorder metrics using a drag-and-drop menu.
* Build custom theme color palettes.
* Adjust UI scale and opacity.
* Rebind any of the 10 available shortcut keys.

### Default Keybinds

| Key | Action |
|---|---|
| `O` | Toggle Overlay |
| `P` | Open Configuration Hub |
| `F4` | Reset Session Stats (Avg FPS, Min/Max) |
| `F5` | Toggle FPS Graph |
| `F6` | Open Position Editor |
| `F7` | Toggle Coordinates |
| `F8` | Toggle FPS |
| `F9` | Toggle Frame Time |
| `F10` | Toggle Memory |
| `F11` | Toggle Ping |

All keybinds can be rebound in the standard Minecraft controls menu or disabled entirely in the config.

## Building from Source

This project uses Architectury to build for both Fabric and NeoForge simultaneously.

1. Clone the repository: `git clone https://github.com/hichamdev/fps-overlay-Fabric.git`
2. Open a terminal in the project directory.
3. Run `./gradlew build` (or `gradlew.bat build` on Windows).
4. The compiled `.jar` files will be in `fabric/build/libs/` and `neoforge/build/libs/`.

> **Note:** Java 21 is required to build this project.

## License

This mod is available under the [MIT License](LICENSE).