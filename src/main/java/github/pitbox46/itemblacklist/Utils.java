package github.pitbox46.itemblacklist;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;

public class Utils {
    public static void broadcastMessage(MinecraftServer server, Component component) {
        Optional<Registry<ChatType>> registryOptional = server.overworld().registryAccess().registry(Registries.CHAT_TYPE);
        if (registryOptional.isEmpty()) {
            throw new IllegalStateException("Dynamic registry of type {ChatType} was not found");
        }
        Registry<ChatType> reg = registryOptional.get();
        ChatType.Bound bound = new ChatType.Bound(reg.get(ChatType.CHAT), Component.literal("name"), null);
        server.getPlayerList().broadcastChatMessage(PlayerChatMessage.system(component.getString()), server.createCommandSourceStack(), bound);
    }
}
