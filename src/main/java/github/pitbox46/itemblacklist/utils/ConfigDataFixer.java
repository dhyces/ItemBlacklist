package github.pitbox46.itemblacklist.utils;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ConfigDataFixer {
    public static <T> Dynamic<T> fixBanList(Dynamic<T> dynamic) {
        if (dynamic.get("ban_list").result().isPresent()) {
            return dynamic;
        }
        Map<Dynamic<?>, Dynamic<?>> map = new HashMap<>();
        convertField(dynamic, "level_0_ban_list", "level_0", map::put, ConfigDataFixer::fixBanDataList);
        convertField(dynamic, "level_1_ban_list", "level_1", map::put, ConfigDataFixer::fixBanDataList);
        convertField(dynamic, "level_2_ban_list", "level_2", map::put, ConfigDataFixer::fixBanDataList);
        convertField(dynamic, "level_3_ban_list", "level_3", map::put, ConfigDataFixer::fixBanDataList);
        convertField(dynamic, "level_4_ban_list", "level_4", map::put, ConfigDataFixer::fixBanDataList);
        return dynamic.set("ban_list", dynamic.createMap(map));
    }

    private static <T> void convertField(Dynamic<T> dynamic, String from, String to, BiConsumer<Dynamic<T>, Dynamic<T>> putter, Function<OptionalDynamic<T>, Dynamic<T>> resolveDynamic) {
        OptionalDynamic<T> toField = dynamic.get(to);
        if (toField.result().isPresent()) {
            return;
        }

        OptionalDynamic<T> fromField = dynamic.get(from);
        if (fromField.result().isEmpty()) {
            return;
        }

        putter.accept(dynamic.createString(to), resolveDynamic.apply(fromField));
    }

    private static <T> Dynamic<T> fixBanDataList(OptionalDynamic<T> element) {
        return element.createList(element.asStream().map(ConfigDataFixer::convertBanData));
    }

    private static <T> Dynamic<T> convertBanData(Dynamic<T> dynamic) {
        Map<Dynamic<T>, Dynamic<T>> item = new HashMap<>();
        OptionalDynamic<T> itemField = dynamic.get("item");
        if (itemField.result().isEmpty()) {
            return dynamic;
        }
        OptionalDynamic<T> tagField = dynamic.get("tag");
        if (tagField.result().isEmpty()) {
            return itemField.result().get();
        }
        item.put(dynamic.createString("id"), itemField.result().get());
        item.put(dynamic.createString("tag"), tagField.result().get());
        if (tagField.get("Count").result().isPresent()) {
            item.put(dynamic.createString("Count"), tagField.get("Count").result().get());
        }
        Dynamic<T> retDynamic = dynamic.set("item", dynamic.createMap(item));
        retDynamic = retDynamic.set("compare_tag", dynamic.createString("strict"));
        return retDynamic;
    }
}
