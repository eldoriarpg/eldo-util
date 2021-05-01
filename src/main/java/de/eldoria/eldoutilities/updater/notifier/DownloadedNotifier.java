package de.eldoria.eldoutilities.updater.notifier;

import de.eldoria.eldoutilities.messages.MessageSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

/**
 * Notifier implementation for updater with download function.
 *
 * @since 1.1.0
 */
public class DownloadedNotifier extends UpdateNotifier {
    private final boolean updated;

    public DownloadedNotifier(Plugin plugin, String permission, String latestVersion, boolean updated) {
        super(plugin, permission, latestVersion);
        this.updated = updated;
    }

    @Override
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PluginDescriptionFile description = plugin.getDescription();
        // send to operator.
        if (event.getPlayer().isOp() || event.getPlayer().hasPermission(permission)) {
            if (updated) {
                MessageSender.getPluginMessageSender(plugin).sendMessage(event.getPlayer(),
                        "New version of §b" + plugin.getName() + "§r downloaded.\n"
                                + "Newest version: §a" + newestVersion + "§r! Current version: §c" + description.getVersion() + "§r!\n"
                                + "Restart to apply update. Patchnotes can be found here: §b" + description.getWebsite());
            } else {
                MessageSender.getPluginMessageSender(plugin).sendMessage(event.getPlayer(), "New version of §b" + plugin.getName() + "§r available.\n"
                        + "Newest version: §a" + newestVersion + "§r! Current version: §c" + description.getVersion() + "§r!\n"
                        + "Download new version here: §b" + description.getWebsite());
            }
        }
    }
}
