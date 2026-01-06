package com.noxcrew.noxesium.showdium.pingsystem;

import com.noxcrew.noxesium.api.component.GameComponents;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.showdium.ShowdiumEntrypoint;
import com.noxcrew.noxesium.showdium.config.PingSystemConfig;
import com.noxcrew.noxesium.showdium.registry.ShowdiumGameComponent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Main feature class for the ping system.
 * Handles initialization and input processing.
 */
public class PingSystemFeature extends NoxesiumFeature {

    private static final String KEYBIND_NAME = "key.showdium.ping";
    private static final int DEFAULT_KEY = GLFW.GLFW_KEY_V;
    private static final long HOLD_THRESHOLD_MS = 300L;
    private static final int DEFAULT_PING_COLOR = 0xFFF0F0F0;

    public static KeyMapping pingKeyBinding;

    public PingSystemFeature() {
        initializeKeyBinding();
        registerEventHandlers();
        loadConfig();
    }

    private void initializeKeyBinding() {
        pingKeyBinding = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(KEYBIND_NAME, DEFAULT_KEY, ShowdiumEntrypoint.Keybindcategory));
    }

    private void registerEventHandlers() {
        ClientTickEvents.END_CLIENT_TICK.register(PingSystemFeature::onTick);
        HudRenderCallback.EVENT.register(PingOverlayRenderer::render);
    }

    private void loadConfig() {
        PingSystemConfig.loadFromConfig();
    }

    /**
     * Handles ping input each tick.
     */
    private static void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Check if ping system is enabled by server
        if (!GameComponents.getInstance().noxesium$hasComponent(ShowdiumGameComponent.PingSystem)) {
            if (!PingManager.getActivePings().isEmpty()) {
                PingManager.clearAllPings();
            }
            return;
        }

        // Check if ping system is enabled by client
        if (!PingSystemConfig.isEnabled()) {
            return;
        }

        processKeyInput(mc);
    }

    /**
     * Processes the ping key input with hold-to-remove functionality.
     */
    private static void processKeyInput(Minecraft mc) {
        boolean isKeyDown = pingKeyBinding.isDown();
        boolean wasKeyDown = PingManager.getPreviousKeyState();
        long currentTime = System.currentTimeMillis();

        // Key just pressed
        if (isKeyDown && !wasKeyDown) {
            PingManager.setKeyPressStartTime(currentTime);
        }

        // Key held for threshold - remove pings
        if (isKeyDown && currentTime - PingManager.getKeyPressStartTime() >= HOLD_THRESHOLD_MS) {
            PingManager.removeLocalPlayerPings(mc.player.getUUID());
            PingManager.setKeyPressStartTime(Long.MAX_VALUE);
        }

        // Key just released - create ping if not held
        if (!isKeyDown && wasKeyDown) {
            if (PingManager.getKeyPressStartTime() != Long.MAX_VALUE) {
                int pingColor = GameComponents.getInstance()
                        .noxesium$getComponentOr(ShowdiumGameComponent.TeamColor, () -> DEFAULT_PING_COLOR);
                PingManager.createCrosshairPing(mc, pingColor);
            }
            PingManager.setKeyPressStartTime(0L);
        }

        PingManager.setPreviousKeyState(isKeyDown);
    }

    @Override
    public void onUnregister() {
        PingManager.clearAllPings();
    }
}
