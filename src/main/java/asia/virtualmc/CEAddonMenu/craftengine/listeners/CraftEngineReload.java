package asia.virtualmc.CEAddonMenu.craftengine.listeners;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.core.ConfigReader;
import asia.virtualmc.CEAddonMenu.managers.CommandManager;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CraftEngineReload implements Listener {
    private final Main plugin;
    private final ConfigReader configReader;
    private final CommandManager commandManager;

    public CraftEngineReload(@NotNull Main plugin) {
        this.plugin = plugin;
        this.configReader = new ConfigReader(plugin);
        this.commandManager = new CommandManager(configReader);

        if (ConfigReader.isAutomatic()) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler
    public void onReload(CraftEngineReloadEvent event) {
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> {
            configReader.readAndBuild();
            commandManager.register();
        }, ConfigReader.getRefreshDelay());
    }
}