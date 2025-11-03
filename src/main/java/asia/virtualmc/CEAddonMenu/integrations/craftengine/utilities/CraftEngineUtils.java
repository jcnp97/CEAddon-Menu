package asia.virtualmc.CEAddonMenu.integrations.craftengine.utilities;

import asia.virtualmc.CEAddonMenu.utilities.messages.ConsoleUtils;
import net.momirealms.craftengine.bukkit.api.CraftEngineImages;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.font.BitmapImage;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class CraftEngineUtils {

    public static ItemStack get(String itemId) {
        CustomItem<ItemStack> item = CraftEngineItems.byId(Key.of(itemId));

        if (item != null) {
            return item.buildItemStack();
        }

        return null;
    }

    public static ItemStack get(String namespace, String itemName) {
        CustomItem<ItemStack> item = CraftEngineItems.byId(Key.of(namespace, itemName));

        if (item != null) {
            return item.buildItemStack();
        }

        return null;
    }

    public static void give(Player player, ItemStack item, int amount) {
        Key key = CraftEngineItems.getCustomItemId(item);
        if (key == null) {
            return;
        }

        CustomItem<ItemStack> customItem = CraftEngineItems.byId(key);
        if (customItem != null) {
            ItemStack customItemStack = customItem.buildItemStack();
            customItemStack.setAmount(Math.max(1, amount));

            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(customItemStack);
            if (!leftover.isEmpty()) {
                Location dropLocation = player.getLocation();
                leftover.values().forEach(stack -> player.getWorld().dropItemNaturally(dropLocation, stack));
            }
        }

        player.playSound(player, "minecraft:entity.item.pickup", 1, 1);
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

        player.playSound(player, "minecraft:entity.item.pickup", 1, 1);
    }

    @Nullable
    public static String getImage(@NotNull Key imageId) {
        BitmapImage image = CraftEngineImages.byId(imageId);
        if (image == null) {
            ConsoleUtils.severe("Trying to get the unicode of " + imageId + " but it is NULL!");
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (int row = 0; row < image.rows(); row++) {
            for (int col = 0; col < image.columns(); col++) {
                int codepoint = image.codepointAt(row, col);
                result.append(new String(Character.toChars(codepoint)));
            }
        }
        return result.toString();
    }
}