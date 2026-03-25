package net.hicham.fps_overlay;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ConfigScreenFactory {
    public static Screen createConfigScreen(Screen parent) {
        return new ConfigHubScreen(parent);
    }

    public static Screen createSettingsScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.fps_overlay.config"))
                .setTransparentBackground(true)
                .setSavingRunnable(ConfigManager::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ModConfig config = ConfigManager.getConfig();

        ConfigCategory hud = builder.getOrCreateCategory(Component.translatable("category.fps_overlay.hud"));
        addHudEntries(hud, entryBuilder, config);

        ConfigCategory appearance = builder.getOrCreateCategory(Component.translatable("category.fps_overlay.appearance"));
        addAppearanceEntries(appearance, entryBuilder, config);

        ConfigCategory colors = builder.getOrCreateCategory(Component.translatable("category.fps_overlay.colors"));
        addColorEntries(colors, entryBuilder, config);

        return builder.build();
    }

    private static void addHudEntries(ConfigCategory category, ConfigEntryBuilder entryBuilder, ModConfig config) {
        ModConfig defaults = new ModConfig();
        List<AbstractConfigListEntry> entries = new ArrayList<>();

        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.enabled", config.general.enabled,
                defaults.general.enabled, value -> config.general.enabled = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.enableKeybindings", config.general.enableKeybindings,
                defaults.general.enableKeybindings, value -> config.general.enableKeybindings = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showGraph", config.hud.showGraph,
                defaults.hud.showGraph, value -> config.hud.showGraph = value));
        entries.add(booleanToggle(entryBuilder, "option.fps_overlay.showMinMaxStats", config.hud.showMinMaxStats,
                defaults.hud.showMinMaxStats, value -> config.hud.showMinMaxStats = value));
        entries.add(entryBuilder.startTextDescription(Component.translatable("text.fps_overlay.metric_order_config_hint")).build());

        for (AbstractConfigListEntry<?> entry : entries) {
            category.addEntry(entry);
        }
    }

    private static void addAppearanceEntries(ConfigCategory category, ConfigEntryBuilder entryBuilder, ModConfig config) {
        ModConfig defaults = new ModConfig();

        category.addEntry(entryBuilder
                .startEnumSelector(Component.translatable("option.fps_overlay.overlay_style"), ModConfig.OverlayStyle.class,
                        config.appearance.overlayStyle)
                .setDefaultValue(defaults.appearance.overlayStyle)
                .setEnumNameProvider(value -> Component.translatable(
                        "enum.fps_overlay.overlaystyle." + value.name().toLowerCase(Locale.ROOT)))
                .setSaveConsumer(value -> config.appearance.overlayStyle = value)
                .build());

        category.addEntry(entryBuilder
                .startEnumSelector(Component.translatable("option.fps_overlay.position"), ModConfig.OverlayPosition.class,
                        config.appearance.position)
                .setDefaultValue(defaults.appearance.position)
                .setEnumNameProvider(value -> ((ModConfig.OverlayPosition) value).getDisplayText())
                .setSaveConsumer(value -> config.appearance.position = value)
                .build());

        category.addEntry(entryBuilder
                .startFloatField(Component.translatable("option.fps_overlay.hudScale"), config.appearance.hudScale)
                .setDefaultValue(defaults.appearance.hudScale)
                .setMin(0.2f)
                .setMax(1.5f)
                .setSaveConsumer(value -> config.appearance.hudScale = value)
                .build());

        category.addEntry(entryBuilder
                .startIntSlider(Component.translatable("option.fps_overlay.updateInterval"), config.general.updateIntervalMs, 16,
                        1000)
                .setDefaultValue(defaults.general.updateIntervalMs)
                .setSaveConsumer(value -> config.general.updateIntervalMs = value)
                .build());

        category.addEntry(entryBuilder
                .startIntField(Component.translatable("option.fps_overlay.xOffset"), config.appearance.xOffset)
                .setDefaultValue(defaults.appearance.xOffset)
                .setMin(-2000)
                .setMax(2000)
                .setSaveConsumer(value -> config.appearance.xOffset = value)
                .build());

        category.addEntry(entryBuilder
                .startIntField(Component.translatable("option.fps_overlay.yOffset"), config.appearance.yOffset)
                .setDefaultValue(defaults.appearance.yOffset)
                .setMin(-2000)
                .setMax(2000)
                .setSaveConsumer(value -> config.appearance.yOffset = value)
                .build());

        category.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("option.fps_overlay.showBackground"), config.appearance.showBackground)
                .setDefaultValue(defaults.appearance.showBackground)
                .setSaveConsumer(value -> config.appearance.showBackground = value)
                .build());

        category.addEntry(entryBuilder
                .startIntSlider(Component.translatable("option.fps_overlay.backgroundOpacity"),
                        config.appearance.backgroundOpacity, 0, 255)
                .setDefaultValue(defaults.appearance.backgroundOpacity)
                .setSaveConsumer(value -> config.appearance.backgroundOpacity = value)
                .build());

        category.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("option.fps_overlay.autoHideF3"), config.appearance.autoHideF3)
                .setDefaultValue(defaults.appearance.autoHideF3)
                .setSaveConsumer(value -> config.appearance.autoHideF3 = value)
                .build());

        category.addEntry(entryBuilder
                .startBooleanToggle(Component.translatable("option.fps_overlay.adaptiveColors"), config.appearance.adaptiveColors)
                .setDefaultValue(defaults.appearance.adaptiveColors)
                .setSaveConsumer(value -> config.appearance.adaptiveColors = value)
                .build());

        category.addEntry(entryBuilder
                .startEnumSelector(Component.translatable("option.fps_overlay.textEffect"), ModConfig.TextEffect.class,
                        config.appearance.textEffect)
                .setDefaultValue(defaults.appearance.textEffect)
                .setEnumNameProvider(value -> Component.translatable(
                        "enum.fps_overlay.textEffect." + value.name().toLowerCase(Locale.ROOT)))
                .setSaveConsumer(value -> config.appearance.textEffect = value)
                .build());

        category.addEntry(entryBuilder
                .startEnumSelector(Component.translatable("option.fps_overlay.themePreset"), ModConfig.ThemePreset.class,
                        config.appearance.themePreset)
                .setDefaultValue(defaults.appearance.themePreset)
                .setEnumNameProvider(value -> Component.translatable(
                        "enum.fps_overlay.themePreset." + value.name().toLowerCase(Locale.ROOT)))
                .setSaveConsumer(config.appearance::applyThemePreset)
                .build());

        category.addEntry(entryBuilder.startTextDescription(Component.translatable("text.fps_overlay.position_config_hint")).build());
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

    private static AbstractConfigListEntry<?> booleanToggle(ConfigEntryBuilder entryBuilder, String key, boolean value,
            boolean defaultValue, Consumer<Boolean> saveConsumer) {
        return entryBuilder.startBooleanToggle(Component.translatable(key), value)
                .setDefaultValue(defaultValue)
                .setYesNoTextSupplier(enabled -> enabled ? Component.literal("[ ON ]") : Component.literal("[ OFF ]"))
                .setSaveConsumer(saveConsumer)
                .build();
    }

    private static AbstractConfigListEntry<?> colorEntry(ConfigEntryBuilder entryBuilder, String key, int value,
            int defaultValue, IntConsumer saveConsumer, ModConfig config) {
        return entryBuilder.startColorField(Component.translatable(key), value & 0xFFFFFF)
                .setDefaultValue(defaultValue & 0xFFFFFF)
                .setSaveConsumer(color -> {
                    saveConsumer.accept(0xFF000000 | (color & 0xFFFFFF));
                    config.appearance.themePreset = ModConfig.ThemePreset.CUSTOM;
                })
                .build();
    }
}
