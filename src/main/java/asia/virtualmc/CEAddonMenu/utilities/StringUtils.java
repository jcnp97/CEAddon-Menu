package asia.virtualmc.CEAddonMenu.utilities;

public class StringUtils {

    public static Integer toInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
