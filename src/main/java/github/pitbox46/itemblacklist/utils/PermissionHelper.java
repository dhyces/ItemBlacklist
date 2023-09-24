package github.pitbox46.itemblacklist.utils;

import github.pitbox46.itemblacklist.core.BuiltInPermissions;
import github.pitbox46.itemblacklist.mixins.EntityAccessor;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PermissionHelper {
    public static boolean hasPermission(Player player, String permission) {
        if (BuiltInPermissions.isLevelPermission(permission)) {
            int permissionLevel;
            if (player instanceof ServerPlayer serverPlayer) {
                permissionLevel = serverPlayer.server.getProfilePermissions(player.getGameProfile());
            } else if (player != null) {
                permissionLevel = ((EntityAccessor)player).invokeGetPermissionLevel();
            } else {
                return true;
            }
            int minPermissionLevel = BuiltInPermissions.getFrom(permission);
            return permissionLevel > minPermissionLevel;
        }
        return Permissions.check(player, permission);
    }

    public static boolean hasPermission(CommandSourceStack sourceStack, String permission) {
        if (BuiltInPermissions.isLevelPermission(permission)) {
            int minPermissionLevel = Integer.decode(permission.substring(permission.length()-1));
            return sourceStack.hasPermission(minPermissionLevel);
        }
        return Permissions.check(sourceStack, permission);
    }
}
