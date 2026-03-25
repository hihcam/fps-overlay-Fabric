package net.hicham.fps_overlay;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.hicham.fps_overlay.ModConfig.OverlayPosition;
import net.hicham.fps_overlay.ModConfig.OverlayStyle;
import net.hicham.fps_overlay.ModConfig.TextEffect;
import net.hicham.fps_overlay.ModConfig.ThemePreset;
import net.minecraft.class_2561;
import net.minecraft.class_437;
import java.util.ArrayList;
import java.util.List;

import static net.hicham.fps_overlay.ModConfig.OverlayPosition;
import static net.hicham.fps_overlay.ModConfig.OverlayStyle;
import static net.hicham.fps_overlay.ModConfig.TextEffect;
import static net.hicham.fps_overlay.ModConfig.ThemePreset;

public class ConfigScreenFactory {
    @SuppressWarnings("null")
    public static class_437 createConfigScreen(class_437 parent) {
        return new ConfigHubScreen(parent);
    }

    @SuppressWarnings("null")
    public static class_437 createSettingsScreen(class_437 parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(class_2561.method_43471("title.fps_overlay.config"))
                .setTransparentBackground(true)
                .setSavingRunnable(ConfigManager::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ModConfig config = ConfigManager.getConfig();

        ConfigCategory hud = builder.getOrCreateCategory(class_2561.method_43471("category.fps_overlay.hud"));
        addHudEntries(hud, entryBuilder, config);

        ConfigCategory appearance = builder.getOrCreateCategory(class_2561.method_43471("category.fps_overlay.appearance"));
        addAppearanceEntries(appearance, entryBuilder, config);

        ConfigCategory colors = builder.getOrCreateCategory(class_2561.method_43471("category.fps_overlay.colors"));
        addColorEntries(colors, entryBuilder, config);

        return builder.build();
    }

    private static void addHudEntries(ConfigCategory category, ConfigEntryBuilder entryBuilder, ModConfig config) {
        ModConfig defaults = new ModConfig();
        List<AbstractConfigListEntry> entries = new ArrayList<>();
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.enabled", config.general.enabled, defaults.general.enabled,
                value -> config.general.enabled = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.enableKeybindings", config.general.enableKeybindings,
                defaults.general.enableKeybindings,
                value -> config.general.enableKeybindings = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showFps", config.hud.showFps, defaults.hud.showFps,
                value -> config.hud.showFps = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showAverageFps", config.hud.showAverageFps,
                defaults.hud.showAverageFps,
                value -> config.hud.showAverageFps = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showFrameTime", config.hud.showFrameTime,
                defaults.hud.showFrameTime,
                value -> config.hud.showFrameTime = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.show1PercentLow", config.hud.show1PercentLow,
                defaults.hud.show1PercentLow,
                value -> config.hud.show1PercentLow = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showMemory", config.hud.showMemory, defaults.hud.showMemory,
                value -> config.hud.showMemory = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showCpuUsage", config.hud.showCpuUsage,
                defaults.hud.showCpuUsage,
                value -> config.hud.showCpuUsage = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showGpuUsage", config.hud.showGpuUsage,
                defaults.hud.showGpuUsage,
                value -> config.hud.showGpuUsage = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showPing", config.hud.showPing, defaults.hud.showPing,
                value -> config.hud.showPing = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showMspt", config.hud.showMspt, defaults.hud.showMspt,
                value -> config.hud.showMspt = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showTps", config.hud.showTps, defaults.hud.showTps,
                value -> config.hud.showTps = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showChunks", config.hud.showChunks, defaults.hud.showChunks,
                value -> config.hud.showChunks = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showCoordinates", config.hud.showCoordinates,
                defaults.hud.showCoordinates,
                value -> config.hud.showCoordinates = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showBiome", config.hud.showBiome, defaults.hud.showBiome,
                value -> config.hud.showBiome = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showGraph", config.hud.showGraph, defaults.hud.showGraph,
                value -> config.hud.showGraph = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showMinMaxStats", config.hud.showMinMaxStats,
                defaults.hud.showMinMaxStats,
                value -> config.hud.showMinMaxStats = value));
        entries.add(entryBuilder.startTextDescription(class_2561.method_43471("text.fps_overlay.metric_order_config_hint")).build());

