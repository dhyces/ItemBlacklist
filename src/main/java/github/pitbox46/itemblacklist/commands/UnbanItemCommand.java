package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.BuiltInPermissions;
import github.pitbox46.itemblacklist.core.ModComponents;
import github.pitbox46.itemblacklist.utils.FileUtils;
import github.pitbox46.itemblacklist.utils.PermissionHelper;
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
import static github.pitbox46.itemblacklist.core.ModCommands.*;

public class UnbanItemCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands
                .literal("unban")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument(ITEM_ARG, ItemArgument.item(context))
                        .executes(UnbanItemCommand::unbanItem).then(Commands.argument(PERMISSION_ARG, IntegerArgumentType.integer(0, 4))
                                        .executes(UnbanItemCommand::unbanItemWithPermission)
                        )
                ).then(Commands.literal("all")
                        .executes(UnbanItemCommand::unbanAll).then(Commands.argument(PERMISSION_ARG, IntegerArgumentType.integer(0, 4))
                                .executes(UnbanItemCommand::unbanAllWithPermission)
                        ));
    }

    private static int unbanItemWithPermission(CommandContext<CommandSourceStack> context) {
        try {
            ItemStack stack = ItemArgument.getItem(context, ITEM_ARG).createItemStack(1, false);
            String permission = StringArgumentType.getString(context, PERMISSION_ARG);

            removeItemWithPermission(context.getSource(), permission, stack);
        } catch(IndexOutOfBoundsException | CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("The item could not be unbanned."));
        }
        return SINGLE_SUCCESS;
    }

    private static int unbanItem(CommandContext<CommandSourceStack> context) {
        try {
            ItemInput input = ItemArgument.getItem(context, ITEM_ARG);
            removeItem(context.getSource(), input.createItemStack(1, false));
        } catch(IndexOutOfBoundsException | CommandSyntaxException e) {
            context.getSource().sendFailure(Component.literal("The item could not be unbanned."));
        }
        return SINGLE_SUCCESS;
    }

    private static int unbanAll(CommandContext<CommandSourceStack> context) {
        checkPermission(context.getSource(), BuiltInPermissions.LEVEL_4);

        ItemBlacklist.getConfig().unbanAllItems();
        Utils.broadcastMessage(context.getSource().getServer(), ModComponents.ALL_ITEMS_UNBANNED.create());
        return SINGLE_SUCCESS;
    }

    private static int unbanAllWithPermission(CommandContext<CommandSourceStack> context) {
        String permission = StringArgumentType.getString(context, PERMISSION_ARG);
        checkPermission(context.getSource(), permission);

        ItemBlacklist.getConfig().unbanAllItems(permission);
        Utils.broadcastMessage(context.getSource().getServer(), ModComponents.ALL_ITEMS_UNBANNED_FOR.create(permission));
        return SINGLE_SUCCESS;
    }

    private static void removeItem(CommandSourceStack commandSource, ItemStack itemStack) {
        checkPermission(commandSource, BuiltInPermissions.LEVEL_4);

        ItemBlacklist.getConfig().unbanItem(itemStack);
        Utils.broadcastMessage(commandSource.getServer(), ModComponents.ITEM_UNBANNED.create(itemStack.getDisplayName()));
    }

    private static void removeItemWithPermission(CommandSourceStack commandSource, String permission, ItemStack itemStack) {
        checkPermission(commandSource, permission);

        ItemBlacklist.getConfig().unbanItem(permission, itemStack);
        Utils.broadcastMessage(commandSource.getServer(), ModComponents.ITEM_UNBANNED_FOR.create(itemStack.getDisplayName(), permission));
    }

    private static void checkPermission(CommandSourceStack commandSource, String permission) {
        if (!PermissionHelper.hasPermission(commandSource, permission)) {
            throw new CommandRuntimeException(ModComponents.INADEQUATE_PERMS.create(permission));
        }
    }
}
