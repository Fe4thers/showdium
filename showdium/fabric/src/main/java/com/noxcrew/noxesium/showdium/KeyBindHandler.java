package com.noxcrew.noxesium.showdium;

import com.mojang.blaze3d.platform.InputConstants;
import com.noxcrew.noxesium.api.feature.NoxesiumFeature;
import com.noxcrew.noxesium.api.feature.qib.QibDefinition;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.registry.NoxesiumRegistries;
import com.noxcrew.noxesium.showdium.network.serverbound.ServerboundKeybindTriggeredPacket;
import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.kyori.adventure.key.Key;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

/**
 * Manages all dynamic and vanilla keybind handlers for the Showdium feature.
 * This class is responsible for registering a pool of placeholder keybinds at startup,
 * listening for server-sent packets to assign actions to these keys, and processing
 * user input each tick to trigger the appropriate actions based on their configuration
 * (e.g., press, release, or hold).
 */
public final class KeyBindHandler extends NoxesiumFeature {

    /**
     * A record to hold the configuration for a specific keybind action.
     * This encapsulates all server-defined properties for a given keybind.
     *
     * @param keybindName        The translation key of the action (e.g., "key.noxesium.customkeybind1").
     * @param isClientLogic      Whether this action should trigger client-side QIB logic.
     * @param isSinglePress      If true, triggers on press/release events. If false, triggers every tick while held.
     * @param delayMillis        The cooldown duration in milliseconds.
     * @param qibBehaviorId      The identifier for the QIB effect to execute for client-side actions.
     */
    private record KeybindAction(
            String keybindName, boolean isClientLogic, boolean isSinglePress, int delayMillis, String qibBehaviorId) {}

    private final Map<KeyMapping, KeybindAction> activeActions = new HashMap<>();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private final Map<KeyMapping, Boolean> wasKeyDown = new HashMap<>();
    private final Set<KeyMapping> cancelledKeys = new HashSet<>();

