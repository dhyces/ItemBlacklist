package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.BuiltInPermissions;
import github.pitbox46.itemblacklist.core.ItemWithTag;
import github.pitbox46.itemblacklist.core.ModComponents;
import github.pitbox46.itemblacklist.utils.FileUtils;
import github.pitbox46.itemblacklist.utils.PermissionHelper;
import github.pitbox46.itemblacklist.utils.Utils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static github.pitbox46.itemblacklist.core.ModCommands.*;

public class UnbanItemCommand {
    public static final String NULL_PERMISSION = "itemblacklist.commands.permissions.NULL";
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        return Commands
                .literal("unban")
                .requires(Permissions.require("itemblacklist.commands.ban", 2))
                .then(
                        item(buildContext, context -> {
                            ItemWithTag itemWithTag = ItemWithTag.fromInput(ItemArgument.getItem(context, ITEM_ARG));
                            return unban(context.getSource(), itemWithTag, NULL_PERMISSION);
                        })
                                .then(
                                        permission(context -> {
                                            ItemWithTag itemWithTag = ItemWithTag.fromInput(ItemArgument.getItem(context, ITEM_ARG));
                                            String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                            return unban(context.getSource(), itemWithTag, permission);
                                        })
                                )
                ).then(
                        Commands.literal("all")
                                .executes(context ->
                                    unbanAll(context.getSource(), NULL_PERMISSION)
                                )
                                .then(
                                        permission(context -> {
                                            String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                            return unbanAll(context.getSource(), permission);
                                        })
                                )
                );
    }

    private static int unban(CommandSourceStack commandSource, ItemWithTag ItemWithTag, String permission) {
        if (ItemWithTag.is(Items.AIR)) {
            return 0;
        }

        if (permission.equals(NULL_PERMISSION)) {
            if (!Permissions.check(commandSource, "itemblacklist.commands.unban_all", 4)) {
                commandSource.sendFailure(ModComponents.INADEQUATE_PERMS.create(permission));
                return 0;
            }
            ItemBlacklist.getConfig().unbanItem(ItemWithTag);
            Utils.broadcastMessage(commandSource.getServer(), ModComponents.ITEM_UNBANNED.create(ItemWithTag.asStack().getDisplayName()));
        } else {
            if (!PermissionHelper.hasPermission(commandSource, permission)) {
                commandSource.sendFailure(ModComponents.INADEQUATE_PERMS.create(permission));
                return 0;
            }
            ItemBlacklist.getConfig().unbanItem(permission, ItemWithTag);
            Utils.broadcastMessage(commandSource.getServer(), ModComponents.ITEM_UNBANNED_FOR.create(ItemWithTag.asStack().getDisplayName(), permission));
        }

        return SINGLE_SUCCESS;
    }

    private static int unbanAll(CommandSourceStack commandSource, String permission) {
        if (permission.equals(NULL_PERMISSION)) {
            if (!Permissions.check(commandSource, "itemblacklist.commands.unban_all", 4)) {
                commandSource.sendFailure(ModComponents.INADEQUATE_PERMS.create(permission));
                return 0;
            }
            ItemBlacklist.getConfig().unbanAllItems();
            Utils.broadcastMessage(commandSource.getServer(), ModComponents.ALL_ITEMS_UNBANNED.create());
        } else {
            if (!PermissionHelper.hasPermission(commandSource, permission)) {
                commandSource.sendFailure(ModComponents.INADEQUATE_PERMS.create(permission));
                return 0;
            }
            ItemBlacklist.getConfig().unbanAllItems(permission);
            Utils.broadcastMessage(commandSource.getServer(), ModComponents.ALL_ITEMS_UNBANNED_FOR.create(permission));
        }

        return SINGLE_SUCCESS;
    }
}
