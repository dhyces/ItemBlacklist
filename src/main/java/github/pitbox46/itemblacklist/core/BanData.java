package github.pitbox46.itemblacklist.core;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.utils.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Contains the item predicate for the banned item and the reason for the ban
 */
public class BanData implements Predicate<ItemStack> {
    public static final Codec<BanData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Utils.ITEM_OR_STACK.fieldOf("item").forGetter(BanData::getStack),
                    NbtComparator.CODEC.optionalFieldOf("compare_tag", NbtComparator.NONE).forGetter(BanData::getComparison),
                    Codec.STRING.optionalFieldOf("reason", "").forGetter(BanData::getReason)
            ).apply(instance, BanData::new)
    );

    public static final Codec<BanData> EITHER_CODEC = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), BanData.CODEC).xmap(
            either -> either.map(item -> new BanData(new ItemWithTag(item, null), NbtComparator.NONE, ""), Function.identity()),
            data -> data.stack.tag() == null && data.comparison == NbtComparator.NONE ? Either.left(data.stack.item()) : Either.right(data)
    );

    private final ItemWithTag stack;
    private final NbtComparator comparison;
    private final String reason;

    private BanData(ItemWithTag stack, NbtComparator comparison, String banReason) {
        this.stack = stack;
        this.comparison = comparison;
        this.reason = banReason;
    }

    public static BanData of(ItemStack stack) {
        return new BanData(ItemWithTag.fromStack(stack.copy()), NbtComparator.NONE, "");
    }

    public static BanData of(ItemStack stack, NbtComparator comparison, String banReason) {
        return new BanData(ItemWithTag.fromStack(stack.copy()), comparison, banReason);
    }

    public ItemWithTag getStack() {
        return stack;
    }

    public NbtComparator getComparison() {
        return comparison;
    }

    public String getReason() {
        return reason;
    }

    public Component getComponent() {
        MutableComponent component = Component.empty().append(stack.asStack().getDisplayName());
        if (!reason.isEmpty()) {
            component.append(" ").append(ModComponents.BAN_REASON.create(Component.literal(reason)));
        }
        return component;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BanData banData = (BanData) o;
        return Objects.equals(stack, banData.stack) && comparison == banData.comparison;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack, comparison);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return itemStack.is(stack.item()) && comparison.compareTags(itemStack.getTag(), this.stack.tag());
    }
}
