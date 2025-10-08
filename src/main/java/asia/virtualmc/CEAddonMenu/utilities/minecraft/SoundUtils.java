package asia.virtualmc.CEAddonMenu.utilities.minecraft;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

public class SoundUtils {

    public static void play(Player player, String name) {
        if (player == null || !player.isOnline()) return;
        String[] parts = name.split(":", 2);
        String namespace = parts.length > 1 ? parts[0] : "minecraft";
        String key = parts.length > 1 ? parts[1] : parts[0];

        Sound sound = Sound.sound()
                .type(Key.key(namespace, key))
                .source(Sound.Source.PLAYER)
                .volume(1.0f)
                .pitch(1.0f)
                .build();

        player.playSound(sound);
    }
}
