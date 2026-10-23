/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

import de.eldoria.eldoutilities.messages.MessageSender;
import de.eldoria.eldoutilities.updater.notifier.DownloadedNotifier;
import de.eldoria.eldoutilities.updater.notifier.UpdateNotifier;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * How the updater reacts to new settings and to being stopped.
 */
class UpdaterTest {
    private Plugin plugin;
    private PluginManager pluginManager;
    private final Queue<Runnable> serverTasks = new ConcurrentLinkedQueue<>();
    private MockedStatic<HandlerList> handlerLists;
    private TestUpdater updater;
    private final List<AutoUpdateDecision> decisions = new ArrayList<>();
    private MessageSender messageSender;

    @BeforeEach
    void setUp() {
        plugin = mock(Plugin.class);
        var server = mock(Server.class);
        var scheduler = mock(GlobalRegionScheduler.class);
        var description = mock(PluginDescriptionFile.class);
        pluginManager = mock(PluginManager.class);
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getName()).thenReturn("TestPlugin");
        when(plugin.getLogger()).thenReturn(Logger.getLogger("UpdaterTest"));
        when(plugin.getServer()).thenReturn(server);
        when(plugin.getDescription()).thenReturn(description);
        when(description.getVersion()).thenReturn("1.0.0");
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getGlobalRegionScheduler()).thenReturn(scheduler);
        when(server.getCommandMap()).thenReturn(mock(CommandMap.class));
        doAnswer(invocation -> serverTasks.add(invocation.getArgument(1)))
                .when(scheduler)
                .execute(any(Plugin.class), any(Runnable.class));
        handlerLists = mockStatic(HandlerList.class);
        messageSender = mock(MessageSender.class);
        when(messageSender.translatePlain(any(String.class), any(TagResolver[].class)))
                .thenReturn("Update available");
    }

    @AfterEach
    void tearDown() {
        updater.stop();
        handlerLists.close();
    }

    @Test
    void switchingNotificationsOffRemovesTheNotice() {
        updater = new TestUpdater(data(true, false));
        updater.performCheck(true);
        runServerTasks();
        var notice = registeredListener(1);

        updater.reconfigure(data(false, false)).join();
        runServerTasks();

        handlerLists.verify(() -> HandlerList.unregisterAll(notice));
        verify(pluginManager, times(1)).registerEvents(any(Listener.class), eq(plugin));
    }

    @Test
    void switchingAutoUpdateOnInstallsTheFoundUpdate() {
        updater = new TestUpdater(data(true, false));
        updater.performCheck(true);
        runServerTasks();
        assertInstanceOf(UpdateNotifier.class, registeredListener(1));
        assertEquals(0, updater.installs);

        updater.reconfigure(data(true, true)).join();
        runServerTasks();

        assertEquals(1, updater.installs);
        assertInstanceOf(DownloadedNotifier.class, registeredListener(2));
    }

    @Test
    void aDownloadedUpdateIsNotDownloadedAgain() {
        updater = new TestUpdater(data(true, true));
        updater.performCheck(true);
        runServerTasks();

        updater.reconfigure(data(true, true)).join();
        runServerTasks();

        assertEquals(1, updater.installs);
    }

    @Test
    void aStoppedUpdaterDoesNothing() {
        updater = new TestUpdater(data(true, false));
        updater.stop();

        updater.reconfigure(data(true, true)).join();
        runServerTasks();

        assertEquals(0, updater.installs);
        assertFalse(updater.download().join());
    }

    @Test
    void withoutAHookNothingIsOffered() {
        updater = new TestUpdater(data(true, false));
        updater.performCheck(true);

        assertFalse(updater.offersAutoUpdate());
    }

    @Test
    void withoutAskingNothingIsOffered() {
        updater = new TestUpdater(offeringData(false));
        updater.performCheck(true);

        assertFalse(updater.offersAutoUpdate());
    }

    @Test
    void enablingAutoUpdateInstallsTheUpdateAndReportsIt() {
        updater = new TestUpdater(offeringData(true));
        updater.performCheck(true);
        runServerTasks();
        assertTrue(updater.offersAutoUpdate());

        assertTrue(updater.enableAutoUpdate().join());
        runServerTasks();

        assertEquals(List.of(AutoUpdateDecision.ENABLED), decisions);
        assertEquals(1, updater.installs);
        assertTrue(updater.isAutoUpdate());
        assertFalse(updater.offersAutoUpdate());
        assertInstanceOf(DownloadedNotifier.class, registeredListener(2));
    }

    @Test
    void decliningReportsItAndStopsAsking() {
        updater = new TestUpdater(offeringData(true));
        updater.performCheck(true);

        updater.declineAutoUpdate().join();
        updater.declineAutoUpdate().join();
        runServerTasks();

        assertEquals(List.of(AutoUpdateDecision.NEVER_ASK), decisions);
        assertFalse(updater.offersAutoUpdate());
        assertEquals(0, updater.installs);
    }

    @Test
    void reconfiguringFollowsThePluginsConfigAgain() {
        updater = new TestUpdater(offeringData(true));
        updater.performCheck(true);
        updater.enableAutoUpdate().join();

        updater.reconfigure(offeringData(true)).join();
        runServerTasks();

        assertFalse(updater.isAutoUpdate());
        assertTrue(updater.offersAutoUpdate());
    }

    @Test
    void aManualDownloadAsksAboutAutoUpdates() {
        updater = new TestUpdater(offeringData(true));
        updater.performCheck(true);
        var admin = mock(CommandSender.class);

        new UpdateCommand(plugin, updater).download(admin).join();
        runServerTasks();

        var messages = ArgumentCaptor.forClass(String.class);
        verify(messageSender, times(3)).sendMessage(eq(admin), messages.capture(), any(TagResolver.class));
        assertTrue(messages.getValue().contains("/testplugin:eldo-update enable-auto"));
        assertTrue(messages.getValue().contains("/testplugin:eldo-update never"));
    }

    @Test
    void withoutAHookAManualDownloadDoesNotAsk() {
        updater = new TestUpdater(new TestData(plugin, true, false, messageSender));
        updater.performCheck(true);
        var admin = mock(CommandSender.class);

        new UpdateCommand(plugin, updater).download(admin).join();
        runServerTasks();

        verify(messageSender, times(2)).sendMessage(eq(admin), any(String.class), any(TagResolver.class));
    }

    @Test
    void aMajorUpdateIsNotInstalledAutomatically() {
        updater = new TestUpdater(new TestData(plugin, true, true, messageSender), true);
        updater.performCheck(true);

        assertEquals(0, updater.installs);
    }

    @Test
    void downloadingAMajorUpdateAsksToConfirm() {
        updater = new TestUpdater(new TestData(plugin, true, false, messageSender), true);
        updater.performCheck(true);
        var admin = mock(CommandSender.class);
        when(admin.isOp()).thenReturn(true);
        var command = new UpdateCommand(plugin, updater);

        command.execute(admin, "eldo-update", new String[] {"download"});
        runServerTasks();

        var messages = ArgumentCaptor.forClass(String.class);
        verify(messageSender).sendMessage(eq(admin), messages.capture(), any(TagResolver.class));
        assertTrue(messages.getValue().contains("/testplugin:eldo-update download confirm"));
        assertEquals(0, updater.installs);

        command.execute(admin, "eldo-update", new String[] {"download", "confirm"});
        updater.download().join();
        runServerTasks();

        assertEquals(1, updater.installs);
    }

    private void runServerTasks() {
        Runnable task;
        while ((task = serverTasks.poll()) != null) task.run();
    }

    private Listener registeredListener(int registrations) {
        var captor = ArgumentCaptor.forClass(Listener.class);
        verify(pluginManager, times(registrations)).registerEvents(captor.capture(), eq(plugin));
        return captor.getValue();
    }

    private TestData data(boolean notifyUpdate, boolean autoUpdate) {
        return new TestData(plugin, notifyUpdate, autoUpdate);
    }

    private TestData offeringData(boolean askForAutoUpdate) {
        var data = new TestData(plugin, true, false, messageSender);
        data.autoUpdateOptIn(askForAutoUpdate, decisions::add);
        return data;
    }

    private record TestResponse(boolean isOutdated, String latestVersion) implements UpdateResponse {}

    private static final class TestData extends UpdateData<TestResponse> {
        private TestData(Plugin plugin, boolean notifyUpdate, boolean autoUpdate) {
            this(plugin, notifyUpdate, autoUpdate, null);
        }

        private TestData(Plugin plugin, boolean notifyUpdate, boolean autoUpdate, MessageSender messageSender) {
            super(
                    plugin,
                    "test.notify",
                    notifyUpdate,
                    autoUpdate,
                    "https://example.com",
                    "Update to {new_version}",
                    messageSender);
        }
    }

    private static final class TestUpdater extends Updater<TestResponse, TestData> {
        private final boolean major;
        private int installs;

        private TestUpdater(TestData data) {
            this(data, false);
        }

        private TestUpdater(TestData data, boolean major) {
            super(data);
            this.major = major;
        }

        @Override
        public boolean isMajorUpdate() {
            return major;
        }

        @Override
        protected Optional<TestResponse> checkUpdate(TestData data) {
            return Optional.of(new TestResponse(true, "2.0.0"));
        }

        @Override
        protected boolean update() {
            installs++;
            return true;
        }

        @Override
        protected boolean offersDownload() {
            return true;
        }
    }
}
