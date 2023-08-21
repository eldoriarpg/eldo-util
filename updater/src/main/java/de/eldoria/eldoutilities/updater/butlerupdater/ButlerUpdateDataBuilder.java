/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.butlerupdater;

import de.eldoria.eldoutilities.updater.UpdateDataBuilder;
import org.bukkit.plugin.Plugin;

public class ButlerUpdateDataBuilder extends UpdateDataBuilder<ButlerUpdateDataBuilder, ButlerUpdateData> {
    private final int butlerId;
    private String host = ButlerUpdateData.HOST;

    public ButlerUpdateDataBuilder(Plugin plugin, int butlerId) {
        super(plugin);
        this.butlerId = butlerId;
    }


    public ButlerUpdateDataBuilder host(String host) {
        this.host = host;
        return this;
    }

    @Override
    public ButlerUpdateData build() {
        return new ButlerUpdateData(plugin, notifyPermission, notifyUpdate, autoUpdate, butlerId, host, updateUrl, updateMessage);
    }
}
