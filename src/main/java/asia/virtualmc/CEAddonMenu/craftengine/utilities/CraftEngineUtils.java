package asia.virtualmc.CEAddonMenu.craftengine.utilities;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;

public class CraftEngineUtils {

    public static ItemStack get(String namespace, String itemName) {
        CustomItem<ItemStack> item = CraftEngineItems.byId(Key.of(namespace, itemName));

        if (item != null) {
            return item.buildItemStack();
        }

        return null;
    }

    public static void give(Player player, String namespace, String itemName, int amount) {
        ItemStack item = get(namespace, itemName);
        if (item == null || amount <= 0) return;

        PlayerInventory inventory = player.getInventory();
        Location dropLocation = player.getLocation();

        while (amount > 0) {
            int stackSize = Math.min(item.getMaxStackSize(), amount);
            ItemStack stackToGive = item.clone();
            stackToGive.setAmount(stackSize);

            HashMap<Integer, ItemStack> leftover = inventory.addItem(stackToGive);
            if (!leftover.isEmpty()) {
                for (ItemStack leftoverItem : leftover.values()) {
                    player.getWorld().dropItemNaturally(dropLocation, leftoverItem);
                }
            }

            amount -= stackSize;
        }
    }
}
