/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.spigotupdater;

import de.eldoria.eldoutilities.updater.UpdateDataBuilder;
import org.bukkit.plugin.Plugin;

/**
 * @deprecated Use the Lyna updater from {@link de.eldoria.eldoutilities.updater.Updater#lyna}. Removed in 4.0.
 */
@Deprecated(since = "3.6.0", forRemoval = true)
public class SpigotUpdateDataBuilder extends UpdateDataBuilder<SpigotUpdateDataBuilder, SpigotUpdateData> {
    private final int spigotId;

    public SpigotUpdateDataBuilder(Plugin plugin, int spigotId) {
        super(plugin);
        this.spigotId = spigotId;
        updateUrl = "https://www.spigotmc.org/resources/%s/".formatted(spigotId);
    }

    @Override
    public SpigotUpdateData build() {
        return applyCommon(new SpigotUpdateData(
                plugin, notifyPermission, notifyUpdate, spigotId, updateUrl, updateMessage, messageSender));
    }
}
