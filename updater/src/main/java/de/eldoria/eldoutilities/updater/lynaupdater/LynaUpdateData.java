/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import de.eldoria.eldoutilities.debug.DefaultProperties;
import de.eldoria.eldoutilities.debug.UserData;
import de.eldoria.eldoutilities.updater.UpdateData;
import de.eldoria.eldoutilities.utils.Durations;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * What a Lyna update check needs: the product id and the Lyna host.
 * <p>
 * Free products are updated from the public download address the update check answers with. Premium products
 * trade an upgrade token from the {@link #tokenStore()} for their download.
 *
 * @since 1.1.0
 */
public class LynaUpdateData extends UpdateData<LynaUpdateResponse> {
    /**
     * Default adress to submit debug data and update checks
     */
    public static final String HOST = "https://lyna.eldoria.de";
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withZone(ZoneId.of("Z"));

    private final int productId;
    private final String host;
    private final UserData userData;
    private final UpgradeTokenStore tokenStore;
    private final @Nullable Integer downloadType;

    /**
     * Create a new update data object to pass to lyna.
     *
     * @param plugin           plugin instance
     * @param notifyPermission permission which will be required for update notification
     * @param notifyUpdate     true if users with permission should be notified
     * @param autoUpdate       true if new versions should be downloaded
     * @param productId        id of lyna product
     * @param host             host of lyna instance
     * @param tokenStore       upgrade token of a premium product
     * @param downloadType     download type to request for a premium product, or null for the first one available
     */
    LynaUpdateData(Plugin plugin, String notifyPermission, boolean notifyUpdate, boolean autoUpdate, int productId,
                   String host, String updateUrl, String updateMessage, UpgradeTokenStore tokenStore,
                   @Nullable Integer downloadType) {
        super(plugin, notifyPermission, notifyUpdate, autoUpdate, updateUrl, updateMessage);
        this.productId = productId;
        this.host = host;
        this.tokenStore = tokenStore;
        this.downloadType = downloadType;
        userData = UserData.get(plugin);
    }

    public static LynaUpdateDataBuilder builder(Plugin plugin, int productId) {
        return new LynaUpdateDataBuilder(plugin, productId);
    }


    @Override
    protected Map<String, Object> replacements(LynaUpdateResponse updateResponse) {
        var replacements = super.replacements(updateResponse);
        replacements.put("new_time", updateResponse.publishedDuration());
        userData.property(DefaultProperties.UNIX)
                .map(Long::parseLong)
                .map(Instant::ofEpochSecond)
                .map(instant -> Duration.between(instant, Instant.now()))
                .map(Durations::simpleDurationFormat)
                .ifPresent(time -> replacements.put("current_time", time));
        userData.property(DefaultProperties.UNIX)
                .map(Long::parseLong)
                .map(Instant::ofEpochSecond)
                .map(format::format)
                .ifPresent(time -> replacements.put("current_date_time", time));
        replacements.put("new_date_time", format.format(Instant.ofEpochSecond(updateResponse.published())));
        return replacements;
    }

    public int productId() {
        return productId;
    }

    public String host() {
        return host;
    }

    public UserData userData() {
        return userData;
    }

    public UpgradeTokenStore tokenStore() {
        return tokenStore;
    }

    public @Nullable Integer downloadType() {
        return downloadType;
    }
}
