package com.noxcrew.noxesium.showdium.config;

import com.mojang.serialization.Codec;
import com.noxcrew.noxesium.core.fabric.config.NoxesiumSettingsScreen;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Extended settings screen that adds Showdium-specific options.
 */
public class ShowdiumSettingsScreen extends NoxesiumSettingsScreen {

    private static final OptionInstance<Boolean> pingSystemEnabled = OptionInstance.createBoolean(
            "showdium.options.ping_enabled.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("showdium.options.ping_enabled.tooltip")),
            PingSystemConfig.isEnabled(),
            (newValue) -> {
                PingSystemConfig.setEnabled(newValue);
                ShowdiumConfig.save();
            });

    private static final OptionInstance<Double> pingVolume = new OptionInstance<>(
            "showdium.options.ping_volume.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("showdium.options.ping_volume.tooltip")),
            ShowdiumSettingsScreen::percentageLabel,
            new OptionInstance.IntRange(0, 100).xmap(it -> (double) it / 100.0, it -> (int) (it * 100.0), true),
            Codec.doubleRange(0.0, 1.0),
            (double) PingSystemConfig.getVolume(),
            (newValue) -> {
                PingSystemConfig.setVolume(newValue.floatValue());
                ShowdiumConfig.save();
            });

    private static final OptionInstance<Double> pingScale = new OptionInstance<>(
            "showdium.options.ping_scale.name",
            OptionInstance.cachedConstantTooltip(Component.translatable("showdium.options.ping_scale.tooltip")),
            ShowdiumSettingsScreen::percentageLabel,
            new OptionInstance.IntRange(25, 200).xmap(it -> (double) it / 100.0, it -> (int) (it * 100.0), true),
            Codec.doubleRange(0.25, 2.0),
            (double) PingSystemConfig.getPingScale(),
            (newValue) -> {
                PingSystemConfig.setPingScale(newValue.floatValue());
                ShowdiumConfig.save();
            });

    public ShowdiumSettingsScreen(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void addToDeveloperTab(GridLayout.RowHelper rowHelper) {
        rowHelper.addChild(createWidget(pingSystemEnabled));
        rowHelper.addChild(createWidget(pingVolume));
        rowHelper.addChild(createWidget(pingScale));
    }

    private static Component percentageLabel(Component component, double value) {
        return Component.translatable("options.percent_value", component, (int) (value * 100.0));
    }
}
