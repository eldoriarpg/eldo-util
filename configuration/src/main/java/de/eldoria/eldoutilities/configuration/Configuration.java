/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.configuration;

import org.bukkit.plugin.Plugin;

/**
 * Wrapper class for EldoConfig. Can be used as default and unconfigured config.
 * @deprecated Use the jackson-configuration module. This module is removed in 4.0.
 */
@Deprecated(since = "3.6.0", forRemoval = true)
public class Configuration extends EldoConfig {
    public Configuration(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected void saveConfigs() {
    }

    @Override
    protected void reloadConfigs() {

    }

    @Override
    protected void init() {

    }
}
