package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.PermissionLevel;
import github.pitbox46.itemblacklist.utils.FileUtils;
import github.pitbox46.itemblacklist.utils.Utils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BanItemCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands
                .literal("ban")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("item", ItemArgument.item(context)).executes(BanItemCommand::banItem)
                        .then(Commands.argument("permission_level", IntegerArgumentType.integer(0, 4)).executes(BanItemCommand::banItemWithPermission))
                )
                .then(Commands.literal("hand").executes(BanItemCommand::banHand)
                        .then(Commands.argument("permission_level", IntegerArgumentType.integer(0, 4)).executes(BanItemCommand::banHandWithPermission))
                );
    }

    private static int banHand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        int retVal = removeItem(context.getSource(), new ItemInput(stack.getItemHolder(), stack.getTag()), PermissionLevel.LEVEL_4);
        if (!player.hasPermissions(5)) {
            stack.setCount(0);
        }
        return retVal;
    }

    private static int banHandWithPermission(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int permissionLevel = IntegerArgumentType.getInteger(context, "permission_level");

        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        int retVal = removeItem(context.getSource(), new ItemInput(stack.getItemHolder(), stack.getTag()), PermissionLevel.getFromInt(permissionLevel));
        if (!player.hasPermissions(permissionLevel+1)) {
            stack.setCount(0);
        }
        return retVal;
    }

    private static int banItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return removeItem(context.getSource(), ItemArgument.getItem(context, "item"), PermissionLevel.LEVEL_4);
    }

    private static int banItemWithPermission(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return removeItem(context.getSource(), ItemArgument.getItem(context, "item"), PermissionLevel.getFromInt(IntegerArgumentType.getInteger(context, "permission_level")));
    }

    private static int removeItem(CommandSourceStack commandSource, ItemInput input, PermissionLevel permissionLevel) throws CommandSyntaxException {
        if(input.getItem() == Items.AIR)
            return 0;

        boolean hasPermission = commandSource.hasPermission(permissionLevel.ordinal());
        if (!hasPermission) {
            commandSource.sendFailure(Component.literal("Command source does not have adequate permissions. Must have at least permission level " + permissionLevel.ordinal()));
            return 0;
        }

        ItemStack itemStack = input.createItemStack(1, false);
        FileUtils.appendItemAndSave(ItemBlacklist.banList, permissionLevel, itemStack);
        PlayerList playerList = commandSource.getServer().getPlayerList();
        Component itemComponent = itemStack.getDisplayName();
        Utils.broadcastMessage(commandSource.getServer(),
                Component.literal("Item banned: ").append(itemComponent).append(" for players " + permissionLevel + " and under"));
        for(ServerPlayer player : playerList.getPlayers()) {
            for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if(ItemBlacklist.shouldDelete(player, player.getInventory().getItem(i)))
                    player.getInventory().getItem(i).setCount(0);
            }
        }
        return SINGLE_SUCCESS;
    }
}
