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
        private static final String TOOLTIP_SHOW_PING = "tooltip.fps_overlay.showPing";
        private static final String TOOLTIP_POSITION = "tooltip.fps_overlay.position";
        private static final String TOOLTIP_SHOW_BACKGROUND = "tooltip.fps_overlay.showBackground";
        private static final String TOOLTIP_BACKGROUND_OPACITY = "tooltip.fps_overlay.backgroundOpacity";

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
                                Text.translatable("option.fps_overlay.overlay_style"),
                                OverlayStyle.class,
                                config.appearance.overlayStyle)
                                .setDefaultValue(OverlayStyle.NAVBAR)
                                .setSaveConsumer(value -> config.appearance.overlayStyle = value)
                                .build());

                appearance.addEntry(entryBuilder.startEnumSelector(
                                Text.translatable("option.fps_overlay.position"),
                                OverlayPosition.class,
                                config.appearance.position)
                                .setDefaultValue(OverlayPosition.TOP_LEFT)
                                .setTooltip(Text.translatable(TOOLTIP_POSITION))
                                .setSaveConsumer(value -> config.appearance.position = value)
                                .build());

                appearance.addEntry(entryBuilder
                                .startSelector(Text.translatable("option.fps_overlay.hudScale"),
                                                new Float[] { 0.65f, 0.85f, 1.0f },
                                                config.appearance.hudScale)
                                .setDefaultValue(0.65f)
                                .setNameProvider(val -> {
                                        if (val == 0.65f)
                                                return Text.translatable("enum.fps_overlay.scale.small");
                                        if (val == 0.85f)
                                                return Text.translatable("enum.fps_overlay.scale.normal");
                                        if (val == 1.0f)
                                                return Text.translatable("enum.fps_overlay.scale.big");
                                        return Text.literal(val.toString());
                                })
                                .setSaveConsumer(value -> config.appearance.hudScale = value)
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

                return builder.build();
        }
}