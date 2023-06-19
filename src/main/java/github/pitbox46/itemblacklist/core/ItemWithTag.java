package github.pitbox46.itemblacklist.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemWithTag(Item item, CompoundTag tag) {
    public static final Codec<ItemWithTag> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemWithTag::item),
                    CompoundTag.CODEC.optionalFieldOf("tag", null).forGetter(ItemWithTag::tag)
            ).apply(instance, ItemWithTag::new)
    );

    public static ItemWithTag fromStack(ItemStack stack) {
        return new ItemWithTag(stack.getItem(), stack.hasTag() ? stack.getTag() : null);
    }

    public ItemStack asStack() {
        ItemStack stack = new ItemStack(item);
        if (tag != null) {
            stack.setTag(tag);
        }
        return stack;
    }

    public boolean is(Item item) {
        return this.item == item;
    }
}
