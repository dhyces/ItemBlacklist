package github.pitbox46.itemblacklist.core;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import github.pitbox46.itemblacklist.commands.BanItemCommand;
import github.pitbox46.itemblacklist.commands.BanListCommand;
import github.pitbox46.itemblacklist.commands.UnbanItemCommand;
import github.pitbox46.itemblacklist.mixins.ItemInputAccessor;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

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

    public static final String ITEM_ARG = "item";
    public static ArgumentBuilder<CommandSourceStack, ?> item(CommandBuildContext buildContext, Command<CommandSourceStack> command) {
        return Commands.argument(ITEM_ARG, ItemArgument.item(buildContext))
                .executes(command);
    }
    public static final String PERMISSION_ARG = "permission";
    public static ArgumentBuilder<CommandSourceStack, ?> permission(Command<CommandSourceStack> command) {
        return Commands.argument(PERMISSION_ARG, StringArgumentType.word())
                .suggests(BuiltInPermissions::createSuggestions)
                .executes(command);
    }
    public static final String NBT_COMPARE_ARG = "nbt_compare";
    public static ArgumentBuilder<CommandSourceStack, ?> nbtComparison(Command<CommandSourceStack> command) {
        return Commands.argument(NBT_COMPARE_ARG, StringArgumentType.word())
                .suggests(NbtComparator::createSuggestions)
                .executes(command);

    }
    public static final String REASON_ARG = "reason";
    public static ArgumentBuilder<CommandSourceStack, ?> reason(Command<CommandSourceStack> command) {
        return Commands.argument(REASON_ARG, StringArgumentType.string()).executes(command);
    }
}
