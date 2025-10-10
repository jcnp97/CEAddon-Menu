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
                .version("0.11.3")
                .build();

        libby.loadLibrary(boostedYaml);
        libby.loadLibrary(ifLib);
    }

//    public Dependency(Main plugin) {
//        BukkitLibraryManager libraryManager = new BukkitLibraryManager(plugin);
//        Library lib = Library.builder()
//                .groupId("dev{}dejvokep")
//                .artifactId("boosted-yaml")
//                .version("1.3.6")
////                .relocate("dev{}dejvokep{}boostedyaml",
////                        "asia{}virtualmc{}CEAddonMenu{}lib{}boostedyaml")
//
//                .groupId("com{}github{}stefvanschie{}inventoryframework")
//                .artifactId("IF")
//                .version("0.11.3")
////                .relocate("com{}github{}stefvanschie{}inventoryframework",
////                        "asia{}virtualmc{}CEAddonMenu{}lib{}inventoryframework")
//
////                .groupId("dev{}jorel")
////                .artifactId("commandapi-paper-shade")
////                .version("11.0.0")
////                .relocate("dev{}jorel{}commandapi",
////                        "asia{}virtualmc{}CEAddonMenu{}lib{}commandapi")
//
//                .build();
//
//        libraryManager.addMavenCentral();
//        libraryManager.loadLibrary(lib);
//    }
}
