package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.BanData;
import github.pitbox46.itemblacklist.core.BuiltInPermissions;
import github.pitbox46.itemblacklist.core.ModCommands;
import github.pitbox46.itemblacklist.core.ModComponents;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BanListCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands.literal("list")
                .requires(Permissions.require("itemblacklist.commands.list", true))
                .executes(BanListCommand::list)
                .then(Commands.argument(ModCommands.PERMISSION_ARG, StringArgumentType.word()).executes(BanListCommand::listWithPermission));
    }

    private static int listWithPermission(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String permission = StringArgumentType.getString(context, ModCommands.PERMISSION_ARG);
        CommandSourceStack commandSource = context.getSource();

        commandSource.sendSystemMessage(createBannedItemsComponent(permission));
        return SINGLE_SUCCESS;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack commandSource = context.getSource();

        List<Component> components = Util.make(new ArrayList<>(), list -> {
            list.add(ModComponents.LIST_BANNED_ITEMS.create());
            list.add(createBannedItemsComponent(BuiltInPermissions.LEVEL_0));
            list.add(createBannedItemsComponent(BuiltInPermissions.LEVEL_1));
            list.add(createBannedItemsComponent(BuiltInPermissions.LEVEL_2));
            list.add(createBannedItemsComponent(BuiltInPermissions.LEVEL_3));
            list.add(createBannedItemsComponent(BuiltInPermissions.LEVEL_4));
            ItemBlacklist.getConfig().getAllPermissions().stream().sorted().forEach(s -> {
                if (!BuiltInPermissions.isLevelPermission(s)) {
                    list.add(createBannedItemsComponent(s));
                }
            });
        });
        commandSource.sendSystemMessage(CommonComponents.joinLines(components));

        return SINGLE_SUCCESS;
    }

    private static MutableComponent createBannedItemsComponent(String permission) {
        return appendToMessage(Component.literal(permission + ": "), permission);
    }

    private static MutableComponent appendToMessage(MutableComponent levelMessage, String permission) {
        List<Component> gathered = gatherComponents(permission);
        if (!gathered.isEmpty()) {
            levelMessage.append(ComponentUtils.formatList(gathered, Component.literal(", ")));
        } else {
            levelMessage.append("[]");
        }
        return levelMessage;
    }

    private static List<Component> gatherComponents(String permission) {
        if (BuiltInPermissions.isLevelPermission(permission)) {
            return ItemBlacklist.getConfig().getRecursiveBannedItems(permission).stream()
                    .map(BanData::getComponent)
                    .toList();
        } else {
            return ItemBlacklist.getConfig().getBanData(permission).stream().map(BanData::getComponent).toList();
        }
    }
}