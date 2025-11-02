package asia.virtualmc.CEAddonMenu.utilities.files;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.core.ConfigReader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class JSONUtils {

    public static void write(String path, String fileName, Map<String, String> content) {
        File directory = new File(Main.getInstance().getDataFolder(), path);
        if (!directory.exists() && !directory.mkdirs()) {
            System.err.println("Failed to create directory: " + path);
            return;
        }

        File file = new File(directory, fileName);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(file, false)) {
            gson.toJson(content, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
