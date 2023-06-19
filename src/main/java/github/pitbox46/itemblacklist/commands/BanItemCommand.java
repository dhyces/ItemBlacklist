package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.BuiltInPermissions;
import github.pitbox46.itemblacklist.core.ModComponents;
import github.pitbox46.itemblacklist.core.NbtComparator;
import github.pitbox46.itemblacklist.utils.PermissionHelper;
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
import static github.pitbox46.itemblacklist.core.ModCommands.*;

public class BanItemCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        return Commands
                .literal("ban")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument(ITEM_ARG, ItemArgument.item(buildContext))
                        .executes(context -> {
                            ItemInput item = ItemArgument.getItem(context, ITEM_ARG);
                            return ban(context.getSource(), BuiltInPermissions.LEVEL_4, item.createItemStack(1, false), NbtComparator.NONE, "");
                        })
                        .then(
                                permission(context -> {
                                    ItemInput item = ItemArgument.getItem(context, ITEM_ARG);
                                    String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                    return ban(context.getSource(), permission, item.createItemStack(1, false), NbtComparator.NONE, "");
                                })
                                        .then(
                                                reason(context -> {
                                                    ItemInput item = ItemArgument.getItem(context, ITEM_ARG);
                                                    String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                                    String reason = StringArgumentType.getString(context, REASON_ARG);
                                                    return ban(context.getSource(), permission, item.createItemStack(1, false), NbtComparator.NONE, reason);
                                                })
                                                        .then(
                                                                nbtComparison(context -> {
                                                                    ItemInput item = ItemArgument.getItem(context, ITEM_ARG);
                                                                    String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                                                    String reason = StringArgumentType.getString(context, REASON_ARG);
                                                                    NbtComparator nbtComparator = NbtComparator.fromName(StringArgumentType.getString(context, NBT_COMPARE_ARG));
                                                                    return ban(context.getSource(), permission, item.createItemStack(1, false), nbtComparator, reason);
                                                                })
                                                        )
                                        )
                        )
                )
                .then(Commands.literal("hand")
                        .executes(context -> {
                            ItemStack stack = context.getSource().getPlayerOrException().getMainHandItem();
                            return ban(context.getSource(), BuiltInPermissions.LEVEL_4, stack, NbtComparator.NONE, "");
                        })
                        .then(
                                permission(context -> {
                                            ItemStack stack = context.getSource().getPlayerOrException().getMainHandItem();
                                            String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                            return ban(context.getSource(), permission, stack, NbtComparator.NONE, "");
                                })
                                        .then(
                                                reason(context -> {
                                                    ItemStack stack = context.getSource().getPlayerOrException().getMainHandItem();
                                                    String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                                    String reason = StringArgumentType.getString(context, REASON_ARG);
                                                    return ban(context.getSource(), permission, stack, NbtComparator.NONE, reason);
                                                })
                                                        .then(
                                                                nbtComparison(context -> {
                                                                    ItemStack stack = context.getSource().getPlayerOrException().getMainHandItem();
                                                                    String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                                                    String reason = StringArgumentType.getString(context, REASON_ARG);
                                                                    NbtComparator nbtComparator = NbtComparator.fromName(StringArgumentType.getString(context, NBT_COMPARE_ARG));
                                                                    return ban(context.getSource(), permission, stack, nbtComparator, reason);
                                                                })
                                                        )
                                        )
                        )
                );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> permission(Command<CommandSourceStack> command) {
        return Commands.argument(PERMISSION_ARG, StringArgumentType.word()).executes(command);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> nbtComparison(Command<CommandSourceStack> command) {
        return Commands.argument(NBT_COMPARE_ARG, StringArgumentType.word()).executes(command);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> reason(Command<CommandSourceStack> command) {
        return Commands.argument(REASON_ARG, StringArgumentType.string()).executes(command);
    }

    private static int banHand(CommandSourceStack commandSource, String permission, ItemStack stack, NbtComparator comparator, String reason) throws CommandSyntaxException {
        int retVal = ban(commandSource, permission, stack, comparator, reason);
        if (!PermissionHelper.hasPermission(commandSource, permission)) {
            stack.setCount(0);
        }
        return retVal;
    }

    private static int ban(CommandSourceStack commandSource, String permission, ItemStack stack, NbtComparator comparator, String reason) throws CommandSyntaxException {
        if(stack.getItem() == Items.AIR)
            return 0;

        boolean hasPermission = PermissionHelper.hasPermission(commandSource, permission);
        if (!hasPermission) {
            commandSource.sendFailure(ModComponents.INADEQUATE_PERMS.create(permission));
            return 0;
        }

        ItemBlacklist.getConfig().banItem(permission, stack, comparator, reason);
        PlayerList playerList = commandSource.getServer().getPlayerList();
        Component itemComponent = stack.getDisplayName();
        Utils.broadcastMessage(commandSource.getServer(), getBanComponent(itemComponent, permission));
        for(ServerPlayer player : playerList.getPlayers()) {
            for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if(ItemBlacklist.shouldDelete(player, player.getInventory().getItem(i)))
                    player.getInventory().getItem(i).setCount(0);
            }
        }
        return SINGLE_SUCCESS;
    }

    private static Component getBanComponent(Component itemComponent, String permission) {
        return BuiltInPermissions.isLevelPermission(permission)
                ? ModComponents.ITEM_BANNED_FOR_LEVELS.create(itemComponent, permission)
                : ModComponents.ITEM_BANNED.create(itemComponent, permission);
    }
}
