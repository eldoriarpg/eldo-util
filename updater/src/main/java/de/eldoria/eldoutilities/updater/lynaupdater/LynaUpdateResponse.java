/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater.lynaupdater;

import de.eldoria.eldoutilities.updater.UpdateResponse;
import de.eldoria.eldoutilities.utils.Durations;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

/**
 * A Lyna instance's answer to an update check.
 *
 * @since 1.1.0
 */
public class LynaUpdateResponse implements UpdateResponse {
    private final boolean update;
    private final String latest;
    /**
     * The publishing date as unix timestamp
     */
    private final long published;
    /**
     * Public address of the latest build. Only set for free products.
     */
    private final @Nullable String download;

    /**
     * Create a new Update check response.
     *
     * @param update    whether a new version is available or not
     * @param latest    latest available version
     * @param published publishing date of the latest version as unix timestamp
     * @param download  public address of the latest build, or null for a premium product
     */
    public LynaUpdateResponse(boolean update, String latest, long published, @Nullable String download) {
        this.update = update;
        this.latest = latest;
        this.published = published;
        this.download = download;
    }

    /**
     * Create a new Update check response without a public download.
     *
     * @param update    whether a new version is available or not
     * @param latest    latest available version
     * @param published publishing date of the latest version as unix timestamp
     */
    public LynaUpdateResponse(boolean update, String latest, long published) {
        this(update, latest, published, null);
    }

    public boolean isUpdate() {
        return update;
    }

    @Override
    public boolean isOutdated() {
        return update;
    }

    @Override
    public String latestVersion() {
        return latest;
    }

    public long published() {
        return published;
    }

    /**
     * Public address of the latest build, which Lyna only hands out for free products.
     *
     * @return download address, or empty for a premium product
     */
    public Optional<String> download() {
        return Optional.ofNullable(download);
    }

    /**
     * Whether the latest version is a stable release, by the rule lyna sorts versions with: neither snapshot nor dev
     * builds.
     *
     * @return true for a stable release
     */
    public boolean isStable() {
        return isStable(latest);
    }

    /**
     * Whether a version is a stable release, by the rule lyna sorts versions with: neither snapshot nor dev builds.
     *
     * @param version version to check
     * @return true for a stable release
     */
    static boolean isStable(String version) {
        var lower = version.toLowerCase(Locale.ROOT);
        return !lower.contains("snapshot") && !lower.contains("dev");
    }

    /**
     * Whether two versions share their major version, the number before the first dot. A version without a leading
     * number shares it with none.
     *
     * @param current installed version
     * @param latest  newer version
     * @return true if both have the same major version
     */
    static boolean sameMajor(String current, String latest) {
        var major = major(current);
        return major != null && major.equals(major(latest));
    }

    private static String major(String version) {
        var digits = 0;
        while (digits < version.length() && Character.isDigit(version.charAt(digits))) digits++;
        return digits == 0 ? null : version.substring(0, digits);
    }

    public String publishedDuration() {
        return Durations.simpleDurationFormat(Duration.between(Instant.ofEpochSecond(published), Instant.now()));
    }
}
