package net.hicham.fps_overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;

public class KeybindSettingsScreen extends Screen {
    private final Screen parent;
    KeybindList keybindList;
    private KeybindList.Entry capturedEntry;
    private Button resetAllButton;
    private Button doneButton;

    public KeybindSettingsScreen(Screen parent) {
        super(Component.translatable("screen.fps_overlay.keybinds"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.keybindList = addRenderableWidget(new KeybindList(this.minecraft, this.width, this.height - 80, 40, 24, this));

        this.resetAllButton = addRenderableWidget(
                Button.builder(Component.translatable("button.fps_overlay.reset_all_metrics"), button -> resetAll())
                        .bounds(this.width / 2 - 155, this.height - 28, 150, 20)
                        .build());

        this.doneButton = addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), button -> onClose())
                        .bounds(this.width / 2 + 5, this.height - 28, 150, 20)
                        .build());
    }

    public KeybindList.Entry getCapturedEntry() {
        return this.capturedEntry;
    }

    public void startCapture(KeybindList.Entry entry) {
        this.capturedEntry = entry;
        entry.updateButtonText();
    }

    public boolean isConflicting(KeyMapping targetMapping) {
        if (targetMapping.isUnbound()) {
            return false;
        }
        for (KeyMapping other : this.minecraft.options.keyMappings) {
            if (other != targetMapping && !other.isUnbound() && other.same(targetMapping)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.capturedEntry != null) {
            KeyMapping keyMapping = this.capturedEntry.getKeyMapping();
            if (event.key() != GLFW.GLFW_KEY_ESCAPE) {
                InputConstants.Key newKey = InputConstants.Type.KEYSYM.getOrCreate(event.key());
                keyMapping.setKey(newKey);
                KeyMapping.resetMapping();
                this.minecraft.options.save();
            }
            this.capturedEntry = null;
            refreshAllButtons();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (this.capturedEntry != null) {
            KeyMapping keyMapping = this.capturedEntry.getKeyMapping();
            InputConstants.Key newKey = InputConstants.Type.MOUSE.getOrCreate(click.button());
            keyMapping.setKey(newKey);
            KeyMapping.resetMapping();
            this.minecraft.options.save();
            this.capturedEntry = null;
            refreshAllButtons();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    public void refreshAllButtons() {
        if (this.keybindList != null) {
            for (KeybindList.Entry entry : this.keybindList.children()) {
                entry.updateButtonText();
            }
        }
    }

    private void resetAll() {
        for (KeyMapping keyMapping : FpsOverlayMod.getKeyMappings()) {
            keyMapping.setKey(keyMapping.getDefaultKey());
        }
        KeyMapping.resetMapping();
        this.minecraft.options.save();
        refreshAllButtons();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, 0, this.width, this.height, 0xC0182028, 0xD010141A);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.text(font, title, this.width / 2 - font.width(title) / 2, 8, 0xFFFFFFFF, false);
        Component hint = Component.translatable("text.fps_overlay.keybinds_hint");
        g.text(font, hint, this.width / 2 - font.width(hint) / 2, 22, 0xFFB7C6D1, false);
    }
}
