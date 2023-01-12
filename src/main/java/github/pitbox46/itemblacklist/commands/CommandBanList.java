package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.ItemBlacklist;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CommandBanList implements Command<CommandSourceStack> {
    private static final CommandBanList CMD = new CommandBanList();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands
                .literal("list")
                .executes(CMD);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        context.getSource().getPlayerOrException().displayClientMessage(
                Component.literal("Items banned: ").append(ItemBlacklist.itemListToString(ItemBlacklist.BANNED_ITEMS)),
                false);
        return 0;
    }
}
