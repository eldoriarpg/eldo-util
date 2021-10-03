package de.eldoria.eldoutilities.updater.spigotupdater;

import de.eldoria.eldoutilities.updater.UpdateData;
import org.bukkit.plugin.Plugin;

/**
 * Updater implementation for spigot update check.
 *
 * @since 1.0.0
 */
public class SpigotUpdateData extends UpdateData {
    private final int spigotId;

    /**
     * Creates a new Spigot Update Data.
     *
     * @param plugin           plugin to update
     * @param notifyPermission permission to receive notification
     * @param notifyUpdate     set to true to notify admins on login
     * @param spigotId         spigot id of the plugin
     */
    public SpigotUpdateData(Plugin plugin, String notifyPermission, boolean notifyUpdate, int spigotId) {
        super(plugin, notifyPermission, notifyUpdate, false);
        this.spigotId = spigotId;
    }

    public int getSpigotId() {
        return spigotId;
    }
}
