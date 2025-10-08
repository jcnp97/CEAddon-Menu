package asia.virtualmc.CEAddonMenu.utilities.items;

import asia.virtualmc.CEAddonMenu.utilities.messages.AdventureUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemStackUtils {

    public static ItemStack modify(ItemStack item, String name, List<String> toAdd) {
        if (item == null || toAdd == null) return item;

        ItemStack clonedItem = item.clone();
        ItemMeta meta = clonedItem.getItemMeta();
        if (meta == null) return clonedItem;

        meta.displayName(AdventureUtils.toComponent(name));
        List<Component> lore = new ArrayList<>();
        if (meta.lore() != null) {
            lore.addAll(meta.lore());
        }

        for (String line : toAdd) {
            lore.add(AdventureUtils.toComponent(line));
        }

        meta.lore(lore);
        clonedItem.setItemMeta(meta);
        return clonedItem;
    }

    public static ItemStack addLore(ItemStack item, List<String> toAdd) {
        if (item == null || toAdd == null) return item;

        ItemStack clonedItem = item.clone();
        ItemMeta meta = clonedItem.getItemMeta();
        if (meta == null) return clonedItem;

        List<Component> lore = new ArrayList<>();
        if (meta.lore() != null) {
            lore.addAll(meta.lore());
        }

        for (String line : toAdd) {
            lore.add(AdventureUtils.toComponent(line));
        }

        meta.lore(lore);
        clonedItem.setItemMeta(meta);
        return clonedItem;
    }

    public static ItemStack clearLore(ItemStack item) {
        if (item == null) return null;

        ItemStack clonedItem = item.clone();
        ItemMeta meta = clonedItem.getItemMeta();
        if (meta == null) return clonedItem;

        meta.lore(new ArrayList<>());
        clonedItem.setItemMeta(meta);

        return clonedItem;
    }

    @NotNull
    public static ItemStack create(Material material, String displayName,
                                   List<String> lore, int modelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(AdventureUtils.toComponent(displayName));
            meta.setCustomModelData(modelData);
            if (lore != null) meta.lore(AdventureUtils.toComponent(lore));

            item.setItemMeta(meta);
            return item.clone();
        }

        return new ItemStack(material);
    }
}
