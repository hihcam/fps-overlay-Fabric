package net.hicham.fps_overlay;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import static net.hicham.fps_overlay.ModConfig.*;

public class ConfigScreenFactory {
        private static final String TITLE_CONFIG = "title.fps_overlay.config";

        private static final String CATEGORY_HUD = "category.fps_overlay.hud";
        private static final String CATEGORY_APPEARANCE = "category.fps_overlay.appearance";

        private static final String TOOLTIP_ENABLED = "tooltip.fps_overlay.enabled";
        private static final String TOOLTIP_UPDATE_INTERVAL = "tooltip.fps_overlay.updateInterval";

        private static final String TOOLTIP_SHOW_FPS = "tooltip.fps_overlay.showFps";
        private static final String TOOLTIP_SHOW_AVERAGE_FPS = "tooltip.fps_overlay.showAverageFps";

        private static final String TOOLTIP_SHOW_MEMORY = "tooltip.fps_overlay.showMemory";
        private static final String TOOLTIP_SHOW_MEMORY_PERCENTAGE = "tooltip.fps_overlay.showMemoryPercentage";
        private static final String TOOLTIP_SHOW_PING = "tooltip.fps_overlay.showPing";
        private static final String TOOLTIP_POSITION = "tooltip.fps_overlay.position";
        private static final String TOOLTIP_OVERLAY_STYLE = "tooltip.fps_overlay.style";
        private static final String TOOLTIP_SCALE = "tooltip.fps_overlay.scale";
        private static final String TOOLTIP_PADDING = "tooltip.fps_overlay.padding";
        private static final String TOOLTIP_COMPACT_MODE = "tooltip.fps_overlay.compactMode";
        private static final String TOOLTIP_SHOW_BACKGROUND = "tooltip.fps_overlay.showBackground";
        private static final String TOOLTIP_BACKGROUND_OPACITY = "tooltip.fps_overlay.backgroundOpacity";
        private static final String TOOLTIP_TEXT_COLOR = "tooltip.fps_overlay.textColor";
        private static final String TOOLTIP_USE_TEXT_SHADOW = "tooltip.fps_overlay.useTextShadow";
        private static final String TOOLTIP_USE_ADAPTIVE_COLORS = "tooltip.fps_overlay.useAdaptiveColors";

