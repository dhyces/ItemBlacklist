package github.pitbox46.itemblacklist.core;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum PermissionLevel implements StringRepresentable {
    LEVEL_0("Level 0"), // NO PERMISSIONS
    LEVEL_1("Level 1"), // BYPASS SPAWN PROTECTIONS
    LEVEL_2("Level 2"), // CHEAT COMMANDS
    LEVEL_3("Level 3"), // MULTIPLAYER MANAGEMENT
    LEVEL_4("Level 4"); // SERVER OPERATOR

    public static final Codec<PermissionLevel> CODEC = StringRepresentable.fromEnum(PermissionLevel::values);

    private final String userFriendlyName;

    PermissionLevel(String userFriendlyName) {
        this.userFriendlyName = userFriendlyName;
    }

    public static PermissionLevel getFromInt(int ordinal) {
        return switch (ordinal) {
            case 1 -> LEVEL_1;
            case 2 -> LEVEL_2;
            case 3 -> LEVEL_3;
            case 4 -> LEVEL_4;
            default -> LEVEL_0;
        };
    }

    public PermissionLevel lower() {
        return switch (this) {
            case LEVEL_1 -> LEVEL_0;
            case LEVEL_2 -> LEVEL_1;
            case LEVEL_3 -> LEVEL_2;
            case LEVEL_4 -> LEVEL_3;
            default -> throw new IllegalStateException("Tried to call lower() on PermissionLevel.LEVEL_0. There is nothing lower.");
        };
    }

    public String getUserFriendlyName() {
        return userFriendlyName;
    }

    @Override
    public String getSerializedName() {
        return this.name();
    }
}
