package github.pitbox46.itemblacklist;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.FileSystemLoopException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static File initialize(Path worldFolder, String configFolderName, String fileName) {
        try {
            Path configFolder = worldFolder.resolve(configFolderName);
            File file;
            if (Files.exists(configFolder)) {
                file = new File(configFolder.toFile(), fileName);
            } else {
                if (configFolder.toFile().mkdirs()) {
                    file = new File(configFolder.toFile(), fileName);
                } else {
                    throw new FileSystemLoopException("Could not create " + configFolder);
                }
            }
            if(file.createNewFile()) {
                Path defaultConfigPath = FabricLoader.getInstance().getConfigDir().resolve("itemblacklist.json");
                if (Files.exists(defaultConfigPath)) {
                    Files.copy(defaultConfigPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    FileWriter configWriter = new FileWriter(file);
                    configWriter.write(GSON.toJson(new JsonArray()));
                    configWriter.close();
                }
            }
            return file;
        } catch(IOException e) {
            LOGGER.warn(e.getMessage());
        }
        return null;
    }

    /**
     * Reads items from a Json that has a top level array
     */
    public static List<Item> readItemsFromJson(File jsonFile) {
        try {
            Reader reader = new FileReader(jsonFile);
            JsonArray array = GsonHelper.fromJson(GSON, reader, JsonArray.class);
            List<Item> returnedArrays = new ArrayList<>();
            assert array != null;
            for(JsonElement element: array) {
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(element.getAsString()));
                if(!(item instanceof AirItem)) {
                    returnedArrays.add(item);
                }
            }
            return returnedArrays;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Writes a new item to a json that has a top level array
     */
    public static void appendItemToJson(File jsonFile, Item item) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonArray array = GsonHelper.fromJson(GSON, reader, JsonArray.class);
            assert array != null;

            JsonPrimitive string = new JsonPrimitive(BuiltInRegistries.ITEM.getKey(item).toString());
            if(!array.contains(string))
                array.add(string);

            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(GSON.toJson(array));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ItemBlacklist.BANNED_ITEMS = JsonUtils.readItemsFromJson(ItemBlacklist.BANLIST);
    }

    /**
     * Removes an item from a json that has a top level array
     */
    public static void removeItemFromJson(File jsonFile, Item item) throws IndexOutOfBoundsException {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonArray array = GsonHelper.fromJson(GSON, reader, JsonArray.class);
            assert array != null;
            int itemLocation = -1;
            int i = 0;
            for(JsonElement element: array) {
                if(element.getAsString().equals(BuiltInRegistries.ITEM.getKey(item).toString())) itemLocation = i;
                i++;
            }
            array.remove(itemLocation);
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(GSON.toJson(array));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ItemBlacklist.BANNED_ITEMS = JsonUtils.readItemsFromJson(ItemBlacklist.BANLIST);
    }

    public static void removeAllItemsFromJson(File jsonFile) throws IndexOutOfBoundsException {
        try (FileWriter fileWriter = new FileWriter(jsonFile)) {
            fileWriter.write(GSON.toJson(new JsonArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ItemBlacklist.BANNED_ITEMS = JsonUtils.readItemsFromJson(ItemBlacklist.BANLIST);
    }
}
