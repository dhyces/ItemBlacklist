package github.pitbox46.itemblacklist.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ServerSavedCallback {
    Event<ServerSaved> EVENT = EventFactory.createArrayBacked(ServerSaved.class, callbacks -> (suppressLog, flush, forced) -> {
        for (ServerSaved callback : callbacks) {
            callback.onServerSaved(suppressLog, flush, forced);
        }
    });

    @FunctionalInterface
    interface ServerSaved {
        void onServerSaved(boolean suppressLog, boolean flush, boolean forced);
    }
}
