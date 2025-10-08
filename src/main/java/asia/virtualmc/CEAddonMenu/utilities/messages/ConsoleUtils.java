package asia.virtualmc.CEAddonMenu.utilities.messages;

import asia.virtualmc.CEAddonMenu.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class ConsoleUtils {

    public static void info(String message) {
        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(
                AdventureUtils.toComponent("<green>" + Main.getPrefix() + " " + message));
    }

    public static void warning(String message) {
        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(
                AdventureUtils.toComponent("<yellow>" + Main.getPrefix() + " " + message));
    }

    public static void severe(String message) {
        CommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(
                AdventureUtils.toComponent("<red>" + Main.getPrefix() + " " + message));
    }

    public static <K, V> void debugMap(Map<K, V> map) {
        if (!map.isEmpty()) {
            for (Map.Entry<K, V> entry : map.entrySet()) {
                info("Map debugging: key=" + entry.getKey() + ", value=" + entry.getValue());
            }
        }
    }
}
