package github.pitbox46.itemblacklist.core;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

public record ItemStackData(Item item, CompoundTag tag) {
    public static final Codec<ItemStackData> CODEC = Codec.pair(
            Registry.ITEM.byNameCodec().fieldOf("item").codec(),
                    Codec.optionalField("tag", CompoundTag.CODEC).codec()
            ).xmap(
                    pair -> new ItemStackData(pair.getFirst(), pair.getSecond().orElse(new CompoundTag())),
                    data -> Pair.of(data.item, Optional.ofNullable(data.tag.isEmpty() ? null : data.tag))
            );

    public static ItemStackData of(ItemStack stack) {
        return new ItemStackData(stack.getItem(), stack.hasTag() ? stack.getTag() : new CompoundTag());
    }

    public ItemStack asStack() {
        ItemStack stack = new ItemStack(item);
        stack.setTag(tag);
        return stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStackData that = (ItemStackData) o;
        return Objects.equals(item, that.item) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, tag);
    }
}
