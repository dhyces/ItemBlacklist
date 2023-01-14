package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    @Shadow @Nullable
    protected Level level;

    @Inject(at = @At(value = "HEAD"), method = "setChanged()V")
    public void onMarkDirty(CallbackInfo ci) {
        if(level != null) {
            if (this instanceof Container thiz) {
                for (int i = 0; i < thiz.getContainerSize(); i++) {
                    if (ItemBlacklist.shouldDelete(null, thiz.getItem(i))) {
                        thiz.setItem(i, ItemStack.EMPTY);
                    }
                }
            } else {
                // TODO: This was a capability handler for items, should implement CardinalComponents integration
            }
        }
    }
}
