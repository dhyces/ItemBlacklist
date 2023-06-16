package github.pitbox46.itemblacklist.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientSidedUtils {
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}
