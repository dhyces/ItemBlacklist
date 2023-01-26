package github.pitbox46.itemblacklist.core;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

public record BanData(Item item, Optional<CompoundTag> tag) {
    public static final Codec<BanData> CODEC = Codec.pair(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").codec(),
                    Codec.optionalField("tag", CompoundTag.CODEC).codec()
            ).xmap(
                    pair -> new BanData(pair.getFirst(), pair.getSecond()),
                    data -> Pair.of(data.item, data.tag)
            );

    public static BanData of(ItemStack stack) {
        return new BanData(stack.getItem(), Optional.ofNullable(stack.hasTag() ? stack.getTag() : null));
    }

    public ItemStack asStack() {
        ItemStack stack = new ItemStack(item);
        tag.ifPresent(stack::setTag);
        return stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BanData that)) return false;
        return Objects.equals(item, that.item) && (tag.isEmpty() || Objects.equals(tag, that.tag));
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, tag);
    }
}
