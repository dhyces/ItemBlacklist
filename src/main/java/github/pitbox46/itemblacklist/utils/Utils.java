package github.pitbox46.itemblacklist.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.core.Config;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.client.utils.ClientUtils;
import github.pitbox46.itemblacklist.core.BanData;
import github.pitbox46.itemblacklist.core.ItemWithTag;
import github.pitbox46.itemblacklist.mixins.TransientCraftingContainerAccessor;
import github.pitbox46.itemblacklist.mixins.ServerPlayerAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Utils {
    public static final Codec<ItemWithTag> ITEM_OR_STACK = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), ItemWithTag.CODEC).xmap(
            fsEither -> fsEither.map(item -> new ItemWithTag(item, null), Function.identity()),
            stack -> stack.tag() == null ? Either.left(stack.item()) : Either.right(stack)
    );

    public static void broadcastMessage(MinecraftServer server, Component component) {
        CommandSourceStack sourceStack = server.createCommandSourceStack();
        ChatType.Bound bound = ChatType.bind(ChatType.CHAT, sourceStack);
        server.getPlayerList().broadcastChatMessage(PlayerChatMessage.system(component.getString()), sourceStack, bound);
    }

    @Nullable
    public static Player getPlayer(Container container) {
        if (container instanceof CraftingContainer craftingContainer) {
            return getPlayer(((TransientCraftingContainerAccessor)craftingContainer).getMenu());
        } else if (container instanceof Inventory inventory) {
            return inventory.player;
        }
        return null;
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
        return ClientUtils.getClientPlayer();
    }

    public static boolean areTagsSimilar(Tag tag, Tag tag2) {
        if (tag instanceof ShortTag || tag instanceof ByteTag || tag instanceof IntTag) {
            if (((NumericTag)tag).getAsInt() != ((NumericTag)tag2).getAsInt()) {
                return false;
            }
        } else if (tag instanceof FloatTag || tag instanceof DoubleTag) {
            if (((NumericTag)tag).getAsDouble() != ((NumericTag)tag2).getAsDouble()) {
                return false;
            }
        } else if (tag instanceof LongTag) {
            if (((NumericTag)tag).getAsLong() != ((NumericTag)tag2).getAsLong()) {
                return false;
            }
        } else if (tag instanceof StringTag) {
            if (!tag.equals(tag2) && !testResourceLocations((StringTag) tag, (StringTag) tag2)) {
                return false;
            }
        } else if (tag instanceof CompoundTag compoundTag) {
            for (String key : compoundTag.getAllKeys()) {
                if (!areTagsSimilar(compoundTag.get(key), ((CompoundTag)tag2).get(key))) {
                    return false;
                }
            }
        } else if (tag instanceof CollectionTag<?> collectionTag) {
            for (int i = 0; i < collectionTag.size(); i++) {
                if (!areTagsSimilar(collectionTag.get(i), ((CollectionTag<?>)tag2).get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean testResourceLocations(StringTag tag, StringTag tag2) {
        if (ResourceLocation.isValidResourceLocation(tag.getAsString()) && ResourceLocation.isValidResourceLocation(tag2.getAsString())) {
            return new ResourceLocation(tag.getAsString()).equals(new ResourceLocation(tag2.getAsString()));
        }
        return false;
    }
}
