package github.pitbox46.itemblacklist.mixins;

import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemInput.class)
public interface ItemInputAccessor {

    @Accessor
    CompoundTag getTag();
}
