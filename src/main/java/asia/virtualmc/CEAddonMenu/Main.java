package asia.virtualmc.CEAddonMenu;

import asia.virtualmc.CEAddonMenu.craftengine.listeners.CraftEngineReload;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private static Main plugin;
    private static final String prefix = "[CEAddon-Menu]";

    @Override
    public void onEnable() {
        plugin = this;
        CommandAPI.onEnable();
        new CraftEngineReload(this);
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .verboseOutput(false)
                .silentLogs(true));
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }

    public static Main getInstance() {
        return plugin;
    }
    public static String getPrefix() { return prefix; }
}
