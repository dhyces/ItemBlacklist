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
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Utils {
    public static final Codec<ItemWithTag> ITEM_OR_STACK = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), ItemWithTag.CODEC).xmap(
            fsEither -> fsEither.map(ItemWithTag::fromItem, Function.identity()),
            itemWithTag -> !itemWithTag.hasTag() ? Either.left(itemWithTag.item()) : Either.right(itemWithTag)
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
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? ClientUtils.getClientPlayer() : null;
    }

    // if the config part is null, just return true
    public static boolean areTagsSimilar(Tag stackTag, @Nullable Tag configTag) {
        if (configTag == null) {
            return true;
        }
        if (stackTag instanceof ShortTag || stackTag instanceof ByteTag || stackTag instanceof IntTag) {
            if (((NumericTag)stackTag).getAsInt() != ((NumericTag)configTag).getAsInt()) {
                return false;
            }
        } else if (stackTag instanceof FloatTag || stackTag instanceof DoubleTag) {
            if (((NumericTag)stackTag).getAsDouble() != ((NumericTag)configTag).getAsDouble()) {
                return false;
            }
        } else if (stackTag instanceof LongTag) {
            if (((NumericTag)stackTag).getAsLong() != ((NumericTag)configTag).getAsLong()) {
                return false;
            }
        } else if (stackTag instanceof StringTag) {
            if (!stackTag.equals(configTag) && !testResourceLocations((StringTag) stackTag, (StringTag) configTag)) {
                return false;
            }
        } else if (stackTag instanceof CompoundTag compoundTag) {
            for (String key : compoundTag.getAllKeys()) {
                if (!areTagsSimilar(compoundTag.get(key), ((CompoundTag)configTag).get(key))) {
                    return false;
                }
            }
        } else if (stackTag instanceof CollectionTag<?> collectionTag) {
            int maxTests = Math.min(collectionTag.size(), ((CollectionTag<?>)configTag).size());
            for (int i = 0; i < maxTests; i++) {
                if (!areTagsSimilar(collectionTag.get(i), ((CollectionTag<?>)configTag).get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    // test so that if the config value says "sharpness" but the stack tag says "minecraft:sharpness", it still works
    private static boolean testResourceLocations(StringTag tag, StringTag tag2) {
        if (ResourceLocation.isValidResourceLocation(tag.getAsString()) && ResourceLocation.isValidResourceLocation(tag2.getAsString())) {
            return new ResourceLocation(tag.getAsString()).equals(new ResourceLocation(tag2.getAsString()));
        }
        return false;
    }
}
