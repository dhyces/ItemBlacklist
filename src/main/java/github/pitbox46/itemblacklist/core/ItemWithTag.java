package github.pitbox46.itemblacklist.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.mixins.ItemInputAccessor;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ItemWithTag(Item item, @NotNull CompoundTag tag) {
    private static final CompoundTag EMPTY = new CompoundTag();
    public static final Codec<ItemWithTag> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemWithTag::item),
                    CompoundTag.CODEC.optionalFieldOf("tag", EMPTY).forGetter(ItemWithTag::tag)
            ).apply(instance, ItemWithTag::new)
    );

    public static ItemWithTag fromInput(ItemInput input) {
        ItemInputAccessor accessor = (ItemInputAccessor) input;
        return new ItemWithTag(input.getItem(), accessor.getTag() != null ? accessor.getTag() : EMPTY);
    }

    public static ItemWithTag fromStack(ItemStack stack) {
        return new ItemWithTag(stack.getItem(), stack.hasTag() ? stack.getTag() : EMPTY);
    }

    public static ItemWithTag fromItem(Item item) {
        return new ItemWithTag(item, EMPTY);
    }

    public ItemStack asStack() {
        ItemStack stack = new ItemStack(item);
        if (tag != EMPTY) {
            stack.setTag(tag);
        }
        return stack;
    }

    public boolean hasTag() {
        return tag != EMPTY;
    }

    public boolean is(Item item) {
        return this.item == item;
    }
}
