package asia.virtualmc.CEAddonMenu.utilities;

import asia.virtualmc.CEAddonMenu.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class TextUtils {

    public static void insertIntoChat(Player player, String tag, String character) {
        Component first = Component.text("[ᴄʟɪᴄᴋ ᴛᴏ ᴄᴏᴘʏ ᴛᴀɢ]", NamedTextColor.GREEN)
                .clickEvent(ClickEvent.suggestCommand(tag));
        Component space = Component.text(" ");
        Component second = Component.text("[ᴄʟɪᴄᴋ ᴛᴏ ᴄᴏᴘʏ ᴄʜᴀʀ]", NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.suggestCommand(character));

        Component combined = first.append(space).append(second);
        player.sendMessage(combined);
    }

    public static String toUnicodeChar(String input) {
        try {
            if (input == null) return "";
            String hex = input.trim()
                    .replaceFirst("^\\\\?u", "")
                    .replaceFirst("^\\\\?U", "");

            int codePoint = Integer.parseInt(hex, 16);
            if (Character.isValidCodePoint(codePoint)) {
                return new String(Character.toChars(codePoint));
            }
        } catch (NumberFormatException e) {
            Main.getInstance().getLogger().severe(input + " cannot be converted into a unicode character because it is invalid!");
        }
        return "";
    }
}
