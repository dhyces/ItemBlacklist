package github.pitbox46.itemblacklist;

import com.mojang.brigadier.CommandDispatcher;
import github.pitbox46.itemblacklist.api.BanItemEvent;
import github.pitbox46.itemblacklist.core.BanData;
import github.pitbox46.itemblacklist.core.ModCommands;
import github.pitbox46.itemblacklist.mixins.EntityAccessor;
import github.pitbox46.itemblacklist.utils.FileUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
import javax.annotation.Nullable;
import java.nio.file.Path;

public class ItemBlacklist implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();
    public static Path banList;
    public static MinecraftServer serverInstance;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::serverStopping);
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(this::endDataPackReload);
    }

    private void endDataPackReload(MinecraftServer server, CloseableResourceManager resourceManager, boolean success) {
        Config.instance = FileUtils.readConfigFromJson(banList);
        LOGGER.info("Config file reloaded");
    }

    private void serverStarted(MinecraftServer server) {
        Path folder = server.getWorldPath(LevelResource.ROOT);
        banList = FileUtils.initialize(folder, "serverconfig", "itemblacklist.json");
        Config.instance = FileUtils.readConfigFromJson(banList);
        serverInstance = server;
    }

    private void serverStopping(MinecraftServer server) {
        FileUtils.saveToFile(banList);
        LOGGER.info("Config saved to file");
        banList = null;
        Config.instance = null;
        serverInstance = null;
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection environment) {
//        if (environment.includeDedicated) { //TODO:
            ModCommands.register(dispatcher, buildContext);
//        }
    }

    public static boolean requestConfigSet(@Nonnull Config newConfig) {
        Config.instance = newConfig;
        return true;
    }

    public static boolean shouldDelete(Player player, ItemStack stack) {
        if (stack.is(Items.AIR)) return false;
        boolean shouldBan = BanItemEvent.EVENT.invoker().onBannedItem(player, stack);
        if (!shouldBan) return false;
        return hasPermission(player, stack);
    }

    public static boolean hasPermission(Player player, ItemStack stack) {
        int permissionLevel;
        if (player instanceof ServerPlayer serverPlayer) {
            permissionLevel = serverPlayer.server.getProfilePermissions(player.getGameProfile());
        } else {
            permissionLevel = ((EntityAccessor)player).invokeGetPermissionLevel();
        }
        return Config.getInstance().getAllBannedItems(permissionLevel).contains(BanData.of(stack));
    }
}
