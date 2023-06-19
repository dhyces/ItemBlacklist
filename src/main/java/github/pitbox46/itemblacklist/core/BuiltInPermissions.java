package github.pitbox46.itemblacklist.core;

import org.jetbrains.annotations.Range;

import java.util.regex.Pattern;

public final class BuiltInPermissions {
    public static final Pattern LEVEL_PATTERN = Pattern.compile("level_[0-4]");
    public static final String LEVEL_0 = "level_0"; // NO PERMISSIONS
    public static final String LEVEL_1 = "level_1"; // BYPASS SPAWN PROTECTIONS
    public static final String LEVEL_2 = "level_2"; // CHEAT COMMANDS
    public static final String LEVEL_3 = "level_3"; // MULTIPLAYER MANAGEMENT
    public static final String LEVEL_4 = "level_4"; // SERVER OPERATOR

    private BuiltInPermissions() {}

    public static String getFromInt(@Range(from = 0, to = 4) int level) {
        return "level_" + level;
    }

    public static int getFrom(String permissionLevel) {
        return Integer.parseInt(String.valueOf(permissionLevel.charAt(permissionLevel.length()-1)));
    }

    public static String lower(String level) {
        return switch (level) {
            case LEVEL_1 -> LEVEL_0;
            case LEVEL_2 -> LEVEL_1;
            case LEVEL_3 -> LEVEL_2;
            case LEVEL_4 -> LEVEL_3;
            default -> throw new IllegalStateException("Invalid level " + level);
        };
    }

    public static boolean isLevelPermission(String permission) {
        return LEVEL_PATTERN.matcher(permission).matches();
    }
}
