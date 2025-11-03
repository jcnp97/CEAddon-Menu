package asia.virtualmc.CEAddonMenu.core;

import asia.virtualmc.CEAddonMenu.integrations.craftengine.utilities.CraftEngineUtils;
import asia.virtualmc.CEAddonMenu.integrations.placeholderapi.PAPIUtils;
import asia.virtualmc.CEAddonMenu.utilities.core.GUIUtils;
import asia.virtualmc.CEAddonMenu.utilities.core.TextUtils;
import asia.virtualmc.CEAddonMenu.utilities.files.JSONUtils;
import asia.virtualmc.CEAddonMenu.utilities.items.ItemStackUtils;
import asia.virtualmc.CEAddonMenu.utilities.messages.ConsoleUtils;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Stream;

public class GUIBuilder {
    private static ChestGui mainMenu;
    private static ChestGui itemMenu;
    private static ChestGui soundMenu;
    private static ChestGui imageMenu;

    public static void show(Player player) {
        if (mainMenu != null) {
            mainMenu.show(player);
        }
    }

    public static void showItems(Player player) {
        if (itemMenu != null) {
            itemMenu.show(player);
        }
    }

    public static void showSounds(Player player) {
        if (soundMenu != null) {
            soundMenu.show(player);
        }
    }

    public static void showImages(Player player) {
        if (imageMenu != null) {
            imageMenu.show(player);
        }
    }

    public static class Menu {
        private final Map<String, Set<String>> packs;
        private final Map<ConfigReader.ConfigKey, Set<String>> items;
        private final Map<String, Set<String>> sounds;
        private final Map<String, Set<ConfigReader.Image>> images;

        // ChestGui
        // key=packName, value=gui
        private final Map<String, ChestGui> packGuis = new HashMap<>();
        // key = packName, value = (directoryName -> gui)
        private final Map<String, Map<String, ChestGui>> directoryGuis = new HashMap<>();
        // GuiItems
        private final Map<String, Map<String, List<GuiItem>>> yamlItems = new HashMap<>();

        Menu(Map<String, Set<String>> packs,
             Map<ConfigReader.ConfigKey, Set<String>> items,
             Map<String, Set<String>> sounds,
             Map<String, Set<ConfigReader.Image>> images) {
            this.packs = packs;
            this.items = items;
            this.sounds = sounds;
            this.images = images;
        }

        public void build() {
            for (Map.Entry<String, Set<String>> entry : packs.entrySet()) {
                String packName = entry.getKey();
                Set<String> directories = entry.getValue();

                Map<String, ChestGui> dirGuiMap = new HashMap<>();
                for (String directory : directories) {
                    if (directory == null || directory.isEmpty()) continue;
                    dirGuiMap.put(directory, GUIUtils.getEmptyGui(ConfigReader.getTitle()));
                }

                directoryGuis.put(packName, dirGuiMap);
                packGuis.put(packName, GUIUtils.getEmptyGui(ConfigReader.getTitle()));
            }

            buildItems();
            buildSounds();
            buildImages();

            List<GuiItem> packItems = new ArrayList<>();
            itemMenu = GUIUtils.getEmptyGui(ConfigReader.getTitle());
            for (Map.Entry<String, Set<String>> entry : packs.entrySet()) {
                String packName = entry.getKey();
                Set<String> directories = entry.getValue();
                Map<String, List<GuiItem>> directoryItems = yamlItems.get(packName);
                Map<String, ChestGui> packDirs = directoryGuis.get(packName);
                if (directoryItems == null || directoryItems.isEmpty()) continue;

                ChestGui packGui = packGuis.get(packName);
                List<GuiItem> dirItems = new ArrayList<>();

                for (String directory : directories) {
                    if (directory == null || directory.isEmpty()) continue;
                    List<GuiItem> items = directoryItems.get(directory);
                    if (items == null || items.isEmpty()) continue;

                    ChestGui gui = packDirs.get(directory);
                    gui = GUIUtils.populateGui(gui, items);
                    GUIUtils.addReturn(gui, packGuis.get(packName));

                    dirItems.add(GUIUtils.getGuiButton("<gold>" + directory,
                            gui, new ItemStack(Material.BOOK), items.size()));
                }

                // item count (number of .yml files and directories)
                int itemCount = 0;

                // add YAMLs (without subfolders) directly to pack GUI
                List<GuiItem> rootYaml = directoryItems.get(null);
                if (rootYaml != null && !rootYaml.isEmpty()) {
                    dirItems.addAll(rootYaml);
                    itemCount += rootYaml.size();
                }

                packGui = GUIUtils.populateGui(packGui, dirItems);
                GUIUtils.addReturn(packGui, itemMenu);

                int dirCount = (int) directories.stream().filter(Objects::nonNull).count();
                packItems.add(GUIUtils.getGuiButton("<gold>" + packName,
                        packGui, new ItemStack(Material.BOOK), dirCount + itemCount));
            }

            itemMenu = GUIUtils.populateGui(itemMenu, packItems);

            // build the main menu
            if (buildMainMenu()) {
                ConsoleUtils.info("Successfully generated menus for CraftEngine.");
            }
        }

