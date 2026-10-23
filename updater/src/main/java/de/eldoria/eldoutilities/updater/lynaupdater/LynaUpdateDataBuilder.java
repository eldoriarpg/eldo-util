/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import de.eldoria.eldoutilities.updater.UpdateDataBuilder;
import de.eldoria.eldoutilities.updater.UpdaterMessages;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class LynaUpdateDataBuilder extends UpdateDataBuilder<LynaUpdateDataBuilder, LynaUpdateData> {

    private final int productId;
    private String host = LynaUpdateData.HOST;
    private UpgradeTokenStore tokenStore;
    private @Nullable Integer downloadType;
    private boolean customUpdateUrl;

    public LynaUpdateDataBuilder(Plugin plugin, int productId) {
        super(plugin);
        this.productId = productId;
        tokenStore = UpgradeTokenStore.file(plugin.getDataFolder().toPath().resolve("upgrade-token.txt"));
        updateMessage = UpdaterMessages.LYNA_NOTICE;
    }

    public LynaUpdateDataBuilder host(String host) {
        this.host = host;
        return this;
    }

    /**
     * Where the upgrade token of a premium product is kept. Defaults to {@code upgrade-token.txt} in the plugin
     * data folder.
     *
     * @param tokenStore token store
     * @return builder instance
     */
    public LynaUpdateDataBuilder tokenStore(UpgradeTokenStore tokenStore) {
        this.tokenStore = tokenStore;
        return this;
    }

    /**
     * The download type a premium product requests, needed when the product has more than one stable download.
     * Without one, Lyna picks the first download the account may have, stable before dev before snapshot.
     *
     * @param downloadType id of the download type at lyna
     * @return builder instance
     */
    public LynaUpdateDataBuilder downloadType(int downloadType) {
        this.downloadType = downloadType;
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

    /**
     * The address the update message links to. Defaults to the product page of the Lyna instance, not the website of
     * the plugin.
     *
     * @param updateUrl address of the download or patch notes
     * @return builder instance
     */
    @Override
    public LynaUpdateDataBuilder updateUrl(String updateUrl) {
        customUpdateUrl = true;
        return super.updateUrl(updateUrl);
    }

    @Override
    public LynaUpdateData build() {
        var url = customUpdateUrl ? updateUrl : productPage(host, productId);
        return applyCommon(new LynaUpdateData(
                plugin,
                notifyPermission,
                notifyUpdate,
                autoUpdate,
                productId,
                host,
                url,
                updateMessage,
                tokenStore,
                downloadType,
                messageSender));
    }

    static String productPage(String host, int productId) {
        return "%s/products/%d".formatted(host.replaceAll("/+$", ""), productId);
    }
}
