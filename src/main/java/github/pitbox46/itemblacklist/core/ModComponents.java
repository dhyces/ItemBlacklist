package github.pitbox46.itemblacklist.core;

import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.function.Supplier;

public class ModComponents {

    public static final ComponentHolder INADEQUATE_PERMS = args -> Component.translatableWithFallback("itemblacklist.command.inadequate_permissions", "Inadequate permissions. Must have permission \"%s\"", args[0]);
    public static final ComponentHolder ITEM_BANNED = args -> Component.translatableWithFallback("itemblacklist.command.item_banned", "Item banned: %s for all players with %s", args[0], args[1]);
    public static final ComponentHolder ITEM_BANNED_FOR_LEVELS = args -> Component.translatableWithFallback("itemblacklist.command.item_banned_recursive", "Item banned: %s for all players with %s and under", args[0], args[1]);
    public static final ComponentHolder ALL_ITEMS_UNBANNED = ComponentHolder.constant(() -> Component.translatableWithFallback("itemblacklist.command.all_unbanned", "All items unbanned"));
    public static final ComponentHolder ALL_ITEMS_UNBANNED_FOR = args -> Component.translatableWithFallback("itemblacklist.command.all_unbanned_for_perm", "All items unbanned for %s", args[0]);
    public static final ComponentHolder ITEM_UNBANNED = args -> Component.translatableWithFallback("itemblacklist.command.item_unbanned", "Item unbanned: %s", args[0]);
    public static final ComponentHolder ITEM_UNBANNED_FOR = args -> Component.translatableWithFallback("itemblacklist.command.item_unbanned_for_perm", "Item unbanned: %s for %s", args[0], args[1]);
    public static final ComponentHolder LIST_BANNED_ITEMS = ComponentHolder.constant(() -> Component.translatableWithFallback("itemblacklist.command.list_banned_items", "Items banned: "));
    public static final ComponentHolder BAN_REASON = args -> Component.literal("[").append(Component.translatableWithFallback("itemblacklist.info.ban_reason", "Reason")).append(Component.literal("]")).withStyle(ChatFormatting.RED).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Component) args[0])));
    public static final ComponentHolder ITEMS_REMOVED = args -> Component.translatableWithFallback("itemblacklist.info.items_removed", "The following items are banned and have been removed from your inventory: [ %s ]", args[0]);

    public interface ComponentHolder {
        Component create(Object... args);

        static ComponentHolder constant(Supplier<Component> component) {
            return new ComponentHolder() {
                Supplier<Component> memoized = Suppliers.memoize(component::get);

                @Override
                public Component create(Object... args) {
                    return memoized.get();
                }
            };
        }
    }
}
