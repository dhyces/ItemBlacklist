package github.pitbox46.itemblacklist.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.Config;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.BanData;
import github.pitbox46.itemblacklist.mixins.ServerPlayerAccessor;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
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

    public static final Codec<BanData> EITHER_ITEM_CODEC = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), BanData.CODEC).xmap(
            either -> either.left().isEmpty() ? either.right().get() : new BanData(either.left().get(), Optional.empty()),
            data -> data.tag().isEmpty() ? Either.left(data.item()) : Either.right(data)
    );

    public static RecordCodecBuilder<Config, Set<BanData>> optionalConfigSet(String field, Function<Config, Set<BanData>> configFunction) {
        return Codec.optionalField(field, Utils.EITHER_ITEM_CODEC.listOf()).orElse(Optional.of(List.of()))
                .xmap(
                        optional -> optional.isEmpty() ? new HashSet<BanData>() : (Set<BanData>)Util.make(new HashSet<BanData>(), objects -> objects.addAll(optional.get())),
                        itemStacks -> Optional.of(itemStacks == null ? List.of() : List.copyOf(itemStacks))
                )
                .forGetter(configFunction);
    }

    public static void broadcastMessage(MinecraftServer server, Component component) {
        Optional<Registry<ChatType>> registryOptional = server.overworld().registryAccess().registry(Registries.CHAT_TYPE);
        if (registryOptional.isEmpty()) {
            throw new IllegalStateException("Dynamic registry of type {ChatType} was not found");
        }
        Registry<ChatType> reg = registryOptional.get();
        ChatType.Bound bound = new ChatType.Bound(reg.get(ChatType.CHAT), Component.literal("SERVER"), null);
        server.getPlayerList().broadcastChatMessage(PlayerChatMessage.system(component.getString()), server.createCommandSourceStack(), bound);
    }

    @Nullable
    public static Player getPlayer(AbstractContainerMenu menu) {
        for (ServerPlayer player : ItemBlacklist.serverInstance.getPlayerList().getPlayers()) {
            if (((ServerPlayerAccessor)player).getContainerCounter() == menu.containerId) {
                return player;
            }
        }
        return null;
    }
}
