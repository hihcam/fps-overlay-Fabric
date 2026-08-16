## v5.6
* Fixed ping showing 1ms on Hypixel SkyBlock, BedWars, and other game modes that use custom tab-list handling which leaves `PlayerInfo#getLatency()` permanently stuck at 1. The mod now detects this condition after 5 seconds and automatically switches to an active RTT measurement path: it sends a `ServerboundPingRequestPacket` every 2 seconds and reads the round-trip time directly from Minecraft's own debug overlay ping logger. Normal servers are unaffected. The fallback resets immediately on disconnect or server switch.

## v5.3.1
* Fixed AVG FPS and 1% Low being capped by HUD framerate-limiting mods (e.g. Gnetum). Frame timing is now sampled from the core render loop instead of the HUD render callback, so it reflects true framerate regardless of HUD throttling settings from other mods.

## v5.3
* Fixed TPS and MSPT showing "N/A" on dedicated servers.
* OP players receive exact tick data via the vanilla debug sample stream.
* Non-OP players receive an estimated TPS based on server time packets.
* Support for 26.2.

## v5.2
* New: "Show Metric Labels" toggle in General HUD settings. Turn it off and your HUD shows only the numbers and units (like `100fps | 10ms`), with no text clutter.
* Fixed the FPS drop caused by the graph. It was drawing each bar one pixel at a time, hammering the GPU. It now draws each bar in a single operation, cutting GPU work by up to 50x.
* Graph overhead when hidden is now zero. Previously the game still calculated graph data even when the graph wasn't visible.
* A couple of smaller internal cleanups to reduce unnecessary memory usage during startup.

## v5.1

This release focuses on stability, compliance, and accuracy.
No new features — just making sure everything that's there works perfectly.

### Changes
- Fixed a thread safety issue in the performance tracking engine that could
  cause corrupted FPS readings under heavy load
- Fixed a crash that could occur when reordering metrics while the overlay
  was actively rendering
- Multiplayer MSPT and TPS now correctly show N/A instead of fake static values
- Removed CPU and GPU metrics — the only reliable way to read these required
  spawning external system processes

### What's in the mod (carried over from v5.0)
Thirteen trackable metrics: FPS, Average FPS, Frame Time, 1% Low, Ping,
TPS, MSPT, Memory, Coordinates, Biome, Chunks, FPS Graph, Min/Max Stats.

Full customization: drag to reposition, reorder metrics, rename labels,
theme presets (Classic Dark, Light, Glass), custom color palette, adaptive
colors, HUD scale, 10 keybinds, auto-hide on F3.