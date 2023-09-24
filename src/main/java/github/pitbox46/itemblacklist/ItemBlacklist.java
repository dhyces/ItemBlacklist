package github.pitbox46.itemblacklist;

import com.mojang.brigadier.CommandDispatcher;
import github.pitbox46.itemblacklist.api.BanItemEvent;
import github.pitbox46.itemblacklist.api.ServerSavedCallback;
import github.pitbox46.itemblacklist.core.*;
import github.pitbox46.itemblacklist.utils.FileUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class ItemBlacklist implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    private static ConfigFile configFile;
    public static MinecraftServer serverInstance;
    public static final Path DEFAULT_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("itemblacklist.json");

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::serverStopping);
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(this::endDataPackReload);
        ServerSavedCallback.EVENT.register(this::onServerSaved);
        FileUtils.createIfAbsent(DEFAULT_CONFIG_PATH, Config.createDefaultConfig());
    }

    private void endDataPackReload(MinecraftServer server, CloseableResourceManager resourceManager, boolean success) {
        configFile.reload();
        LOGGER.info("Config file reloaded");
    }

    private void serverStarted(MinecraftServer server) {
        Path worldConfigFolder = server.getWorldPath(LevelResource.ROOT);
        Path worldConfigPath = FileUtils.initialize(worldConfigFolder, "serverconfig", "itemblacklist.json");
        configFile = new ConfigFile(worldConfigPath);
        configFile.load();
        serverInstance = server;
    }

    private void serverStopping(MinecraftServer server) {
        configFile.saveAndClose();
        LOGGER.info("Config saved to file");
        configFile = null;
        serverInstance = null;
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection environment) {
        ModCommands.register(dispatcher, buildContext);
    }

    private void onServerSaved(boolean suppressLog, boolean flush, boolean forced) {
        configFile.save();
    }

    public static Config getConfig() {
        return configFile.getConfig();
    }

    public static boolean shouldDelete(Player player, ItemStack stack) {
        if (stack.is(Items.AIR)) return false;
        boolean shouldBan = !hasPermission(player, stack);
        return BanItemEvent.EVENT.invoker().onBannedItem(player, stack, shouldBan);
    }

    public static boolean hasPermission(Player player, ItemStack stack) {
        return getConfig().hasPermission(player, stack);
    }
}
