package github.pitbox46.itemblacklist.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.core.BuiltInPermissions;
import github.pitbox46.itemblacklist.core.ItemWithTag;
import github.pitbox46.itemblacklist.core.ModComponents;
import github.pitbox46.itemblacklist.core.NbtComparator;
import github.pitbox46.itemblacklist.utils.PermissionHelper;
import github.pitbox46.itemblacklist.utils.Utils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.Items;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static github.pitbox46.itemblacklist.core.ModCommands.*;

public class BanItemCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        return Commands
                .literal("ban")
                .requires(Permissions.require("itemblacklist.commands.ban", 2))
                .then(Commands.argument(ITEM_ARG, ItemArgument.item(buildContext))
                        .executes(context -> {
                            ItemWithTag itemWithTag = ItemWithTag.fromInput(ItemArgument.getItem(context, ITEM_ARG));
                            return ban(context.getSource(), itemWithTag, BuiltInPermissions.LEVEL_4, NbtComparator.NONE, "");
                        })
                        .then(
                                permission(context -> {
                                    ItemWithTag itemWithTag = ItemWithTag.fromInput(ItemArgument.getItem(context, ITEM_ARG));
                                    String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                    return ban(context.getSource(), itemWithTag, permission, NbtComparator.NONE, "");
                                })
                                        .then(
                                                nbtComparison(context -> {
                                                    ItemWithTag itemWithTag = ItemWithTag.fromInput(ItemArgument.getItem(context, ITEM_ARG));
                                                    String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                                    NbtComparator nbtComparator = NbtComparator.fromName(StringArgumentType.getString(context, NBT_COMPARE_ARG));
                                                    return ban(context.getSource(), itemWithTag, permission, nbtComparator, "");
                                                })
                                                        .then(
                                                                reason(context -> {
                                                                    ItemWithTag itemWithTag = ItemWithTag.fromInput(ItemArgument.getItem(context, ITEM_ARG));
                                                                    String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                                                    NbtComparator nbtComparator = NbtComparator.fromName(StringArgumentType.getString(context, NBT_COMPARE_ARG));
                                                                    String reason = StringArgumentType.getString(context, REASON_ARG);
                                                                    return ban(context.getSource(), itemWithTag, permission, nbtComparator, reason);
                                                                })
                                                        )
                                        )
                        )
                )
                .then(
                        Commands.literal("hand")
                                .executes(context -> {
                                    ItemWithTag itemWithTag = ItemWithTag.fromStack(context.getSource().getPlayerOrException().getMainHandItem());
                                    return ban(context.getSource(), itemWithTag, BuiltInPermissions.LEVEL_4, NbtComparator.NONE, "");
                                })
                                .then(
                                        permission(context -> {
                                            ItemWithTag itemWithTag = ItemWithTag.fromStack(context.getSource().getPlayerOrException().getMainHandItem());
                                            String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                            return ban(context.getSource(), itemWithTag, permission, NbtComparator.NONE, "");
                                        })
                                        .then(
                                                nbtComparison(context -> {
                                                    ItemWithTag itemWithTag = ItemWithTag.fromStack(context.getSource().getPlayerOrException().getMainHandItem());
                                                    String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                                    NbtComparator nbtComparator = NbtComparator.fromName(StringArgumentType.getString(context, NBT_COMPARE_ARG));
                                                    return ban(context.getSource(), itemWithTag, permission, nbtComparator, "");
                                                })
                                                .then(
                                                        reason(context -> {
                                                            ItemWithTag itemWithTag = ItemWithTag.fromStack(context.getSource().getPlayerOrException().getMainHandItem());
                                                            String permission = StringArgumentType.getString(context, PERMISSION_ARG);
                                                            NbtComparator nbtComparator = NbtComparator.fromName(StringArgumentType.getString(context, NBT_COMPARE_ARG));
                                                            String reason = StringArgumentType.getString(context, REASON_ARG);
                                                            return ban(context.getSource(), itemWithTag, permission, nbtComparator, reason);
                                                        })
                                                )
                                        )
                                )
                );
    }

    private static int ban(CommandSourceStack commandSource, ItemWithTag itemWithTag, String permission, NbtComparator comparator, String reason) {
        if(itemWithTag.is(Items.AIR))
            return 0;

        boolean hasPermission = PermissionHelper.hasPermission(commandSource, permission);
        if (!hasPermission) {
            commandSource.sendFailure(ModComponents.INADEQUATE_PERMS.create(permission));
            return 0;
        }

        ItemBlacklist.getConfig().banItem(permission, itemWithTag, comparator, reason);
        PlayerList playerList = commandSource.getServer().getPlayerList();
        Component itemComponent = itemWithTag.asStack().getDisplayName();
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
