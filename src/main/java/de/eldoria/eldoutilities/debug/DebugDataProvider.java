/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.plugin.EldoPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * A interface which allows to provide additional data when a plugin is debugged via {@link DebugUtil}.
 * <p>
 * Use {@link #getDebugInformations()} to provide information about the current object instance.
 * <p>
 * Use {@link #getDebugProviders()} to provide other debug providers.
 * <p>
 * Your plugin instance is the entry point for debugging. If you want to use this interface your plugin class has to implement it.
 * <p>
 * Instance of {@link EldoPlugin} are already a {@link DebugDataProvider}. Just override the method.
 *
 * @since 1.3.4
 */
public interface DebugDataProvider {

    /**
     * Get debug information for the current object instance.
     *
     * @return array of entry data.
     */
    @NotNull
    EntryData[] getDebugInformations();

    /**
     * Get one or more debug providers which should be debugged.
     * <p>
     * Make sure to avoid loops.
     * <p>
     * This function will be first tried to call on the plugin instance. This is the debug entry point.
     * Return more debug providers from this inital point.
     * <p>
     * Returned debug providers can also return other debug providers and so on.
     *
     * @return list of other debug providers.
     */
    @NotNull
    default List<DebugDataProvider> getDebugProviders() {
        return Collections.emptyList();
    }
}
