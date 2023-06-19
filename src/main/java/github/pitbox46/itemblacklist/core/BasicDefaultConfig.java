package github.pitbox46.itemblacklist.core;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.utils.FileUtils;
import net.minecraft.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;

public class BasicDefaultConfig {
    public static Path createIfAbsent(Path folder) {
        try {
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }
            Path configPath = folder.resolve("itemblacklist.json");
            if (!Files.exists(configPath)) {
                Files.createFile(configPath);
                DataResult<JsonElement> result = Config.BAN_LIST_CODEC.encodeStart(JsonOps.INSTANCE, createBasicData());
                try (JsonWriter writer = FileUtils.GSON.newJsonWriter(Files.newBufferedWriter(configPath))) {
                    FileUtils.GSON.toJson(result.getOrThrow(false, ItemBlacklist.LOGGER::error), writer);
                }
            }
        } catch(IOException e) {
            ItemBlacklist.LOGGER.warn(e.getMessage());
        }
        return null;
    }

    private static BanList createBasicData() {
        return new BanList(Util.make(new HashMap<>(), map -> {
            map.put("level_0", new HashSet<>());
            map.put("level_1", new HashSet<>());
            map.put("level_2", new HashSet<>());
            map.put("level_3", new HashSet<>());
            map.put("level_4", new HashSet<>());
        }));
    }
}
