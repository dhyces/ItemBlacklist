package github.pitbox46.itemblacklist.core;

import com.mojang.serialization.Codec;
import github.pitbox46.itemblacklist.utils.Utils;
import net.minecraft.nbt.*;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.function.ToBooleanBiFunction;

public enum NbtComparator implements StringRepresentable {
    // Both tags contain all the same tag data
    STRICT("strict", CompoundTag::equals),
    // The tested tag contains all the data from the other tag. It may have more, but must at least match the other data
    PARTIAL("partial", (tag, tag2) -> {
        for (String key : tag2.getAllKeys()) {
            if (!tag.contains(key)) {
                return false;
            }

            if (!Utils.areTagsSimilar(tag.get(key), tag2.get(key))) {
                return false;
            }
        }
        return true;
    }),
    // Doesn't test, returns true
    NONE("none", TagComparison.alwaysTrue());

    private final String safeName;
    private final ToBooleanBiFunction<CompoundTag, CompoundTag> tagComparison;

    NbtComparator(String safeName, ToBooleanBiFunction<CompoundTag, CompoundTag> tagComparison) {
        this.safeName = safeName;
        this.tagComparison = tagComparison;
    }

    public static final Codec<NbtComparator> CODEC = StringRepresentable.fromEnum(NbtComparator::values);

    public static NbtComparator fromName(String name) {
        return switch (name) {
            case "strict" -> STRICT;
            case "partial" -> PARTIAL;
            case "none" -> NONE;
            default -> throw new IllegalArgumentException("No comparator found for " + name);
        };
    }

    public boolean compareTags(CompoundTag testStackTag, CompoundTag otherTag) {
        if (testStackTag == otherTag) {
            return true;
        }
        if (testStackTag == null || otherTag == null) {
            return false;
        }
        return tagComparison.applyAsBoolean(testStackTag, otherTag);
    }

    @Override
    public String getSerializedName() {
        return safeName;
    }

    public interface TagComparison extends ToBooleanBiFunction<CompoundTag, CompoundTag> {
        static ToBooleanBiFunction<CompoundTag, CompoundTag> alwaysTrue() {
            return (compoundTag, compoundTag2) -> true;
        }
    }
}
