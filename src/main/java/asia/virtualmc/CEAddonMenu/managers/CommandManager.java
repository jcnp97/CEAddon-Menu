package asia.virtualmc.CEAddonMenu.managers;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.craftengine.utilities.CraftEngineUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class CommandManager {
    private static final Set<String> commands = new HashSet<>();
    private CoreManager coreManager;

    public void register() {
        if (coreManager == null) {
            this.coreManager = Main.getInstance().getCoreManager();
        }

        if (!commands.isEmpty()) {
            for (String string : commands) {
                CommandAPI.unregister(string);
            }
        }

        CommandAPICommand mainCommand = new CommandAPICommand("cea");
        CommandAPICommand getItem = new CommandAPICommand("get");

        for (String namespace : coreManager.getNamespaces()) {
            getItem.withSubcommand(build(namespace));
        }

        mainCommand
                .withSubcommand(getItem)
                .withSubcommand(showGui())
                .register();

        commands.add(mainCommand.getName());
    }

    private CommandAPICommand build(String namespace) {
        Set<String> itemNames = coreManager.getNames(namespace);

        return new CommandAPICommand(namespace)
                .withArguments(
                        new StringArgument("item_name")
                                .replaceSuggestions(ArgumentSuggestions.strings(itemNames))
                )
                .withOptionalArguments(
                        new IntegerArgument("value", 1),
                        new StringArgument("player")
                )
                .withPermission("cea.admin")
                .executesPlayer((player, args) -> {
                    give(player, args, namespace);
                })
                .executes((sender, args) -> {
                    give(sender, args, namespace);
                });
    }

    private void give(CommandSender sender, CommandArguments args, String namespace) {
        Player target;

        String playerName = (String) args.get("player");
        if (playerName != null) {
            target = Bukkit.getPlayer(playerName);

            if (target == null) {
                sender.sendMessage("§cPlayer '" + playerName + "' is not online.");
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            Main.getInstance().getLogger()
                    .severe("[CEA] You must specify a player when using this command from the console.");
            return;
        }

        String itemName = (String) args.get("item_name");
        int amount = (int) args.getOptional("value").orElse(1);

        CraftEngineUtils.give(target, namespace, itemName, amount);
    }

    private CommandAPICommand showGui() {
        return new CommandAPICommand("menu")
                .withPermission("cea.admin")
                .executes((sender, args) -> {
                    if (sender instanceof Player player) {
                        coreManager.show(player);
                    } else {
                        sender.sendMessage("This command can only be used by players.");
                    }
                });
    }
}
