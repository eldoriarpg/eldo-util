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
import org.bukkit.scheduler.BukkitTask;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Core class of EldoUtilitites.
 * <p>
 * If you want to use anything from here you need to call {@link EldoUtilities#preWarm(Plugin)} onLoad and {@link EldoUtilities#ignite(Plugin)} onEnable.
 * If your plugins extends {@link EldoPlugin} this will be done automatically.
 */
public final class EldoUtilities {
    private static DelayedActions delayedActions;
    private static InventoryActionHandler inventoryActionHandler;
    private static AsyncSyncingCallbackExecutor asyncSyncingCallbackExecutor;
    private static ConversationRequester conversationRequester;
    private static ConfigFileWrapper configuration;
    private static Plugin instanceOwner;

    private EldoUtilities() {
    }

    public static DelayedActions getDelayedActions() {
        if (delayedActions == null) {
            delayedActions = DelayedActions.start(instanceOwner);
            logger().config("DelayedActions ignited.");
        }
        return delayedActions;
    }

    public static ConversationRequester getConversationRequester() {
        if (conversationRequester == null) {
            conversationRequester = ConversationRequester.start(instanceOwner);
            logger().config("ConversationRequester ignited.");
        }
        return conversationRequester;
    }

    public static InventoryActionHandler getInventoryActions() {
        if (inventoryActionHandler == null) {
            inventoryActionHandler = InventoryActionHandler.create(instanceOwner);
            logger().config("InventoryActionHandler ignited.");
        }
        return inventoryActionHandler;
    }

    public static AsyncSyncingCallbackExecutor getAsyncSyncingCallbackExecutor() {
        if (asyncSyncingCallbackExecutor == null) {
            asyncSyncingCallbackExecutor = AsyncSyncingCallbackExecutor.create(instanceOwner);
            logger().config("AsyncSyncingCallbackExecutor ignited.");
        }
        return asyncSyncingCallbackExecutor;
    }

    public static Logger logger() {
        return instanceOwner.getLogger();
    }

    public static void preWarm(Plugin eldoPlugin) {
        instanceOwner = eldoPlugin;
        for (Class<? extends ConfigurationSerializable> clazz : getConfigSerialization()) {
            if (clazz.isAnnotationPresent(PluginSerializationName.class)) {
                PluginSerializationName annotation = clazz.getAnnotation(PluginSerializationName.class);
                ConfigurationSerialization.registerClass(clazz,
                        annotation.value().replace("{plugin}", eldoPlugin.getName().toLowerCase(Locale.ROOT)));
            } else {
                ConfigurationSerialization.registerClass(clazz);
            }
        }
    }

    public static void ignite(Plugin eldoPlugin) {
        VersionFunctionBuilder.functionBuilder(null, null)
                .addVersionFunctionBetween(ServerVersion.MC_1_13, ServerVersion.MC_1_17,
                        a -> {
                            Bukkit.getScheduler().runTaskLater(eldoPlugin, EldoUtilities::performLateCleanUp, 5);
                            return null;
                        });
        Path path = Bukkit.getUpdateFolderFile().toPath().toAbsolutePath().getParent().resolve(Paths.get("EldoUtilities", "config.yml"));
        configuration = ConfigFileWrapper.forFile(eldoPlugin, path);
    }

    private static void performLateCleanUp() {
        Iterator<KeyedBossBar> bossBars = Bukkit.getBossBars();
        while (bossBars.hasNext()) {
            KeyedBossBar bar = bossBars.next();
            NamespacedKey key = bar.getKey();
            if (!key.getNamespace().equalsIgnoreCase(instanceOwner.getName())) continue;
            if (key.getKey().startsWith(MessageChannel.KEY_PREFIX)) {
                logger().config("Removed boss bar with key" + key);
                bar.removeAll();
                Bukkit.removeBossBar(key);
            }
        }
    }

    public static void shutdown() {
        if (delayedActions != null) {
            delayedActions.shutdown();
            delayedActions = null;
        }
        if (asyncSyncingCallbackExecutor != null) {
            asyncSyncingCallbackExecutor.shutdown();
            asyncSyncingCallbackExecutor = null;
        }
        if (inventoryActionHandler != null) {
            inventoryActionHandler = null;
        }
        if (conversationRequester != null) {
            conversationRequester = null;
        }
    }

    public static List<Class<? extends ConfigurationSerializable>> getConfigSerialization() {
        return Arrays.asList(MapEntry.class, ArmorStandWrapper.class,
                de.eldoria.eldoutilities.serialization.util.MapEntry.class,
                de.eldoria.eldoutilities.serialization.util.ArmorStandWrapper.class);
    }

    public static ConfigFileWrapper getConfiguration() {
        if (configuration == null) {
            Path config = Bukkit.getUpdateFolderFile().toPath().toAbsolutePath().getParent().resolve(Paths.get("EldoUtilities", "config.yml"));
            configuration = ConfigFileWrapper.forFile(config);
        }
        return configuration;
    }

    public static Plugin getInstanceOwner() {
        return instanceOwner;
    }
}
