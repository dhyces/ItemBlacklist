package github.pitbox46.itemblacklist.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.MalformedJsonException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import github.pitbox46.itemblacklist.Config;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.PermissionLevel;
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
import java.util.HashSet;

public class FileUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
                Path defaultConfigPath = getOrCreateDefaultConfig();
                Files.copy(defaultConfigPath, file, StandardCopyOption.REPLACE_EXISTING);
            }
//            RELOADER.start(); // TODO: If auto-config reload is enabled, uncomment this
            return file;
        } catch(IOException e) {
            LOGGER.warn(e.getMessage());
        }
        return null;
    }

    public static Path getOrCreateDefaultConfig() throws IOException {
        Path defaultConfigPath = FabricLoader.getInstance().getConfigDir().resolve("itemblacklist.json");
        if (!Files.exists(defaultConfigPath)) {
            Files.createFile(defaultConfigPath);
            resetFileToDefault(defaultConfigPath);
        }
        return defaultConfigPath;
    }

    /**
     * Reads items from a Json that has a top level array
     */
    public static Config readConfigFromJson(Path jsonFile) {
        JsonObject obj = null;
        try {
            BufferedReader reader = Files.newBufferedReader(jsonFile);
            obj = GSON.fromJson(reader, JsonObject.class);
            reader.close();
        } catch (MalformedJsonException e) {
            String pathString = jsonFile.toString();
            ItemBlacklist.LOGGER.error("Failed to read config in " + jsonFile.subpath(pathString.indexOf("saves"), pathString.length()) + ": " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (obj == null) {
            obj = new JsonObject();
        }

        DataResult<Config> result = Config.CODEC.parse(JsonOps.INSTANCE, obj);
        if (result.result().isEmpty()) {
            resetFileToDefault(jsonFile);
            return Config.getInstance();
        }

        return result.getOrThrow(false, ItemBlacklist.LOGGER::error);
    }

    public static void appendItemAndSave(Path jsonFile, PermissionLevel permissionLevel, ItemStack item) {
        ItemBlacklist.requestConfigSet(readConfigFromJson(jsonFile));
        Config.getInstance().addItem(permissionLevel, item);
        saveToFile(jsonFile);
    }

    public static void removeItemAndSave(Path jsonFile, PermissionLevel permissionLevel, ItemStack item) {
        ItemBlacklist.requestConfigSet(readConfigFromJson(jsonFile));
        Config.getInstance().removeItem(permissionLevel, item);
        saveToFile(jsonFile);
    }

    public static void removeItemAndSave(Path jsonFile, ItemStack item) {
        ItemBlacklist.requestConfigSet(readConfigFromJson(jsonFile));
        for (PermissionLevel permissionLevel : PermissionLevel.values()) {
            Config.getInstance().removeItem(permissionLevel, item);
        }
        saveToFile(jsonFile);
    }

    /**
     *
     * @param jsonFile The path to the config file
     * @param targetLevel The targeted permission level. ie, I want to remove an item so that LEVEL_2 users can have it, you would use LEVEL_2
     * @param item The itemstack to remove
     */
    public static void removeDownToAndSave(Path jsonFile, PermissionLevel targetLevel, ItemStack item) {
        targetLevel = targetLevel.lower();
        ItemBlacklist.requestConfigSet(readConfigFromJson(jsonFile));
        for (PermissionLevel i = PermissionLevel.LEVEL_4; !i.equals(targetLevel); i = i.lower()) {
            Config.getInstance().removeItem(i, item);
        }
        Config.getInstance().addItem(targetLevel, item);
        saveToFile(jsonFile);
    }

    public static void removeAllItemsAndSave(Path jsonFile) {
        Config.getInstance().clearBannedItems();
        saveToFile(jsonFile);
    }

    public static void removeAllItemsFromPermissionAndSave(Path jsonFile, PermissionLevel permissionLevel) {
        ItemBlacklist.requestConfigSet(readConfigFromJson(jsonFile));
        Config.getInstance().setBannedItems(permissionLevel, new HashSet<>());
        saveToFile(jsonFile);
    }

    private static void resetFileToDefault(Path jsonFile) {
        ItemBlacklist.LOGGER.error("Could not parse config. Resetting to default state.");
        DataResult<JsonElement> result = Config.CODEC.encodeStart(JsonOps.INSTANCE, Config.getInstance());
        String emptyConfig = GSON.toJson(result.getOrThrow(false, ItemBlacklist.LOGGER::error));
        try {
            BufferedWriter writer = Files.newBufferedWriter(jsonFile, StandardCharsets.UTF_8);
            writer.write(emptyConfig);
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void saveToFile(Path jsonFile) {
        DataResult<JsonElement> result = Config.CODEC.encodeStart(JsonOps.INSTANCE, Config.getInstance());
        try {
            BufferedWriter writer = Files.newBufferedWriter(jsonFile, StandardCharsets.UTF_8);
            writer.write(GSON.toJson(result.getOrThrow(false, ItemBlacklist.LOGGER::error)));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
