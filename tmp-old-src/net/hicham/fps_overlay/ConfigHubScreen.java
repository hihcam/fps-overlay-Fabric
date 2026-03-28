package net.hicham.fps_overlay;

import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public class ConfigHubScreen extends class_437 {
    private final class_437 parent;

    public ConfigHubScreen(class_437 parent) {
        super(class_2561.method_43471("screen.fps_overlay.config_hub"));
        this.parent = parent;
    }

    @Override
    protected void method_25426() {
        int centerX = this.field_22789 / 2;
        int startY = this.field_22790 / 2 - 48;
        int buttonWidth = 180;
        int buttonHeight = 20;
        int gap = 24;

        method_37063(class_4185.method_46430(class_2561.method_43471("button.fps_overlay.open_settings"), button -> {
            if (field_22787 != null) {
                field_22787.method_1507(ConfigScreenFactory.createSettingsScreen(this));
            }
        }).method_46434(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).method_46431());

        method_37063(class_4185.method_46430(class_2561.method_43471("button.fps_overlay.edit_position"), button -> {
            if (field_22787 != null) {
                field_22787.method_1507(new PositionEditorScreen(this, ConfigManager.getConfig()));
            }
        }).method_46434(centerX - buttonWidth / 2, startY + gap, buttonWidth, buttonHeight).method_46431());

        method_37063(class_4185.method_46430(class_2561.method_43471("button.fps_overlay.arrange_metrics"), button -> {
            if (field_22787 != null) {
                field_22787.method_1507(new MetricOrderScreen(this, ConfigManager.getConfig()));
            }
        }).method_46434(centerX - buttonWidth / 2, startY + (gap * 2), buttonWidth, buttonHeight).method_46431());

        method_37063(class_4185.method_46430(class_2561.method_43471("gui.done"), button -> method_25419())
                .method_46434(centerX - buttonWidth / 2, startY + (gap * 4), buttonWidth, buttonHeight).method_46431());
    }

    @Override
    public void method_25419() {
        if (field_22787 != null) {
            field_22787.method_1507(parent);
        }
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        context.method_25296(0, 0, this.field_22789, this.field_22790, 0xC0182028, 0xD010141A);
        super.method_25394(context, mouseX, mouseY, delta);
        context.method_27534(field_22793, field_22785, this.field_22789 / 2, this.field_22790 / 2 - 84, 0xFFFFFFFF);
        context.method_27534(field_22793, class_2561.method_43471("text.fps_overlay.config_hub_hint"),
                this.field_22789 / 2, this.field_22790 / 2 - 68, 0xFFB7C6D1);
    }
}
