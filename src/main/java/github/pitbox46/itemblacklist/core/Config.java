package github.pitbox46.itemblacklist.core;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import github.pitbox46.itemblacklist.ItemBlacklist;
import github.pitbox46.itemblacklist.utils.FileUtils;
import github.pitbox46.itemblacklist.utils.FileWatcher;
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
import java.util.stream.Collectors;

public final class Config {
    public static final Codec<BanList> BAN_LIST_CODEC = BanList.CODEC.fieldOf("ban_list").codec();

    private final Path configPath;
    private BanList banList;
    private Reference2ObjectMap<Item, BanList> dataLookup;
    private FileWatcher watcher;

    public Config(Path configPath) {
        this.configPath = configPath;
        this.watcher = new FileWatcher(configPath, BasicDefaultConfig::createIfAbsent, "Config");
    }

    public Path getConfigPath() {
        return configPath;
    }

    public void load() {
        this.banList = FileUtils.readConfigFromJson(configPath).getOrThrow(false, ItemBlacklist.LOGGER::error);
        watcher.start();
        dataLookup = createLookup(banList);
    }

    public void save() {
        FileUtils.saveToFile(configPath, BAN_LIST_CODEC.encodeStart(JsonOps.INSTANCE, banList));
    }

    public void saveAndClose() {
        save();
        watcher.close();
    }

    public void reloadIfChanged() {
        if (watcher.hasFileChanged()) {
            banList = FileUtils.readConfigFromJson(configPath).getOrThrow(false, ItemBlacklist.LOGGER::error);
            dataLookup = createLookup(banList);
        }
    }

    public Reference2ObjectMap<Item, BanList> createLookup(BanList banList) {
        return Util.make(new Reference2ObjectOpenHashMap<>(), map -> {
            if (!banList.isEmpty()) {
                for (Map.Entry<String, Set<BanData>> entry : banList.entrySet()) {
                    for (BanData data : entry.getValue()) {
                        map.computeIfAbsent(data.getStack().item(), i -> new BanList()).addBans(entry.getKey(), entry.getValue().stream().filter(data1 -> data1.getStack().is(data.getStack().item())).collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll));
                    }
                }
            }
        });
    }

    public void banItem(String permission, ItemStack stack, NbtComparator comparator, String reason) {
        reloadIfChanged();
        BanData data = BanData.of(stack, comparator, reason);
        banList.addBan(permission, data);
        dataLookup.computeIfAbsent(stack.getItem(), i -> new BanList()).addBan(permission, data);
    }

    public void unbanItem(String permission, ItemStack stack) {
        reloadIfChanged();
        banList.removeBan(permission, stack);
        if (dataLookup.containsKey(stack.getItem())) {
            dataLookup.get(stack.getItem()).removeBan(permission, stack);
        }
    }

    public void unbanItem(ItemStack stack) {
        reloadIfChanged();
        banList.entrySet().forEach(entry -> entry.getValue().remove(BanData.of(stack)));
        if (dataLookup.containsKey(stack.getItem())) {
            dataLookup.get(stack.getItem()).entrySet().forEach(entry -> entry.getValue().remove(BanData.of(stack)));
        }
    }

    public void unbanAllItems(String permission) {
        reloadIfChanged();
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

    public Set<BanData> getBanData(String permission, ItemStack stack) {
        return banList.getBannedItems(permission).stream().filter(data -> data.test(stack)).collect(Collectors.toSet());
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
