package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin {
    @Inject(at = @At(value = "RETURN"), method = "getOffers", cancellable = true)
    private void getOffers(CallbackInfoReturnable<MerchantOffers> cir) {
        if(cir.getReturnValue() != null) {
            MerchantOffers returnedOffers = new MerchantOffers(Util.make(new CompoundTag(), tag -> tag.put("Recipes", new ListTag())));
            cir.getReturnValue().forEach(offer -> {
                if(!ItemBlacklist.shouldDelete(null, offer.assemble()))
                    returnedOffers.add(offer);
            });
            cir.setReturnValue(returnedOffers);
        }
    }
}
