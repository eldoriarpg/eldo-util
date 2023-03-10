/*
 *     SPDX-License-Identifier: AGPL-3.0-only
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
	void nonNull() {
	}

	@Test
	void testNonNull() {
	}

	@Test
	void testNonNull1() {
	}

	@Test
	void testNonNull2() {
	}

	@Test
	void nonNullOrElse() {
		PlayerInteractEvent event = mock(PlayerInteractEvent.class);
		when(event.getItem()).thenReturn(new ItemStack(Material.AIR));
		Assertions.assertTrue(ObjUtil.nonNullOrElse(event.getItem(), itemStack -> itemStack.getType() == Material.AIR, false));

		event = mock(PlayerInteractEvent.class);
		when(event.getItem()).thenReturn(null);
		Assertions.assertFalse(ObjUtil.nonNullOrElse(event.getItem(), itemStack -> itemStack.getType() == Material.AIR, false));
	}
}