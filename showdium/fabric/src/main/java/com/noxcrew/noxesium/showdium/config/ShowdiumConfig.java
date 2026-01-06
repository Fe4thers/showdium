package com.noxcrew.noxesium.showdium.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores Showdium's configuration values.
 */
public class ShowdiumConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("ShowdiumConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("showdium-config.json");

    private static ShowdiumConfig instance;

    // Ping system settings
    public boolean pingEnabled = true;
    public float pingVolume = 1.0f;
    public float pingScale = 1.0f;

    /**
     * Gets the config instance, loading from file if needed.
     */
    public static ShowdiumConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    /**
     * Loads the config from file.
     */
    private static ShowdiumConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (var reader = new FileReader(CONFIG_PATH.toFile())) {
                ShowdiumConfig config = GSON.fromJson(reader, ShowdiumConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load Showdium config", e);
            }
        }
        return new ShowdiumConfig();
    }

    /**
     * Saves the current config to file.
     */
    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(getInstance()));
        } catch (IOException e) {
            LOGGER.error("Failed to save Showdium config", e);
        }
    }

    /**
     * Applies the loaded config values to the runtime settings.
     */
    public void apply() {
        PingSystemConfig.setEnabled(pingEnabled);
        PingSystemConfig.setVolume(pingVolume);
        PingSystemConfig.setVolume(pingScale);
    }
}
