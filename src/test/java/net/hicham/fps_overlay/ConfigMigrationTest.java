package net.hicham.fps_overlay;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class ConfigMigrationTest {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        System.out.println("Starting ConfigMigrationTest...");

        testLegacyV6ToV9Migration();
        testDeepCopyIsolation();
        testProfileLifecycleAndCollisions();
        testRelationalBoundaryClamping();

        System.out.println("ALL ConfigMigrationTest assertions passed successfully!");
    }

    private static void testLegacyV6ToV9Migration() {
        String legacyV6Json = """
        {
          "general": {
            "enabled": true,
            "enableKeybindings": true,
            "updateIntervalMs": 150,
            "configVersion": 6
          },
          "hud": {
            "showFps": true,
            "showAverageFps": false,
            "showFrameTime": false,
            "show1PercentLow": true,
            "showMemory": true,
            "showPing": true,
            "showMspt": false,
            "showTps": true,
            "metricOrder": ["fps", "memory", "ping"]
          },
          "appearance": {
            "autoHideF3": false,
            "backgroundOpacity": 180,
            "backgroundColor": -16777216,
            "goodColor": -16711936,
            "warningColor": -256,
            "badColor": -65536,
            "themePreset": "CLASSIC_DARK"
          }
        }
        """;

        ModConfig config = GSON.fromJson(legacyV6Json, ModConfig.class);
        if (config == null) {
            throw new AssertionError("Failed to parse legacy v6 JSON");
        }

        // Run sequential migration & validation
        try {
            java.lang.reflect.Method migrateMethod = ModConfig.class.getDeclaredMethod("migrate");
            migrateMethod.setAccessible(true);
            migrateMethod.invoke(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke migrate()", e);
        }
        config.validate();

        // 1. Assert version bumped to 9
        assertEquals(9, config.general.configVersion, "configVersion should be 9 after migration");
        assertEquals(150, config.general.updateIntervalMs, "updateIntervalMs should be preserved");

        // 2. Assert Thresholds initialized with exact defaults
        assertNotNull(config.thresholds, "thresholds should be non-null after migration");
        assertEquals(60, config.thresholds.fpsGood, "thresholds.fpsGood default");
        assertEquals(30, config.thresholds.fpsWarning, "thresholds.fpsWarning default");
        assertEquals(16.7, config.thresholds.frameTimeGood, 0.001, "thresholds.frameTimeGood default");
        assertEquals(33.3, config.thresholds.frameTimeWarning, 0.001, "thresholds.frameTimeWarning default");
        assertEquals(75, config.thresholds.memoryGood, "thresholds.memoryGood default");
        assertEquals(90, config.thresholds.memoryWarning, "thresholds.memoryWarning default");
        assertEquals(60, config.thresholds.pingGood, "thresholds.pingGood default");
        assertEquals(150, config.thresholds.pingWarning, "thresholds.pingWarning default");
        assertEquals(19.5, config.thresholds.tpsGood, 0.001, "thresholds.tpsGood default");
        assertEquals(15.0, config.thresholds.tpsWarning, 0.001, "thresholds.tpsWarning default");

        // 3. Assert AutoHideRules initialized and autoHideF3 carried over
        assertNotNull(config.autoHide, "autoHide should be non-null after migration");
        assertEquals(false, config.autoHide.hideWithF3, "autoHide.hideWithF3 should receive v6 appearance.autoHideF3 (false)");
        assertEquals(false, config.appearance.autoHideF3, "appearance.autoHideF3 should sync with hideWithF3 (false)");
        assertEquals(false, config.autoHide.fadeOnIdle, "autoHide.fadeOnIdle default false");
        assertEquals(30, config.autoHide.fadeDelaySeconds, "autoHide.fadeDelaySeconds default 30");

        // 4. Assert Default profile creation and capture
        assertEquals("Default", config.selectedProfile, "selectedProfile should be 'Default'");
        assertNotNull(config.configProfiles, "configProfiles should not be null");
        assertTrue(config.configProfiles.containsKey("Default"), "configProfiles should contain 'Default'");

        ModConfig.ConfigProfile defaultProfile = config.configProfiles.get("Default");
        assertNotNull(defaultProfile, "Default profile snapshot should be non-null");
        assertEquals(150, defaultProfile.general.updateIntervalMs, "Default profile should capture updateIntervalMs");
        assertEquals(false, defaultProfile.hud.showAverageFps, "Default profile should capture showAverageFps");
        assertEquals(false, defaultProfile.appearance.autoHideF3, "Default profile should capture appearance.autoHideF3");
        assertEquals(false, defaultProfile.autoHide.hideWithF3, "Default profile should capture autoHide.hideWithF3");
        assertEquals(60, defaultProfile.thresholds.fpsGood, "Default profile should capture thresholds");

        System.out.println("  [PASS] testLegacyV6ToV9Migration");
    }

    private static void testDeepCopyIsolation() {
        ModConfig config = new ModConfig();
        config.general.updateIntervalMs = 200;
        config.hud.showFps = true;
        config.thresholds.fpsGood = 80;
        config.autoHide.fadeOnIdle = false;
        config.saveProfile("TestProfile", config, true);

        ModConfig.ConfigProfile snapshot = config.configProfiles.get("TestProfile");
        assertNotNull(snapshot, "Snapshot should exist");

        // Mutate live config
        config.general.updateIntervalMs = 500;
        config.hud.showFps = false;
        config.thresholds.fpsGood = 144;
        config.autoHide.fadeOnIdle = true;

        // Verify snapshot was NOT modified
        assertEquals(200, snapshot.general.updateIntervalMs, "Snapshot general must not mutate with live config");
        assertEquals(true, snapshot.hud.showFps, "Snapshot hud must not mutate with live config");
        assertEquals(80, snapshot.thresholds.fpsGood, "Snapshot thresholds must not mutate with live config");
        assertEquals(false, snapshot.autoHide.fadeOnIdle, "Snapshot autoHide must not mutate with live config");

        System.out.println("  [PASS] testDeepCopyIsolation");
    }

    private static void testProfileLifecycleAndCollisions() {
        ModConfig config = new ModConfig();
        config.resetToDefaults();

        // 1. Saving new profile
        boolean saved = config.saveProfile("Gaming", config, false);
        assertTrue(saved, "Saving new unique profile should succeed");
        assertTrue(config.configProfiles.containsKey("Gaming"), "Profile 'Gaming' should exist");
        assertEquals("Gaming", config.selectedProfile, "selectedProfile should update to 'Gaming'");

        // 2. Collision rejection on duplicate name without overwrite
        boolean duplicateSave = config.saveProfile("Default", config, false);
        assertEquals(false, duplicateSave, "Saving duplicate name without overwrite flag should be rejected");

        // 3. Collision allow when overwrite flag is true
        boolean overwriteSave = config.saveProfile("Default", config, true);
        assertTrue(overwriteSave, "Saving with allowOverwrite true should succeed");

        // 4. Renaming collision rejection
        boolean renameCollision = config.renameProfile("Gaming", "Default");
        assertEquals(false, renameCollision, "Renaming into an existing profile name should be rejected");
        assertTrue(config.configProfiles.containsKey("Gaming"), "Original profile name should still exist");

        // 5. Valid rename
        config.selectedProfile = "Gaming";
        boolean validRename = config.renameProfile("Gaming", "Competitive");
        assertTrue(validRename, "Renaming into a unique name should succeed");
        assertTrue(config.configProfiles.containsKey("Competitive"), "'Competitive' should exist");
        assertEquals(false, config.configProfiles.containsKey("Gaming"), "'Gaming' should no longer exist");
        assertEquals("Competitive", config.selectedProfile, "selectedProfile should update to 'Competitive'");

        // 6. Applying profile
        config.applyProfile("Default");
        assertEquals("Default", config.selectedProfile, "selectedProfile should be 'Default'");

        // 7. Deleting profile and fallback
        config.deleteProfile("Default");
        assertEquals(false, config.configProfiles.containsKey("Default"), "'Default' should be deleted");
        assertEquals("Competitive", config.selectedProfile, "selectedProfile should fall back to remaining profile 'Competitive'");

        System.out.println("  [PASS] testProfileLifecycleAndCollisions");
    }

    private static void testRelationalBoundaryClamping() {
        ModConfig config = new ModConfig();
        config.thresholds.fpsGood = 60;
        config.thresholds.fpsWarning = 100; // Invalid: warning > good for FPS
        config.thresholds.pingGood = 100;
        config.thresholds.pingWarning = 40;  // Invalid: warning < good for Ping

        config.validate();

        assertTrue(config.thresholds.fpsWarning <= config.thresholds.fpsGood, "fpsWarning must be clamped <= fpsGood");
        assertTrue(config.thresholds.pingWarning >= config.thresholds.pingGood, "pingWarning must be clamped >= pingGood");

        System.out.println("  [PASS] testRelationalBoundaryClamping");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertEquals(boolean expected, boolean actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " - Expected: '" + expected + "', Actual: '" + actual + "'");
        }
    }

    private static void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " - Expected: true, Actual: false");
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message + " - Expected non-null object");
        }
    }
}
