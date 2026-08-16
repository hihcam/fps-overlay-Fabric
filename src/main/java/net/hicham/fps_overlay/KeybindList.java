package net.hicham.fps_overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;

public class KeybindList extends ContainerObjectSelectionList<KeybindList.Entry> {
    public KeybindList(Minecraft minecraft, int width, int height, int y, int itemHeight, KeybindSettingsScreen screen) {
        super(minecraft, width, height, y, itemHeight);
        for (KeyMapping keyMapping : FpsOverlayMod.getKeyMappings()) {
            this.addEntry(new Entry(keyMapping, screen));
        }
    }

    @Override
    public int getRowWidth() {
        return 400;
    }

    @Override
    protected int scrollBarX() {
        return this.getRowRight() + 15;
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final KeyMapping keyMapping;
        private final KeybindSettingsScreen screen;
        private final Button changeButton;
        private final Button unbindButton;
        private final Button resetButton;

        public Entry(KeyMapping keyMapping, KeybindSettingsScreen screen) {
            this.keyMapping = keyMapping;
            this.screen = screen;

            this.changeButton = Button.builder(keyMapping.getTranslatedKeyMessage(), b -> {
                screen.startCapture(this);
            }).bounds(0, 0, 100, 20).build();

            this.unbindButton = Button.builder(Component.translatable("button.fps_overlay.unbind"), b -> {
                keyMapping.setKey(InputConstants.UNKNOWN);
                KeyMapping.resetMapping();
                Minecraft.getInstance().options.save();
                screen.refreshAllButtons();
            }).bounds(0, 0, 60, 20).build();

            this.resetButton = Button.builder(Component.translatable("button.fps_overlay.reset"), b -> {
                keyMapping.setKey(keyMapping.getDefaultKey());
                KeyMapping.resetMapping();
                Minecraft.getInstance().options.save();
                screen.refreshAllButtons();
            }).bounds(0, 0, 60, 20).build();

            this.updateButtonText();
        }

        public KeyMapping getKeyMapping() {
            return this.keyMapping;
        }

        public void updateButtonText() {
            if (screen.getCapturedEntry() == this) {
                changeButton.setMessage(Component.translatable("text.fps_overlay.press_key").withStyle(ChatFormatting.YELLOW));
            } else {
                Component msg = keyMapping.getTranslatedKeyMessage();
                if (screen.isConflicting(keyMapping)) {
                    changeButton.setMessage(msg.copy().withStyle(ChatFormatting.RED));
                } else {
                    changeButton.setMessage(msg);
                }
            }
            resetButton.active = !keyMapping.isDefault();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(changeButton, unbindButton, resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(changeButton, unbindButton, resetButton);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor g, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int rowX = getX();
            int rowY = getY();
            int rowWidth = getWidth();
            int rowHeight = getHeight();

            // Draw the label
            Component label = Component.translatable(keyMapping.getName());
            int textColor = screen.isConflicting(keyMapping) ? 0xFFFF5555 : 0xFFFFFFFF;
            g.text(Minecraft.getInstance().font, label, rowX + 5, rowY + (rowHeight - 8) / 2, textColor, false);

            // Position buttons
            changeButton.setX(rowX + rowWidth - 230);
            changeButton.setY(rowY + (rowHeight - changeButton.getHeight()) / 2);

            unbindButton.setX(rowX + rowWidth - 125);
            unbindButton.setY(rowY + (rowHeight - unbindButton.getHeight()) / 2);

            resetButton.setX(rowX + rowWidth - 60);
            resetButton.setY(rowY + (rowHeight - resetButton.getHeight()) / 2);

            // Draw buttons
            changeButton.extractRenderState(g, mouseX, mouseY, partialTick);
            unbindButton.extractRenderState(g, mouseX, mouseY, partialTick);
            resetButton.extractRenderState(g, mouseX, mouseY, partialTick);
        }
    }
}
