package asia.virtualmc.CEAddonMenu.utilities.core;

import asia.virtualmc.CEAddonMenu.utilities.messages.AdventureUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Map;

public class TextUtils {

    public static void insert(Player player, String text, String toCopy) {
        Component component = Component.text(text, NamedTextColor.GREEN)
                .clickEvent(ClickEvent.suggestCommand(toCopy));
        player.sendMessage(component);
    }

    public static void insertImage(Player player, String copyUnicode, String imageId) {
        Component component = AdventureUtils.toComponent("<white>" + copyUnicode + ": ");
        Component unicode = Component.text("[ᴄᴏᴘʏ ᴜɴɪᴄᴏᴅᴇ] ", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.suggestCommand(copyUnicode));

        String copyTag = "<image:" + imageId + ">";
        Component tagFormat = Component.text("[ᴄᴏᴘʏ ᴛᴀɢ]", NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.suggestCommand(copyTag));

        Component combined = component.append(unicode).append(tagFormat);
        player.sendMessage(combined);
    }

    public static void insertImages(Player player, String yamlName, Map<String, String> images) {
        Component combined = AdventureUtils.toComponent("<white>" + yamlName + ": ");
        for (Map.Entry<String, String> entry : images.entrySet()) {
            String unicode = entry.getValue();
            Component component = Component.text(unicode, NamedTextColor.WHITE)
                    .clickEvent(ClickEvent.suggestCommand(unicode));
            combined = combined.append(component);
        }

        player.sendMessage(combined);
    }
}