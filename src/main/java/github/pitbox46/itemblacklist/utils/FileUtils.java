package github.pitbox46.itemblacklist.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.google.gson.stream.MalformedJsonException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import github.pitbox46.itemblacklist.core.Config;
import github.pitbox46.itemblacklist.ItemBlacklist;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FileUtils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Path initialize(Path worldFolder, String configFolderName, String fileName) {
        Path file = worldFolder.resolve(configFolderName).resolve(fileName);
        createIfAbsent(file, Config.createDefaultConfig());
        return file;
    }

    public static void createIfAbsent(Path file, Config config) {
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                if (Files.exists(ItemBlacklist.DEFAULT_CONFIG_PATH)) {
                    Files.copy(ItemBlacklist.DEFAULT_CONFIG_PATH, file);
                } else {
                    Files.createFile(file);
                    saveToFile(file, Config.CODEC.encodeStart(JsonOps.INSTANCE, config));
                }
            }
        } catch(IOException e) {
            ItemBlacklist.LOGGER.warn(e.getMessage());
        }
    }

    /**
     * Reads items from a Json that has a top level array
     */
    public static DataResult<Config> readConfigFromJson(Path jsonFile) {
        JsonObject obj = null;
        try (BufferedReader reader = Files.newBufferedReader(jsonFile)) {
            obj = GSON.getAdapter(JsonObject.class).fromJson(reader);
        } catch (MalformedJsonException e) {
            String pathString = jsonFile.toString();
            ItemBlacklist.LOGGER.error("Failed to read config in " + pathString.substring(pathString.indexOf("saves")) + ": " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (obj == null) {
            obj = new JsonObject();
        }

        Dynamic<JsonElement> dynamic = ConfigDataFixer.fixBanList(new Dynamic<>(JsonOps.INSTANCE, obj));

        return Config.CODEC.parse(dynamic);
    }

    public synchronized static void saveToFile(Path jsonFile, DataResult<JsonElement> result) {
        try (BufferedWriter writer = Files.newBufferedWriter(jsonFile, StandardCharsets.UTF_8)) {
            writer.write(GSON.toJson(result.getOrThrow(false, ItemBlacklist.LOGGER::error)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