    /**
     * Initializes the KeyBindHandler, pre-registering a pool of custom keybinds and setting up the tick listener.
     */
    public KeyBindHandler() {
        for (int i = 0; i <= 9; i++) {
            final String keyName = "key.noxesium.customkeybind" + i;
            final KeyMapping key =
                    new KeyMapping(keyName, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, getKeybindCategory());
            KeyBindingHelper.registerKeyBinding(key);
        }
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    public void disableKeybinds(final String keybindName) {
        final KeyMapping keyMapping = findKeyMapping(keybindName);
        if (keyMapping == null) {
            return;
        }
        this.cancelledKeys.add(keyMapping);
    }

    /**
     * Registers a new keybind action based on data received from the server.
     * This assigns a specific behavior to an existing KeyMapping.
     *
     * @param keybindName           The translation key of the target KeyMapping.
     * @param isClientLogic         True if this action triggers client-side QIB logic.
     * @param isSinglePress         True for press/release events; false for continuous hold events.
     * @param delay                 The cooldown in milliseconds.
     * @param qibImplementationId   The ID of the QIB effect for client-side actions.
     */
    public void registerNewKeybind(
            final String keybindName,
            final Boolean isClientLogic,
            final Boolean isSinglePress,
            final Integer delay,
            final String qibImplementationId) {
        final KeyMapping keyMapping = findKeyMapping(keybindName);
        if (keyMapping == null) {
            return;
        }

        final KeybindAction action =
                new KeybindAction(keybindName, isClientLogic, isSinglePress, delay, qibImplementationId);
        this.activeActions.put(keyMapping, action);
        this.wasKeyDown.put(keyMapping, keyMapping.isDown());
    }

    /**
     * Removes an action handler for a specific keybind and clears its state.
     *
     * @param keybindName The translation key of the KeyMapping to clear.
     */
    public void removeKeybind(final String keybindName) {
        final KeyMapping keyMapping = findKeyMapping(keybindName);
        if (keyMapping == null) {
            return;
        }

        this.activeActions.remove(keyMapping);
        this.cooldowns.remove(keybindName);
        this.wasKeyDown.remove(keyMapping);
        this.cancelledKeys.remove(keyMapping);
    }

    /**
     * The main tick loop, executed at the end of every client tick.
     * It checks the state of all managed keybinds and dispatches events accordingly.
     *
     * @param client The Minecraft client instance.
     */
    private void onClientTick(final Minecraft client) {
        if (!isRegistered() || client.player == null) {
            return;
        }
        // applyCancelledKeys();
        this.activeActions.forEach((key, action) -> {
            final boolean isCurrentlyDown = key.isDown();
            final boolean wasPreviouslyDown = this.wasKeyDown.getOrDefault(key, false);

            if (action.isSinglePress) {
                if (isCurrentlyDown && !wasPreviouslyDown) {
                    handlePress(action);
                } else if (!isCurrentlyDown && wasPreviouslyDown) {
                    handleRelease(action);
                }
            } else {
                if (isCurrentlyDown) {
                    handlePress(action);
                }
            }
            this.wasKeyDown.put(key, isCurrentlyDown);
        });
    }

    public boolean isKeyCancelled(KeyMapping key) {
        return cancelledKeys.contains(key);
    }

    /**
     * Handles the logic for a key press or a continuous hold event. This includes
     * checking cooldowns, sending server packets, and executing client-side effects.
     *
     * @param action The configuration for the action to execute.
     */
    private void handlePress(final KeybindAction action) {
        if (!isCooldownReady(action.keybindName, action.delayMillis)) {
            return;
        }
        startCooldown(action.keybindName);

        NoxesiumServerboundNetworking.send(new ServerboundKeybindTriggeredPacket(action.keybindName, true));

        if (action.isClientLogic) {
            executeQibEffect(action);
        }
    }

    /**
     * Handles the logic for a key release event.
     *
     * @param action The configuration for the keybind that was released.
     */
    private void handleRelease(final KeybindAction action) {
        NoxesiumServerboundNetworking.send(new ServerboundKeybindTriggeredPacket(action.keybindName, false));
    }

    /**
     * Executes the client-side QIB effect associated with an action.
     *
     * @param action The action whose QIB effect should be run.
     */
    private void executeQibEffect(final KeybindAction action) {
        if (action.qibBehaviorId == null || action.qibBehaviorId.isEmpty()) {
            return;
        }

        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        final QibDefinition definition = NoxesiumRegistries.QIB_EFFECTS.getByKey(Key.key(action.qibBehaviorId));
        if (definition != null && definition.onJump() != null) {
            ShowdiumEntrypoint.qibBehaviorExecutor.executeBehavior(player, definition.onJump());
            if (QibBehaviorExecutor.ResetCooldown) {
                this.cooldowns.remove(action.keybindName);
            }
        }
    }

    /**
     * Checks if a keybind's cooldown has expired.
     *
     * @param keybindName The name of the keybind to check.
     * @param delay       The required delay in milliseconds.
     * @return True if the action can be performed, false otherwise.
     */
    private boolean isCooldownReady(final String keybindName, final long delay) {
        final long now = System.currentTimeMillis();
        final long lastUsed = this.cooldowns.getOrDefault(keybindName, 0L);
        return now - lastUsed >= delay;
    }

    /**
     * Puts a keybind on cooldown by recording the current time.
     *
     * @param keybindName The name of the keybind to put on cooldown.
     */
    private void startCooldown(final String keybindName) {
        this.cooldowns.put(keybindName, System.currentTimeMillis());
    }

    /**
     * Finds a registered KeyMapping by its translation key.
     *
     * @param keybindName The translation key to search for.
     * @return The found KeyMapping, or null if it does not exist.
     */
    private KeyMapping findKeyMapping(final String keybindName) {
        Objects.requireNonNull(keybindName, "keybindName cannot be null");
        return Arrays.stream(Minecraft.getInstance().options.keyMappings)
                .filter(km -> keybindName.equals(km.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Cleans up all state when the feature is unregistered.
     */
    @Override
    public void onUnregister() {
        this.activeActions.clear();
        this.cooldowns.clear();
        this.wasKeyDown.clear();
        this.cancelledKeys.clear();
    }

    /**
     * Provides the category name for the keybinds in the game's Controls menu.
     *
     * @return The translation key for the category.
     */
    public KeyMapping.Category getKeybindCategory() {
        return ShowdiumEntrypoint.Keybindcategory;
    }
}
