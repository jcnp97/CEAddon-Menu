package asia.virtualmc.CEAddonMenu.integrations.craftengine.listeners;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.core.ConfigReader;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CraftEngineReload implements Listener {
    private final Main plugin;
    private final ConfigReader configReader;
    private static boolean isLoaded = false;

    public CraftEngineReload(@NotNull Main plugin) {
        this.plugin = plugin;
        this.configReader = new ConfigReader(plugin);

        if (ConfigReader.isAutomatic()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler
    public void onReload(CraftEngineReloadEvent event) {
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
            if (event.isFirstReload()) {
                configReader.readAndBuild(true);
                isLoaded = true;
            } else {
                configReader.readAndBuild(false);
            }

        }, ConfigReader.getRefreshDelay());
    }
}