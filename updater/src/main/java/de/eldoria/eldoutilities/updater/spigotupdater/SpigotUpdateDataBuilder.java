/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.spigotupdater;

import de.eldoria.eldoutilities.updater.UpdateDataBuilder;
import org.bukkit.plugin.Plugin;

public class SpigotUpdateDataBuilder extends UpdateDataBuilder<SpigotUpdateDataBuilder, SpigotUpdateData> {
    private final int spigotId;

    public SpigotUpdateDataBuilder(Plugin plugin, int spigotId) {
        super(plugin);
        this.spigotId = spigotId;
        updateUrl = "https://www.spigotmc.org/resources/%s/".formatted(spigotId);
    }


    @Override
    public SpigotUpdateData build() {
        return new SpigotUpdateData(plugin, notifyPermission, notifyUpdate, spigotId, updateUrl, updateMessage);
    }
}