        @SuppressWarnings("null")
        public static Screen createConfigScreen(Screen parent) {
                ConfigBuilder builder = ConfigBuilder.create()
                                .setParentScreen(parent)
                                .setTitle(Text.translatable(TITLE_CONFIG))
                                .setTransparentBackground(true)
                                .setSavingRunnable(ConfigManager::saveConfig);

                ConfigEntryBuilder entryBuilder = builder.entryBuilder();
                ModConfig config = ConfigManager.getConfig();

                ConfigCategory hud = builder.getOrCreateCategory(Text.translatable(CATEGORY_HUD));

                hud.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("option.fps_overlay.enabled"),
                                                config.general.enabled)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable(TOOLTIP_ENABLED))
                                .setYesNoTextSupplier(val -> val ? Text.literal("[ ON ]") : Text.literal("[ OFF ]"))
                                .setSaveConsumer(value -> config.general.enabled = value)
                                .build());

                hud.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("option.fps_overlay.showFps"), config.hud.showFps)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable(TOOLTIP_SHOW_FPS))
                                .setYesNoTextSupplier(val -> val ? Text.literal("[ ON ]") : Text.literal("[ OFF ]"))
                                .setSaveConsumer(value -> config.hud.showFps = value)
                                .build());

                hud.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("option.fps_overlay.showAverageFps"),
                                                config.hud.showAverageFps)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable(TOOLTIP_SHOW_AVERAGE_FPS))
                                .setYesNoTextSupplier(val -> val ? Text.literal("[ ON ]") : Text.literal("[ OFF ]"))
                                .setSaveConsumer(value -> config.hud.showAverageFps = value)
                                .build());

                hud.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("option.fps_overlay.showMemory"),
                                                config.hud.showMemory)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable(TOOLTIP_SHOW_MEMORY))
                                .setYesNoTextSupplier(val -> val ? Text.literal("[ ON ]") : Text.literal("[ OFF ]"))
                                .setSaveConsumer(value -> config.hud.showMemory = value)
                                .build());

                hud.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("option.fps_overlay.showPing"),
                                                config.hud.showPing)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable(TOOLTIP_SHOW_PING))
                                .setYesNoTextSupplier(val -> val ? Text.literal("[ ON ]") : Text.literal("[ OFF ]"))
                                .setSaveConsumer(value -> config.hud.showPing = value)
                                .build());

                ConfigCategory appearance = builder.getOrCreateCategory(Text.translatable(CATEGORY_APPEARANCE));

                appearance.addEntry(entryBuilder.startEnumSelector(
                                Text.translatable("option.fps_overlay.position"),
                                OverlayPosition.class,
                                config.appearance.position)
                                .setDefaultValue(OverlayPosition.TOP_LEFT)
                                .setTooltip(Text.translatable(TOOLTIP_POSITION))
                                .setSaveConsumer(value -> config.appearance.position = value)
                                .build());

                appearance.addEntry(entryBuilder.startEnumSelector(
                                Text.translatable("option.fps_overlay.style"),
                                ModConfig.OverlayStyle.class,
                                config.appearance.overlayStyle)
                                .setDefaultValue(ModConfig.OverlayStyle.MODERN)
                                .setTooltip(Text.translatable(TOOLTIP_OVERLAY_STYLE))
                                .setSaveConsumer(value -> config.appearance.overlayStyle = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("option.fps_overlay.showMemoryPercentage"),
                                                config.hud.showMemoryPercentage)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable(TOOLTIP_SHOW_MEMORY_PERCENTAGE))
                                .setYesNoTextSupplier(val -> val ? Text.literal("[ ON ]") : Text.literal("[ OFF ]"))
                                .setSaveConsumer(value -> config.hud.showMemoryPercentage = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("option.fps_overlay.compactMode"),
                                                config.appearance.compactMode)
                                .setDefaultValue(false)
                                .setTooltip(Text.translatable(TOOLTIP_COMPACT_MODE))
                                .setYesNoTextSupplier(val -> val ? Text.literal("[ ON ]") : Text.literal("[ OFF ]"))
                                .setSaveConsumer(value -> config.appearance.compactMode = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startSelector(Text.translatable("option.fps_overlay.updateInterval"),
                                                new Integer[] { 16, 33, 50, 100, 250, 500, 1000 },
                                                config.general.updateIntervalMs)
                                .setDefaultValue(250)
                                .setTooltip(Text.translatable(TOOLTIP_UPDATE_INTERVAL))
                                .setNameProvider(val -> {
                                        switch (val) {
                                                case 16:
                                                        return Text.translatable("enum.fps_overlay.update.16");
                                                case 33:
                                                        return Text.translatable("enum.fps_overlay.update.33");
                                                case 50:
                                                        return Text.translatable("enum.fps_overlay.update.50");
                                                case 100:
                                                        return Text.translatable("enum.fps_overlay.update.100");
                                                case 250:
                                                        return Text.translatable("enum.fps_overlay.update.250");
                                                case 500:
                                                        return Text.translatable("enum.fps_overlay.update.500");
                                                case 1000:
                                                        return Text.translatable("enum.fps_overlay.update.1000");
                                                default:
                                                        return Text.literal(val + " ms");
                                        }
                                })
                                .setSaveConsumer(value -> config.general.updateIntervalMs = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startSelector(Text.translatable("option.fps_overlay.scale"),
                                                new Float[] { 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 3.0f },
                                                config.appearance.scale)
                                .setDefaultValue(1.0f)
                                .setTooltip(Text.translatable(TOOLTIP_SCALE))
                                .setNameProvider(val -> {
                                        if (val == 0.5f)
                                                return Text.translatable("enum.fps_overlay.scale.tiny");
                                        if (val == 0.75f)
                                                return Text.translatable("enum.fps_overlay.scale.small");
                                        if (val == 1.0f)
                                                return Text.translatable("enum.fps_overlay.scale.normal");
                                        if (val == 1.25f)
                                                return Text.translatable("enum.fps_overlay.scale.large");
                                        if (val == 1.5f)
                                                return Text.translatable("enum.fps_overlay.scale.very_large");
                                        if (val == 1.75f)
                                                return Text.translatable("enum.fps_overlay.scale.huge");
                                        if (val == 2.0f)
                                                return Text.translatable("enum.fps_overlay.scale.giant");
                                        if (val == 3.0f)
                                                return Text.translatable("enum.fps_overlay.scale.massive");
                                        return Text.literal(val + "x");
                                })
                                .setSaveConsumer(value -> config.appearance.scale = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startSelector(Text.translatable("option.fps_overlay.padding"),
                                                new Integer[] { 0, 2, 4, 5, 8, 10, 12, 15, 20, 25, 30 },
                                                config.appearance.padding)
                                .setDefaultValue(5)
                                .setTooltip(Text.translatable(TOOLTIP_PADDING))
                                .setNameProvider(val -> {
                                        switch (val) {
                                                case 0:
                                                        return Text.translatable("enum.fps_overlay.padding.0");
                                                case 2:
                                                        return Text.translatable("enum.fps_overlay.padding.2");
                                                case 4:
                                                        return Text.translatable("enum.fps_overlay.padding.4");
                                                case 5:
                                                        return Text.translatable("enum.fps_overlay.padding.5");
                                                case 8:
                                                        return Text.translatable("enum.fps_overlay.padding.8");
                                                case 10:
                                                        return Text.translatable("enum.fps_overlay.padding.10");
                                                case 12:
                                                        return Text.translatable("enum.fps_overlay.padding.12");
                                                case 15:
                                                        return Text.translatable("enum.fps_overlay.padding.15");
                                                case 20:
                                                        return Text.translatable("enum.fps_overlay.padding.20");
                                                case 25:
                                                        return Text.translatable("enum.fps_overlay.padding.25");
                                                case 30:
                                                        return Text.translatable("enum.fps_overlay.padding.30");
                                                default:
                                                        return Text.literal(val.toString());
                                        }
                                })
                                .setSaveConsumer(value -> config.appearance.padding = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("option.fps_overlay.showBackground"),
                                                config.appearance.showBackground)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable(TOOLTIP_SHOW_BACKGROUND))
                                .setYesNoTextSupplier(val -> val ? Text.literal("[ ON ]") : Text.literal("[ OFF ]"))
                                .setSaveConsumer(value -> config.appearance.showBackground = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startSelector(Text.translatable("option.fps_overlay.backgroundOpacity"),
                                                new Integer[] { 0, 25, 50, 100, 150, 180, 200, 225, 255 },
                                                config.appearance.backgroundOpacity)
                                .setDefaultValue(180)
                                .setTooltip(Text.translatable(TOOLTIP_BACKGROUND_OPACITY))
                                .setNameProvider(val -> {
                                        switch (val) {
                                                case 0:
                                                        return Text.translatable("enum.fps_overlay.opacity.0");
                                                case 25:
                                                        return Text.translatable("enum.fps_overlay.opacity.25");
                                                case 50:
                                                        return Text.translatable("enum.fps_overlay.opacity.50");
                                                case 100:
                                                        return Text.translatable("enum.fps_overlay.opacity.100");
                                                case 150:
                                                        return Text.translatable("enum.fps_overlay.opacity.150");
                                                case 180:
                                                        return Text.translatable("enum.fps_overlay.opacity.180");
                                                case 200:
                                                        return Text.translatable("enum.fps_overlay.opacity.200");
                                                case 225:
                                                        return Text.translatable("enum.fps_overlay.opacity.225");
                                                case 255:
                                                        return Text.translatable("enum.fps_overlay.opacity.255");
                                                default:
                                                        return Text.literal(val.toString());
                                        }
                                })
                                .setSaveConsumer(value -> config.appearance.backgroundOpacity = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startSelector(Text.translatable("option.fps_overlay.textColor"),
                                                new String[] { "#FFFFFF", "#FF0000", "#00FF00", "#0000FF", "#FFFF00",
                                                                "#FF00FF", "#00FFFF", "#000000", "#AAAAAA" },
                                                config.appearance.textColorHex)
                                .setDefaultValue("#FFFFFF")
                                .setTooltip(Text.translatable(TOOLTIP_TEXT_COLOR))
                                .setNameProvider(val -> {
                                        switch (val) {
                                                case "#FFFFFF":
                                                        return Text.translatable("color.fps_overlay.white");
                                                case "#FF0000":
                                                        return Text.translatable("color.fps_overlay.red");
                                                case "#00FF00":
                                                        return Text.translatable("color.fps_overlay.green");
                                                case "#0000FF":
                                                        return Text.translatable("color.fps_overlay.blue");
                                                case "#FFFF00":
                                                        return Text.translatable("color.fps_overlay.yellow");
                                                case "#FF00FF":
                                                        return Text.translatable("color.fps_overlay.magenta");
                                                case "#00FFFF":
                                                        return Text.translatable("color.fps_overlay.cyan");
                                                case "#AAAAAA":
                                                        return Text.translatable("color.fps_overlay.gray");
                                                case "#000000":
                                                        return Text.translatable("color.fps_overlay.black");
                                                default:
                                                        return Text.literal(val);
                                        }
                                })
                                .setSaveConsumer(value -> config.appearance.textColorHex = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("option.fps_overlay.useTextShadow"),
                                                config.appearance.useTextShadow)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable(TOOLTIP_USE_TEXT_SHADOW))
                                .setYesNoTextSupplier(val -> val ? Text.literal("[ ON ]") : Text.literal("[ OFF ]"))
                                .setSaveConsumer(value -> config.appearance.useTextShadow = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startBooleanToggle(Text.translatable("option.fps_overlay.useAdaptiveColors"),
                                                config.appearance.useAdaptiveColors)
                                .setDefaultValue(true)
                                .setTooltip(Text.translatable(TOOLTIP_USE_ADAPTIVE_COLORS))
                                .setYesNoTextSupplier(val -> val ? Text.literal("[ ON ]") : Text.literal("[ OFF ]"))
                                .setSaveConsumer(value -> config.appearance.useAdaptiveColors = value)
                                .build());

                return builder.build();
        }
}