package asia.virtualmc.CEAddonMenu.managers;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.utilities.AsyncUtils;
import asia.virtualmc.CEAddonMenu.utilities.FileUtils;
import asia.virtualmc.CEAddonMenu.utilities.GUIUtils;
import asia.virtualmc.CEAddonMenu.utilities.YAMLUtils;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class CoreManager {
    private static ChestGui mainMenu;
    // Map<namespace, itemNames> for command: /cea get
    private static final Map<String, Set<String>> itemIDs = new HashMap<>();
    private CommandManager commandManager;

    public void load() {
        if (commandManager == null) {
            this.commandManager = new CommandManager();
        }

        AsyncUtils.runAsyncThenSync(Main.getInstance(),
                this::build,
                result -> {
                    commandManager.register();
                });
    }

    private void clearCache() {
        itemIDs.clear();
        mainMenu = null;
    }

    private boolean build() {
        clearCache();

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

        List<ChestGui> yamlGuis = new ArrayList<>();
        List<GuiItem> dirList = getDirectories(config, craftEngine, yamlGuis);

        // Main Menu
        mainMenu = GUIUtils.getPaginatedGUI(config.getString("gui.directory-menu"), dirList);

        // Add Return Buttons
        for (ChestGui gui : yamlGuis) {
            GUIUtils.addReturn("Return to Menu", gui, mainMenu);
        }

        return true;
    }

    private List<GuiItem> getDirectories(YamlDocument config, Plugin craftEngine, List<ChestGui> yamlGuis) {
        Set<String> excludedDirs = new HashSet<>(config.getStringList("excluded-directories"));
        Set<String> excludedFiles = new HashSet<>(config.getStringList("excluded-files"));
        Set<String> keysToRead = new HashSet<>(config.getStringList("keys-to-read"));

        File resources = new File(craftEngine.getDataFolder(), "resources");
        List<File> directories = FileUtils.getDirectories(resources, excludedDirs);
        List<GuiItem> dirList = new ArrayList<>();

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

            // Retrieve list of yaml files per directory
            String title = config.getString("gui.items-menu");
            List<GuiItem> yamlList = getYamlFiles(files, namespace, keysToRead, itemsGuis, title);

            // Create YAML Menu
            ChestGui yamlMenu = GUIUtils.getPaginatedGUI(config.getString("gui.yaml-menu"), yamlList);
            yamlGuis.add(yamlMenu);

            // Add Return Buttons
            for (ChestGui gui : itemsGuis) {
                GUIUtils.addReturn("Return to Files", gui, yamlMenu);
            }

            // Add items to Main Menu
            dirList.add(GUIUtils.getGuiButton(file.getName(), yamlMenu, Material.BOOK));
        }

        return dirList;
    }

    private List<GuiItem> getYamlFiles(Map<String, YamlDocument> files, String namespace,
                                       Set<String> keysToRead, List<ChestGui> itemsGuis,
                                       String title) {
        List<GuiItem> yamlList = new ArrayList<>();

        for (Map.Entry<String, YamlDocument> entry : files.entrySet()) {
            String yamlName = entry.getKey();
            YamlDocument yaml  = entry.getValue();

            // looping through top-level keys of each yaml
            List<GuiItem> itemsList = getCraftEngineItems(yaml, namespace, keysToRead);

            // Create itemsMenu
            ChestGui itemsMenu = GUIUtils.getPaginatedGUI(title, itemsList);
            itemsGuis.add(itemsMenu);

            // Add YAML to directory
            yamlList.add(GUIUtils.getGuiButton(yamlName, itemsMenu, Material.PAPER));
        }

        return yamlList;
    }

    private List<GuiItem> getCraftEngineItems(YamlDocument yaml, String namespace, Set<String> keysToRead) {
        Set<String> names = new HashSet<>();
        List<GuiItem> itemsList = new ArrayList<>();

        for (String key : yaml.getRoutesAsStrings(false)) {
            if (!keysToRead.contains(key)) {
                continue;
            }

            Section section = yaml.getSection(key);
            if (section == null) continue;

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
                GuiItem guiItem = getGuiItem(section, itemName, namespace, key);
                if (guiItem != null) {
                    itemsList.add(guiItem);
                }
            }
        }

        if (!names.isEmpty()) {
            itemIDs.computeIfAbsent(namespace, k -> new HashSet<>()).addAll(names);
        }

        return itemsList;
    }

    private GuiItem getGuiItem(Section section, String itemName, String namespace, String key) {
        GuiItem guiItem = null;
        if (key.contains("items")) {
            guiItem = GUIUtils.getItemButton(namespace, itemName);
        } else if (key.contains("images")) {
            String character = section.getSection(itemName).getString("char");
            guiItem = GUIUtils.getImageButton(namespace + ":" + itemName, character);
        }

        return guiItem;
    }

    public void show(Player player) {
        if (mainMenu != null) {
            mainMenu.show(player);
        }
    }

    public Set<String> getNamespaces() {
        return itemIDs.keySet();
    }

    public Set<String> getNames(String namespace) {
        if (itemIDs.containsKey(namespace)) {
            return itemIDs.get(namespace);
        }

        return new HashSet<>();
    }
}
