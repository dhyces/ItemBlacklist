package github.pitbox46.itemblacklist.core;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import github.pitbox46.itemblacklist.utils.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.*;
import net.minecraft.util.StringRepresentable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;

public enum NbtComparator implements StringRepresentable {
    // Both tags contain all the same tag data
    STRICT("strict", CompoundTag::equals),
    // The tested tag contains all the data from the other tag. It may have more, but must at least match the other data
    PARTIAL("partial", (stackTag, configTag) -> {
        for (String key : configTag.getAllKeys()) {
            if (!stackTag.contains(key)) {
                return false;
            }

            if (!Utils.areTagsSimilar(stackTag.get(key), configTag.get(key))) {
                return false;
            }
        }
        return true;
    }),
    // Doesn't test, returns true
    NONE("none", TagComparison.alwaysTrue());

    private final String safeName;
    private final BiPredicate<CompoundTag, CompoundTag> tagComparison;

    NbtComparator(String safeName, BiPredicate<CompoundTag, CompoundTag> tagComparison) {
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

    public boolean compareTags(CompoundTag testStackTag, CompoundTag configTag) {
        if (testStackTag == configTag) {
            return true;
        }
        if (testStackTag == null || configTag == null) {
            return false;
        }
        return tagComparison.test(testStackTag, configTag);
    }

    @Override
    public String getSerializedName() {
        return safeName;
    }

    public static CompletableFuture<Suggestions> createSuggestions(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return builder
                .suggest("none", () -> "Only compares the item")
                .suggest("partial", () -> "NBT on this item is matched on tested, but the tested item can have more NBT")
                .suggest("strict", () -> "All NBT on this item must be present on the tested item")
                .buildFuture();
    }

    public interface TagComparison extends BiPredicate<CompoundTag, CompoundTag> {
        static BiPredicate<CompoundTag, CompoundTag> alwaysTrue() {
            return (compoundTag, compoundTag2) -> true;
        }
    }
}
