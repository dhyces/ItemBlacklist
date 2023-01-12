package github.pitbox46.itemblacklist;

import com.mojang.brigadier.CommandDispatcher;
import github.pitbox46.itemblacklist.commands.ModCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBlacklist implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();
    public static File BANLIST;
    public static List<Item> BANNED_ITEMS = new ArrayList<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::serverStarting);
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        ServerEntityEvents.ENTITY_LOAD.register(this::entityLoad);
    }

    private void serverStarting(MinecraftServer server) {
        Path folder = server.getWorldPath(LevelResource.ROOT);
        BANLIST = JsonUtils.initialize(folder, "serverconfig", "itemblacklist.json");
        BANNED_ITEMS = JsonUtils.readItemsFromJson(BANLIST);
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection environment) {
        ModCommands.register(dispatcher, buildContext);
    }

    private void entityLoad(Entity entity, ServerLevel world) {
        if (entity instanceof ItemEntity item) {
            if (shouldDelete(item.getItem())) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    public static boolean shouldDelete(ItemStack stack) {
        boolean shouldBan = BanItemEvent.EVENT.invoker().onBannedItem(stack);
        return shouldBan && BANNED_ITEMS.contains(stack.getItem());
    }

    public static String itemListToString(List<Item> itemList) {
        return Arrays.toString(itemList.stream().map(BuiltInRegistries.ITEM::getKey).toArray());
    }
}
