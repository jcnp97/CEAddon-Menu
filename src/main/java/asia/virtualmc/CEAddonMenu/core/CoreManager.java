package asia.virtualmc.CEAddonMenu.core;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.commands.CommandManager;
import asia.virtualmc.CEAddonMenu.craftengine.utilities.CraftEngineUtils;
import asia.virtualmc.CEAddonMenu.utilities.FileUtils;
import asia.virtualmc.CEAddonMenu.utilities.GUIUtils;
import asia.virtualmc.CEAddonMenu.utilities.YAMLUtils;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.component.PagingButtons;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class CoreManager {
    private static ChestGui mainMenu;
    // Map<YAML name, Gui>
    private static final Map<String, ChestGui> itemsMenu = new LinkedHashMap<>();
    // Map<namespace, set of item names>
    private static final Map<String, Set<String>> itemIDs = new HashMap<>();

    public static void load() {
        itemIDs.clear();
        mainMenu = null;

        Plugin craftEngine = Bukkit.getPluginManager().getPlugin("CraftEngine");
        if (craftEngine == null) {
            Main.getInstance().getLogger().severe("Looks like CraftEngine wasn't installed. Skipping menu generation..");
            return;
        }

        YamlDocument config = YAMLUtils.getYaml(Main.getInstance(), "config.yml");
        if (config == null) {
            Main.getInstance().getLogger().severe("Couldn't find config.yml. Skipping menu generation..");
            return;
        }

        Set<String> excludedDirs = new HashSet<>(config.getStringList("excluded-directories"));
        Set<String> excludedFiles = new HashSet<>(config.getStringList("excluded-files"));

        File resources = new File(craftEngine.getDataFolder(), "resources");
        List<File> directories = FileUtils.getDirectories(resources, excludedDirs);

        List<GuiItem> dirList = new ArrayList<>();

        // looping through directories
        for (File file : directories) {
            YamlDocument packConfig = YAMLUtils.getYaml(file, "pack.yml");
            if (packConfig == null) {
                Main.getInstance().getLogger().severe("Couldn't find pack.yml from " + file.getName());
                continue;
            }

            String namespace = packConfig.getString("namespace");
            Map<String, YamlDocument> files = YAMLUtils.getFiles(
                    new File(file, "configuration"), excludedFiles);

            if (files.isEmpty()) {
                Main.getInstance().getLogger().severe("Couldn't find any .yml files from " + file.getName());
                continue;
            }

            // List of YAML per directory
            List<GuiItem> yamlList = new ArrayList<>();

            // looping through yaml files
            for (Map.Entry<String, YamlDocument> entry : files.entrySet()) {
                String yamlName = entry.getKey();
                YamlDocument yaml  = entry.getValue();

                Set<String> names = new HashSet<>();
                List<GuiItem> itemsList = new ArrayList<>();

                // looping through top-level keys of each yaml
                for (String key : yaml.getRoutesAsStrings(false)) {
                    if (!key.equals("items") && !key.startsWith("items.")) {
                        continue;
                    }

                    Section section = yaml.getSection(key);
                    if (section != null) {
                        // looping through item keys
                        for (String itemName : section.getRoutesAsStrings(false)) {


                            if (itemName.contains(namespace)) {
                                String[] parts = itemName.split(":");
                                itemName = parts[parts.length - 1];
                                names.add(itemName);
                            } else {
                                names.add(itemName);
                            }

                            // Add item to itemsMenu
                            GuiItem guiItem = GUIUtils.getItemButton(namespace, itemName);
                            if (guiItem != null) {
                                itemsList.add(guiItem);
                            }
                        }
                    }
                }

                if (!names.isEmpty()) {
                    itemIDs.put(namespace, names);
                }

                // Create itemsMenu
                ChestGui itemsMenu = new ChestGui(6, yamlName);
                PaginatedPane itemsPane = new PaginatedPane(0, 0, 9, 5);
                itemsPane.populateWithGuiItems(itemsList);

                PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(0, 5), 9, itemsPane);
                pagingButtons.setBackwardButton(new GuiItem(GUIUtils.getPrevious()));
                pagingButtons.setForwardButton(new GuiItem(GUIUtils.getNext()));

                itemsMenu.addPane(itemsPane);
                itemsMenu.addPane(pagingButtons);
                itemsMenu.setOnGlobalClick(event -> event.setCancelled(true));

                // Add YAML to directory
                yamlList.add(GUIUtils.getGuiButton(yamlName, itemsMenu));
            }

            // Create YAML Menu
            ChestGui yamlMenu = new ChestGui(6, file.getName());
            PaginatedPane yamlPane = new PaginatedPane(0, 0, 9, 5);
            yamlPane.populateWithGuiItems(yamlList);

            PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(0, 5), 9, yamlPane);
            pagingButtons.setBackwardButton(new GuiItem(GUIUtils.getPrevious()));
            pagingButtons.setForwardButton(new GuiItem(GUIUtils.getNext()));

            yamlMenu.addPane(yamlPane);
            yamlMenu.addPane(pagingButtons);
            yamlMenu.setOnGlobalClick(event -> event.setCancelled(true));

            // Add items to Main Menu
            dirList.add(GUIUtils.getGuiButton(file.getName(), yamlMenu));
        }

        // Main Menu
        mainMenu = new ChestGui(6, "CraftEngine");
        PaginatedPane mainPane = new PaginatedPane(0, 0, 9, 5);
        mainPane.populateWithGuiItems(dirList);

        PagingButtons pagingButtons = new PagingButtons(Slot.fromXY(0, 5), 9, mainPane);
        pagingButtons.setBackwardButton(new GuiItem(GUIUtils.getPrevious()));
        pagingButtons.setForwardButton(new GuiItem(GUIUtils.getNext()));

        mainMenu.addPane(mainPane);
        mainMenu.addPane(pagingButtons);
        mainMenu.setOnGlobalClick(event -> event.setCancelled(true));

        CommandManager.register();
    }

    public static void build(Map<String, Map<String, Set<String>>> cache) {
        for (String yamlName : cache.keySet()) {
            Map<String, Set<String>> temp = cache.get(yamlName);
            List<ItemStack> items = new ArrayList<>();

            if (temp == null || temp.isEmpty()) {
                continue;
            }

            for (Map.Entry<String, Set<String>> entry : temp.entrySet()) {
                String namespace = entry.getKey();

                for (String name : entry.getValue()) {
                    ItemStack item = CraftEngineUtils.get(namespace, name);
                    if (item == null) {
                        Main.getInstance().getLogger().severe("Unable to load " + namespace + ":" + name + " into gui.");
                        continue;
                    }

                    items.add(item);
                }
            }

            itemsMenu.put(yamlName, GUIUtils.getItems(yamlName, items));
        }

        mainMenu = GUIUtils.getMainMenu(itemsMenu);

    }

    public static void show(Player player) {
        if (mainMenu != null) {
            mainMenu.show(player);
        }
    }

    // Getter Methods
    public static Set<String> getNamespaces() {
        return itemIDs.keySet();
    }

    public static Set<String> getNames(String namespace) {
        if (itemIDs.containsKey(namespace)) {
            return itemIDs.get(namespace);
        }

        return new HashSet<>();
    }
}
