package com.mikitellurium.superflatbiomeextension.client;

import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatGeneratorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.Consumer;

public class EditCustomFlatLevelScreen extends Screen {
    private static final Component TITLE = Component.translatable("createWorld.superflatbiomeextension.customize.flat.title");
    private final CreateWorldScreen parent;
    private final ConfigStorage config;
    private final Consumer<CustomFlatGeneratorConfig> configConsumer;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private SettingsListWidget settingsListWidget;

    public EditCustomFlatLevelScreen(CreateWorldScreen parent, CustomFlatGeneratorConfig config, Consumer<CustomFlatGeneratorConfig> configConsumer) {
        super(TITLE);
        this.parent = parent;
        this.config = new ConfigStorage(config);
        this.configConsumer = configConsumer;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        this.settingsListWidget = this.layout.addToContents(new SettingsListWidget());
        LinearLayout directionalLayoutWidget = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        directionalLayoutWidget.addChild(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.configConsumer.accept(this.getConfig());
            this.onClose();
        }).build());
        directionalLayoutWidget.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.onClose()).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.settingsListWidget != null) {
            this.settingsListWidget.updateSize(this.width, this.layout);
        }
        this.layout.arrangeElements();
    }

    public CustomFlatGeneratorConfig getConfig() {
        return this.config.getConfig();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public abstract static class AbstractSettingWidget extends ObjectSelectionList.Entry<AbstractSettingWidget> {}

    public class SettingsListWidget extends ObjectSelectionList<AbstractSettingWidget> {
        public SettingsListWidget() {
            super(Minecraft.getInstance(), EditCustomFlatLevelScreen.this.width, EditCustomFlatLevelScreen.this.layout.getContentHeight(), EditCustomFlatLevelScreen.this.layout.getHeaderHeight(), 24);
            this.addEntry(new SettingWidget<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.flat.generate_water"),
                    CycleButton.onOffBuilder(config.settings[0]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.flat.tooltip.generate_water")),
                    (button, newValue) -> config.settings[0] = newValue
            ));
            this.addEntry(new SettingWidget<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.flat.has_features"),
                    CycleButton.onOffBuilder(config.settings[1]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.flat.tooltip.has_features")),
                    (button, newValue) -> config.settings[1] = newValue
            ));
            this.addEntry(new SettingWidget<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.flat.has_structures"),
                    CycleButton.onOffBuilder(config.settings[2]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.flat.tooltip.has_structures")),
                    (button, newValue) -> config.settings[2] = newValue
            ));
            this.addEntry(new SettingWidget<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.flat.has_lakes"),
                    CycleButton.onOffBuilder(config.settings[3]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.flat.tooltip.has_lakes")),
                    (button, newValue) -> config.settings[3] = newValue
            ));
            this.addEntry(new SettingWidget<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.flat.generate_ores"),
                    CycleButton.onOffBuilder(config.settings[4]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.flat.tooltip.generate_ores")),
                    (button, newValue) -> config.settings[4] = newValue
            ));
        }
    }

    public class SettingWidget<T> extends AbstractSettingWidget {
        private final List<FormattedCharSequence> description;
        private final CycleButton<T> button;

        public SettingWidget(Component description, CycleButton.Builder<T> button, Tooltip tooltip, CycleButton.OnValueChange<T> callback) {
            this.description = font.split(description, 145);
            this.button = button.create(0, 0, 45, 20, Component.empty(), callback);
            this.button.setTooltip(tooltip);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            int x = this.getContentX();
            int y = this.getContentY();
            int height = this.getContentHeight();
            int yPos;
            if (this.description.size() == 1) {
                yPos = y + (height - EditCustomFlatLevelScreen.this.font.lineHeight) / 2;
                graphics.text(EditCustomFlatLevelScreen.this.font, this.description.getFirst(), x, yPos, -1);
            } else {
                yPos = y + (height - EditCustomFlatLevelScreen.this.font.lineHeight * 2) / 2;
                graphics.text(EditCustomFlatLevelScreen.this.font, this.description.get(0), x, yPos, -1);
                graphics.text(EditCustomFlatLevelScreen.this.font, this.description.get(1), x, yPos + EditCustomFlatLevelScreen.this.font.lineHeight + 1, -1);
            }
            button.setX(x + this.getContentWidth() - button.getWidth());
            button.setY(y);
            button.extractRenderState(graphics, mouseX, mouseY, a);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return this.button.mouseClicked(event, doubleClick);
        }

        @Override
        public Component getNarration() {
            return this.button.getMessage();
        }
    }

    private static class ConfigStorage {
        private final CustomFlatGeneratorConfig config;
        private final boolean[] settings = new boolean[5];

        ConfigStorage(CustomFlatGeneratorConfig config) {
            this.config = config;
            this.settings[0] = config.generateWater();
            this.settings[1] = config.hasFeatures();
            this.settings[2] = config.hasStructures();
            this.settings[3] = config.hasLakes();
            this.settings[4] = config.generateOres();
        }

        public CustomFlatGeneratorConfig getConfig() {
            return new CustomFlatGeneratorConfig(config.getGenerationShapeConfig(), config.getLayerCount(),
                    this.settings[0], this.settings[1], this.settings[2], this.settings[3], this.settings[4],
                    config.getBiomes(), config.getDensityFunctions(), config.getNoises()
            );
        }
    }
}
