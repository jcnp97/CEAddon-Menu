package asia.virtualmc.CEAddonMenu.utilities.files;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class FileUtils {

    @NotNull
    public static List<File> getDirectories(File main, Set<String> excludedDirs) {
        List<File> directories = new ArrayList<>();
        if (main == null || !main.isDirectory()) {
            return directories;
        }

        File[] files = main.listFiles();
        if (files == null) {
            return directories;
        }

        for (File file : files) {
            if (file.isDirectory() && !excludedDirs.contains(file.getName())) {
                directories.add(file);
            }
        }

        return directories;
    }
}
