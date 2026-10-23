/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObjUtilTest {

    @Test
    void nonNull() {}

    @Test
    void testNonNull() {}

    @Test
    void testNonNull1() {}

    @Test
    void testNonNull2() {}

    @Test
    void nonNullOrElse() {
        PlayerInteractEvent event = mock(PlayerInteractEvent.class);
        ItemStack air = mock(ItemStack.class);
        when(air.getType()).thenReturn(Material.AIR);
        when(event.getItem()).thenReturn(air);
        Assertions.assertTrue(
                ObjUtil.nonNullOrElse(event.getItem(), itemStack -> itemStack.getType() == Material.AIR, false));

        event = mock(PlayerInteractEvent.class);
        when(event.getItem()).thenReturn(null);
        Assertions.assertFalse(
                ObjUtil.nonNullOrElse(event.getItem(), itemStack -> itemStack.getType() == Material.AIR, false));
    }
}
