package github.pitbox46.itemblacklist.core;

import com.mojang.serialization.Codec;
import github.pitbox46.itemblacklist.utils.SetCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Function;

public class BanList {
    public static final Codec<BanList> CODEC = Codec.unboundedMap(Codec.STRING, new SetCodec<>(BanData.EITHER_CODEC))
            .xmap(BanList::new, banList -> banList.bannedItems);

    private final Object2ObjectMap<String, Set<BanData>> bannedItems;

    BanList() {
        bannedItems = new Object2ObjectOpenHashMap<>();
    }

    public BanList(Map<String, Set<BanData>> bannedData) {
        this.bannedItems = new Object2ObjectOpenHashMap<>();
        bannedData.forEach((s, banData) -> bannedItems.put(s, new LinkedHashSet<>(banData)));
    }

    public boolean addBan(String permission, BanData data) {
        return bannedItems.computeIfAbsent(permission, perm -> new LinkedHashSet<>()).add(data);
    }

    public void addBans(String permission, Set<BanData> data) {
        bannedItems.compute(permission, (s, banData) -> {
            if (banData == null) {
                return data;
            }
            banData.addAll(data);
            return banData;
        });
    }

    public boolean removeBan(String permission, ItemWithTag bannedItem) {
        if (!containsPermission(permission)) {
            return false;
        }
        return bannedItems.get(permission).remove(BanData.of(bannedItem));
    }

    public boolean removeBans(String permission) {
        if (!containsPermission(permission)) {
            return false;
        }
        bannedItems.remove(permission);
        return true;
    }

    public boolean containsPermission(String permission) {
        return bannedItems.containsKey(permission);
    }

    public Set<BanData> getBannedItems(String permission) {
        return bannedItems.getOrDefault(permission, Set.of());
    }

    public Set<String> getPermissionSet() {
        return bannedItems.keySet();
    }

    public boolean isEmpty() {
        return bannedItems.isEmpty();
    }

    public Set<Map.Entry<String, Set<BanData>>> entrySet() {
        return bannedItems.entrySet();
    }

    public static BanList defaultList() {
        return new BanList(Util.make(new HashMap<>(), map -> {
            map.put("level_0", new HashSet<>());
            map.put("level_1", new HashSet<>());
            map.put("level_2", new HashSet<>());
            map.put("level_3", new HashSet<>());
            map.put("level_4", new HashSet<>());
        }));
    }
}
