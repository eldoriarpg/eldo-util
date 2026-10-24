/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The Lyna licence key a plugin jar was handed out with.
 * <p>
 * The build writes {@code lynaLicense=%%__LYNA_LICENSE__%%} into its build data, with publishdata's
 * {@code buildData { lynaLicense() }}. Whoever hands the jar out replaces the placeholder with the key the jar was
 * obtained with: Lyna when it serves the download, BuiltByBit through a custom placeholder of the same name. A local
 * build, or one taken straight from the repository, keeps the placeholder as written.
 * <p>
 * The placeholder is assembled at runtime rather than written as one literal, so a jar that shades this class does not
 * have it replaced along with the build data and then mistake every key for the placeholder.
 *
 * @since 3.7.0
 */
public final class LynaLicense {
    /**
     * The placeholder the build data holds until the jar is handed out.
     */
    public static final String PLACEHOLDER = String.join("", "%%__", "LYNA_LICENSE", "__%%");

    /**
     * What BuiltByBit writes in place of the key when it could not get one.
     */
    static final String BUILTBYBIT_FAILURE = "Unable to acquire a license key automatically";

    private final State state;
    private final @Nullable String key;

    private LynaLicense(State state, @Nullable String key) {
        this.state = state;
        this.key = key;
    }

    /**
     * Reads the licence from the build data of a plugin.
     *
     * @param plugin plugin whose jar to read
     * @return the licence
     */
    public static LynaLicense of(Plugin plugin) {
        return of(UserData.get(plugin));
    }

    /**
     * Reads the licence from build data already read.
     *
     * @param userData build data of the plugin
     * @return the licence
     */
    public static LynaLicense of(UserData userData) {
        return of(userData.property(DefaultProperties.LYNA_LICENSE).orElse(null));
    }

    /**
     * Reads the licence from the value of the build data entry.
     *
     * @param value the value, or null when the build data has no such entry
     * @return the licence
     */
    public static LynaLicense of(@Nullable String value) {
        if (value == null || value.isBlank() || PLACEHOLDER.equals(value.strip())) {
            return new LynaLicense(State.NOT_INJECTED, null);
        }
        if (value.contains(BUILTBYBIT_FAILURE)) {
            return new LynaLicense(State.UNAVAILABLE, null);
        }
        return new LynaLicense(State.INJECTED, value.strip());
    }

    /**
     * Whether the jar carries a key, and if not, why.
     *
     * @return the state
     */
    public State state() {
        return state;
    }

    /**
     * The licence key, present only when {@link #state()} is {@link State#INJECTED}.
     *
     * @return the key
     */
    public Optional<String> key() {
        return Optional.ofNullable(key);
    }

    /**
     * Whether a jar carries a licence key.
     */
    public enum State {
        /**
         * The placeholder was replaced with a key.
         */
        INJECTED,
        /**
         * The build data has no entry, or still holds the placeholder: a local build, one taken from the repository,
         * or a download no licence of the downloader covers.
         */
        NOT_INJECTED,
        /**
         * BuiltByBit could not get a key and wrote its apology instead.
         */
        UNAVAILABLE
    }
}
