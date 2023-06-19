package github.pitbox46.itemblacklist.core;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.utils.FileUtils;
import github.pitbox46.itemblacklist.utils.PermissionHelper;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;

public final class Config {
    public static final Codec<BanList> BAN_LIST_CODEC = BanList.CODEC.fieldOf("ban_list").codec();

    private final Path configPath;
    private BanList banList;
    private Reference2ObjectMap<Item, BanList> dataLookup;

    public Config(Path configPath) {
        this.configPath = configPath;
    }

    public void load() {
        this.banList = FileUtils.readConfigFromJson(configPath).getOrThrow(false, ItemBlacklist.LOGGER::error);
        this.dataLookup = Util.make(new Reference2ObjectOpenHashMap<>(), map -> {
            if (!banList.isEmpty()) {
                for (Map.Entry<String, Set<BanData>> entry : banList.entrySet()) {
                    for (BanData data : entry.getValue()) {
                        map.computeIfAbsent(data.getStack().getItem(), i -> new BanList()).addBans(entry.getKey(), entry.getValue());
                    }
                }
            }
        });
    }

    public void save() {
        FileUtils.saveToFile(configPath, BAN_LIST_CODEC.encodeStart(JsonOps.INSTANCE, banList));
    }

    public void banItem(String permission, ItemStack stack, NbtComparator comparator, String reason) {
        BanData data = BanData.of(stack, comparator, reason);
        banList.addBan(permission, data);
        dataLookup.computeIfAbsent(stack.getItem(), i -> new BanList()).addBan(permission, data);
    }

    public void unbanItem(String permission, ItemStack stack) {
        banList.removeBan(permission, stack);
        if (dataLookup.containsKey(stack.getItem())) {
            dataLookup.get(stack.getItem()).removeBan(permission, stack);
        }
    }

    public void unbanItem(ItemStack stack) {
        banList.entrySet().forEach(entry -> entry.getValue().remove(BanData.of(stack)));
        dataLookup.get(stack.getItem()).entrySet().forEach(entry -> entry.getValue().remove(BanData.of(stack)));
    }

    public void unbanAllItems(String permission) {
        banList.removeBans(permission);
        for (Map.Entry<Item, BanList> entry : dataLookup.entrySet()) {
            entry.getValue().removeBans(permission);
        }
    }

    public void unbanAllItems() {
        banList = new BanList();
        dataLookup = new Reference2ObjectOpenHashMap<>();
    }

    public boolean hasPermission(Player player, ItemStack stack) {
        if (!dataLookup.containsKey(stack.getItem())) {
            return true;
        }
        for (Map.Entry<String, Set<BanData>> entry : dataLookup.get(stack.getItem()).entrySet()) {
            if (PermissionHelper.hasPermission(player, entry.getKey()) && entry.getValue().stream().anyMatch(data -> data.test(stack))) {
                return false;
            }
        }
        return true;
    }

    public Set<BanData> getBanData(String permission) {
        return banList.getBannedItems(permission);
    }

    public Set<String> getAllPermissions() {
        return banList.getPermissionSet();
    }

    /**
     * This collects all banned items for a given permission level. It works like so:
     *      NO_PERMISSIONS = [NO_PERMISSIONS, BYPASS_SPAWN_PROTECTION, CHEAT_COMMANDS, MULTIPLAYER_MANAGEMENT, SERVER_OPERATOR]
     *      BYPASS_SPAWN_PROTECTION = [BYPASS_SPAWN_PROTECTION, CHEAT_COMMANDS, MULTIPLAYER_MANAGEMENT, SERVER_OPERATOR]
     *      CHEAT_COMMANDS = [CHEAT_COMMANDS, MULTIPLAYER_MANAGEMENT, SERVER_OPERATOR]
     *      MULTIPLAYER_MANAGEMENT = [MULTIPLAYER_MANAGEMENT, SERVER_OPERATOR]
     *      SERVER_OPERATOR = [SERVER_OPERATOR]
     * @param permissionLevel The max permission level to collect for
     * @return Every item stack that is banned for each permission level equal to and below the given level
     */
    public Set<BanData> getRecursiveBannedItems(String permissionLevel) {
        ImmutableSet.Builder<BanData> builder = ImmutableSet.builder();
        int i = 4;
        int max = BuiltInPermissions.getFrom(permissionLevel);
        while (i >= max) {
            builder.addAll(banList.getBannedItems(BuiltInPermissions.getFromInt(i)));
            i--;
        }
        return builder.build();
    }
}
