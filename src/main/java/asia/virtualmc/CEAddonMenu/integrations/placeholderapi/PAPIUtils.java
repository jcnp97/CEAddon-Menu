package asia.virtualmc.CEAddonMenu.integrations.placeholderapi;

import me.clip.placeholderapi.PlaceholderAPI;

public class PAPIUtils {

    public static String getValue(String placeholder) {
        return PlaceholderAPI.setPlaceholders(null, placeholder);
    }
}
