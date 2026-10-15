/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import com.google.gson.Gson;
import de.eldoria.EldoUtilities;
import de.eldoria.eldoutilities.debug.DebugUpload.Failed;
import de.eldoria.eldoutilities.debug.DebugUpload.Result;
import de.eldoria.eldoutilities.debug.DebugUpload.Uploaded;
import de.eldoria.eldoutilities.debug.data.EntryData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Uploads debug reports to a Lyna instance and hands the links to whoever asked for one.
 * <p>
 * The first request on a server explains what is sent and asks to run the command again; that consent is kept as
 * {@value #CONSENT} in {@code plugins/EldoUtilities/config.yml}, where setting it to {@code false} withdraws it.
 *
 * @since 1.3.2
 */
public final class DebugUtil {
    static final String CONSENT = "debugConsent";
    private static final Gson GSON = new Gson();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withZone(ZoneId.systemDefault());
    private static final String CONSENT_TEXT = """
            <gold>Debug reports</gold>
            <gray>This uploads data about your server to <white><host></white>, so we can help you:</gray>
            <gray> - installed plugins and their versions</gray>
            <gray> - the latest log</gray>
            <gray> - server details such as world names and the player count</gray>
            <gray> - the configuration files of <white><plugin></white>, and what it adds about itself</gray>
            <gray>IP addresses, passwords, tokens and similar values are hidden before anything is sent, but we <red>cannot guarantee</red> that every value you consider confidential is caught.</gray>
            <gray>Only who has the link can read the report. <red>Everyone you give the link to can read it.</red> It is deleted after two weeks, or earlier with the delete link.</gray>
            <green>Run the command again to agree and upload.</green> <gray>You are asked once; set <white>debugConsent: false</white> in plugins/EldoUtilities/config.yml to withdraw.</gray>""";

    private DebugUtil() {
    }

    /**
     * Collects a debug report about the plugin, uploads it, and sends the links to the sender.
     * <p>
     * The server and the plugin are asked about themselves on the calling thread; reading the log and the files,
     * filtering and uploading run in the background. The sender hears back when the upload is done or has failed.
     *
     * @param sender   sender which requested the data
     * @param plugin   plugin to collect the data for
     * @param settings settings for debug dispatching
     */
    public static void dispatchDebug(CommandSender sender, Plugin plugin, DebugSettings settings) {
        var config = EldoUtilities.getConfiguration();
        if (!config.getBoolean(CONSENT, false)) {
            sender.sendMessage(MINI_MESSAGE.deserialize(CONSENT_TEXT,
                    Placeholder.unparsed("host", settings.getHost()),
                    Placeholder.unparsed("plugin", plugin.getName())));
            config.set(CONSENT, true);
            try {
                EldoUtilities.saveConfiguration();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not save the debug consent.", e);
            }
            return;
        }

        sender.sendMessage(Component.text("Collecting and uploading debug data...", NamedTextColor.GRAY));
        var snapshot = DebugReport.collect(plugin);
        CompletableFuture.supplyAsync(() -> GSON.toJson(DebugReport.create(snapshot, settings)))
                .thenCompose(json -> DebugUpload.send(settings.getHost(), json))
                .whenComplete((result, error) -> {
                    if (error != null) {
                        plugin.getLogger().log(Level.WARNING, "Could not create a debug report.", error);
                        sender.sendMessage(Component.text("Could not create the debug report. See the console.", NamedTextColor.RED));
                        return;
                    }
                    sender.sendMessage(message(result, settings));
                });
    }

    static Component message(Result result, DebugSettings settings) {
        if (result instanceof Uploaded uploaded) {
            return MINI_MESSAGE.deserialize("""
                            <green>Debug report uploaded.</green>
                            <gray>Read:</gray> <read>
                            <gray>Delete:</gray> <delete>
                            <gray>Give the read link to whoever helps you. The report is deleted on <white><expires></white>.</gray>""",
                    Placeholder.component("read", link(uploaded.readUrl(), NamedTextColor.AQUA, "Open the report")),
                    Placeholder.component("delete", link(uploaded.deleteUrl(), NamedTextColor.RED, "Delete the report")),
                    Placeholder.unparsed("expires", DATE.format(uploaded.expires())));
        }
        var failed = (Failed) result;
        TagResolver details = TagResolver.resolver(
                Placeholder.unparsed("message", failed.message()),
                Placeholder.unparsed("host", settings.getHost()),
                Placeholder.unparsed("seconds", String.valueOf(failed.retryAfter().orElse(10))));
        return MINI_MESSAGE.deserialize(switch (failed.reason()) {
            case RATE_LIMITED -> "<red>A report was just uploaded from this server. Try again in <seconds> seconds.</red>";
            case TOO_LARGE -> "<red>The report is too large: <message></red>";
            case REFUSED -> "<red>The report was refused: <message></red>";
            case UNREACHABLE -> "<red>Could not upload to <host> (<message>). Try again later.</red>";
        }, details);
    }

    private static Component link(String url, NamedTextColor color, String hover) {
        return Component.text(url, color)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(Component.text(hover, NamedTextColor.GRAY)));
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
        meta.add(new EntryData("User Data", UserData.get(plugin).asString()));
        for (var provider : providers(plugin)) {
            meta.addAll(Arrays.asList(provider.getDebugInformations()));
        }
        return meta.toArray(new EntryData[0]);
    }

    /**
     * Runs the health checks of every {@link DebugDataProvider} the plugin reaches.
     *
     * @param plugin plugin to check
     * @return the outcomes, in the order the providers were found
     */
    public static @NotNull List<HealthCheck> getHealthChecks(Plugin plugin) {
        List<HealthCheck> checks = new ArrayList<>();
        for (var provider : providers(plugin)) {
            checks.addAll(provider.getHealthChecks());
        }
        return checks;
    }

    /**
     * The plugin, when it is a {@link DebugDataProvider}, and every provider it leads to, each once.
     */
    private static List<DebugDataProvider> providers(Plugin plugin) {
        if (!(plugin instanceof DebugDataProvider root)) return List.of();
        List<DebugDataProvider> found = new ArrayList<>();
        Set<DebugDataProvider> debugged = new HashSet<>();
        Queue<DebugDataProvider> providers = new ArrayDeque<>();
        providers.add(root);
        debugged.add(root);
        while (!providers.isEmpty()) {
            var provider = providers.poll();
            found.add(provider);
            for (var nextProvider : provider.getDebugProviders()) {
                if (!debugged.add(nextProvider)) {
                    plugin.getLogger()
                            .warning("Loop in debug data detected. Instance of class "
                                     + nextProvider.getClass().getSimpleName()
                                     + " returns a reference to already debugged instance of "
                                     + provider.getClass().getSimpleName());
                    continue;
                }
                providers.add(nextProvider);
            }
        }
        return found;
    }
}
