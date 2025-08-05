package asia.virtualmc.CEAddonMenu.utilities;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.craftengine.utilities.CraftEngineUtils;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.component.PagingButtons;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUIUtils {

    public static ChestGui getPaginatedGUI(String title, List<GuiItem> content) {
        ChestGui gui = new ChestGui(6, title);
        PaginatedPane pane = new PaginatedPane(0, 0, 9, 5);
        pane.populateWithGuiItems(content);

        PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(0, 5), 9, pane);
        pagingButtons.setBackwardButton(new GuiItem(getPrevious()));
        pagingButtons.setForwardButton(new GuiItem(getNext()));

        pagingButtons.setOnClick(event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, "minecraft:ui.button.click", 1, 1);
        });

        gui.addPane(pane);
        gui.addPane(pagingButtons);
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        return gui;
    }

    public static GuiItem getItemButton(String namespace, String itemName) {
        ItemStack item = CraftEngineUtils.get(namespace, itemName);
        if (item == null) {
            Main.getInstance().getLogger().severe(namespace + ":" + itemName + " not found!");
            return null;
        }

        return new GuiItem(item, event -> {
            CraftEngineUtils.give((Player) event.getWhoClicked(), item);
        });
    }

    public static GuiItem getGuiButton(String title, ChestGui gui, Material material) {
        ItemStack item = createButton(material, title, 1);

        return new GuiItem(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, "minecraft:ui.button.click", 1, 1);
            gui.show(player);
        });
    }

    public static void addReturn(String title, ChestGui toModify, ChestGui gui) {
        StaticPane staticPane = new StaticPane(0, 0, 9, 6);
        ItemStack item = createButton(Material.REDSTONE_BLOCK, "<red>" + title, 1);

        GuiItem guiItem = new GuiItem(item, event -> {
            gui.show(event.getWhoClicked());
        });

        staticPane.addItem(guiItem, 4, 5);
        toModify.addPane(staticPane);
    }

    private static ItemStack getPrevious() {
        return createButton(Material.ARROW, "<green>Previous Page", 1);
    }

    private static ItemStack getNext() {
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
