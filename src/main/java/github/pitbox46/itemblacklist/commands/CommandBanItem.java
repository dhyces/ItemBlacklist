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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CommandBanItem implements Command<CommandSourceStack> {
    private static final CommandBanItem CMD = new CommandBanItem();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        return Commands
                .literal("ban")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("item", ItemArgument.item(context)).executes(CMD))
                .then(Commands.literal("hand").executes(ctx -> {
                    ItemStack stack = ctx.getSource().getPlayerOrException().getMainHandItem();
                    int i = banItem(ctx, ctx.getSource().getPlayerOrException().getMainHandItem().getItem());
                    stack.setCount(0);
                    return i;
                }));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return banItem(context, ItemArgument.getItem(context, "item").getItem());
    }

    private static int banItem(CommandContext<CommandSourceStack> context, Item item) {
        if(item == Items.AIR)
            return 1;
        JsonUtils.appendItemToJson(ItemBlacklist.BANLIST, item);
        PlayerList playerList = context.getSource().getServer().getPlayerList();
        Utils.broadcastMessage(context.getSource().getServer(),
                Component.literal("Item banned: ").append(BuiltInRegistries.ITEM.getKey(item).toString()));
        for(ServerPlayer player : playerList.getPlayers()) {
            for(int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if(ItemBlacklist.shouldDelete(player.getInventory().getItem(i)))
                    player.getInventory().getItem(i).setCount(0);
            }
        }
        return 0;
    }
}
