package de.eldoria.eldoutilities.updater.notifier;

import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.updater.Notifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * Notifier implementation for updater with update check function.
 *
 * @since 1.1.0
 */
public class UpdateNotifier extends Notifier {

    public UpdateNotifier(Plugin plugin, String permission, String latestVersion) {
        super(plugin, permission, latestVersion);
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PluginDescriptionFile description = plugin.getDescription();
        // send to operator.
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission(permission)) {
            MessageSender.getPluginMessageSender(plugin).sendMessage(event.getPlayer(), "New version of §b" + plugin.getName() + "§r available.\n"
                    + "Newest version: §a" + newestVersion + "§r! Current version: §c" + description.getVersion() + "§r!\n"
                    + "Download new version here: §b" + description.getWebsite());
        }
    }
}
