package asia.virtualmc.CEAddonMenu.core;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.integrations.placeholderapi.PAPIUtils;
import asia.virtualmc.CEAddonMenu.utilities.paper.AsyncUtils;
import asia.virtualmc.CEAddonMenu.utilities.Enums;
import asia.virtualmc.CEAddonMenu.utilities.files.FileUtils;
import asia.virtualmc.CEAddonMenu.utilities.files.YAMLUtils;
import asia.virtualmc.CEAddonMenu.utilities.messages.ConsoleUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigReader {
    private final Main plugin;

    // config values
    private static boolean automaticRefresh;
    private static long refreshDelay;
    private static String title;
    private static Set<String> excluded;
    private static Plugin craftEngine;

    public record ConfigKey(
            @NotNull String packName,
            @NotNull String dirName,
            @NotNull String yamlName
    ) {}

    public record Image(
            @NotNull String imageId,
            @NotNull String unicode
    ) {}

    // key=packName, value=directories
    private final Map<String, Set<String>> packs = new ConcurrentHashMap<>();

    // items cache: mainKey=packName, subKey=dirName, value=data
    private final Map<ConfigKey, Set<String>> items = new ConcurrentHashMap<>();
    // image cache: mainKey=yamlName, subKey=dirName, value=data
    private final Map<String, Set<Image>> images = new ConcurrentHashMap<>();
    // sounds cache: key=yamlName, value=soundId
    private final Map<String, Set<String>> sounds = new ConcurrentHashMap<>();

    public ConfigReader(Main plugin) {
        this.plugin = plugin;
        load();
    }

    public void readAndBuild() {
        AsyncUtils.runAsyncThenSync(plugin, this::read, (result) -> {
                if (result) {
                    new GUIBuilder.Menu(packs, items, sounds, images).build();
                }
        });
    }

    // private methods
    private void load() {
        craftEngine = Bukkit.getPluginManager().getPlugin("CraftEngine");
        if (craftEngine == null) {
            ConsoleUtils.severe("CraftEngine is not found. Skipping menu generation..");
            return;
        }

        YamlDocument config = YAMLUtils.getYaml(Main.getInstance(), "config.yml");
        if (config == null) {
            ConsoleUtils.severe("Couldn't find config.yml. Skipping menu generation..");
            return;
        }

        // Read config.yml
        automaticRefresh = config.getBoolean("automatic-reload");
        refreshDelay = config.getLong("refresh-delay");
        title = config.getString("menu-title");
        excluded = new HashSet<>(config.getStringList("excluded-directories"));
    }

    private boolean read() {
        if (!clearCache()) return false;

        // Retrieve every CraftEngine pack directories
        File resources = new File(craftEngine.getDataFolder(), "resources");
        List<File> packFiles = FileUtils.getDirectories(resources, excluded);

        // Loop into packs
        for (File packFile : packFiles) {
            String packName = packFile.getName();

            // Retrieve pack.yml if there's a namespace
            YamlDocument packConfig = YAMLUtils.getYaml(packFile, "pack.yml");
            String namespace = getNamespaceFromPack(packName, packConfig);

            // Skip if pack is disabled or no pack.yml
            if (namespace == null) continue;

            // Recursively get all .yml files from file/directory pack
            File configuration = new File(packFile, "configuration");
            if (!configuration.isDirectory()) {
                ConsoleUtils.severe("Unable to find configuration directory from " + packName + ".");
                continue;
            }

            List<File> dirFiles = FileUtils.getDirectories(configuration, excluded);
            if (dirFiles.isEmpty()) continue;

            for (File directory : dirFiles) {
                String dirName = directory.getName();
                packs.computeIfAbsent(packName, k -> new HashSet<>()).add(dirName);

                Map<String, YamlDocument> files = YAMLUtils.getFiles(directory);
                for (Map.Entry<String, YamlDocument> entry : files.entrySet()) {
                    String yamlName = entry.getKey() + ".yml";
                    YamlDocument yaml = entry.getValue();
                    readIntoYaml(packName, dirName, yamlName, namespace, yaml);
                }
            }
        }

        return true;
    }

    private void readIntoYaml(String packName, String dirName, String yamlName,
                              String namespace, YamlDocument yaml) {
        Set<String> topKeys = yaml.getRoutesAsStrings(false);
        for (String topKey : topKeys) {

            Enums.Type type = null;
            if (topKey.contains("items")) {
                type = Enums.Type.ITEM;
            } else if (topKey.contains("images")) {
                type = Enums.Type.IMAGE;
            } else if (topKey.contains("sounds")) {
                type = Enums.Type.SOUND;
            }

            if (type == null) continue;
            Section section = yaml.getSection(topKey);

            // Retrieve item keys
            Set<String> keys = section.getRoutesAsStrings(false);
            for (String key : keys) {
                if (!key.contains(":") && !namespace.isEmpty()) {
                    key = namespace + ":" + key;
                } else if (!key.contains(":")) {
                    key = packName + ":" + key;
                }

                switch (type) {
                    case ITEM -> {
                        addItem(packName, dirName, yamlName, key);
                    }

                    case SOUND -> {
                        sounds.computeIfAbsent(yamlName, k -> new HashSet<>()).add(key);
                    }

                    case IMAGE -> {
                        //String unicode = section.getString(key + ".char");
                        String unicode = PAPIUtils.getValue("%image_raw_" + key + "%");
                        images.computeIfAbsent(yamlName, k -> new HashSet<>()).add(new Image(key, unicode));
                    }
                }
            }
        }
    }

    private boolean clearCache() {
        try {
            packs.clear();
            items.clear();
            images.clear();
            sounds.clear();
            return true;
        } catch (Exception e) {
            ConsoleUtils.severe("An error occurred when trying to clear caches: " + e);
        }

        return false;
    }

    private void addItem(String packName, String dirName, String yamlName, String id) {
        ConfigKey key = new ConfigKey(packName, dirName, yamlName);
        items.computeIfAbsent(key, k -> new HashSet<>()).add(id);
    }

    private String getNamespaceFromPack(String dirName, YamlDocument yaml) {
        if (yaml == null) {
            ConsoleUtils.severe("No pack.yml found from " + dirName + ". Skipping GUI creation for this pack.");
            return null;
        }

        if (yaml.contains("enable")) {
            boolean enabled = yaml.getBoolean("enable");
            if (!enabled) {
                ConsoleUtils.severe("Pack " + dirName + " is disabled. Skipping GUI creation for this pack.");
                return null;
            }
        }

        String namespace = yaml.getString("namespace");
        return namespace != null ? namespace : "";
    }

    // getter methods
    public Map<String, Set<String>> getItems() {
        Map<String, Set<String>> cache = new HashMap<>();
        for (Map.Entry<ConfigKey, Set<String>> entry : items.entrySet()) {
            for (String itemId : entry.getValue()) {
                if (itemId.contains(":")) {
                    String[] parts = itemId.split(":");
                    cache.computeIfAbsent(parts[0], k -> new HashSet<>()).add(parts[1]);
                }
            }
        }

        return cache;
    }

    public static boolean isAutomatic() { return automaticRefresh; }
    public static long getRefreshDelay() { return refreshDelay; }
    public static String getTitle() { return title; }
}