package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.Config;
import github.pitbox46.itemblacklist.core.BuiltInPermissions;
import github.pitbox46.itemblacklist.core.ModCommands;
import github.pitbox46.itemblacklist.core.ModComponents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BanListCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands.literal("list")
                .executes(BanListCommand::list)
                .then(Commands.argument(ModCommands.PERMISSION_ARG, StringArgumentType.word()).executes(BanListCommand::listWithPermission));
    }

    private static int listWithPermission(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String permission = StringArgumentType.getString(context, ModCommands.PERMISSION_ARG);
        CommandSourceStack commandSource = context.getSource();

        commandSource.sendSystemMessage(createBannedItemsComponent(permission));
        return SINGLE_SUCCESS;
    }

    private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack commandSource = context.getSource();

        commandSource.sendSystemMessage(ModComponents.LIST_BANNED_ITEMS.create());
        ItemBlacklist.getConfig().getAllPermissions().stream().sorted().forEach(s -> {
            commandSource.sendSystemMessage(createBannedItemsComponent(s));
        });

        return SINGLE_SUCCESS;
    }

    private static MutableComponent createBannedItemsComponent(String permission) {
        return appendToMessage(Component.literal(permission + " "), permission);
    }

    private static MutableComponent appendToMessage(MutableComponent levelMessage, String permission) {
        List<Component> gathered = gatherComponents(permission);
        if (!gathered.isEmpty()) {
            levelMessage.append(gathered.get(0));
            for (int i = 1; i < gathered.size(); i++) {
                levelMessage.append(", ").append(gathered.get(i));
            }
        } else {
            levelMessage.append("[]");
        }
        return levelMessage;
    }

    private static List<Component> gatherComponents(String permission) {
        if (BuiltInPermissions.isLevelPermission(permission)) {
            return ItemBlacklist.getConfig().getRecursiveBannedItems(permission).stream()
                    .map(data -> data.getStack().getDisplayName())
                    .toList();
        } else {
            return ItemBlacklist.getConfig().getBanData(permission).stream().map(data -> data.getStack().getDisplayName()).toList();
        }
    }
}