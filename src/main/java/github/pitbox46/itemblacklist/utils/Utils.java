package github.pitbox46.itemblacklist.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.Config;
import github.pitbox46.itemblacklist.core.ItemStackData;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.function.Function;

public class Utils {

    public static final Codec<ItemStackData> EITHER_ITEM_CODEC = Codec.either(Registry.ITEM.byNameCodec(), ItemStackData.CODEC).xmap(
            either -> either.left().isEmpty() ? either.right().get() : new ItemStackData(either.left().get(), new CompoundTag()),
            data -> data.tag().isEmpty() ? Either.left(data.item()) : Either.right(data)
    );

    public static RecordCodecBuilder<Config, Set<ItemStackData>> optionalConfigSet(String field, Function<Config, Set<ItemStackData>> configFunction) {
        return Codec.optionalField(field, Utils.EITHER_ITEM_CODEC.listOf()).orElse(Optional.of(List.of()))
                .xmap(
                        optional -> optional.isEmpty() ? new HashSet<ItemStackData>() : (Set<ItemStackData>)Util.make(new HashSet<ItemStackData>(), objects -> objects.addAll(optional.get())),
                        itemStacks -> Optional.of(itemStacks == null ? List.of() : List.copyOf(itemStacks))
                )
                .forGetter(configFunction);
    }
}
