/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import de.eldoria.eldoutilities.updater.UpdateDataBuilder;
import org.bukkit.plugin.Plugin;

public class LynaUpdateDataBuilder extends UpdateDataBuilder<LynaUpdateDataBuilder, LynaUpdateData> {

    private final int productId;
    private String host = LynaUpdateData.HOST;

    public LynaUpdateDataBuilder(Plugin plugin, int productId) {
        super(plugin);
        this.productId = productId;
    }

    public LynaUpdateDataBuilder host(String host) {
        this.host = host;
        return this;
    }

    @Override
    public LynaUpdateData build() {
        return new LynaUpdateData(plugin, notifyPermission, notifyUpdate, productId, host, updateUrl, updateMessage);
    }
}