        private boolean buildMainMenu() {
            List<GuiItem> categories = new ArrayList<>();
            mainMenu = GUIUtils.getEmptyGui(ConfigReader.getTitle());
            if (itemMenu != null && !itemMenu.getItems().isEmpty()) {
                GUIUtils.addReturn(itemMenu, mainMenu);
                GuiItem guiItem = GUIUtils.getGuiButton("<gold>Items Category", itemMenu, Material.NETHERITE_SWORD);
                categories.add(guiItem);
            }

            if (imageMenu != null && !imageMenu.getItems().isEmpty()) {
                GUIUtils.addReturn(imageMenu, mainMenu);
                GuiItem guiItem = GUIUtils.getGuiButton("<gold>Images Category", imageMenu, Material.ITEM_FRAME);
                categories.add(guiItem);
            }

            if (soundMenu != null && !soundMenu.getItems().isEmpty()) {
                GUIUtils.addReturn(soundMenu, mainMenu);
                GuiItem guiItem = GUIUtils.getGuiButton("<gold>Sounds Category", soundMenu, Material.MUSIC_DISC_WAIT);
                categories.add(guiItem);
            }

            mainMenu = GUIUtils.populateGui(mainMenu, categories);
            return true;
        }

        private void buildItems() {
            for (Map.Entry<ConfigReader.ConfigKey, Set<String>> entry : items.entrySet()) {
                ConfigReader.ConfigKey key = entry.getKey();
                List<GuiItem> items = new ArrayList<>();

                for (String itemId : entry.getValue()) {
                    GuiItem guiItem = GUIUtils.getItemButton(itemId);
                    if (guiItem != null) items.add(guiItem);
                }

                if (items.isEmpty()) continue;
                ChestGui gui = GUIUtils.getGui(ConfigReader.getTitle(), items);

                Map<String, ChestGui> packDirMap = directoryGuis
                        .getOrDefault(key.packName(), Collections.emptyMap());

                // try folder first
                ChestGui prevGui = packDirMap.get(key.dirName());

                // if directory == null → attach to the pack GUI
                if (prevGui == null) {
                    prevGui = packGuis.get(key.packName());
                }

                if (gui != null && prevGui != null) {
                    ItemStack icon = items.getLast().getItem().clone();
                    GUIUtils.addReturn(gui, prevGui);
                    GuiItem guiItem = GUIUtils.getGuiButton(key.yamlName(), gui,
                            ItemStackUtils.clearLore(icon), items.size());
                    yamlItems
                            .computeIfAbsent(key.packName(), k -> new HashMap<>())
                            .computeIfAbsent(key.dirName(), k -> new ArrayList<>())
                            .add(guiItem);
                }

            }
        }

