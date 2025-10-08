package asia.virtualmc.CEAddonMenu.utilities.core;

import asia.virtualmc.CEAddonMenu.utilities.messages.AdventureUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class TextUtils {

    public static void insertIntoChat(Player player, String copyUnicode, String imageId) {
        Component component = AdventureUtils.toComponent("<white>" + copyUnicode + ": ");
        Component unicode = Component.text("[ᴄᴏᴘʏ ᴜɴɪᴄᴏᴅᴇ] ", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.suggestCommand(copyUnicode));

        String copyTag = "<image:" + imageId + ">";
        Component tagFormat = Component.text("[ᴄᴏᴘʏ ᴛᴀɢ]", NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.suggestCommand(copyTag));

        Component combined = component.append(unicode).append(tagFormat);
        player.sendMessage(combined);
    }

//    public static void insertIntoChat(Player player, String message, String copyText) {
//        Component component = Component.text(message, NamedTextColor.GREEN)
//                .clickEvent(ClickEvent.suggestCommand(copyText));
//        player.sendMessage(component);
//    }
}