        category.addEntry(entryBuilder.startSubCategory(class_2561.method_43471("category.fps_overlay.metrics"), entries).build());
    }

    private static void addAppearanceEntries(ConfigCategory category, ConfigEntryBuilder entryBuilder, ModConfig config) {
        ModConfig defaults = new ModConfig();
        category.addEntry(entryBuilder
                .startEnumSelector(class_2561.method_43471("option.fps_overlay.overlay_style"), OverlayStyle.class,
                        config.appearance.overlayStyle)
                .setDefaultValue(defaults.appearance.overlayStyle)
                .setSaveConsumer(value -> config.appearance.overlayStyle = value)
                .build());

        category.addEntry(entryBuilder
                .startEnumSelector(class_2561.method_43471("option.fps_overlay.position"), OverlayPosition.class,
                        config.appearance.position)
                .setDefaultValue(defaults.appearance.position)
                .setEnumNameProvider(value -> ((OverlayPosition) value).getDisplayText())
                .setSaveConsumer(value -> config.appearance.position = value)
                .build());

        category.addEntry(entryBuilder
                .startFloatField(class_2561.method_43471("option.fps_overlay.hudScale"), config.appearance.hudScale)
                .setDefaultValue(defaults.appearance.hudScale)
                .setMin(0.2f)
                .setMax(1.5f)
                .setSaveConsumer(value -> config.appearance.hudScale = value)
                .build());

        category.addEntry(entryBuilder
                .startIntSlider(class_2561.method_43471("option.fps_overlay.updateInterval"), config.general.updateIntervalMs, 16,
                        1000)
                .setDefaultValue(defaults.general.updateIntervalMs)
                .setSaveConsumer(value -> config.general.updateIntervalMs = value)
                .build());

        category.addEntry(entryBuilder
                .startIntField(class_2561.method_43471("option.fps_overlay.xOffset"), config.appearance.xOffset)
                .setDefaultValue(defaults.appearance.xOffset)
                .setMin(-2000)
                .setMax(2000)
                .setSaveConsumer(value -> config.appearance.xOffset = value)
                .build());

        category.addEntry(entryBuilder
                .startIntField(class_2561.method_43471("option.fps_overlay.yOffset"), config.appearance.yOffset)
                .setDefaultValue(defaults.appearance.yOffset)
                .setMin(-2000)
                .setMax(2000)
                .setSaveConsumer(value -> config.appearance.yOffset = value)
                .build());

        category.addEntry(entryBuilder
                .startBooleanToggle(class_2561.method_43471("option.fps_overlay.showBackground"), config.appearance.showBackground)
                .setDefaultValue(defaults.appearance.showBackground)
                .setSaveConsumer(value -> config.appearance.showBackground = value)
                .build());

        category.addEntry(entryBuilder
                .startIntSlider(class_2561.method_43471("option.fps_overlay.backgroundOpacity"),
                        config.appearance.backgroundOpacity, 0, 255)
                .setDefaultValue(defaults.appearance.backgroundOpacity)
                .setSaveConsumer(value -> config.appearance.backgroundOpacity = value)
                .build());

        category.addEntry(entryBuilder
                .startBooleanToggle(class_2561.method_43471("option.fps_overlay.autoHideF3"), config.appearance.autoHideF3)
                .setDefaultValue(defaults.appearance.autoHideF3)
                .setSaveConsumer(value -> config.appearance.autoHideF3 = value)
                .build());

        category.addEntry(entryBuilder
                .startBooleanToggle(class_2561.method_43471("option.fps_overlay.adaptiveColors"),
                        config.appearance.adaptiveColors)
                .setDefaultValue(defaults.appearance.adaptiveColors)
                .setSaveConsumer(value -> config.appearance.adaptiveColors = value)
                .build());

        category.addEntry(entryBuilder
                .startEnumSelector(class_2561.method_43471("option.fps_overlay.textEffect"), TextEffect.class,
                        config.appearance.textEffect)
                .setDefaultValue(defaults.appearance.textEffect)
                .setSaveConsumer(value -> config.appearance.textEffect = value)
                .build());

        category.addEntry(entryBuilder
                .startEnumSelector(class_2561.method_43471("option.fps_overlay.themePreset"), ThemePreset.class,
                        config.appearance.themePreset)
                .setDefaultValue(defaults.appearance.themePreset)
                .setSaveConsumer(value -> config.appearance.applyThemePreset(value))
                .build());

        category.addEntry(entryBuilder.startTextDescription(class_2561.method_43471("text.fps_overlay.position_config_hint")).build());
    }

    private static void addColorEntries(ConfigCategory category, ConfigEntryBuilder entryBuilder, ModConfig config) {
        ModConfig defaults = new ModConfig();
        category.addEntry(colorEntry(entryBuilder, "option.fps_overlay.backgroundColor", config.appearance.backgroundColor,
                defaults.appearance.backgroundColor, value -> config.appearance.backgroundColor = value, config));
        category.addEntry(colorEntry(entryBuilder, "option.fps_overlay.labelColor", config.appearance.labelColor,
                defaults.appearance.labelColor, value -> config.appearance.labelColor = value, config));
        category.addEntry(colorEntry(entryBuilder, "option.fps_overlay.valueColor", config.appearance.valueColor,
                defaults.appearance.valueColor, value -> config.appearance.valueColor = value, config));
        category.addEntry(colorEntry(entryBuilder, "option.fps_overlay.unitColor", config.appearance.unitColor,
                defaults.appearance.unitColor, value -> config.appearance.unitColor = value, config));
        category.addEntry(colorEntry(entryBuilder, "option.fps_overlay.dividerColor", config.appearance.dividerColor,
                defaults.appearance.dividerColor, value -> config.appearance.dividerColor = value, config));
        category.addEntry(colorEntry(entryBuilder, "option.fps_overlay.goodColor", config.appearance.goodColor,
                defaults.appearance.goodColor, value -> config.appearance.goodColor = value, config));
        category.addEntry(colorEntry(entryBuilder, "option.fps_overlay.warningColor", config.appearance.warningColor,
                defaults.appearance.warningColor, value -> config.appearance.warningColor = value, config));
        category.addEntry(colorEntry(entryBuilder, "option.fps_overlay.badColor", config.appearance.badColor,
                defaults.appearance.badColor, value -> config.appearance.badColor = value, config));
    }

    private static AbstractConfigListEntry booleanToggle(ConfigEntryBuilder entryBuilder, String key, boolean value,
            boolean defaultValue,
            java.util.function.Consumer<Boolean> saveConsumer) {
        return entryBuilder.startBooleanToggle(class_2561.method_43471(key), value)
                .setDefaultValue(defaultValue)
                .setYesNoTextSupplier(enabled -> enabled ? class_2561.method_43470("[ ON ]") : class_2561.method_43470("[ OFF ]"))
                .setSaveConsumer(saveConsumer)
                .build();
    }

    private static AbstractConfigListEntry colorEntry(ConfigEntryBuilder entryBuilder, String key, int value,
            int defaultValue,
            java.util.function.IntConsumer saveConsumer, ModConfig config) {
        return entryBuilder.startColorField(class_2561.method_43471(key), value & 0xFFFFFF)
                .setDefaultValue(defaultValue & 0xFFFFFF)
                .setSaveConsumer(color -> {
                    saveConsumer.accept(0xFF000000 | (color & 0xFFFFFF));
                    config.appearance.themePreset = ThemePreset.CUSTOM;
                })
                .build();
    }
}
