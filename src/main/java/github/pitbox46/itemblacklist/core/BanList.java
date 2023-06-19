package github.pitbox46.itemblacklist.core;

import com.mojang.serialization.Codec;
import github.pitbox46.itemblacklist.utils.SetCodec;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Function;

public class BanList {
    public static final Codec<BanList> CODEC = Codec.unboundedMap(
                    Codec.STRING,
                    new SetCodec<>(BanData.EITHER_CODEC).xmap(banData -> (Set<BanData>)new HashSet<>(banData), Function.identity())
            )
            .xmap(BanList::new, banList -> banList.bannedItems);

    private final Object2ObjectMap<String, Set<BanData>> bannedItems;

    BanList() {
        bannedItems = new Object2ObjectOpenHashMap<>();
    }

    public BanList(Map<String, Set<BanData>> bannedData) {
        this.bannedItems = new Object2ObjectOpenHashMap<>(bannedData);

    }

    public boolean addBan(String permission, BanData data) {
        return bannedItems.computeIfAbsent(permission, perm -> new HashSet<>()).add(data);
    }

    public void addBans(String permission, Set<BanData> data) {
        bannedItems.put(permission, data);
    }

    public boolean removeBan(String permission, ItemStack bannedItem) {
        if (!bannedItems.containsKey(permission)) {
            return false;
        }
        return bannedItems.get(permission).remove(BanData.of(bannedItem));
    }

    public boolean removeBans(String permission) {
        if (!bannedItems.containsKey(permission)) {
            return false;
        }
        bannedItems.remove(permission);
        return true;
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
}
