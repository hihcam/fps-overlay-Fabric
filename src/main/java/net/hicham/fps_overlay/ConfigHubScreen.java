package net.hicham.fps_overlay;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigHubScreen extends Screen {
    private static final int COL_BG_SCREEN = 0xFF0D1117;
    private static final int COL_PANEL_BG = 0xBB141B22;
    private static final int COL_BORDER = 0xFF2A3A4A;
    private static final int TEXT_PRIMARY = 0xFFE8EEF2;
    private static final int TEXT_HEADER = 0xFFA8C0CE;
    private static final int TEXT_MUTED = 0xFF7A9AB0;
    private static final int TEXT_ACCENT = 0xFF5BBDE4;

    private final Screen parent;

    public ConfigHubScreen(Screen parent) {
        super(Component.translatable("screen.fps_overlay.config_hub"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int gap = 26;
        int startY = this.height / 2 - 52;

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.open_settings"), button -> {
            if (minecraft != null) {
                minecraft.setScreenAndShow(ConfigScreenFactory.createSettingsScreen(this));
            }
        }).bounds(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.edit_position"), button -> {
            if (minecraft != null) {
                minecraft.setScreenAndShow(new PositionEditorScreen(this, ConfigManager.getConfig()));
            }
        }).bounds(centerX - buttonWidth / 2, startY + gap, buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.arrange_metrics"), button -> {
            if (minecraft != null) {
                minecraft.setScreenAndShow(new MetricOrderScreen(this, ConfigManager.getConfig()));
            }
        }).bounds(centerX - buttonWidth / 2, startY + (gap * 2), buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("button.fps_overlay.keybinds"), button -> {
            if (minecraft != null) {
                minecraft.setScreenAndShow(new KeybindSettingsScreen(this));
            }
        }).bounds(centerX - buttonWidth / 2, startY + (gap * 3), buttonWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(centerX - buttonWidth / 2, startY + (gap * 4) + 6, buttonWidth, buttonHeight).build());
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, COL_BG_SCREEN);

        ModConfig config = ConfigManager.getConfig();
        OverlayRenderer.renderPreview(guiGraphics, minecraft, config, this.width, this.height);

        int centerX = this.width / 2;
        int cardWidth = 232;
        int cardHeight = 176;
        int cardLeft = centerX - cardWidth / 2;
        int cardTop = this.height / 2 - 64;
        int cardRight = cardLeft + cardWidth;
        int cardBottom = cardTop + cardHeight;

        guiGraphics.fill(cardLeft, cardTop, cardRight, cardBottom, COL_PANEL_BG);
        drawBorder(guiGraphics, cardLeft, cardTop, cardRight, cardBottom, COL_BORDER);
        drawCornerBrackets(guiGraphics, cardLeft, cardTop, cardWidth, cardHeight, 8, TEXT_ACCENT);

        guiGraphics.text(font, title, centerX - font.width(title) / 2, cardTop - 36, TEXT_PRIMARY, true);
        Component hint = Component.translatable("text.fps_overlay.config_hub_hint");
        guiGraphics.text(font, hint, centerX - font.width(hint) / 2, cardTop - 20, TEXT_HEADER, false);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        String profileName = (config.selectedProfile == null || config.selectedProfile.isBlank())
                ? "Default"
                : config.selectedProfile;
        Component profileLine = Component.translatable("text.fps_overlay.config_hub_profile_line", profileName);
        guiGraphics.text(font, profileLine, centerX - font.width(profileLine) / 2, cardBottom + 12, TEXT_MUTED, false);
    }

    private void drawBorder(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    private void drawCornerBrackets(GuiGraphicsExtractor g, int bx, int byt, int bw, int bh, int cs, int color) {
        int byb = byt + bh;
        // top-left
        g.fill(bx, byt, bx + cs, byt + 1, color);
        g.fill(bx, byt, bx + 1, byt + cs, color);
        // top-right
        g.fill(bx + bw - cs, byt, bx + bw, byt + 1, color);
        g.fill(bx + bw - 1, byt, bx + bw, byt + cs, color);
        // bottom-left
        g.fill(bx, byb - cs, bx + 1, byb, color);
        g.fill(bx, byb - 1, bx + cs, byb, color);
        // bottom-right
        g.fill(bx + bw - 1, byb - cs, bx + bw, byb, color);
        g.fill(bx + bw - cs, byb - 1, bx + bw, byb, color);
    }
}
