package asia.virtualmc.CEAddonMenu;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;

public class Dependency {

    public Dependency(Main plugin) {
        BukkitLibraryManager libby = new BukkitLibraryManager(plugin);
        libby.addMavenCentral();

        Library boostedYaml = Library.builder()
                .groupId("dev{}dejvokep")
                .artifactId("boosted-yaml")
                .version("1.3.6")
                .build();

        Library ifLib = Library.builder()
                .groupId("com{}github{}stefvanschie{}inventoryframework")
                .artifactId("IF")
                .version("0.11.5")
                .build();

        libby.loadLibrary(boostedYaml);
        libby.loadLibrary(ifLib);
    }
}