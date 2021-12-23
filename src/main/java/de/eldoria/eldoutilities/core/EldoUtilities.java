package de.eldoria.eldoutilities.core;

import de.eldoria.eldoutilities.configuration.ConfigFileWrapper;
import de.eldoria.eldoutilities.conversation.ConversationRequester;
import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.builder.VersionFunctionBuilder;
import de.eldoria.eldoutilities.inventory.InventoryActionHandler;
import de.eldoria.eldoutilities.messages.MessageChannel;
import de.eldoria.eldoutilities.plugin.EldoPlugin;
import de.eldoria.eldoutilities.scheduling.DelayedActions;
import de.eldoria.eldoutilities.serialization.util.PluginSerializationName;
import de.eldoria.eldoutilities.serialization.wrapper.ArmorStandWrapper;
import de.eldoria.eldoutilities.serialization.wrapper.MapEntry;
import de.eldoria.eldoutilities.threading.AsyncSyncingCallbackExecutor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Core class of EldoUtilitites.
 * <p>
 * If you want to use anything from here you need to call {@link EldoUtilities#preWarm(Plugin)} onLoad and {@link EldoUtilities#ignite(Plugin)} onEnable.
 * If your plugins extends {@link EldoPlugin} this will be done automatically.
 */
public final class EldoUtilities {
    private static Plugin mainOwner;
    private static Map<Class<? extends Plugin>, Plugin> instanceOwners = new LinkedHashMap<>();
    private static ConfigFileWrapper configuration;

    private EldoUtilities() {
    }

    public static Logger logger() {
        return Bukkit.getLogger();
    }

    public static void preWarm(Plugin eldoPlugin) {
        instanceOwners.put(eldoPlugin.getClass(), eldoPlugin);
        for (var clazz : getConfigSerialization()) {
            if (clazz.isAnnotationPresent(PluginSerializationName.class)) {
                var annotation = clazz.getAnnotation(PluginSerializationName.class);
                ConfigurationSerialization.registerClass(clazz,
                        annotation.value().replace("{plugin}", eldoPlugin.getName().toLowerCase(Locale.ROOT)));
            } else {
                ConfigurationSerialization.registerClass(clazz);
            }
        }
    }

    public static void ignite(Plugin plugin) {
        VersionFunctionBuilder.functionBuilder(null, null)
                .addVersionFunctionBetween(ServerVersion.MC_1_13, ServerVersion.MC_1_17,
                        a -> {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> performLateCleanUp(plugin), 5);
                            return null;
                        });
        var path = Bukkit.getUpdateFolderFile().toPath().toAbsolutePath().getParent().resolve(Paths.get("EldoUtilities", "config.yml"));
        configuration = ConfigFileWrapper.forFile(plugin, path);
    }

    private static void performLateCleanUp(Plugin plugin) {
        var bossBars = Bukkit.getBossBars();
        while (bossBars.hasNext()) {
            var bar = bossBars.next();
            var key = bar.getKey();
            if (!key.getNamespace().equalsIgnoreCase(plugin.getName())) continue;
            if (key.getKey().startsWith(MessageChannel.KEY_PREFIX)) {
                logger().config("Removed boss bar with key" + key);
                bar.removeAll();
                Bukkit.removeBossBar(key);
            }
        }
    }

    public static void shutdown() {
    }

    public static List<Class<? extends ConfigurationSerializable>> getConfigSerialization() {
        return Arrays.asList(MapEntry.class, ArmorStandWrapper.class,
                de.eldoria.eldoutilities.serialization.util.MapEntry.class,
                de.eldoria.eldoutilities.serialization.util.ArmorStandWrapper.class);
    }

    public static ConfigFileWrapper getConfiguration() {
        if (configuration == null) {
            var config = Bukkit.getUpdateFolderFile().toPath().toAbsolutePath().getParent().resolve(Paths.get("EldoUtilities", "config.yml"));
            configuration = ConfigFileWrapper.forFile(config);
        }
        return configuration;
    }

    public static Plugin getInstanceOwner(Class<? extends Plugin> plugin) {
        return instanceOwners.get(plugin);
    }

    public static void forceInstanceOwner(Plugin plugin) {
        if (mainOwner != null) {
            throw new IllegalStateException("A instance owner is already set");
        }
        mainOwner = plugin;
    }

    public static Plugin getInstanceOwner() {
        if (mainOwner != null) {
            return mainOwner;
        }

        for (var entry : instanceOwners.entrySet()) {
            return entry.getValue();
        }

        throw new IllegalStateException("No instance owner is set but requested");
    }
}
