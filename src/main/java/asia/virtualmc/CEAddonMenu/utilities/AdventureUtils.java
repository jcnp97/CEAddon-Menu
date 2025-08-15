package asia.virtualmc.CEAddonMenu.utilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class AdventureUtils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();

    public static Component toComponent(String string) {
        Component component;
        if (string.contains("&")) {
            component = legacyAmpersand.deserialize(string);
        } else if (string.contains("§")) {
            component = legacySection.deserialize(string);
        } else {
            component = miniMessage.deserialize(string);
        }

        return component.decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> toComponent(List<String> strings) {
        List<Component> components = new ArrayList<>();

        for (String string : strings) {
            components.add(toComponent(string));
        }

        return components;
    }
}
