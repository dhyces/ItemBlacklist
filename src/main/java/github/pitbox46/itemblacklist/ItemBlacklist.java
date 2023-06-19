package github.pitbox46.itemblacklist;

import com.mojang.brigadier.CommandDispatcher;
import github.pitbox46.itemblacklist.api.BanItemEvent;
import github.pitbox46.itemblacklist.core.BanData;
import github.pitbox46.itemblacklist.core.BasicDefaultConfig;
import github.pitbox46.itemblacklist.core.Config;
import github.pitbox46.itemblacklist.core.ModCommands;
import github.pitbox46.itemblacklist.mixins.EntityAccessor;
import github.pitbox46.itemblacklist.utils.FileUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class ItemBlacklist implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    private static Config config;
    public static MinecraftServer serverInstance;
    public static final Path DEFAULT_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("itemblacklist.json");
    private static Path worldConfigPath;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::serverStopping);
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(this::endDataPackReload);
        BasicDefaultConfig.createIfAbsent(DEFAULT_CONFIG_PATH);
    }

    private void endDataPackReload(MinecraftServer server, CloseableResourceManager resourceManager, boolean success) {
        config.reloadIfChanged();
        LOGGER.info("Config file reloaded");
    }

    private void serverStarted(MinecraftServer server) {
        Path worldConfigFolder = server.getWorldPath(LevelResource.ROOT);
        worldConfigPath = FileUtils.initialize(worldConfigFolder, "serverconfig", "itemblacklist.json");
        config = new Config(worldConfigPath);
        config.load();
        serverInstance = server;
    }

    private void serverStopping(MinecraftServer server) {
        config.saveAndClose();
        LOGGER.info("Config saved to file");
        worldConfigPath = null;
        config = null;
        serverInstance = null;
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection environment) {
//        if (environment.includeDedicated) { //TODO:
            ModCommands.register(dispatcher, buildContext);
//        }
    }

    public static Config getConfig() {
        return config;
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
