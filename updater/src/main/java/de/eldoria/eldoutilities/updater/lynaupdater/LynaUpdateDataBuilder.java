/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
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
        updateMessage = """
                New version of <gold>{plugin_name}<default> available.
                New Version: <gold>{new_version}<default>. Published <gold>{new_time}<default> ago! {new_date_time}
                Current version: <gold>{current_version}<default> Published §c{current_time}<default> ago! {current_date_time}<default>
                Download the new version via Discord: <gold>{website}""".stripIndent();
    }

    public LynaUpdateDataBuilder host(String host) {
        this.host = host;
        return this;
    }

    /**
     * {@inheritDoc}
     * <table border="1">
     *   <caption><b>Available Lyna placeholder</b></caption>
     *   <tr>
     *     <td>new_time</td> <td>Pretty formatted duration string showing the time since new version was published</td>
     *   </tr>
     *   <tr>
     *     <td>current_time</td> <td>Pretty formatted duration string showing the time since current version was published</td>
     *   </tr>
     *   <tr>
     *     <td>new_date_time</td> <td>The date and time the version was published</td>
     *   </tr>
     *   <tr>
     *     <td>current_date_time</td> <td>The date and time the current used version was published</td>
     *   </tr>
     * </table>
     */
    @Override
    public LynaUpdateDataBuilder updateMessage(String updateMessage) {
        return super.updateMessage(updateMessage);
    }

    @Override
    public LynaUpdateData build() {
        return new LynaUpdateData(plugin, notifyPermission, notifyUpdate, productId, host, updateUrl, updateMessage);
    }
}
