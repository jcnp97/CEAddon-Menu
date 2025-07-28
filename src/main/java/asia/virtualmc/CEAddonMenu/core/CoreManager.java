package asia.virtualmc.CEAddonMenu.core;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.commands.CommandManager;
import asia.virtualmc.CEAddonMenu.utilities.AsyncUtils;
import asia.virtualmc.CEAddonMenu.utilities.FileUtils;
import asia.virtualmc.CEAddonMenu.utilities.GUIUtils;
import asia.virtualmc.CEAddonMenu.utilities.YAMLUtils;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class CoreManager {
    private static ChestGui mainMenu;
    // Map<namespace, itemNames> for command: /cea get
    private static final Map<String, Set<String>> itemIDs = new HashMap<>();

    public static void load() {
        AsyncUtils.runAsyncThenSync(Main.getInstance(),
                CoreManager::build,
                result -> {
                    CommandManager.register();
                });
    }

    public static boolean build() {
        itemIDs.clear();
        mainMenu = null;

        Plugin craftEngine = Bukkit.getPluginManager().getPlugin("CraftEngine");
        if (craftEngine == null) {
            Main.getInstance().getLogger().severe("Looks like CraftEngine wasn't installed. Skipping menu generation..");
            return false;
        }

        YamlDocument config = YAMLUtils.getYaml(Main.getInstance(), "config.yml");
        if (config == null) {
            Main.getInstance().getLogger().severe("Couldn't find config.yml. Skipping menu generation..");
            return false;
        }

        Set<String> excludedDirs = new HashSet<>(config.getStringList("excluded-directories"));
        Set<String> excludedFiles = new HashSet<>(config.getStringList("excluded-files"));

        File resources = new File(craftEngine.getDataFolder(), "resources");
        List<File> directories = FileUtils.getDirectories(resources, excludedDirs);

        List<GuiItem> dirList = new ArrayList<>();
        List<ChestGui> yamlGuis = new ArrayList<>();

        // looping through directories
        for (File file : directories) {
            List<ChestGui> itemsGuis = new ArrayList<>();

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
                ChestGui itemsMenu = GUIUtils.getPaginatedGUI(yamlName, itemsList);
                itemsGuis.add(itemsMenu);

                // Add YAML to directory
                yamlList.add(GUIUtils.getGuiButton(yamlName, itemsMenu));
            }

            // Create YAML Menu
            ChestGui yamlMenu = GUIUtils.getPaginatedGUI(file.getName(), yamlList);
            yamlGuis.add(yamlMenu);

            // Add Return Buttons
            for (ChestGui gui : itemsGuis) {
                GUIUtils.addReturn("Return to Files", gui, yamlMenu);
            }

            // Add items to Main Menu
            dirList.add(GUIUtils.getGuiButton(file.getName(), yamlMenu));
        }

        // Main Menu
        mainMenu = GUIUtils.getPaginatedGUI("CraftEngine", dirList);

        // Add Return Buttons
        for (ChestGui gui : yamlGuis) {
            GUIUtils.addReturn("Return to Menu", gui, mainMenu);
        }

        return true;
    }

    public static void show(Player player) {
        if (mainMenu != null) {
            mainMenu.show(player);
        }
    }

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
