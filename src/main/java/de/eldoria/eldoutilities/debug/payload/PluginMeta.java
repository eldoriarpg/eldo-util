/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.payload;

import de.eldoria.eldoutilities.debug.data.PluginMetaData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public final class PluginMeta extends PluginMetaData {

    private PluginMeta(String name, String version, boolean enabled, String main, String[] authors, String[] loadBefore, String[] dependencies, String[] softDependencies, String[] provides) {
        super(name, version, enabled, main, authors, loadBefore, dependencies, softDependencies, provides);
    }

    /**
     * Create a new plugin meta data for a plugin
     *
     * @param plugin plugin to create meta for
     * @return plugin meta instance for plugin
     */
    public static PluginMetaData create(Plugin plugin) {
        var name = plugin.getName();
        var descr = plugin.getDescription();
        var version = descr.getVersion();
        var enabled = plugin.isEnabled();
        var main = descr.getMain();
        var authors = descr.getAuthors().toArray(new String[0]);
        var loadBefore = descr.getLoadBefore().toArray(new String[0]);
        var dependencies = descr.getDepend().toArray(new String[0]);
        var softDependencies = descr.getSoftDepend().toArray(new String[0]);
        String[] provides;
        // TODO: Replace this with proper version detection
        try {
            provides = descr.getProvides().toArray(new String[0]);
        } catch (NoSuchMethodError e) {
            provides = new String[0];
        }
        return new PluginMeta(name, version, enabled, main, authors,
                loadBefore, dependencies, softDependencies, provides);
    }

}
