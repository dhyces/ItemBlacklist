package github.pitbox46.itemblacklist.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Fired whenever an item stack is looked at for deletion. Listeners wishing to have higher precedence should register
 * with a phase.
 */
public interface BanItemEvent {
    Event<BanItemCallback> EVENT = EventFactory.createWithPhases(BanItemCallback.class, banItemCallbacks -> (player, stack, original) -> {
        boolean result = original;
        for (BanItemCallback callback : banItemCallbacks) {
            result = callback.onBannedItem(player, stack, original);
        }
        return result;
    }, Event.DEFAULT_PHASE);

    @FunctionalInterface
    interface BanItemCallback {
        /**
         *
         * @param player Player that is being checked for permissions
         * @param stack ItemStack that is being checked for removal
         * @param original Original state. If it's true, the item would be deleted, if false, the item would not have been deleted
         * @return Whether to succeed with deletion of the item
         */
        boolean onBannedItem(@Nullable Player player, ItemStack stack, boolean original);
    }
}
