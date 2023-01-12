package github.pitbox46.itemblacklist;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;

/**
 * Fired whenever an item stack is looked at for deletion. Listeners wishing to have higher precedence should register
 * with a phase.
 */
public interface BanItemEvent {
    Event<BanItemCallback> EVENT = EventFactory.createWithPhases(BanItemCallback.class, banItemCallbacks -> stack -> {
        boolean result = true; // By default, should ban. We see if it's blocked by an event.
        for (BanItemCallback callback : banItemCallbacks) {
            result = callback.onBannedItem(stack);
        }
        return result;
    }, Event.DEFAULT_PHASE);

    @FunctionalInterface
    interface BanItemCallback {
        boolean onBannedItem(ItemStack stack);
    }
}
