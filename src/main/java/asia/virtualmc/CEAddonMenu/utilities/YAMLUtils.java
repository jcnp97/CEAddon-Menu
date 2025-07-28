package asia.virtualmc.CEAddonMenu.utilities;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class YAMLUtils {

    public static Map<String, YamlDocument> getFiles(File directory, Set<String> excluded) {
        Map<String, YamlDocument> filesMap = new LinkedHashMap<>();
        if (directory == null || !directory.isDirectory()) {
            return filesMap;
        }
        File[] list = directory.listFiles();
        if (list == null) {
            return filesMap;
        }
        for (File file : list) {
            String fileName = file.getName();
            if (file.isFile()
                    && fileName.endsWith(".yml")
                    && !excluded.contains(fileName)) {
                try {
                    YamlDocument document = YamlDocument.create(file);
                    filesMap.put(fileName, document);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filesMap;
    }

    @Nullable
    public static YamlDocument getYaml(@NotNull Plugin plugin, @NotNull String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);

        try {
            InputStream defaultFile = plugin.getResource(fileName);
            YamlDocument config;

            if (defaultFile != null) {
                config = YamlDocument.create(file, defaultFile);
            } else {
                config = YamlDocument.create(file);
            }

            return config;

        } catch (IOException e) {
            plugin.getLogger().severe("An error occurred when trying to read " + fileName);
            e.getCause();
        }

        return null;
    }

    @Nullable
    public static YamlDocument getYaml(@NotNull File directory, @NotNull String filePath) {
        File file = new File(directory, filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }
        try {
            return YamlDocument.create(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
