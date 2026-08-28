package com.mikitellurium.superflatbiomeextension.client;

import com.mikitellurium.superflatbiomeextension.registry.ModTags;
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
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.function.Consumer;

public class EditCustomFlatLevelScreen extends Screen {
    private static final Component TITLE = Component.translatable("createWorld.superflatbiomeextension.customize.title");
    private final CreateWorldScreen parent;
    private final ConfigStorage config;
    private final Consumer<ConfigStorage> configConsumer;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private SettingEntryList settingEntryList;

    public EditCustomFlatLevelScreen(CreateWorldScreen parent, CustomFlatGeneratorConfig config, Consumer<ConfigStorage> configConsumer) {
        super(TITLE);
        this.parent = parent;
        this.config = new ConfigStorage(config);
        this.configConsumer = configConsumer;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        this.settingEntryList = this.layout.addToContents(new SettingEntryList());
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.configConsumer.accept(config);
            this.onClose();
        }).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.onClose()).build());
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.settingEntryList != null) {
            this.settingEntryList.updateSize(this.width, this.layout);
        }
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public class SettingEntryList extends ObjectSelectionList<Entry> {
        public SettingEntryList() {
            super(Minecraft.getInstance(), EditCustomFlatLevelScreen.this.width, EditCustomFlatLevelScreen.this.layout.getContentHeight(), EditCustomFlatLevelScreen.this.layout.getHeaderHeight(), 24);
            this.addEntry(new SettingEntry<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.surface_fluid"),
                    CycleButton.onOffBuilder(config.settings[0]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.surface_fluid.tooltip")),
                    (button, newValue) -> config.settings[0] = newValue
            ));
            this.addEntry(new LabelEntry(Component.translatable("createWorld.superflatbiomeextension.customize.header.enabled_features").withColor(TextColor.GREEN)));
            this.addEntry(new SettingEntry<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.enabled_features.vegetation"),
                    CycleButton.onOffBuilder(config.settings[1]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.enabled_features.vegetation.tooltip")),
                    (button, newValue) -> config.settings[1] = newValue
            ));
            this.addEntry(new SettingEntry<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.enabled_features.terrain"),
                    CycleButton.onOffBuilder(config.settings[2]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.enabled_features.terrain.tooltip")),
                    (button, newValue) -> config.settings[2] = newValue
            ));
            this.addEntry(new SettingEntry<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.enabled_features.lakes"),
                    CycleButton.onOffBuilder(config.settings[3]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.enabled_features.lakes.tooltip")),
                    (button, newValue) -> config.settings[3] = newValue
            ));
            this.addEntry(new SettingEntry<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.enabled_features.ores"),
                    CycleButton.onOffBuilder(config.settings[4]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.enabled_features.ores.tooltip")),
                    (button, newValue) -> config.settings[4] = newValue
            ));
            this.addEntry(new SettingEntry<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.enabled_features.misc"),
                    CycleButton.onOffBuilder(config.settings[5]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.enabled_features.misc.tooltip")),
                    (button, newValue) -> config.settings[5] = newValue
            ));
            this.addEntry(new LabelEntry(Component.translatable("createWorld.superflatbiomeextension.customize.header.experimental").withColor(TextColor.WHITE)));
            this.addEntry(new SettingEntry<>(
                    Component.translatable("createWorld.superflatbiomeextension.customize.reduce_underground_biomes"),
                    CycleButton.onOffBuilder(config.settings[6]).displayOnlyValue(),
                    Tooltip.create(Component.translatable("createWorld.superflatbiomeextension.customize.reduce_underground_biomes.tooltip")),
                    (button, newValue) -> config.settings[6] = newValue
            ));
        }
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {}

    public class SettingEntry<T> extends Entry {
        private final List<FormattedCharSequence> description;
        private final CycleButton<T> button;

        public SettingEntry(Component description, CycleButton.Builder<T> button, Tooltip tooltip, CycleButton.OnValueChange<T> callback) {
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

    public class LabelEntry extends Entry {
        private final Component label;

        public LabelEntry(Component label) {
            this.label = label;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            graphics.centeredText(EditCustomFlatLevelScreen.this.minecraft.font, this.label, this.getContentXMiddle(), this.getContentY() + 5, -1);
        }

        @Override
        public Component getNarration() {
            return this.label;
        }
    }

    public static class ConfigStorage {
        private final boolean[] settings = new boolean[7];

        private ConfigStorage(CustomFlatGeneratorConfig config) {
            CustomFlatGeneratorConfig.EnabledFeatures enabledFeatures = config.getEnabledFeatures();
            this.settings[0] = config.hasSurfaceFluid();
            this.settings[1] = enabledFeatures.isEnabled(ModTags.VEGETATION_FEATURES);
            this.settings[2] = enabledFeatures.isEnabled(ModTags.TERRAIN_FEATURES);
            this.settings[3] = enabledFeatures.isEnabled(ModTags.LAKE_FEATURES);
            this.settings[4] = enabledFeatures.isEnabled(ModTags.ORE_FEATURES);
            this.settings[5] = enabledFeatures.isEnabled(ModTags.MISC_FEATURES);
            this.settings[6] = config.reduceUndergroundBiomes();
        }

        public boolean hasSurfaceFluid() { return settings[0]; }
        public boolean hasVegetationFeatures() { return settings[1]; }
        public boolean hasTerrainFeatures() { return settings[2]; }
        public boolean hasLakeFeatures() { return settings[3]; }
        public boolean hasOreFeatures() { return settings[4]; }
        public boolean hasMiscFeatures() { return settings[5]; }
        public boolean reduceUndergroundBiomes() { return settings[6]; }
    }
}
