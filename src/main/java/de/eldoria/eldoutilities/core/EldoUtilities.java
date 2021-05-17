package de.eldoria.eldoutilities.core;

import de.eldoria.eldoutilities.configuration.ConfigFileWrapper;
import de.eldoria.eldoutilities.conversation.ConversationRequester;
import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.functionbuilder.VersionFunctionBuilder;
import de.eldoria.eldoutilities.inventory.InventoryActionHandler;
import de.eldoria.eldoutilities.messages.MessageChannel;
import de.eldoria.eldoutilities.scheduling.DelayedActions;
import de.eldoria.eldoutilities.serialization.wrapper.ArmorStandWrapper;
import de.eldoria.eldoutilities.serialization.wrapper.MapEntry;
import de.eldoria.eldoutilities.serialization.util.PluginSerializationName;
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
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public final class EldoUtilities {
    private static DelayedActions delayedActions = null;
    private static InventoryActionHandler inventoryActionHandler = null;
    private static AsyncSyncingCallbackExecutor asyncSyncingCallbackExecutor = null;
    private static ConversationRequester conversationRequester = null;
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
        Path eldoUtilconfig = Paths.get("..", "EldoUtilities", "config.yml");
        configuration = ConfigFileWrapper.forFile(eldoPlugin, eldoUtilconfig.toString());
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
        return Arrays.asList(MapEntry.class, ArmorStandWrapper.class);
    }

    public static ConfigFileWrapper getConfiguration() {
        return configuration;
    }

    public static Plugin getInstanceOwner() {
        return instanceOwner;
    }
}
