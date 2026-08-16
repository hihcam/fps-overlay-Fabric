package net.hicham.fps_overlay;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.Locale;

public class ConfigScreenFactory {
    public static Screen createConfigScreen(Screen parent) {
        return new ConfigHubScreen(parent);
    }

    public static Screen createSettingsScreen(Screen parent) {
        ModConfig config = ConfigManager.getConfig();
        ModConfig defaults = new ModConfig();

        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("title.fps_overlay.config"))
                .save(ConfigManager::saveConfig)
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("category.fps_overlay.hud"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("category.fps_overlay.hud"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.enabled"))
                                        .binding(defaults.general.enabled, () -> config.general.enabled, value -> config.general.enabled = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.enableKeybindings"))
                                        .binding(defaults.general.enableKeybindings, () -> config.general.enableKeybindings, value -> config.general.enableKeybindings = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.showGraph"))
                                        .binding(defaults.hud.showGraph, () -> config.hud.showGraph, value -> config.hud.showGraph = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.showMinMaxStats"))
                                        .binding(defaults.hud.showMinMaxStats, () -> config.hud.showMinMaxStats, value -> config.hud.showMinMaxStats = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.showLabels"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.showLabels")))
                                        .binding(defaults.hud.showLabels, () -> config.hud.showLabels, value -> config.hud.showLabels = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("category.fps_overlay.appearance"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("category.fps_overlay.appearance"))
                                .option(Option.<ModConfig.OverlayStyle>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.overlay_style"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.overlay_style")))
                                        .binding(defaults.appearance.overlayStyle, () -> config.appearance.overlayStyle, value -> config.appearance.overlayStyle = value)
                                        .controller(builder -> EnumControllerBuilder.create(builder)
                                                .enumClass(ModConfig.OverlayStyle.class)
                                                .formatValue(value -> Component.translatable("enum.fps_overlay.overlaystyle." + value.name().toLowerCase(Locale.ROOT))))
                                        .build())
                                .option(Option.<ModConfig.OverlayPosition>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.position"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.position")))
                                        .binding(defaults.appearance.position, () -> config.appearance.position, value -> config.appearance.position = value)
                                        .controller(builder -> EnumControllerBuilder.create(builder)
                                                .enumClass(ModConfig.OverlayPosition.class)
                                                .formatValue(ModConfig.OverlayPosition::getDisplayText))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.hudScale"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.hud_scale")))
                                        .binding(defaults.appearance.hudScale, () -> config.appearance.hudScale, value -> config.appearance.hudScale = value)
                                        .controller(builder -> FloatSliderControllerBuilder.create(builder)
                                                .range(0.2f, 1.5f)
                                                .step(0.05f)
                                                .formatValue(value -> Component.literal((int)(value * 100) + "%")))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.updateInterval"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.updateInterval")))
                                        .binding(defaults.general.updateIntervalMs, () -> config.general.updateIntervalMs, value -> config.general.updateIntervalMs = value)
                                        .controller(builder -> IntegerSliderControllerBuilder.create(builder)
                                                .range(16, 1000)
                                                .step(1)
                                                .formatValue(value -> {
                                                    if (value == 16) return Component.translatable("enum.fps_overlay.update.16");
                                                    if (value == 33) return Component.translatable("enum.fps_overlay.update.33");
                                                    if (value == 50) return Component.translatable("enum.fps_overlay.update.50");
                                                    if (value == 100) return Component.translatable("enum.fps_overlay.update.100");
                                                    if (value == 250) return Component.translatable("enum.fps_overlay.update.250");
                                                    if (value == 500) return Component.translatable("enum.fps_overlay.update.500");
                                                    if (value == 1000) return Component.translatable("enum.fps_overlay.update.1000");
                                                    return Component.literal(value + "ms");
                                                }))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.xOffset"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.xOffset")))
                                        .binding(defaults.appearance.xOffset, () -> config.appearance.xOffset, value -> config.appearance.xOffset = value)
                                        .controller(builder -> IntegerSliderControllerBuilder.create(builder)
                                                .range(-1000, 1000)
                                                .step(1)
                                                .formatValue(value -> Component.literal(value + "px")))
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.yOffset"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.yOffset")))
                                        .binding(defaults.appearance.yOffset, () -> config.appearance.yOffset, value -> config.appearance.yOffset = value)
                                        .controller(builder -> IntegerSliderControllerBuilder.create(builder)
                                                .range(-1000, 1000)
                                                .step(1)
                                                .formatValue(value -> Component.literal(value + "px")))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.showBackground"))
                                        .binding(defaults.appearance.showBackground, () -> config.appearance.showBackground, value -> config.appearance.showBackground = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.backgroundOpacity"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.backgroundOpacity")))
                                        .binding(defaults.appearance.backgroundOpacity, () -> config.appearance.backgroundOpacity, value -> config.appearance.backgroundOpacity = value)
                                        .controller(builder -> IntegerSliderControllerBuilder.create(builder)
                                                .range(0, 255)
                                                .step(1)
                                                .formatValue(value -> Component.literal(value + " (" + (int)(value * 100 / 255f) + "%)")))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.adaptiveColors"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.adaptiveColors")))
                                        .binding(defaults.appearance.adaptiveColors, () -> config.appearance.adaptiveColors, value -> config.appearance.adaptiveColors = value)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<ModConfig.TextEffect>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.textEffect"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.textEffect")))
                                        .binding(defaults.appearance.textEffect, () -> config.appearance.textEffect, value -> config.appearance.textEffect = value)
                                        .controller(builder -> EnumControllerBuilder.create(builder)
                                                .enumClass(ModConfig.TextEffect.class)
                                                .formatValue(value -> Component.translatable("enum.fps_overlay.textEffect." + value.name().toLowerCase(Locale.ROOT))))
                                        .build())
                                .option(Option.<ModConfig.ThemePreset>createBuilder()
                                        .name(Component.translatable("option.fps_overlay.themePreset"))
                                        .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.themePreset")))
                                        .binding(defaults.appearance.themePreset, () -> config.appearance.themePreset, value -> {
                                            config.appearance.applyThemePreset(value);
                                        })
                                        .controller(builder -> EnumControllerBuilder.create(builder)
                                                .enumClass(ModConfig.ThemePreset.class)
                                                .formatValue(value -> Component.translatable("enum.fps_overlay.themePreset." + value.name().toLowerCase(Locale.ROOT))))
                                        .build())
                                .build())
                        .build())
                .category(buildProfileCategory(parent, config))
                .category(buildAutoHideCategory(config, defaults))
                .category(buildThresholdCategory(config, defaults))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("category.fps_overlay.colors"))
                        .group(OptionGroup.createBuilder()
                                .name(Component.translatable("category.fps_overlay.colors"))
                                .option(colorOption("option.fps_overlay.backgroundColor", defaults.appearance.backgroundColor, () -> config.appearance.backgroundColor, value -> config.appearance.backgroundColor = value, config))
                                .option(colorOption("option.fps_overlay.labelColor", defaults.appearance.labelColor, () -> config.appearance.labelColor, value -> config.appearance.labelColor = value, config))
                                .option(colorOption("option.fps_overlay.valueColor", defaults.appearance.valueColor, () -> config.appearance.valueColor, value -> config.appearance.valueColor = value, config))
                                .option(colorOption("option.fps_overlay.unitColor", defaults.appearance.unitColor, () -> config.appearance.unitColor, value -> config.appearance.unitColor = value, config))
                                .option(colorOption("option.fps_overlay.dividerColor", defaults.appearance.dividerColor, () -> config.appearance.dividerColor, value -> config.appearance.dividerColor = value, config))
                                .option(colorOption("option.fps_overlay.goodColor", defaults.appearance.goodColor, () -> config.appearance.goodColor, value -> config.appearance.goodColor = value, config))
                                .option(colorOption("option.fps_overlay.warningColor", defaults.appearance.warningColor, () -> config.appearance.warningColor, value -> config.appearance.warningColor = value, config))
                                .option(colorOption("option.fps_overlay.badColor", defaults.appearance.badColor, () -> config.appearance.badColor, value -> config.appearance.badColor = value, config))
                                .build())
                        .build())
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory buildThresholdCategory(ModConfig config, ModConfig defaults) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("category.fps_overlay.thresholds"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("group.fps_overlay.thresholds.fps"))
                        .option(intSlider("option.fps_overlay.thresholds.fpsGood", "tooltip.fps_overlay.thresholds.fpsGood",
                                defaults.thresholds.fpsGood, () -> config.thresholds.fpsGood,
                                value -> config.thresholds.fpsGood = value, 1, 500, value -> Component.literal(value + " FPS")))
                        .option(intSlider("option.fps_overlay.thresholds.fpsWarning", "tooltip.fps_overlay.thresholds.fpsWarning",
                                defaults.thresholds.fpsWarning, () -> config.thresholds.fpsWarning,
                                value -> config.thresholds.fpsWarning = value, 1, 500, value -> Component.literal(value + " FPS")))
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("group.fps_overlay.thresholds.timing"))
                        .option(doubleSlider("option.fps_overlay.thresholds.frameTimeGood", "tooltip.fps_overlay.thresholds.frameTimeGood",
                                defaults.thresholds.frameTimeGood, () -> config.thresholds.frameTimeGood,
                                value -> config.thresholds.frameTimeGood = value, 0.1, 200.0,
                                value -> Component.literal(String.format(Locale.ROOT, "%.1f ms", value))))
                        .option(doubleSlider("option.fps_overlay.thresholds.frameTimeWarning", "tooltip.fps_overlay.thresholds.frameTimeWarning",
                                defaults.thresholds.frameTimeWarning, () -> config.thresholds.frameTimeWarning,
                                value -> config.thresholds.frameTimeWarning = value, 0.1, 200.0,
                                value -> Component.literal(String.format(Locale.ROOT, "%.1f ms", value))))
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("group.fps_overlay.thresholds.network"))
                        .option(intSlider("option.fps_overlay.thresholds.pingGood", "tooltip.fps_overlay.thresholds.pingGood",
                                defaults.thresholds.pingGood, () -> config.thresholds.pingGood,
                                value -> config.thresholds.pingGood = value, 0, 1000, value -> Component.literal(value + " ms")))
                        .option(intSlider("option.fps_overlay.thresholds.pingWarning", "tooltip.fps_overlay.thresholds.pingWarning",
                                defaults.thresholds.pingWarning, () -> config.thresholds.pingWarning,
                                value -> config.thresholds.pingWarning = value, 0, 2000, value -> Component.literal(value + " ms")))
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("group.fps_overlay.thresholds.server"))
                        .option(doubleSlider("option.fps_overlay.thresholds.tpsGood", "tooltip.fps_overlay.thresholds.tpsGood",
                                defaults.thresholds.tpsGood, () -> config.thresholds.tpsGood,
                                value -> config.thresholds.tpsGood = value, 0.1, 20.0,
                                value -> Component.literal(String.format(Locale.ROOT, "%.1f TPS", value))))
                        .option(doubleSlider("option.fps_overlay.thresholds.tpsWarning", "tooltip.fps_overlay.thresholds.tpsWarning",
                                defaults.thresholds.tpsWarning, () -> config.thresholds.tpsWarning,
                                value -> config.thresholds.tpsWarning = value, 0.1, 20.0,
                                value -> Component.literal(String.format(Locale.ROOT, "%.1f TPS", value))))
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("group.fps_overlay.thresholds.resources"))
                        .option(intSlider("option.fps_overlay.thresholds.memoryGood", "tooltip.fps_overlay.thresholds.memoryGood",
                                defaults.thresholds.memoryGood, () -> config.thresholds.memoryGood,
                                value -> config.thresholds.memoryGood = value, 1, 100, value -> Component.literal(value + "%")))
                        .option(intSlider("option.fps_overlay.thresholds.memoryWarning", "tooltip.fps_overlay.thresholds.memoryWarning",
                                defaults.thresholds.memoryWarning, () -> config.thresholds.memoryWarning,
                                value -> config.thresholds.memoryWarning = value, 1, 100, value -> Component.literal(value + "%")))
                        .build())
                .build();
    }

    private static Option<Integer> intSlider(String key, String tooltipKey, int defaultValue, java.util.function.Supplier<Integer> getter,
                                            java.util.function.Consumer<Integer> setter, int min, int max, java.util.function.Function<Integer, Component> formatter) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable(key))
                .description(OptionDescription.of(Component.translatable(tooltipKey)))
                .binding(defaultValue, getter, setter)
                .controller(builder -> IntegerSliderControllerBuilder.create(builder)
                        .range(min, max)
                        .step(1)
                        .formatValue(formatter::apply))
                .build();
    }

    private static Option<Double> doubleSlider(String key, String tooltipKey, double defaultValue, java.util.function.Supplier<Double> getter,
                                               java.util.function.Consumer<Double> setter, double min, double max, java.util.function.Function<Double, Component> formatter) {
        return Option.<Double>createBuilder()
                .name(Component.translatable(key))
                .description(OptionDescription.of(Component.translatable(tooltipKey)))
                .binding(defaultValue, getter, setter)
                .controller(builder -> DoubleSliderControllerBuilder.create(builder)
                        .range(min, max)
                        .step(0.1)
                        .formatValue(formatter::apply))
                .build();
    }

    private static ConfigCategory buildAutoHideCategory(ModConfig config, ModConfig defaults) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("category.fps_overlay.autohide"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("category.fps_overlay.autohide"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("option.fps_overlay.autohide.f3"))
                                .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.autohide.f3")))
                                .binding(defaults.autoHide.hideWithF3, () -> config.autoHide.hideWithF3, value -> {
                                    config.autoHide.hideWithF3 = value;
                                    config.appearance.autoHideF3 = value;
                                })
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("option.fps_overlay.autohide.chat"))
                                .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.autohide.chat")))
                                .binding(defaults.autoHide.hideInChat, () -> config.autoHide.hideInChat, value -> config.autoHide.hideInChat = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("option.fps_overlay.autohide.inventory"))
                                .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.autohide.inventory")))
                                .binding(defaults.autoHide.hideInInventory, () -> config.autoHide.hideInInventory, value -> config.autoHide.hideInInventory = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("option.fps_overlay.autohide.screenshot"))
                                .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.autohide.screenshot")))
                                .binding(defaults.autoHide.hideInScreenshots, () -> config.autoHide.hideInScreenshots, value -> config.autoHide.hideInScreenshots = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("option.fps_overlay.autohide.fade"))
                                .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.autohide.fade")))
                                .binding(defaults.autoHide.fadeOnIdle, () -> config.autoHide.fadeOnIdle, value -> config.autoHide.fadeOnIdle = value)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(intSlider("option.fps_overlay.autohide.delay", "tooltip.fps_overlay.autohide.delay",
                                defaults.autoHide.fadeDelaySeconds, () -> config.autoHide.fadeDelaySeconds,
                                value -> config.autoHide.fadeDelaySeconds = value, 1, 300, value -> Component.literal(value + "s")))
                        .option(floatSlider("option.fps_overlay.autohide.opacity", "tooltip.fps_overlay.autohide.opacity",
                                defaults.autoHide.fadeOpacity, () -> config.autoHide.fadeOpacity,
                                value -> config.autoHide.fadeOpacity = value, 0.0f, 1.0f, 0.05f,
                                value -> Component.literal((int) (value * 100) + "%")))
                        .build())
                .build();
    }

    private static Option<Float> floatSlider(String key, String tooltipKey, float defaultValue, java.util.function.Supplier<Float> getter,
                                            java.util.function.Consumer<Float> setter, float min, float max, float step, java.util.function.Function<Float, Component> formatter) {
        return Option.<Float>createBuilder()
                .name(Component.translatable(key))
                .description(OptionDescription.of(Component.translatable(tooltipKey)))
                .binding(defaultValue, getter, setter)
                .controller(builder -> FloatSliderControllerBuilder.create(builder)
                        .range(min, max)
                        .step(step)
                        .formatValue(formatter::apply))
                .build();
    }

    private static Option<Color> colorOption(String key, int defaultValue, java.util.function.Supplier<Integer> getter, java.util.function.Consumer<Integer> setter, ModConfig config) {
        return Option.<Color>createBuilder()
                .name(Component.translatable(key))
                .binding(new Color(defaultValue, true), () -> new Color(getter.get(), true), value -> {
                    setter.accept(value.getRGB());
                    config.appearance.themePreset = ModConfig.ThemePreset.CUSTOM;
                })
                .controller(builder -> ColorControllerBuilder.create(builder)
                        .allowAlpha(true))
                .build();
    }

    private static final String NO_PROFILE_VALUE = "(No Profile)";

    private static ConfigCategory buildProfileCategory(Screen parent, ModConfig config) {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("category.fps_overlay.profiles"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("group.fps_overlay.profiles.actions"))
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("button.fps_overlay.save_profile"))
                                .text(Component.translatable("button.fps_overlay.save_profile"))
                                .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.save_profile")))
                                .action((screen, option) -> Minecraft.getInstance().setScreenAndShow(
                                        new ProfileNameScreen((Screen) screen,
                                                Component.translatable("screen.fps_overlay.save_profile"),
                                                "",
                                                config.getProfileNames(),
                                                name -> {
                                                    saveProfile(config, name);
                                                    Minecraft.getInstance().setScreenAndShow(createSettingsScreen(parent));
                                                })))
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("button.fps_overlay.update_profile"))
                                .text(Component.translatable("button.fps_overlay.update_profile"))
                                .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.update_profile")))
                                .available(hasSelectedProfile(config))
                                .action((screen, option) -> {
                                    updateSelectedProfile(config);
                                    Minecraft.getInstance().setScreenAndShow(createSettingsScreen(parent));
                                })
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("button.fps_overlay.rename_profile"))
                                .text(Component.translatable("button.fps_overlay.rename_profile"))
                                .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.rename_profile")))
                                .available(hasSelectedProfile(config))
                                .action((screen, option) -> {
                                    Set<String> disallowed = new java.util.HashSet<>(config.getProfileNames());
                                    disallowed.remove(config.selectedProfile);
                                    Minecraft.getInstance().setScreenAndShow(
                                            new ProfileNameScreen((Screen) screen,
                                                    Component.translatable("screen.fps_overlay.rename_profile"),
                                                    config.selectedProfile,
                                                    disallowed,
                                                    name -> {
                                                        renameProfile(config, name);
                                                        Minecraft.getInstance().setScreenAndShow(createSettingsScreen(parent));
                                                    }));
                                })
                                .build())
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("button.fps_overlay.delete_profile"))
                                .text(Component.translatable("button.fps_overlay.delete_profile"))
                                .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.delete_profile")))
                                .available(hasSelectedProfile(config))
                                .action((screen, option) -> {
                                    deleteProfile(config);
                                    Minecraft.getInstance().setScreenAndShow(createSettingsScreen(parent));
                                })
                                .build())
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("group.fps_overlay.profiles.available"))
                        .option(ButtonOption.createBuilder()
                                .name(Component.translatable("option.fps_overlay.configProfile"))
                                .text(Component.literal(getProfileSelectionValue(config)))
                                .description(OptionDescription.of(Component.translatable("tooltip.fps_overlay.configProfile")))
                                .action((screen, option) -> Minecraft.getInstance().setScreenAndShow(
                                        new ProfileSelectScreen((Screen) screen, config,
                                                profileName -> {
                                                    selectProfile(config, profileName);
                                                    Minecraft.getInstance().setScreenAndShow(createSettingsScreen(parent));
                                                })))
                                .build())
                        .build())
                .build();
    }

    private static boolean hasSelectedProfile(ModConfig config) {
        return config.selectedProfile != null
                && !config.selectedProfile.isBlank()
                && config.configProfiles.containsKey(config.selectedProfile);
    }

    private static void saveProfile(ModConfig config, String name) {
        config.saveProfile(name, config, false);
        ConfigManager.saveConfig();
    }

    private static void updateSelectedProfile(ModConfig config) {
        if (!hasSelectedProfile(config)) {
            return;
        }
        config.saveProfile(config.selectedProfile, config, true);
        ConfigManager.saveConfig();
    }

    private static void renameProfile(ModConfig config, String newName) {
        config.renameProfile(config.selectedProfile, newName);
        ConfigManager.saveConfig();
    }

    private static void deleteProfile(ModConfig config) {
        config.deleteProfile(config.selectedProfile);
        ConfigManager.saveConfig();
    }

    private static void selectProfile(ModConfig config, String profileName) {
        if (NO_PROFILE_VALUE.equals(profileName) || profileName == null || profileName.isEmpty()) {
            config.selectedProfile = "";
            ConfigManager.saveConfig();
            return;
        }
        config.applyProfile(profileName);
        ConfigManager.saveConfig();
    }

    private static String getProfileSelectionValue(ModConfig config) {
        return hasSelectedProfile(config) ? config.selectedProfile : NO_PROFILE_VALUE;
    }
}
