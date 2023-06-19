package github.pitbox46.itemblacklist.mixins;

import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.BanData;
import github.pitbox46.itemblacklist.utils.Utils;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;

@Mixin(AbstractContainerMenu.class)
public abstract class ContainerMenuMixin {
    @Shadow @Final public NonNullList<Slot> slots;

    @Shadow public abstract NonNullList<ItemStack> getItems();

    @Inject(at = @At(value = "HEAD"), method = "broadcastChanges")
    public void onDetectAndSendChanges(CallbackInfo ci) {
        AbstractContainerMenu thiz = ((AbstractContainerMenu)(Object)this);
        boolean isInventory = thiz instanceof InventoryMenu;
        Player nullableOwner = isInventory ? ((InventoryMenuAccessor)thiz).getOwner() : Utils.getPlayer(thiz);
        Deque<Component> bannedItems = new LinkedList<>();
        for (int i = 0; i < this.slots.size(); ++i) {
            if (!this.slots.get(i).hasItem()) {
                continue;
            }
            Set<BanData> data = ItemBlacklist.getConfig().getBanData(nullableOwner, this.slots.get(i).getItem());
            if (!data.isEmpty()) {
                if (isInventory) {
                    data.forEach(data1 -> bannedItems.add(data1.getComponent()));
                }
                this.slots.get(i).set(ItemStack.EMPTY);
            }
        }
        if (isInventory && nullableOwner instanceof ServerPlayer && !bannedItems.isEmpty()) {
            MutableComponent message = Component.literal("");
            boolean isSingleItem = bannedItems.size() == 1;
            message.append(bannedItems.pop());
            for (Component item : bannedItems) {
                message.append(", ");
                message.append(item);
            }
            nullableOwner.displayClientMessage(message.append((isSingleItem ? " is" : " are") + " banned and has been removed from your inventory"), false);
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "removed")
    public void onContainerClosed(Player playerIn, CallbackInfo ci) {
        if (!(playerIn instanceof ServerPlayer player)) return;
        for (int i = 0; i < this.slots.size(); ++i) {
            if (ItemBlacklist.shouldDelete(player, this.getItems().get(i))) {
                this.getItems().set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < playerIn.getInventory().getContainerSize(); ++i) {
            if (ItemBlacklist.shouldDelete(player, playerIn.getInventory().getItem(i))) {
                playerIn.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
