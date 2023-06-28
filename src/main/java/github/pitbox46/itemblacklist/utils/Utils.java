package github.pitbox46.itemblacklist.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.Config;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.client.utils.ClientSidedUtils;
import github.pitbox46.itemblacklist.core.BanData;
import github.pitbox46.itemblacklist.mixins.ServerPlayerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Utils {

    public static final Codec<BanData> EITHER_ITEM_CODEC = Codec.either(Registry.ITEM.byNameCodec(), BanData.CODEC).xmap(
            either -> either.map(item -> new BanData(item, Optional.empty()), Function.identity()),
            data -> data.tag().isEmpty() ? Either.left(data.item()) : Either.right(data)
    );

    public static RecordCodecBuilder<Config, Set<BanData>> optionalConfigSet(String field, Function<Config, Set<BanData>> configFunction) {
        return Utils.EITHER_ITEM_CODEC.listOf().optionalFieldOf(field, List.of())
                .xmap(banData -> (Set<BanData>)new HashSet<>(banData), List::copyOf)
                .forGetter(configFunction);
    }

    public static void broadcastMessage(MinecraftServer server, Component component) {
            server.getPlayerList().broadcastSystemMessage(component, false);
    }

    @Nullable
    public static Player getPlayer(AbstractContainerMenu menu) {
        if (ItemBlacklist.serverInstance != null) {
            for (ServerPlayer player : ItemBlacklist.serverInstance.getPlayerList().getPlayers()) {
                if (((ServerPlayerAccessor)player).getContainerCounter() == menu.containerId) {
                    return player;
                }
            }
        }
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? ClientSidedUtils.getClientPlayer() : null;
    }
}
