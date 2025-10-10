package asia.virtualmc.CEAddonMenu.utilities.core;

import asia.virtualmc.CEAddonMenu.integrations.craftengine.utilities.CraftEngineUtils;
import asia.virtualmc.CEAddonMenu.utilities.items.ItemStackUtils;
import asia.virtualmc.CEAddonMenu.utilities.messages.AdventureUtils;
import asia.virtualmc.CEAddonMenu.utilities.messages.ConsoleUtils;
import asia.virtualmc.CEAddonMenu.utilities.minecraft.SoundUtils;
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

    public static ChestGui getEmptyGui(String title) {
        ChestGui gui = new ChestGui(6, title);
        gui.setOnGlobalClick(event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, "minecraft:ui.button.click", 1, 1);
            event.setCancelled(true);
        });

        return gui;
    }

    public static ChestGui populateGui(ChestGui gui, List<GuiItem> content) {
        if (content == null || content.isEmpty()) return gui;

        PaginatedPane pane = new PaginatedPane(0, 0, 9, 5);
        pane.populateWithGuiItems(content);

        PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(1, 5), 7, pane);
        pagingButtons.setBackwardButton(new GuiItem(getPrevious()));
        pagingButtons.setForwardButton(new GuiItem(getNext()));

        gui.addPane(pane);
        gui.addPane(pagingButtons);
        gui.setOnGlobalClick(event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, "minecraft:ui.button.click", 1, 1);
            event.setCancelled(true);
        });

        return gui;
    }

    public static ChestGui getGui(String title, List<GuiItem> content) {
        if (content.isEmpty()) return null;

        ChestGui gui = new ChestGui(6, title);
        PaginatedPane pane = new PaginatedPane(0, 0, 9, 5);
        pane.populateWithGuiItems(content);

        PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(1, 5), 7, pane);
        pagingButtons.setBackwardButton(new GuiItem(getPrevious()));
        pagingButtons.setForwardButton(new GuiItem(getNext()));

        gui.addPane(pane);
        gui.addPane(pagingButtons);
        gui.setOnGlobalClick(event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player, "minecraft:ui.button.click", 1, 1);
            event.setCancelled(true);
        });

        return gui;
    }

    public static GuiItem getItemButton(String itemId) {
        ItemStack item = CraftEngineUtils.get(itemId);
        if (item == null) {
            ConsoleUtils.severe(itemId + " not found!");
            return null;
        }

        item = ItemStackUtils.addLore(item, List.of("",
                "<green>ʟᴇғᴛ-ᴄʟɪᴄᴋ <yellow>ᴛᴏ ɢᴇᴛ 1x ɪᴛᴇᴍ",
                "<green>ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <yellow>ᴛᴏ ɢᴇᴛ 64x ɪᴛᴇᴍ"));
        ItemStack finalItem = item;
        return new GuiItem(item, event -> {
            Player player = (Player) event.getWhoClicked();
            if (event.isLeftClick()) {
                CraftEngineUtils.give(player, finalItem, 1);
            } else if (event.isRightClick()) {
                CraftEngineUtils.give(player, finalItem, 64);
            }
        });
    }

    public static GuiItem getSoundButton(String soundId) {
        ItemStack item = ItemStackUtils.create(Material.MUSIC_DISC_WAIT, "<gold>" + soundId,
                List.of("",
                        "<green>ʟᴇғᴛ-ᴄʟɪᴄᴋ <yellow>ᴛᴏ ᴘʟᴀʏ",
                        "<green>ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <yellow>ᴛᴏ ᴄᴏᴘʏ ᴘʟᴀʏ ᴄᴏᴍᴍᴀɴᴅ"
                        ), 1);
        return new GuiItem(item, event -> {
            Player player = (Player) event.getWhoClicked();
            if (event.isLeftClick()) {
                SoundUtils.play(player, soundId);
            } else if (event.isRightClick()) {
                String command = "/playsound " + soundId + " player ";
                TextUtils.insert(player, "<green>" + soundId + " [ᴄᴏᴘʏ ᴘʟᴀʏ ᴄᴏᴍᴍᴀɴᴅ]", command);
                event.getWhoClicked().closeInventory();
            }
        });
    }

    public static GuiItem getImageButton(String imageId, String unicode) {
        String name = "<gold>" + imageId + ": <white>" + unicode;
        ItemStack item = ItemStackUtils.create(Material.ITEM_FRAME, name,
                List.of("",
                        "<green>ʟᴇғᴛ-ᴄʟɪᴄᴋ <yellow>ᴛᴏ ᴄᴏᴘʏ",
                        "<green>ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <yellow>ᴛᴏ ᴄᴏᴘʏ ᴀɴᴅ ᴄʟᴏsᴇ"
                ), 1);
        return new GuiItem(item, event -> {
            Player player = (Player) event.getWhoClicked();
            if (event.isLeftClick()) {
                TextUtils.insertImage(player, unicode, imageId);
            } else if (event.isRightClick()) {
                TextUtils.insertImage(player, unicode, imageId);
                event.getWhoClicked().closeInventory();
            }
        });
    }

    public static GuiItem getGuiButton(String title, ChestGui gui, Material material) {
        if (gui == null) return null;

        ItemStack item = createButton(material, title, 1);
        return new GuiItem(item, event -> {
            gui.show(event.getWhoClicked());
        });
    }

    public static GuiItem getGuiButton(String title, ChestGui gui, ItemStack item, int amount) {
        if (gui == null) return null;

        ItemStack newItem = ItemStackUtils.modify(item.clone(), title, List.of("", "<green>" + amount + " items"));
        return new GuiItem(newItem, event -> {
            gui.show(event.getWhoClicked());
        });
    }

    public static void addReturn(ChestGui toModify, ChestGui gui) {
        if (toModify == null || gui == null) return;

        StaticPane staticPane = new StaticPane(0, 0, 9, 6);
        ItemStack item = createButton(Material.ARROW, "<red>Return to previous", 1004);

        GuiItem guiItem = new GuiItem(item, event -> {
            gui.show(event.getWhoClicked());
        });

        staticPane.addItem(guiItem, 4, 5);
        toModify.addPane(staticPane);
    }

    private static ItemStack getPrevious() {
        return createButton(Material.ARROW, "<green>Previous Page", 1002);
    }

    private static ItemStack getNext() {
        return createButton(Material.ARROW, "<green>Next Page", 1000);
    }

    public static ItemStack createButton(Material material, String displayName, int modelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component name = AdventureUtils.toComponent("<!i>" + displayName);
            meta.displayName(name);
            meta.setCustomModelData(modelData);

            item.setItemMeta(meta);
        }
        return item;
    }
}
