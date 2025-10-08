package asia.virtualmc.CEAddonMenu.utilities.files;

import asia.virtualmc.CEAddonMenu.utilities.messages.ConsoleUtils;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.ConstructorException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class YAMLUtils {

    public static Map<String, YamlDocument> getFiles(File directory) {
        Map<String, YamlDocument> files = new HashMap<>();
        if (directory == null || !directory.exists()) return files;

        File[] list = directory.listFiles();
        if (list == null) return files;

        for (File file : list) {
            if (file.isDirectory()) {
                files.putAll(getFiles(file));
                continue;
            }

            if (!file.isFile() || !file.getName().toLowerCase().endsWith(".yml"))
                continue;

            try {
                boolean hasContent = Files.lines(file.toPath())
                        .anyMatch(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"));
                if (!hasContent) {
                    ConsoleUtils.severe("Skipping empty/comment-only YAML: " + file.getPath());
                    continue;
                }

                YamlDocument yaml = YamlDocument.create(file);
                String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                files.put(name, yaml);

            } catch (IOException | ConstructorException e) {
                ConsoleUtils.severe("Failed to load YAML file " + file.getPath() + ": " + e.getMessage());
            }
        }

        return files;
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
