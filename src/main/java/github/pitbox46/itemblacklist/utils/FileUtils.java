package github.pitbox46.itemblacklist.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.MalformedJsonException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import github.pitbox46.itemblacklist.core.BanList;
import github.pitbox46.itemblacklist.core.BasicDefaultConfig;
import github.pitbox46.itemblacklist.core.Config;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.BuiltInPermissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtils {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    // TODO: Maybe!
//    private static final Object BLOCK = new Object();
//    private static FileTime lastTime;
//    private static final Thread RELOADER = new Thread(() -> {
//        while (true) {
//            synchronized (BLOCK) {
//                try {
//                    if (Files.exists(ItemBlacklist.banList)) {
//                        FileTime time = Files.getLastModifiedTime(ItemBlacklist.banList);
//                        if (!time.equals(lastTime)) {
//                            ItemBlacklist.requestConfigSet(readConfigFromJson(ItemBlacklist.banList));
//                            lastTime = time;
//                            ItemBlacklist.LOGGER.info("Config modified");
//                        }
//                    } else {
//                        Files.createFile(ItemBlacklist.banList);
//                        Config.getInstance().clearBannedItems();
//                        resetFileToDefault(ItemBlacklist.banList);
//                    }
//                } catch (NoSuchFileException | AccessDeniedException | FileAlreadyExistsException ignored) { // Bah humbug, just ignore it
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }, "Config thread");

    public static Path initialize(Path worldFolder, String configFolderName, String fileName) {
        try {
            Path configFolder = worldFolder.resolve(configFolderName);
            Path file;
            if (!Files.exists(configFolder)) {
                Files.createDirectories(configFolder);
            }
            file = configFolder.resolve(fileName);
            if (!Files.exists(file)) {
                Files.createFile(file);
                if (Files.exists(ItemBlacklist.DEFAULT_CONFIG_PATH)) {
                    Files.copy(ItemBlacklist.DEFAULT_CONFIG_PATH, file, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    BasicDefaultConfig.createIfAbsent(file);
                }
            }
//            RELOADER.start(); // TODO: If auto-config reload is enabled, uncomment this
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads items from a Json that has a top level array
     */
    public static DataResult<BanList> readConfigFromJson(Path jsonFile) {
        JsonObject obj = null;
        try (BufferedReader reader = Files.newBufferedReader(jsonFile)) {
            obj = GSON.fromJson(reader, JsonObject.class);
        } catch (MalformedJsonException e) {
            String pathString = jsonFile.toString();
            ItemBlacklist.LOGGER.error("Failed to read config in " + jsonFile.subpath(pathString.indexOf("saves"), pathString.length()) + ": " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (obj == null) {
            obj = new JsonObject();
        }

        Dynamic<JsonElement> dynamic = ConfigDataFixer.fixBanList(new Dynamic<>(JsonOps.INSTANCE, obj));

        return Config.BAN_LIST_CODEC.parse(dynamic);
    }

    public static void saveToFile(Path jsonFile, DataResult<JsonElement> result) {
        try (BufferedWriter writer = Files.newBufferedWriter(jsonFile, StandardCharsets.UTF_8)) {
            writer.write(GSON.toJson(result.getOrThrow(false, ItemBlacklist.LOGGER::error)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
