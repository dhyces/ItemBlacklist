package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMixin {
    @Shadow @Final public NonNullList<Slot> slots;

    @Shadow public abstract NonNullList<ItemStack> getItems();

    @Inject(at = @At(value = "HEAD"), method = "broadcastChanges")
    public void onDetectAndSendChanges(CallbackInfo ci) {
        for(int i = 0; i < this.slots.size(); ++i) {
            if(ItemBlacklist.shouldDelete(this.slots.get(i).getItem())) {
                this.slots.get(i).set(ItemStack.EMPTY);
            }
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "removed")
    public void onContainerClosed(Player playerIn, CallbackInfo ci) {
        for(int i = 0; i < this.slots.size(); ++i) {
            if(ItemBlacklist.shouldDelete(this.getItems().get(i))) {
                this.getItems().set(i, ItemStack.EMPTY);
            }
        }
        for(int i = 0; i < playerIn.getInventory().getContainerSize(); ++i) {
            if(playerIn instanceof ServerPlayer && !playerIn.hasPermissions(playerIn.getServer().getOperatorUserPermissionLevel()) && ItemBlacklist.shouldDelete(playerIn.getInventory().getItem(i))) {
                playerIn.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
