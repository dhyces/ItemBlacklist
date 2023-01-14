package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.Config;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.PermissionLevel;
import github.pitbox46.itemblacklist.utils.FileUtils;
import github.pitbox46.itemblacklist.utils.Utils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class UnbanItemCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands
                .literal("unban")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("item", ItemArgument.item(context))
                        .executes(UnbanItemCommand::unbanItem).then(Commands.argument("permission_level", IntegerArgumentType.integer(0, 4))
                                        .executes(UnbanItemCommand::unbanItemWithPermission)
                        )
                ).then(Commands.literal("all")
                        .executes(UnbanItemCommand::unbanAll).then(Commands.argument("permission_level", IntegerArgumentType.integer(0, 4))
                                .executes(UnbanItemCommand::unbanAllWithPermission)
                        ));
    }

    private static int unbanItemWithPermission(CommandContext<CommandSourceStack> context) {
        try {
            ItemStack stack = ItemArgument.getItem(context, "item").createItemStack(1, false);
            int toLevel = IntegerArgumentType.getInteger(context, "permission_level");

            removeItemWithPermission(context.getSource(), PermissionLevel.getFromInt(toLevel), stack);
        } catch(IndexOutOfBoundsException | CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("The item could not be unbanned."));
        }
        return SINGLE_SUCCESS;
    }

    private static int unbanItem(CommandContext<CommandSourceStack> context) {
        try {
            ItemInput input = ItemArgument.getItem(context, "item");
            removeItem(context.getSource(), input.createItemStack(1, false));
        } catch(IndexOutOfBoundsException | CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("The item could not be unbanned."));
        }
        return SINGLE_SUCCESS;
    }

    private static int unbanAll(CommandContext<CommandSourceStack> context) {
        PermissionLevel permsNeeded = getHighestPermsNeeded();
        checkPermission(context.getSource(), permsNeeded);

        FileUtils.removeAllItemsAndSave(ItemBlacklist.banList);
        Utils.broadcastMessage(context.getSource().getServer(), Component.literal("All items unbanned"));
        return SINGLE_SUCCESS;
    }

    private static int unbanAllWithPermission(CommandContext<CommandSourceStack> context) {
        int intLevel = IntegerArgumentType.getInteger(context, "permission_level");
        PermissionLevel permissionLevel = PermissionLevel.getFromInt(intLevel);
        checkPermission(context.getSource(), permissionLevel);

        FileUtils.removeAllItemsFromPermissionAndSave(ItemBlacklist.banList, permissionLevel);
        Utils.broadcastMessage(context.getSource().getServer(), Component.literal("All items unbanned for " + permissionLevel.getUserFriendlyName()));
        return SINGLE_SUCCESS;
    }

    private static void removeItemWithPermission(CommandSourceStack commandSource, PermissionLevel permissionLevel, ItemStack itemStack) {
        checkPermission(commandSource, permissionLevel);

        FileUtils.removeDownToAndSave(ItemBlacklist.banList, permissionLevel, itemStack);
        Utils.broadcastMessage(commandSource.getServer(), Component.literal("Item unbanned: ").append(itemStack.getDisplayName()).append(" for " + permissionLevel.getUserFriendlyName()));
    }

    private static void removeItem(CommandSourceStack commandSource, ItemStack itemStack) {
        checkPermission(commandSource, getHighestPermsNeeded());

        FileUtils.removeItemAndSave(ItemBlacklist.banList, itemStack);
        Utils.broadcastMessage(commandSource.getServer(), Component.literal("Item unbanned: ").append(itemStack.getDisplayName()));
    }

    private static PermissionLevel getHighestPermsNeeded() {
        for (PermissionLevel i = PermissionLevel.LEVEL_4; !i.equals(PermissionLevel.LEVEL_0); i = i.lower()) {
            if (!Config.getInstance().getBannedItems(i).isEmpty()) {
                return i;
            }
        }
        return PermissionLevel.LEVEL_0;
    }

    private static void checkPermission(CommandSourceStack commandSource, PermissionLevel permissionLevel) {
        if (!commandSource.hasPermission(permissionLevel.ordinal())) {
            throw new CommandRuntimeException(Component.literal("You do not have sufficient permissions. Required: " + permissionLevel.getUserFriendlyName()));
        }
    }
}
