package net.hicham.fps_overlay;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ProfileNameScreen extends Screen {
    private final Screen parent;
    private final Consumer<String> onConfirm;
    private final String initialValue;
    private EditBox nameBox;
    private Button confirmButton;

    public ProfileNameScreen(Screen parent, Component title, String initialValue, Consumer<String> onConfirm) {
        super(title);
        this.parent = parent;
        this.initialValue = initialValue == null ? "" : initialValue;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int fieldWidth = 220;
        int fieldX = centerX - (fieldWidth / 2);
        int fieldY = this.height / 2 - 10;

        nameBox = addRenderableWidget(new EditBox(this.font, fieldX, fieldY, fieldWidth, 20,
                Component.translatable("text.fps_overlay.profile_name")));
        nameBox.setMaxLength(40);
        nameBox.setValue(initialValue);
        nameBox.setHint(Component.translatable("text.fps_overlay.profile_name_hint"));
        nameBox.setResponder(value -> updateConfirmState());
        setInitialFocus(nameBox);

        confirmButton = addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> confirm())
                .bounds(centerX - 110, fieldY + 30, 100, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(centerX + 10, fieldY + 30, 100, 20)
                .build());

        updateConfirmState();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xD0121820, 0xE0091016);

        int panelX = this.width / 2 - 140;
        int panelY = this.height / 2 - 42;
        int panelWidth = 280;
        int panelHeight = 96;
        int background = 0xD019232D;
        int border = 0xFF334A5C;

        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, background);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, border);
        guiGraphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, border);
        guiGraphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, border);
        guiGraphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, border);

        guiGraphics.centeredText(font, title, this.width / 2, panelY + 10, 0xFFFFFFFF);
        guiGraphics.centeredText(font, Component.translatable("text.fps_overlay.profile_name_prompt"),
                this.width / 2, panelY + 24, 0xFFB7C6D1);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void confirm() {
        if (confirmButton == null || !confirmButton.active) {
            return;
        }
        onConfirm.accept(nameBox.getValue().trim());
    }

    private void updateConfirmState() {
        if (confirmButton != null) {
            confirmButton.active = nameBox != null && !nameBox.getValue().trim().isEmpty();
        }
    }
}
