package com.mikitellurium.superflatbiomeextension.client;

import com.mikitellurium.superflatbiomeextension.util.MouseUtils;
import com.mikitellurium.superflatbiomeextension.worldgen.CustomFlatGeneratorConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipState;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class EditCustomFlatLevelScreen extends Screen {
    private static final Text TITLE = Text.translatable("createWorld.superflatbiomeextension.customize.flat.title");
    private final CreateWorldScreen parent;
    private final ConfigStorage config;
    private final Consumer<CustomFlatGeneratorConfig> configConsumer;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private SettingsListWidget settingsListWidget;

    public EditCustomFlatLevelScreen(CreateWorldScreen parent, CustomFlatGeneratorConfig config, Consumer<CustomFlatGeneratorConfig> configConsumer) {
        super(TITLE);
        this.parent = parent;
        this.config = new ConfigStorage(config);
        this.configConsumer = configConsumer;
    }

    @Override
    protected void init() {
        this.layout.addHeader(TITLE, this.textRenderer);
        this.settingsListWidget = this.layout.addBody(new SettingsListWidget());
        DirectionalLayoutWidget directionalLayoutWidget = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.configConsumer.accept(this.getConfig());
            this.close();
        }).build());
        directionalLayoutWidget.add(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> this.close()).build());
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
        if (this.settingsListWidget != null) {
            this.settingsListWidget.position(this.width, this.layout);
        }
    }

    public CustomFlatGeneratorConfig getConfig() {
        return this.config.getConfig();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    public abstract static class AbstractSettingWidget extends ElementListWidget.Entry<AbstractSettingWidget> {}

    public class SettingsListWidget extends ElementListWidget<AbstractSettingWidget> {
        public SettingsListWidget() {
            super(MinecraftClient.getInstance(), EditCustomFlatLevelScreen.this.width, EditCustomFlatLevelScreen.this.layout.getContentHeight(), EditCustomFlatLevelScreen.this.layout.getHeaderHeight(), 24);
            this.addEntry(new SettingWidget<>(
                    Text.translatable("createWorld.superflatbiomeextension.customize.flat.generate_water"),
                    CyclingButtonWidget.onOffBuilder(config.settings[0]).omitKeyText(),
                    Tooltip.of(Text.translatable("createWorld.superflatbiomeextension.customize.flat.tooltip.generate_water")),
                    (button, newValue) -> config.settings[0] = newValue
            ));
            this.addEntry(new SettingWidget<>(
                    Text.translatable("createWorld.superflatbiomeextension.customize.flat.has_features"),
                    CyclingButtonWidget.onOffBuilder(config.settings[1]).omitKeyText(),
                    Tooltip.of(Text.translatable("createWorld.superflatbiomeextension.customize.flat.tooltip.has_features")),
                    (button, newValue) -> config.settings[1] = newValue
            ));
            this.addEntry(new SettingWidget<>(
                    Text.translatable("createWorld.superflatbiomeextension.customize.flat.has_structures"),
                    CyclingButtonWidget.onOffBuilder(config.settings[2]).omitKeyText(),
                    Tooltip.of(Text.translatable("createWorld.superflatbiomeextension.customize.flat.tooltip.has_structures")),
                    (button, newValue) -> config.settings[2] = newValue
            ));
            this.addEntry(new SettingWidget<>(
                    Text.translatable("createWorld.superflatbiomeextension.customize.flat.has_lava_lakes"),
                    CyclingButtonWidget.onOffBuilder(config.settings[3]).omitKeyText(),
                    Tooltip.of(Text.translatable("createWorld.superflatbiomeextension.customize.flat.tooltip.has_lava_lakes")),
                    (button, newValue) -> config.settings[3] = newValue
            ));
            this.addEntry(new SettingWidget<>(
                    Text.translatable("createWorld.superflatbiomeextension.customize.flat.generate_ores"),
                    CyclingButtonWidget.onOffBuilder(config.settings[4]).omitKeyText(),
                    Tooltip.of(Text.translatable("createWorld.superflatbiomeextension.customize.flat.tooltip.generate_ores")),
                    (button, newValue) -> config.settings[4] = newValue
            ));
        }
    }

    public class SettingWidget<T> extends AbstractSettingWidget {
        private final List<OrderedText> description;
        private final CyclingButtonWidget<T> button;
        private final TooltipState tooltip = new TooltipState();

        public SettingWidget(Text description, CyclingButtonWidget.Builder<T> button, Tooltip tooltip, CyclingButtonWidget.UpdateCallback<T> callback) {
            this.description = textRenderer.wrapLines(description, 145);
            this.button = button.build(0, 0, 45, 20, Text.empty(), callback);
            this.tooltip.setTooltip(tooltip);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(button);
        }

        @Override
        public List<? extends Element> children() {
            return List.of(button);
        }

        private void drawText(DrawContext context, int x, int y, int height) {
            int yPos;
            if (this.description.size() == 1) {
                yPos = y + (height - EditCustomFlatLevelScreen.this.textRenderer.fontHeight) / 2;
                context.drawTextWithShadow(textRenderer, this.description.getFirst(), x, yPos, -1);
            } else {
                yPos = y + (height - EditCustomFlatLevelScreen.this.textRenderer.fontHeight * 2) / 2;
                context.drawTextWithShadow(textRenderer, this.description.get(0), x, yPos, -1);
                context.drawTextWithShadow(textRenderer, this.description.get(1), x, yPos + textRenderer.fontHeight + 1, -1);
            }
        }

        private void drawOutline(DrawContext context, int x, int y, int width, int height) {
            context.drawHorizontalLine(x, x + width, y, -1);
            context.drawHorizontalLine(x, x + width, y + height - 1, -1);
            context.drawVerticalLine(x, y, y + height - 1, -1);
            context.drawVerticalLine(x + width, y, y + height - 1, -1);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {
            this.drawText(context, x, y, entryHeight);
            button.setX(x + entryWidth - button.getWidth());
            button.setY(y);
            button.render(context, mouseX, mouseY, tickProgress);
            this.drawOutline(context, x, y, entryWidth, entryHeight);
            this.tooltip.render(this.isHovered(mouseX, mouseY, x, y, entryWidth, entryHeight), this.isFocused(), this.getNavigationFocus());
        }

        public boolean isHovered(int mouseX, int mouseY, int x, int y, int width, int height) {
            return MouseUtils.isAboveArea(mouseX, mouseY, x, y, width, height);
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
            this.settings[3] = config.hasLavaLakes();
            this.settings[4] = config.generateOres();
        }

        public CustomFlatGeneratorConfig getConfig() {
            return new CustomFlatGeneratorConfig(config.getGenerationShapeConfig(), config.getLayerCount(),
                    this.settings[0], this.settings[1], this.settings[2], this.settings[3], this.settings[4]
            );
        }
    }
}
