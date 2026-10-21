/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.debug.data.chart.ChartData;
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
 * Use {@link #getHealthChecks()} to report what the plugin checked about its own setup.
 * <p>
 * Your plugin instance is the entry point for debugging. If you want to use this interface your plugin class has to implement it.
 * <p>
 * Instance of {@code EldoPlugin} are already a {@link DebugDataProvider}. Just override the method.
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

    /**
     * Checks this part of the plugin's setup, e.g. whether its database answers or the worlds it is configured for
     * exist. Runs on the thread the debug command runs on, so keep it quick.
     *
     * @return the outcomes, empty by default
     */
    @NotNull
    default List<HealthCheck> getHealthChecks() {
        return Collections.emptyList();
    }

    /**
     * Charts drawn from this part of the plugin's data, e.g. timings it keeps. Runs on the thread the debug command
     * runs on, so only copy what is already there.
     *
     * @return the charts, empty by default
     */
    @NotNull
    default List<ChartData<?>> getCharts() {
        return Collections.emptyList();
    }
}