        private void buildSounds() {
            List<GuiItem> yamlItems = new ArrayList<>();
            ChestGui yamlGui = GUIUtils.getEmptyGui(ConfigReader.getTitle());
            for (Map.Entry<String, Set<String>> entry : sounds.entrySet()) {
                List<GuiItem> guiItems = new ArrayList<>();

                for (String soundId : entry.getValue()) {
                    GuiItem guiItem = GUIUtils.getSoundButton(soundId);
                    guiItems.add(guiItem);
                }

                ChestGui gui = GUIUtils.getGui(ConfigReader.getTitle(), guiItems);
                if (gui != null) {
                    GUIUtils.addReturn(gui, yamlGui);
                    yamlItems.add(GUIUtils.getGuiButton("<gold>" + entry.getKey(),
                            gui, new ItemStack(Material.BOOK), guiItems.size()));
                }
            }

            soundMenu = GUIUtils.populateGui(yamlGui, yamlItems);
        }

        private void buildImages() {
            List<GuiItem> yamlItems = new ArrayList<>();
            ChestGui yamlGui = GUIUtils.getEmptyGui(ConfigReader.getTitle());
            for (Map.Entry<String, Set<ConfigReader.Image>> entry : images.entrySet()) {
                List<GuiItem> guiItems = new ArrayList<>();
                Map<String, String> images = new HashMap<>();

                for (ConfigReader.Image data : entry.getValue()) {
                    String id = data.imageId();
                    String unicode = data.unicode();

                    if (unicode == null || unicode.isEmpty()) {
                        unicode = Stream.of(CraftEngineUtils.getImage(Key.of(id)),
                                PAPIUtils.getValue("%image_raw_" + id + "%"))
                                .filter(s -> s != null && !s.isEmpty())
                                .findFirst()
                                .orElse("");
                    }

                    images.put(id, unicode);
                    GuiItem guiItem = GUIUtils.getImageButton(id, unicode);
                    guiItems.add(guiItem);
                }

                ChestGui gui = GUIUtils.getGui(ConfigReader.getTitle(), guiItems);
                if (gui != null) {
                    GUIUtils.addReturn(gui, yamlGui);
                    String yamlName = entry.getKey();
                    List<String> lore = List.of("",
                            "<green>ʟᴇғᴛ-ᴄʟɪᴄᴋ <yellow>ᴛᴏ ᴠɪᴇᴡ ɪɴᴅɪᴠɪᴅᴜᴀʟ",
                            "<green>ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <yellow>ᴛᴏ ᴄᴏᴘʏ ᴀʟʟ",
                            "",
                            "<green>" + guiItems.size() + " items"
                    );

                    ItemStack newItem = ItemStackUtils.modify(new ItemStack(Material.BOOK),
                            "<gold>" + yamlName, lore);
                    GuiItem guiItem = new GuiItem(newItem, event -> {
                        Player player = (Player) event.getWhoClicked();
                        if (event.isLeftClick()) {
                            gui.show(player);
                        } else if (event.isRightClick()) {
                            TextUtils.insertImages(player, yamlName, images);
                            event.getWhoClicked().closeInventory();
                        }
                    });

                    yamlItems.add(guiItem);
                }
            }

            imageMenu = GUIUtils.populateGui(yamlGui, yamlItems);

            // write all unicodes into images directory
            for (Map.Entry<String, Set<ConfigReader.Image>> entry : images.entrySet()) {
                Map<String, String> unicodes = new HashMap<>();
                for (ConfigReader.Image data : entry.getValue()) {
                    String id = data.imageId();
                    String unicode = data.unicode();

                    if (unicode == null || unicode.isEmpty()) {
                        unicode = Stream.of(CraftEngineUtils.getImage(Key.of(id)),
                                        PAPIUtils.getValue("%image_raw_" + id + "%"))
                                .filter(s -> s != null && !s.isEmpty())
                                .findFirst()
                                .orElse("");
                    }

                    unicodes.put(id, unicode);
                }

                if (!unicodes.isEmpty()) {
                    JSONUtils.write("images", entry.getKey(), unicodes);
                }
            }
        }
    }
}