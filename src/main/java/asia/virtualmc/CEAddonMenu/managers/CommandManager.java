package asia.virtualmc.CEAddonMenu.managers;

import asia.virtualmc.CEAddonMenu.Main;
import asia.virtualmc.CEAddonMenu.core.ConfigReader;
import asia.virtualmc.CEAddonMenu.core.GUIBuilder;
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
import java.util.Map;
import java.util.Set;

public class CommandManager {
    private final ConfigReader configReader;
    private static final Set<String> commands = new HashSet<>();

    public CommandManager(ConfigReader configReader) {
        this.configReader = configReader;
    }

    public void register() {
        if (!commands.isEmpty()) {
            for (String string : commands) {
                CommandAPI.unregister(string);
            }
        }

        CommandAPICommand mainCommand = new CommandAPICommand("cea");
        CommandAPICommand getItem = new CommandAPICommand("get");

        for (Map.Entry<String, Set<String>> entry : configReader.getItems().entrySet()) {
            getItem.withSubcommand(build(entry.getKey(), entry.getValue()));
        }

        mainCommand
                .withSubcommand(reload())
                .withSubcommand(getItem)
                .withSubcommand(show())
                .withSubcommand(showItems())
                .withSubcommand(showSounds())
                .withSubcommand(showImages())
                .register();

        commands.add(mainCommand.getName());
    }

    private CommandAPICommand build(String namespace, Set<String> itemNames) {
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
                    .severe("[CEAddon-Menu] You must specify a player when using this command from the console.");
            return;
        }

        String itemName = (String) args.get("item_name");
        int amount = (int) args.getOptional("value").orElse(1);

        CraftEngineUtils.give(target, namespace, itemName, amount);
    }

    private CommandAPICommand show() {
        return new CommandAPICommand("menu")
                .withPermission("cea.admin")
                .executes((sender, args) -> {
                    if (sender instanceof Player player) {
                        GUIBuilder.show(player);
                    } else {
                        sender.sendMessage("This command can only be used by players.");
                    }
                });
    }

    private CommandAPICommand showItems() {
        return new CommandAPICommand("items")
                .withPermission("cea.admin")
                .executes((sender, args) -> {
                    if (sender instanceof Player player) {
                        GUIBuilder.showItems(player);
                    } else {
                        sender.sendMessage("This command can only be used by players.");
                    }
                });
    }

    private CommandAPICommand showSounds() {
        return new CommandAPICommand("sounds")
                .withPermission("cea.admin")
                .executes((sender, args) -> {
                    if (sender instanceof Player player) {
                        GUIBuilder.showSounds(player);
                    } else {
                        sender.sendMessage("This command can only be used by players.");
                    }
                });
    }

    private CommandAPICommand showImages() {
        return new CommandAPICommand("images")
                .withPermission("cea.admin")
                .executes((sender, args) -> {
                    if (sender instanceof Player player) {
                        GUIBuilder.showImages(player);
                    } else {
                        sender.sendMessage("This command can only be used by players.");
                    }
                });
    }

    private CommandAPICommand reload() {
        return new CommandAPICommand("reload")
                .withPermission("cea.admin")
                .executes((sender, args) -> {
                    if (sender instanceof Player) {
                        configReader.readAndBuild();
                        register();
                        sender.sendMessage("[CEAddon-Menu] Successfully generated menus for CraftEngine.");
                    } else {
                        sender.sendMessage("This command can only be used by players.");
                    }
                });
    }
}
