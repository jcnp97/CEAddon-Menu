package asia.virtualmc.CEAddonMenu.craftengine.listeners;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.core.CoreManager;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class CraftEngineReload implements Listener {

    public CraftEngineReload(@NotNull Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onReload(CraftEngineReloadEvent event) {
        CoreManager.load();
    }
}
