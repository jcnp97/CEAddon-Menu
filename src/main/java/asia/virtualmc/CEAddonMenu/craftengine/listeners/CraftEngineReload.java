package asia.virtualmc.CEAddonMenu.craftengine.listeners;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.managers.CoreManager;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CraftEngineReload implements Listener {
    private final CoreManager coreManager;

    public CraftEngineReload(@NotNull Main plugin) {
        this.coreManager = plugin.getCoreManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onReload(CraftEngineReloadEvent event) {
        coreManager.load();
    }
}
