package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.JsonUtils;
import github.pitbox46.itemblacklist.Utils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

public class CommandUnbanItem implements Command<CommandSourceStack> {
    private static final CommandUnbanItem CMD = new CommandUnbanItem();
    private static final UnbanAll CMD_ALL = new UnbanAll();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands
                .literal("unban")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("item", ItemArgument.item(context))
                        .executes(CMD))
                .then(Commands.literal("all")
                        .executes(CMD_ALL));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        try {
            JsonUtils.removeItemFromJson(ItemBlacklist.BANLIST, ItemArgument.getItem(context, "item").getItem());
            Utils.broadcastMessage(context.getSource().getServer(),
                    Component.literal("Item unbanned: ")
                            .append(BuiltInRegistries.ITEM.getKey(ItemArgument.getItem(context, "item").getItem()).toString()));
        } catch(IndexOutOfBoundsException e) {
            context.getSource().sendFailure(Component.literal("The item could not be unbanned."));
        }
        return 0;
    }

    public static class UnbanAll implements Command<CommandSourceStack>{
        @Override
        public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            JsonUtils.removeAllItemsFromJson(ItemBlacklist.BANLIST);
            Utils.broadcastMessage(context.getSource().getServer(), Component.literal("All items unbanned"));
            return 0;
        }
    }
}
