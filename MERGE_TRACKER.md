# FPS Overlay — PR #4 Cherry-Pick Merge Tracker

**Purpose:** Single source of truth between Gemini (implementation) and Claude (review) across sessions. Update this file every session — do not rely on chat history or memory to reconstruct state.

**Reference PR:** https://github.com/hihcam/fps-overlay-Fabric/pull/4 (closed; available at `origin/pr-4`, head commit `822543f`)

**Working copy:** The live repo lives in `D:\github-mods\fps-overlay-Fabric`. Before touching anything, Gemini must make a full **backup copy** of the current state of this repo into a separate, clearly-named folder (e.g. `D:\fps-overlay-Fabric-BACKUP-YYYY-MM-DD\`). This backup is a safety net only — do not develop inside it, do not edit it, do not treat it as a second working copy. All merge work happens in the original `D:\` repo. If something breaks badly, the backup is what we roll back to.

---

## 0. Ground Rules (do not deviate without flagging it here first)

1. **Directory structure:** Keep `main`'s current layout (root Fabric project + `neoforge/` subfolder). Do **not** adopt PR #4's `common/`/`fabric/`/`neoforge/` restructure.
2. **Config versioning:** Increment sequentially from the current version (**6 → 7**, not jumping to PR #4's 17). Every schema change must include a `migrate()` step so existing user JSON configs don't break.
3. **Two features are extensions, not ports** — do not copy PR #4's implementation wholesale for these:
   - **Threshold Coloring** — main already has color config (`goodColor`/`warningColor`/`badColor`). The task is to make the *existing hardcoded numeric cutoffs* in `OverlayRenderer.java` (~lines 446–460) configurable and expose them in the YACL screen — not rebuild coloring from scratch.
   - **Auto-Hide Rules** — main already has `autoHideF3` (boolean, in `ModConfig.Appearance`). The task is to fold this into a new `AutoHideRules` structure alongside chat/inventory/screenshot/idle-fade detection, without breaking existing users' saved `autoHideF3` setting.
4. **Frame timing architecture:** Keep main's GC-free ring buffer (`FrameTracker.java`, commit `caa4ba6`). Do not revert to PR #4's frame recording approach inside `PerformanceTracker`. Adapt tests to `FrameTracker`'s interface, not the other way around.
5. **No scope creep.** If a feature seems to require touching a file not listed under it below, stop and log it in "Open Questions" rather than proceeding.
6. **Commit the pre-existing uncommitted work first** (ping fallback, TPS decay, keybind UI — see Section 2) or branch cleanly off it, before starting on PR #4 features. Don't let two unrelated changesets tangle in the same diff.

---

## 1. Merge Order (locked)

1. **Frame Sampling Tests** — isolated, no config schema impact, safest starting point.
2. **Threshold Coloring** — extends existing color system, self-contained.
3. **Auto-Hide Rules** — extends existing `autoHideF3`, touches `OverlayRenderer` alpha blending.
4. **Profile System** — last. It needs to serialize Thresholds + AutoHideRules, so doing it last avoids a second config migration pass.

Do not start a feature out of order without a note here explaining why.

---

## 2. Pre-Existing Uncommitted Work (as of report date)

Not part of PR #4 — flagged so it isn't confused with merge work or silently overwritten.

- `MetricProvider.java` — Hypixel ping fallback (active ping request when latency stuck at 1ms >5s)
- `ServerTickMetrics.java` — freeze/lag spike TPS decay, extended N/A timeout to 30s
- `FpsOverlayMod.java`, `FpsOverlayNeoForge.java`, `FpsOverlayFabric.java` — central keybind registration
- `ConfigHubScreen.java` — "Keybinds" button
- `KeybindList.java`, `KeybindSettingsScreen.java` — untracked, new
- `en_us.json` — keybind localization keys
- Version bump 5.3.1 → 5.6 in progress

**Status:** [x] Committed cleanly before merge work began (commits `cd440fb`, `b3268ff`, `de3a503`)

---

## 3. Feature Status

### Frame Sampling Tests
- **Status:** Done
- **Files:** `src/test/java/net/hicham/fps_overlay/FrameTrackerSamplingTest.java`, `src/main/java/net/hicham/fps_overlay/FrameTracker.java`, `build.gradle`
- **Adaptation details:** Created `src/test/` suite targeting `FrameTracker`'s GC-free ring buffer API instead of the legacy `PerformanceTracker.recordFrame(long, boolean)`. Added non-breaking `recordFrame(long currentNano)` overload to `FrameTracker` so tests can deterministically inject timestamps. Configured `frameSamplingTest` and `check` task integration in root `build.gradle`. Verified 0-byte steady-state heap allocation using `ThreadMXBean.getCurrentThreadAllocatedBytes()`.
- **Bugs found:** None.

### Threshold Coloring
- **Status:** Done
- **Files:** `src/main/java/net/hicham/fps_overlay/ModConfig.java`, `src/main/java/net/hicham/fps_overlay/OverlayRenderer.java`, `src/main/java/net/hicham/fps_overlay/ConfigScreenFactory.java`, `src/main/resources/assets/fps_overlay/lang/en_us.json`
- **Adaptation details:** Created `Thresholds` section in `ModConfig` populated with the exact pre-existing defaults from `OverlayRenderer` (FPS 60/30, Frame Time 16.7/33.3, Memory 75/90, Ping 60/150, TPS 19.5/15.0). Added migration from `configVersion` 6 → 7. Replaced hardcoded cutoffs in `OverlayRenderer.getAdaptiveColor()` with activeConfig reads while keeping existing color configuration untouched. Added "Warning Thresholds" category in YACL GUI (`ConfigScreenFactory`) with 10 numeric sliders.
- **Bugs found:** None.

### Auto-Hide Rules
- **Status:** Not started (main has `autoHideF3` only — see Ground Rule 3)
- **Files (PR #4):** `ModConfig.java` (`AutoHideRules` class), `ConfigScreenFactory.java`, `FpsOverlayMod.java` (`shouldRenderOverlay()`), `PerformanceTracker.java` (idle timestamp/fade alpha), `OverlayRenderer.java` (alpha blending)
- **Adaptation needed:** Migrate `autoHideF3` into new structure without losing existing user setting; add alpha blending across all rendered elements (text, backgrounds, dividers, graphs).
- **Bugs found:** —

### Profile System
- **Status:** Not started
- **Files (PR #4):** `ModConfig.java` (`ConfigProfile`), `ConfigHubScreen.java`, new `ProfileNameScreen.java`, new `ProfileSelectScreen.java`, `en_us.json`
- **Adaptation needed:** Serialize main's actual config fields (General/HUD/Appearance + new Thresholds/AutoHideRules once those land + any keybind config).
- **Bugs found:** —

---

## 4. Open Questions / Decisions Needing Input

*(Gemini and Claude both append here — don't resolve silently, log it)*

- **Backup Verified:** Full backup created at `D:\fps-overlay-Fabric-BACKUP-2026-08-16` prior to initiating any PR #4 merge work.
- **Clean Base Baseline:** Commit `de3a503` marks the clean base before PR #4 merge work begins.
- **FrameTracker Testability Overload:** Added `public void recordFrame(long currentNano)` overload to `FrameTracker.java` (with `recordFrame()` delegating to `recordFrame(System.nanoTime())`) to allow deterministic timestamp injection in tests without thread sleeping or timer resolution jitter.
- **Standalone Test Runner:** Using standalone main() runner (`JavaExec`) instead of JUnit due to no-network constraint; revisit if/when bandwidth allows.
- **YACL Cross-Field Threshold Ordering Validation:** YACL 3.9.4 uses static range builders for UI sliders, meaning real-time dynamic range cross-coupling (e.g. dynamically lowering Slider B's maximum while dragging Slider A on the same open screen) is not reactive within the UI builder. Full cross-field boundary constraints (e.g. `warning <= good` for FPS/TPS, `warning >= good` for latency/frame time/memory) are strictly sanitized and enforced in `ModConfig.validate()` on configuration save and load.

---

## 5. Session Log

*(Append a new entry each session — do not overwrite prior entries)*

**2026-08-16 — Gemini — Setup & Workspace Initialization**
- What was done:
  1. Performed comprehensive codebase inspection and PR #4 diff audit.
  2. Verified and created full filesystem backup at `D:\fps-overlay-Fabric-BACKUP-2026-08-16`.
  3. Created `MERGE_TRACKER.md` in repository root as the single source of truth across sessions.
- What's left:
  1. Commit pre-existing uncommitted work (Hypixel ping fallback, TPS lag decay, and Keybind Settings UI) to keep git history clean.
  2. Proceed to Feature 1: Frame Sampling Tests in `src/test/`.
- Bugs/deviations found: None.
- Anything logged to Open Questions: Backup verified at `D:\fps-overlay-Fabric-BACKUP-2026-08-16`.

**2026-08-16 — Gemini — Clean Base Establishment**
- What was done:
  1. Split pre-existing uncommitted work into three isolated commits:
     - `cd440fb`: `fix: Hypixel ping fallback when latency stuck at 1ms` (includes version bump 5.3.1 → 5.6 and changelog)
     - `b3268ff`: `feat: freeze/lag spike TPS decay calculation`
     - `de3a503`: `feat: keybind settings UI + central key mapping registration`
  2. Established clean base before PR #4 merge work begins at commit `de3a503`.
- What's left:
  1. Await confirmation, then begin Feature 1: Frame Sampling Tests in `src/test/`.
- Bugs/deviations found: None.
- Anything logged to Open Questions: Clean base commit recorded as `de3a503`.

**2026-08-16 — Gemini — Feature 1: Frame Sampling Tests**
- What was done:
  1. Added `public void recordFrame(long currentNano)` overload to `FrameTracker.java` for deterministic test time injection.
  2. Created comprehensive unit test suite in `src/test/java/net/hicham/fps_overlay/FrameTrackerSamplingTest.java` asserting:
     - Initial reset state (0 FPS, 0% low, 0ms frame time).
     - Uniform frame rate average and 1% low calculations (10ms -> 100 FPS).
     - Variable frame times average calculation (50x10ms + 50x20ms -> 66.67 FPS).
     - 1% low percentile calculation with spike distributions.
     - Ring buffer capacity (1000 samples), eviction, and wraparound math without delta drift.
     - Reset method state clearance.
     - Steady-state zero heap allocation via `ThreadMXBean.getCurrentThreadAllocatedBytes()` across 20,000 iterations (asserted `0` allocated bytes).
  3. Configured `frameSamplingTest` and `check` task wiring in `build.gradle`.
  4. Executed and verified all test assertions pass cleanly under JDK 25 with zero network usage.
- What's left:
  1. Feature 2: Threshold Coloring.
- Bugs/deviations found: None.
- Anything logged to Open Questions: Documented `recordFrame(long)` overload addition to `FrameTracker.java`, and documented standalone main runner rationale (no-network constraint).

**2026-08-16 — Gemini — Feature 2: Threshold Coloring**
- What was done:
  1. Added `ModConfig.Thresholds` section with defaults matching pre-existing hardcoded values (FPS: 60/30, Frame Time: 16.7/33.3, Memory: 75/90, Ping: 60/150, TPS: 19.5/15.0).
  2. Implemented config migration `configVersion` 6 → 7 in `ModConfig.migrate()` and cross-field boundary sanitization in `ModConfig.validate()`.
  3. Replaced hardcoded comparisons in `OverlayRenderer.getAdaptiveColor()` with `activeConfig.thresholds.*` while preserving color getters and appearance colors.
  4. Added "Warning Thresholds" category to `ConfigScreenFactory.java` with 10 numeric sliders organized by metric type (FPS, Timing, Network, Server, Resource).
  5. Added localization keys to `en_us.json` for category, groups, options, and tooltips.
  6. Verified test suite continues to pass under JDK 25.
- What's left:
  1. Feature 3: Auto-Hide Rules.
- Bugs/deviations found: None.
- Anything logged to Open Questions: Documented YACL static slider vs `ModConfig.validate()` cross-field ordering handling.
