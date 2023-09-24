package github.pitbox46.itemblacklist.core;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.utils.BetterFileWatcher;
import github.pitbox46.itemblacklist.utils.FileUtils;
import github.pitbox46.itemblacklist.utils.PermissionHelper;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class Config {
    public static final Codec<Config> CODEC = BanList.CODEC.fieldOf("ban_list")
            .xmap(Config::new, config -> config.baseBanList).codec();

    private BanList baseBanList;
    private Reference2ObjectMap<Item, BanList> dataLookup;
    private Runnable updateCallback;

    public Config(BanList banList) {
        this.baseBanList = banList;
        dataLookup = createLookup(banList);
    }

    void setUpdateCallback(Runnable updateCallback) {
        this.updateCallback = updateCallback;
    }

    private void runUpdateCallback() {
        if (updateCallback != null) {
            updateCallback.run();
        }
    }

    private Reference2ObjectMap<Item, BanList> createLookup(BanList banList) {
        return Util.make(new Reference2ObjectOpenHashMap<>(), map -> {
            if (!banList.isEmpty()) {
                for (Map.Entry<String, Set<BanData>> entry : banList.entrySet()) {
                    for (BanData data : entry.getValue()) {
                        Set<BanData> banData = entry.getValue().stream()
                                .filter(data1 -> data1.getItemWithTag().is(data.getItemWithTag().item()))
                                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
                        map.computeIfAbsent(data.getItemWithTag().item(), i -> new BanList()).addBans(entry.getKey(), banData);
                    }
                }
            }
        });
    }

    public BanData banItem(String permission, ItemWithTag itemWithTag, NbtComparator comparator, String reason) {
        BanData data = BanData.of(itemWithTag, comparator, reason);
        if (BuiltInPermissions.isLevelPermission(permission)) {
            for (String levelPerm : BuiltInPermissions.values()) {
                unbanItem(levelPerm, itemWithTag);
            }
        }
        baseBanList.addBan(permission, data);
        dataLookup.computeIfAbsent(itemWithTag.item(), i -> new BanList()).addBan(permission, data);
        runUpdateCallback();
        return data;
    }

    public void unbanItem(String permission, ItemWithTag itemWithTag) {
        baseBanList.removeBan(permission, itemWithTag);
        if (dataLookup.containsKey(itemWithTag.item())) {
            dataLookup.get(itemWithTag.item()).removeBan(permission, itemWithTag);
        }
        runUpdateCallback();
    }

    public void unbanItem(ItemWithTag itemWithTag) {
        BanData banData = BanData.of(itemWithTag);
        for (Map.Entry<String, Set<BanData>> entry : baseBanList.entrySet()) {
            entry.getValue().remove(banData);
        }
        if (dataLookup.containsKey(itemWithTag.item())) {
            dataLookup.get(itemWithTag.item()).entrySet().forEach(entry -> entry.getValue().remove(banData));
        }
        runUpdateCallback();
    }

    public void unbanAllItems(String permission) {
        baseBanList.removeBans(permission);
        for (Map.Entry<Item, BanList> entry : dataLookup.entrySet()) {
            entry.getValue().removeBans(permission);
        }
        runUpdateCallback();
    }

    public void unbanAllItems() {
        baseBanList = new BanList();
        dataLookup = new Reference2ObjectOpenHashMap<>();
        runUpdateCallback();
    }

    public boolean hasPermission(Player player, ItemStack stack) {
        if (!dataLookup.containsKey(stack.getItem())) {
            return true;
        }
        for (Map.Entry<String, Set<BanData>> entry : dataLookup.get(stack.getItem()).entrySet()) {
            if (!PermissionHelper.hasPermission(player, entry.getKey()) && entry.getValue().stream().anyMatch(data -> data.test(stack))) {
                return false;
            }
        }
        return true;
    }

    public Set<BanData> getBanData(String permission) {
        return baseBanList.getBannedItems(permission);
    }

    public Set<BanData> getBanData(String permission, ItemStack stack) {
        return baseBanList.getBannedItems(permission).stream().filter(data -> data.test(stack)).collect(Collectors.toSet());
    }

    public Set<BanData> getBanData(Player player, ItemStack stack) {
        if (!dataLookup.containsKey(stack.getItem())) {
            return Set.of();
        }
        ImmutableSet.Builder<BanData> builder = ImmutableSet.builder();
        for (Map.Entry<String, Set<BanData>> entry : dataLookup.get(stack.getItem()).entrySet()) {
            if (!PermissionHelper.hasPermission(player, entry.getKey())) {
                entry.getValue().forEach(data -> {
                    if (data.test(stack)) {
                        builder.add(data);
                    }
                });
            }
        }
        return builder.build();
    }

    public Set<String> getAllPermissions() {
        return baseBanList.getPermissionSet();
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
            builder.addAll(baseBanList.getBannedItems(BuiltInPermissions.getFromInt(i)));
            i--;
        }
        return builder.build();
    }

    void merge(Config config) {
        for (Map.Entry<String, Set<BanData>> entry : config.baseBanList.entrySet()) {
            baseBanList.addBans(entry.getKey(), entry.getValue());
        }
        dataLookup = createLookup(baseBanList);
    }

    public static Config createDefaultConfig() {
        return new Config(BanList.defaultList());
    }
}
