package asia.virtualmc.CEAddonMenu.utilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ItemUtils {

    public static void give(@NotNull Player player, @NotNull ItemStack item) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            Location dropLocation = player.getLocation();
            leftover.values().forEach(stack -> player.getWorld().dropItemNaturally(dropLocation, stack));
        }
    }
}
