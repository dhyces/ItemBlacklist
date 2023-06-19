package github.pitbox46.itemblacklist.utils;

import github.pitbox46.itemblacklist.core.BuiltInPermissions;
import github.pitbox46.itemblacklist.mixins.EntityAccessor;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PermissionHelper {
    public static boolean hasPermission(Player player, String permission) {
        if (BuiltInPermissions.LEVEL_PATTERN.matcher(permission).matches()) {
            int permissionLevel;
            if (player instanceof ServerPlayer serverPlayer) {
                permissionLevel = serverPlayer.server.getProfilePermissions(player.getGameProfile());
            } else {
                permissionLevel = ((EntityAccessor)player).invokeGetPermissionLevel();
            }
            int minPermissionLevel = BuiltInPermissions.getFrom(permission);
            return permissionLevel >= minPermissionLevel;
        }
        return Permissions.check(player, permission);
    }

    public static boolean hasPermission(CommandSourceStack sourceStack, String permission) {
        if (BuiltInPermissions.LEVEL_PATTERN.matcher(permission).matches()) {
            int minPermissionLevel = Integer.decode(String.valueOf(permission.charAt(permission.length()-1)));
            return sourceStack.hasPermission(minPermissionLevel);
        }
        return Permissions.check(sourceStack, permission);
    }
}
