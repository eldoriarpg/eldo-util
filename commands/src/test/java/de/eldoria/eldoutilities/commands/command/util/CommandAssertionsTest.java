/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.commands.command.util;

import de.eldoria.eldoutilities.commands.command.CommandMeta;
import de.eldoria.eldoutilities.commands.exceptions.CommandException;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CommandAssertionsTest {
    private final Player player = mock(Player.class);
    private final ConsoleCommandSender console = mock(ConsoleCommandSender.class);

    @Test
    void allowedSenderAcceptsServerImplementations() {
        var meta = CommandMeta.builder("test").allowPlayer().build();

        assertDoesNotThrow(() -> CommandAssertions.allowedSender(meta, player));
        assertThrows(CommandException.class, () -> CommandAssertions.allowedSender(meta, console));
    }

    @Test
    void senderAcceptsServerImplementations() {
        var meta = CommandMeta.builder("test").allowConsole().build();

        assertDoesNotThrow(() -> CommandAssertions.sender(console, meta));
        assertThrows(CommandException.class, () -> CommandAssertions.sender(player, meta));
    }

    @Test
    void allowedSenderAcceptsEveryoneWithoutRestriction() {
        var meta = CommandMeta.builder("test").build();

        assertDoesNotThrow(() -> CommandAssertions.allowedSender(meta, console));
    }
}
