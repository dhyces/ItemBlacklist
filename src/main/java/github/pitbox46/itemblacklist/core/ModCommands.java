package github.pitbox46.itemblacklist.core;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.pitbox46.itemblacklist.commands.BanItemCommand;
import github.pitbox46.itemblacklist.commands.BanListCommand;
import github.pitbox46.itemblacklist.commands.UnbanItemCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        LiteralCommandNode<CommandSourceStack> cmdTut = dispatcher.register(
                Commands.literal("itemblacklist")
                        .then(BanItemCommand.register(dispatcher, context))
                        .then(UnbanItemCommand.register(dispatcher, context))
                        .then(BanListCommand.register(dispatcher, context))
        );

        dispatcher.register(Commands.literal("ib").redirect(cmdTut));
    }
}
