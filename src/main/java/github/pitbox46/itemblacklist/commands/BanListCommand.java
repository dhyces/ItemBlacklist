package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.Config;
import github.pitbox46.itemblacklist.core.PermissionLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BanListCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("list")
                .executes(BanListCommand::list)
                .then(Commands.argument("permission_level", IntegerArgumentType.integer(0, 4)).executes(BanListCommand::listWithPermission));
    }

    private static int listWithPermission(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int permissionLevel = IntegerArgumentType.getInteger(context, "permission_level");
        CommandSourceStack commandSource = context.getSource();

        commandSource.sendSuccess(createBannedItemsComponent(PermissionLevel.getFromInt(permissionLevel)), false);
        return SINGLE_SUCCESS;
    }

    private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack commandSource = context.getSource();

        commandSource.sendSuccess(new TextComponent("Items banned: "), false);
        commandSource.sendSuccess(createBannedItemsComponent(PermissionLevel.LEVEL_0), false);
        commandSource.sendSuccess(createBannedItemsComponent(PermissionLevel.LEVEL_1), false);
        commandSource.sendSuccess(createBannedItemsComponent(PermissionLevel.LEVEL_2), false);
        commandSource.sendSuccess(createBannedItemsComponent(PermissionLevel.LEVEL_3), false);
        commandSource.sendSuccess(createBannedItemsComponent(PermissionLevel.LEVEL_4), false);
        return SINGLE_SUCCESS;
    }

    private static MutableComponent createBannedItemsComponent(PermissionLevel permissionLevel) {
        return appendToMessage(new TextComponent(permissionLevel.getUserFriendlyName() + " Permission Banned Items: "), permissionLevel);
    }

    private static MutableComponent appendToMessage(MutableComponent levelMessage, PermissionLevel permissionLevel) {
        List<Component> gathered = gatherComponents(permissionLevel);
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

    private static List<Component> gatherComponents(PermissionLevel permissionLevel) {
        return Config.getInstance().getAllBannedItems(permissionLevel).stream()
                .map(data -> data.asStack().getDisplayName())
                .toList();
    }
}
