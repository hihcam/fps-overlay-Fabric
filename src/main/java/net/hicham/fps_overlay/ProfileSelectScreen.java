package net.hicham.fps_overlay;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ProfileSelectScreen extends Screen {
    private static final int LIST_WIDTH = 300;
    private static final int ROW_HEIGHT = 24;
    private static final int LIST_TOP = 72;
    private static final int NO_PROFILE_INDEX = -1;
    private static final int TEXT_MUTED = 0xFFB7C6D1;
    private static final int TEXT_SELECTED = 0xFF4DE89A;

    private final Screen parent;
    private final ModConfig config;
    private final Consumer<String> onSelect;
    private final List<String> profileNames;
    private final List<Button> optionButtons = new ArrayList<>();

    private EditBox searchBox;
    private double scrollAmount = 0;
    private int maxScroll = 0;

    public ProfileSelectScreen(Screen parent, ModConfig config, Consumer<String> onSelect) {
        super(Component.translatable("screen.fps_overlay.select_profile"));
        this.parent = parent;
        this.config = config;
        this.onSelect = onSelect;
        this.profileNames = new ArrayList<>(config.getProfileNames());
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int fieldWidth = Math.min(LIST_WIDTH, this.width - 32);
        int fieldX = centerX - (fieldWidth / 2);

        searchBox = addRenderableWidget(new EditBox(this.font, fieldX, 42, fieldWidth, 20,
                Component.translatable("text.fps_overlay.profile_search")));
        searchBox.setHint(Component.translatable("text.fps_overlay.profile_search_hint"));
        searchBox.setResponder(ignored -> refreshOptions());
        setInitialFocus(searchBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(centerX - 75, this.height - 28, 150, 20)
                .build());

        refreshOptions();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollAmount -= verticalAmount * 18;
        clampScroll();
        layoutOptions();
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xD0121820, 0xE0091016);
        guiGraphics.centeredText(font, title, this.width / 2, 16, 0xFFFFFFFF);
        guiGraphics.centeredText(font, Component.translatable("text.fps_overlay.profile_select_hint"),
                this.width / 2, 29, TEXT_MUTED);

        int left = getListLeft();
        int top = LIST_TOP - 4;
        int right = left + getListWidth();
        int bottom = getListBottom() + 4;
        guiGraphics.fill(left - 2, top, right + 2, bottom, 0xA9141B22);
        drawBorder(guiGraphics, left - 2, top, right + 2, bottom, 0xFF334A5C);

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void refreshOptions() {
        for (Button button : optionButtons) {
            removeWidget(button);
        }
        optionButtons.clear();

        optionButtons.add(addRenderableWidget(createOptionButton(NO_PROFILE_INDEX,
                Component.literal(Component.translatable("button.fps_overlay.no_profile").getString()))));

        String query = searchBox == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        for (int i = 0; i < profileNames.size(); i++) {
            String profileName = profileNames.get(i);
            if (!query.isEmpty() && !profileName.toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }
            optionButtons.add(addRenderableWidget(createOptionButton(i, Component.literal(profileName))));
        }

        updateMaxScroll();
        clampScroll();
        layoutOptions();
    }

    private Button createOptionButton(int profileIndex, Component label) {
        String profileName = profileIndex == NO_PROFILE_INDEX ? "" : profileNames.get(profileIndex);
        boolean selected = profileName.equals(config.selectedProfile);
        Component text = selected
                ? Component.literal(label.getString() + "  -  "
                        + Component.translatable("button.fps_overlay.profile_selected").getString())
                        .withColor(TEXT_SELECTED)
                : label;

        return Button.builder(text, button -> onSelect.accept(profileName))
                .bounds(0, 0, getListWidth(), 20)
                .build();
    }

    private void layoutOptions() {
        int x = getListLeft();
        int top = LIST_TOP;
        int bottom = getListBottom();

        for (int i = 0; i < optionButtons.size(); i++) {
            Button button = optionButtons.get(i);
            int y = (int) (top + (i * ROW_HEIGHT) - scrollAmount);
            boolean visible = y + ROW_HEIGHT > top && y < bottom;
            button.setX(x);
            button.setY(visible ? y : -200);
            button.setWidth(getListWidth());
            button.visible = visible;
        }
    }

    private int getListLeft() {
        return (this.width - getListWidth()) / 2;
    }

    private int getListWidth() {
        return Math.max(160, Math.min(LIST_WIDTH, this.width - 32));
    }

    private int getListBottom() {
        return Math.max(LIST_TOP + ROW_HEIGHT, this.height - 42);
    }

    private void updateMaxScroll() {
        int contentHeight = optionButtons.size() * ROW_HEIGHT;
        int viewportHeight = getListBottom() - LIST_TOP;
        maxScroll = Math.max(0, contentHeight - viewportHeight);
    }

    private void clampScroll() {
        scrollAmount = Math.max(0, Math.min(scrollAmount, maxScroll));
    }

    private void drawBorder(GuiGraphicsExtractor guiGraphics, int x1, int y1, int x2, int y2, int color) {
        guiGraphics.fill(x1, y1, x2, y1 + 1, color);
        guiGraphics.fill(x1, y2 - 1, x2, y2, color);
        guiGraphics.fill(x1, y1, x1 + 1, y2, color);
        guiGraphics.fill(x2 - 1, y1, x2, y2, color);
    }
}
