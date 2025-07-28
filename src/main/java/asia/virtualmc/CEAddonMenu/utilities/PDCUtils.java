package asia.virtualmc.CEAddonMenu.utilities;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PDCUtils {

    public static String getString(ItemStack item, NamespacedKey key) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return "";
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.getOrDefault(key, PersistentDataType.STRING, "");
    }
}
