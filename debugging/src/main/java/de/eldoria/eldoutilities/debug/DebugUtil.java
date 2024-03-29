/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.eldoria.EldoUtilities;
import de.eldoria.eldoutilities.debug.data.DebugPayloadData;
import de.eldoria.eldoutilities.debug.data.DebugResponse;
import de.eldoria.eldoutilities.debug.data.EntryData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        var config = EldoUtilities.getConfiguration();
        if (!config.getBoolean("debugConsens", false)) {
            var message = """
                    By using this command you agree that we will send data belonging to you to §lour server§r.\n"
                    We will only send data when someone executes this command.
                    The data will be handled confidential from our side and will be only available by a hashed key.
                    Unless you share this key no one can access it. §cEveryone who receives this key will have access to your data.§r
                    You can delete your data at every time with the deletion key. §cIf you lose or didnt save your key we can not help you.§r
                    Your data will be deleted after §l§c14 days§r.
                    This data includes but is §l§cnot§r limited to:
                      - Installed Plugins and their meta data
                      - Latest log
                      - Server Informations like Worldnames and Playercount
                      - The configuration file or files of the debugged plugin
                      - Additional Data provided by our own plugins.
                    We will filter sensitive data like IPs before sending.
                    However we §l§ccan not§r and §l§cwill not§r gurantee that we can remove all data which is considered confidential by you.
                    §2If you agree please execute this command once again.
                    §2This is a one time opt in.
                    You can opt out again in the EldoUtilities config file""".stripIndent();
            sender.sendMessage(message);
            config.set("debugConsens", true);
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            var debugResponse = sendDebug(plugin, DebugPayload.create(plugin, settings), settings);
            if (debugResponse.isPresent()) {
                var message = """
                        Your data is available here:
                        §6%s/debug/v1/read/%s§r
                        You can delete it via this link:
                        §c%s/debug/v1/delete/%s""".formatted(settings.getHost(), debugResponse.get().getHash(), settings.getHost(), debugResponse.get().getDeletionHash());
                sender.sendMessage(message);
            } else {
                sender.sendMessage("Could not send data. Please try again later");
            }
        });
    }

    /**
     * Extracts additional Plugin meta data from a {@code EldoPlugin}.
     * <p>
     * If the plugin is not a eldo plugin it will return an empty array.
     *
     * @param plugin plugin to get meta
     * @return array with meta data. may be empty, but not null.
     */
    public static @NotNull EntryData[] getAdditionalPluginMeta(Plugin plugin) {
        List<EntryData> meta = new LinkedList<>();

        UserData userData = UserData.get(plugin);

        meta.add(new EntryData("User Data", userData.asString()));

        if (plugin instanceof DebugDataProvider) {
            Set<DebugDataProvider> debuged = new HashSet<>();
            Queue<DebugDataProvider> providers = new ArrayDeque<>();
            providers.add((DebugDataProvider) plugin);
            debuged.add((DebugDataProvider) plugin);
            while (!providers.isEmpty()) {
                var provider = providers.poll();
                for (var nextProvider : provider.getDebugProviders()) {
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
            var url = new URL(settings.getHost() + "/debug/v1/submit");
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
        try (var outputStream = con.getOutputStream()) {
            var input = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
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
        try (var br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            var builder = new StringBuilder();
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
