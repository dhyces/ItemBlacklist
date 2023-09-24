package github.pitbox46.itemblacklist.core;

import com.mojang.serialization.JsonOps;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.utils.BetterFileWatcher;
import github.pitbox46.itemblacklist.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

public class ConfigFile {
    private final Path configFile;
    private Config config;
    private final BetterFileWatcher fileWatcher;

    public ConfigFile(Path configFile) {
        this.configFile = configFile;
        this.fileWatcher = new BetterFileWatcher(configFile, path -> save(), path -> reload(), "Config");
    }

    public Config getConfig() {
        return config;
    }

    public void load() {
        this.config = FileUtils.readConfigFromJson(configFile).getOrThrow(false, ItemBlacklist.LOGGER::error);
        config.setUpdateCallback(this::save);
        fileWatcher.start();
    }

    public void save() {
        FileUtils.saveToFile(configFile, Config.CODEC.encodeStart(JsonOps.INSTANCE, config));
    }

    public void saveAndClose() {
        save();
        try {
            fileWatcher.close();
        } catch (IOException e) {
            ItemBlacklist.LOGGER.error("Could not close FileWatcher.");
        }
    }

    public void reload() {
        config.merge(FileUtils.readConfigFromJson(configFile).getOrThrow(false, ItemBlacklist.LOGGER::error));
    }
}
