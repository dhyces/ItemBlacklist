package github.pitbox46.itemblacklist.core;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.pitbox46.itemblacklist.commands.BanItemCommand;
import github.pitbox46.itemblacklist.commands.BanListCommand;
import github.pitbox46.itemblacklist.commands.UnbanItemCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> cmdTut = dispatcher.register(
                Commands.literal("itemblacklist")
                        .then(BanItemCommand.register(dispatcher))
                        .then(UnbanItemCommand.register(dispatcher))
                        .then(BanListCommand.register(dispatcher))
        );

        dispatcher.register(Commands.literal("itemblacklist").redirect(cmdTut));
    }
}
