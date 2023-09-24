package github.pitbox46.itemblacklist.core;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.utils.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Contains the item predicate for the banned item and the reason for the ban
 * TODO: ban meta? (when it was banned, who banned it (if available), etc)
 * TODO: allow replace with item instead of air?
 */
public class BanData implements Predicate<ItemStack> {
    public static final Codec<BanData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Utils.ITEM_OR_STACK.fieldOf("item").forGetter(BanData::getItemWithTag),
                    NbtComparator.CODEC.optionalFieldOf("compare_tag", NbtComparator.NONE).forGetter(BanData::getComparison),
                    Codec.STRING.optionalFieldOf("reason", "").forGetter(BanData::getReason)
            ).apply(instance, BanData::new)
    );

    public static final Codec<BanData> EITHER_CODEC = Codec.either(BuiltInRegistries.ITEM.byNameCodec(), BanData.CODEC).xmap(
            either -> either.map(BanData::of, Function.identity()),
            data -> data.isBasicItemBan() ? Either.left(data.itemWithTag.item()) : Either.right(data)
    );

    private final ItemWithTag itemWithTag;
    private final NbtComparator comparison;
    private final String reason;
    private Component cachedComponent;

    private BanData(ItemWithTag itemWithTag) {
        this(itemWithTag, NbtComparator.NONE);
    }

    private BanData(ItemWithTag itemWithTag, NbtComparator comparison) {
        this(itemWithTag, comparison, "");
    }

    private BanData(ItemWithTag itemWithTag, NbtComparator comparison, String banReason) {
        this.itemWithTag = itemWithTag;
        this.comparison = comparison;
        this.reason = banReason;
    }

    public static BanData of(Item item) {
        return new BanData(ItemWithTag.fromItem(item));
    }

    public static BanData of(ItemWithTag itemWithTag) {
        return new BanData(itemWithTag);
    }

    public static BanData of(ItemWithTag itemWithTag, NbtComparator comparison) {
        return new BanData(itemWithTag, comparison);
    }

    public static BanData of(ItemWithTag itemWithTag, NbtComparator comparison, String banReason) {
        return new BanData(itemWithTag, comparison, banReason);
    }

    public ItemWithTag getItemWithTag() {
        return itemWithTag;
    }

    public NbtComparator getComparison() {
        return comparison;
    }

    public String getReason() {
        return reason;
    }

    public Component getComponent() {
        if (cachedComponent == null) {
            MutableComponent component = Component.empty().append(itemWithTag.asStack().getDisplayName());
            if (!reason.isEmpty()) {
                component.append("{").append(ModComponents.BAN_REASON.create(Component.literal(reason))).append("}");
            }
            cachedComponent = component;
        }
        return cachedComponent;
    }

    public boolean isBasicItemBan() {
        return !itemWithTag.hasTag() || comparison == NbtComparator.NONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BanData banData = (BanData) o;
        return Objects.equals(itemWithTag, banData.itemWithTag) && comparison == banData.comparison;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemWithTag, comparison);
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return itemStack.is(itemWithTag.item()) && comparison.compareTags(itemStack.hasTag() ? itemStack.getTag() : new CompoundTag(), this.itemWithTag.tag());
    }
}
