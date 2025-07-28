package asia.virtualmc.CEAddonMenu.utilities;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.craftengine.utilities.CraftEngineUtils;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.component.PagingButtons;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIUtils {

    public static ChestGui getItems(String title, List<ItemStack> items) {
        ChestGui gui = new ChestGui(6, title);
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        List<GuiItem> guiItems = new ArrayList<>();
        for (ItemStack item : items) {
            GuiItem guiItem = new GuiItem(item, event -> {
                ItemUtils.give((Player) event.getWhoClicked(), item);
            });

            guiItems.add(guiItem);
        }

        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 5);
        paginatedPane.populateWithGuiItems(guiItems);

        // Paging Buttons for next/prev pages
        PagingButtons pagingButtons = new PagingButtons(9, paginatedPane);
        pagingButtons.setBackwardButton(new GuiItem(getPrevious()));
        pagingButtons.setForwardButton(new GuiItem(getNext()));

        gui.addPane(paginatedPane);
        gui.addPane(pagingButtons);

        return gui;
    }

    public static ChestGui getMainMenu(Map<String, ChestGui> cache) {
        ChestGui gui = new ChestGui(6, "CraftEngine");
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        OutlinePane content = new OutlinePane(0, 0, 9, 5);
        for (Map.Entry<String, ChestGui> entry : cache.entrySet()) {
            ItemStack item = createButton(Material.PAPER, entry.getKey(), 1);
            GuiItem guiItem = new GuiItem(item, event -> {
                entry.getValue().show(event.getWhoClicked());
            });

            content.addItem(guiItem);
        }

        PaginatedPane paginatedPane = new PaginatedPane(0, 0, 9, 6);
        paginatedPane.addPage(content);

        // Paging Buttons for next/prev pages
        PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(0, 0), 9, paginatedPane);
        pagingButtons.setBackwardButton(new GuiItem(getPrevious()));
        pagingButtons.setForwardButton(new GuiItem(getNext()));

        gui.addPane(paginatedPane);
        gui.addPane(pagingButtons);
        gui.update();

        return gui;
    }

    public static GuiItem getItemButton(String namespace, String itemName) {
        ItemStack item = CraftEngineUtils.get(namespace, itemName);
        if (item == null) {
            Main.getInstance().getLogger().severe(namespace + ":" + itemName + " not found!");
            return null;
        }

        return new GuiItem(item, event -> {
            ItemUtils.give((Player) event.getWhoClicked(), item);
        });
    }

    public static GuiItem getGuiButton(String title, ChestGui gui) {
        ItemStack item = createButton(Material.PAPER, title, 1);

        return new GuiItem(item, event -> {
            gui.show(event.getWhoClicked());
        });
    }

    private static ItemStack getCancel() {
        return createButton(Material.BARRIER, "<red>Exit", 1);
    }

    public static ItemStack getPrevious() {
        return createButton(Material.ARROW, "<green>Previous Page", 1);
    }

    public static ItemStack getNext() {
        return createButton(Material.ARROW, "<green>Next Page", 1);
    }

    public static ItemStack createButton(Material material, String displayName, int modelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component name = AdventureUtils.convertToComponent("<!i>" + displayName);
            meta.displayName(name);
            meta.setCustomModelData(modelData);

            item.setItemMeta(meta);
        }
        return item;
    }
}
