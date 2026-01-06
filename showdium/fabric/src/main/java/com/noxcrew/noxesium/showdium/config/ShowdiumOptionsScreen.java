package com.noxcrew.noxesium.showdium.config;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * Settings screen for Showdium options.
 */
public class ShowdiumOptionsScreen extends OptionsSubScreen {

    private OptionsList optionsList;

    private static OptionInstance<Boolean> pingEnabled;
    private static OptionInstance<Double> pingVolume;
    private static OptionInstance<Double> pingScale;

    public ShowdiumOptionsScreen(Screen parentScreen) {
        super(parentScreen, Minecraft.getInstance().options, Component.translatable("showdium.options.title"));
        initializeOptions();
    }

    private void initializeOptions() {
        pingEnabled = OptionInstance.createBoolean(
                "showdium.options.ping_enabled.name",
                OptionInstance.cachedConstantTooltip(Component.translatable("showdium.options.ping_enabled.tooltip")),
                PingSystemConfig.isEnabled(),
                (newValue) -> {
                    PingSystemConfig.setEnabled(newValue);
                    ShowdiumConfig.save();
                });

        pingVolume = new OptionInstance<>(
                "showdium.options.ping_volume.name",
                OptionInstance.cachedConstantTooltip(Component.translatable("showdium.options.ping_volume.tooltip")),
                (component, value) -> {
                    if (value <= 0.0) {
                        return Component.translatable(
                                "options.generic_value", component, Component.translatable("options.off"));
                    }
                    return Component.translatable("options.percent_value", component, (int) (value * 100.0));
                },
                new OptionInstance.IntRange(0, 100).xmap(it -> (double) it / 100.0, it -> (int) (it * 100.0), true),
                Codec.doubleRange(0.0, 1.0),
                (double) PingSystemConfig.getVolume(),
                (newValue) -> {
                    PingSystemConfig.setVolume(newValue.floatValue());
                    ShowdiumConfig.save();
                });

        pingScale = new OptionInstance<>(
                "showdium.options.ping_scale.name",
                OptionInstance.cachedConstantTooltip(Component.translatable("showdium.options.ping_scale.tooltip")),
                (component, value) -> Component.translatable("options.percent_value", component, (int) (value * 100.0)),
                new OptionInstance.IntRange(25, 200).xmap(it -> (double) it / 100.0, it -> (int) (it * 100.0), true),
                Codec.doubleRange(0.25, 2.0),
                (double) PingSystemConfig.getPingScale(),
                (newValue) -> {
                    PingSystemConfig.setPingScale(newValue.floatValue());
                    ShowdiumConfig.save();
                });
    }

    @Override
    protected void init() {
        optionsList = new OptionsList(Minecraft.getInstance(), width, this);

        optionsList.addBig(pingEnabled);
        optionsList.addBig(pingVolume);
        optionsList.addBig(pingScale);

        addRenderableWidget(optionsList);

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width / 2 - 100, height - 27, 200, 20)
                .build());
    }

    @Override
    protected void addOptions() {
        // Options are added in init() instead
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
    }
}
