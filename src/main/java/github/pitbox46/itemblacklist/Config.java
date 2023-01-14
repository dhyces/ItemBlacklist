package github.pitbox46.itemblacklist;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import github.pitbox46.itemblacklist.core.ItemStackData;
import github.pitbox46.itemblacklist.core.PermissionLevel;
import github.pitbox46.itemblacklist.utils.Utils;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Config {
    public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Utils.optionalConfigSet("level_0_ban_list", config -> config.bannedItems.get(PermissionLevel.LEVEL_0)),
                    Utils.optionalConfigSet("level_1_ban_list", config -> config.bannedItems.get(PermissionLevel.LEVEL_1)),
                    Utils.optionalConfigSet("level_2_ban_list", config -> config.bannedItems.get(PermissionLevel.LEVEL_2)),
                    Utils.optionalConfigSet("level_3_ban_list", config -> config.bannedItems.get(PermissionLevel.LEVEL_3)),
                    Utils.optionalConfigSet("level_4_ban_list", config -> config.bannedItems.get(PermissionLevel.LEVEL_4))
            ).apply(instance, Config::new)
    );
    private final Map<PermissionLevel, Set<ItemStackData>> bannedItems = new EnumMap<>(PermissionLevel.class);
    static Config instance;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {
    }

    private Config(Set<ItemStackData> noPermission, Set<ItemStackData> bypassProtection, Set<ItemStackData> cheats, Set<ItemStackData> multiplayerManager, Set<ItemStackData> operator) {
        bannedItems.put(PermissionLevel.LEVEL_0, noPermission);
        bannedItems.put(PermissionLevel.LEVEL_1, bypassProtection);
        bannedItems.put(PermissionLevel.LEVEL_2, cheats);
        bannedItems.put(PermissionLevel.LEVEL_3, multiplayerManager);
        bannedItems.put(PermissionLevel.LEVEL_4, operator);
    }

    @Nullable
    public PermissionLevel getPermissionLevel(ItemStack stack) {
        for (Map.Entry<PermissionLevel, Set<ItemStackData>> entry : bannedItems.entrySet()) {
            if (entry.getValue().contains(ItemStackData.of(stack))) {
                return entry.getKey();
            }
        }
        return null;
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
    public Set<ItemStackData> getAllBannedItems(PermissionLevel permissionLevel) {
        ImmutableSet.Builder<ItemStackData> builder = ImmutableSet.builder();
        int i = PermissionLevel.values().length - 1;
        int min = permissionLevel.ordinal();
        while (i >= min) {
            builder.addAll(bannedItems.get(PermissionLevel.getFromInt(i)));
            i--;
        }
        return builder.build();
    }

    public Set<ItemStackData> getAllBannedItems(int permissionLevel) {
        return getAllBannedItems(PermissionLevel.getFromInt(permissionLevel));
    }

    public boolean addItem(PermissionLevel permissionLevel, final ItemStack bannedItem) {
        ItemStackData data = ItemStackData.of(bannedItem);
        PermissionLevel existingPermission = getPermissionLevel(bannedItem);
        if (existingPermission != null) removeItem(existingPermission, bannedItem);
        return bannedItems.get(permissionLevel).add(data);
    }

    public boolean removeItem(PermissionLevel permissionLevel, final ItemStack bannedItem) {
        return bannedItems.get(permissionLevel).remove(ItemStackData.of(bannedItem));
    }

    public Set<ItemStackData> getBannedItems(PermissionLevel permissionLevel) {
        return Set.copyOf(bannedItems.get(permissionLevel));
    }

    public Set<ItemStackData> getBannedItems(int permissionLevel) {
        return getBannedItems(PermissionLevel.getFromInt(permissionLevel));
    }

    public void setBannedItems(PermissionLevel permissionLevel, Set<ItemStackData> bannedItems) {
        this.bannedItems.put(permissionLevel, bannedItems);
    }

    public void clearBannedItems() {
        bannedItems.put(PermissionLevel.LEVEL_0, new HashSet<>());
        bannedItems.put(PermissionLevel.LEVEL_1, new HashSet<>());
        bannedItems.put(PermissionLevel.LEVEL_2, new HashSet<>());
        bannedItems.put(PermissionLevel.LEVEL_3, new HashSet<>());
        bannedItems.put(PermissionLevel.LEVEL_4, new HashSet<>());
    }
}
