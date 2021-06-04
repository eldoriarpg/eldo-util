package de.eldoria.eldoutilities.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.eldoria.eldoutilities.configuration.ConfigFileWrapper;
import de.eldoria.eldoutilities.core.EldoUtilities;
import de.eldoria.eldoutilities.debug.data.DebugPayloadData;
import de.eldoria.eldoutilities.debug.data.DebugResponse;
import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.messages.MessageChannel;
import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.messages.MessageType;
import de.eldoria.eldoutilities.plugin.EldoPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

/**
 * @since 1.3.2
 */
public final class DebugUtil {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private DebugUtil() {
    }

    /**
     * This method will collect debug data from the plugin.
     * <p>
     * It will also send it to the butler instance and will send the links to the sender.
     *
     * @param sender   sender which requested the data
     * @param plugin   plugin to collect the data for
     * @param settings settings for debug dispatching
     */
    public static void dispatchDebug(CommandSender sender, Plugin plugin, DebugSettings settings) {
        ConfigFileWrapper config = EldoUtilities.getConfiguration();
        MessageSender messageSender = MessageSender.getPluginMessageSender(plugin);
        if (!config.get().getBoolean("debugConsens", false)) {
            String message = "By using this command you agree that we will send data belonging to you to §lour server§r.\n"
                    + "We will only send data when someone executes this command.\n"
                    + "The data will be handled confidential from our side and will be only available by a hashed key.\n"
                    + "Unless you share this key no one can access it. §cEveryone who receives this key will have access to your data.§r\n"
                    + "You can delete your data at every time with the deletion key. §cIf you lose or didnt save your key we can not help you.§r\n"
                    + "Your data will be deleted after §l§c14 days§r.\n"
                    + "This data includes but is §l§cnot§r limited to:\n"
                    + "  - Installed Plugins and their meta data\n"
                    + "  - Latest log\n"
                    + "  - Server Informations like Worldnames and Playercount\n"
                    + "  - The configuration file or files of the debugged plugin\n"
                    + "  - Additional Data provided by our own plugins.\n"
                    + "We will filter sensitive data like IPs before sending.\n"
                    + "However we §l§ccan not§r and §l§cwill not§r gurantee that we can remove all data which is considered confidential by you.\n"
                    + "§2If you agree please execute this command once again.\n"
                    + "§2This is a one time opt in.\n"
                    + "You can opt out again in the EldoUtilities config file.";
            messageSender.send(MessageChannel.CHAT, () -> "§6", sender, message);
            config.write(c -> c.set("debugConsens", true));
            return;
        }

        EldoUtilities.getAsyncSyncingCallbackExecutor().schedule(
                () -> sendDebug(plugin, DebugPayload.create(plugin, settings), settings),
                debugResponse -> {
                    if (debugResponse.isPresent()) {
                        messageSender.send(MessageChannel.CHAT, MessageType.NORMAL, sender,
                                "Your data is available here:\n"
                                        + "§6" + settings.getHost() + "/debug/v1/read/" + debugResponse.get().getHash()
                                        + "§r\nYou can delete it via this link:\n"
                                        + "§c" + settings.getHost() + "/debug/v1/delete/" + debugResponse.get().getDeletionHash());
                    } else {
                        messageSender.send(MessageChannel.CHAT, MessageType.ERROR, sender, "Could not send data. Please try again later");
                    }
                });
    }

    /**
     * Extracts additional Plugin meta data from a {@link EldoPlugin}.
     * <p>
     * If the plugin is not a eldo plugin it will return an empty array.
     *
     * @param plugin plugin to get meta
     * @return array with meta data. may be empty, but not null.
     */
    public static @NotNull EntryData[] getAdditionalPluginMeta(Plugin plugin) {
        List<EntryData> meta = new LinkedList<>();

        if (plugin instanceof DebugDataProvider) {
            Set<DebugDataProvider> debuged = new HashSet<>();
            Queue<DebugDataProvider> providers = new ArrayDeque<>();
            providers.add((DebugDataProvider) plugin);
            debuged.add((DebugDataProvider) plugin);
            while (!providers.isEmpty()) {
                DebugDataProvider provider = providers.poll();
                for (DebugDataProvider nextProvider : provider.getDebugProviders()) {
                    if (debuged.contains(nextProvider)) {
                        plugin.getLogger()
                                .warning("Loop in debug data detected. Instance of class "
                                        + nextProvider.getClass().getSimpleName()
                                        + " returns a reference to already debugged instance of "
                                        + provider.getClass().getSimpleName());
                        continue;
                    }
                    providers.add(nextProvider);
                    debuged.add(nextProvider);
                }
                meta.addAll(Arrays.asList(provider.getDebugInformations()));
            }
        }

        return meta.toArray(new EntryData[0]);
    }

    private static Optional<DebugResponse> sendDebug(Plugin plugin, DebugPayloadData payload, DebugSettings settings) {
        // Open connection to Butler.
        HttpURLConnection con;
        try {
            URL url = new URL(settings.getHost() + "/debug/v1/submit");
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
        } catch (IOException e) {
            plugin.getLogger().log(Level.FINEST, "Could not open connection.", e);
            return Optional.empty();
        }

        // we will send json. probably.
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        // Lets write our data.
        try (OutputStream outputStream = con.getOutputStream()) {
            byte[] input = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
        } catch (IOException e) {
            plugin.getLogger().info("Could not write to connection.");
        }

        // check if the response was OK.
        try {
            if (con.getResponseCode() != 200) {
                plugin.getLogger().log(Level.FINEST, "Received non 200 request for debug submission.\n" + con.getResponseMessage());
                return Optional.empty();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.INFO, "Could not read response.", e);
            return Optional.empty();
        }

        // Lets read the response.
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                builder.append(responseLine.trim());
            }
            return Optional.of(GSON.fromJson(builder.toString(), DebugResponse.class));
        } catch (IOException e) {
            plugin.getLogger().log(Level.FINEST, "Could not read response.", e);
            return Optional.empty();
        }
    }
}
