/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.inventory;

import de.eldoria.eldoutilities.builder.ItemStackBuilder;
import de.eldoria.eldoutilities.utils.DataContainerUtil;
import de.eldoria.eldoutilities.utils.EMath;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Consumer;

/**
 * Class which holds some simple consumers which can be used in a {@link ActionItem}.
 *
 * @since 1.2.3
 */
public final class ActionConsumer {
    private ActionConsumer() {
    }

    /**
     * Get a consumer which allows to raise and lower a value between a range.
     *
     * @param key key of value
     * @param min min value. inclusive.
     * @param max max value. inclusive.
     * @return consumer with range
     */
    public static Consumer<InventoryClickEvent> getIntRange(NamespacedKey key, int min, int max) {
        return clickEvent -> {
            var amount = 0;
            switch (clickEvent.getClick()) {
                case LEFT:
                    amount = 1;
                    break;
                case SHIFT_LEFT:
                    amount = 10;
                    break;
                case RIGHT:
                    amount = -1;
                    break;
                case SHIFT_RIGHT:
                    amount = -10;
                    break;
                case WINDOW_BORDER_LEFT:
                case WINDOW_BORDER_RIGHT:
                case MIDDLE:
                case NUMBER_KEY:
                case DOUBLE_CLICK:
                case DROP:
                case CONTROL_DROP:
                case CREATIVE:
                case SWAP_OFFHAND:
                case UNKNOWN:
                    return;
            }

            var finalAmount = amount;
            int curr = DataContainerUtil.compute(clickEvent.getCurrentItem(), key, PersistentDataType.INTEGER,
                    integer -> EMath.clamp(min, max, integer + finalAmount));
            ItemStackBuilder.of(clickEvent.getCurrentItem(), false).withLore(String.valueOf(curr));
        };
    }

    /**
     * Get a consumer which allows to raise and lower a value between a range.
     *
     * @param key key of value
     * @param min min value. inclusive.
     * @param max max value. inclusive.
     * @return consumer with range
     */
    public static Consumer<InventoryClickEvent> getDoubleRange(NamespacedKey key, double min, double max) {
        return clickEvent -> {
            double amount = 0;
            switch (clickEvent.getClick()) {
                case LEFT:
                    amount = 0.1;
                    break;
                case SHIFT_LEFT:
                    amount = 1;
                    break;
                case RIGHT:
                    amount = -0.1;
                    break;
                case SHIFT_RIGHT:
                    amount = -1;
                    break;
                case WINDOW_BORDER_LEFT:
                case WINDOW_BORDER_RIGHT:
                case MIDDLE:
                case NUMBER_KEY:
                case DOUBLE_CLICK:
                case DROP:
                case CONTROL_DROP:
                case CREATIVE:
                case SWAP_OFFHAND:
                case UNKNOWN:
                    return;
            }

            var finalAmount = amount;
            double curr = DataContainerUtil.compute(clickEvent.getCurrentItem(), key, PersistentDataType.DOUBLE,
                    integer -> EMath.clamp(min, max, integer + finalAmount));
            ItemStackBuilder.of(clickEvent.getCurrentItem(), false).withLore(String.format("%.2f", curr));
        };
    }

    /**
     * Gets a consumer which allows to toggle a boolean value. Used for {@link PersistentDataType#BYTE} fields.
     *
     * @param key key of value
     * @return consumer for boolean
     */
    public static Consumer<InventoryClickEvent> booleanToggle(NamespacedKey key) {
        return clickEvent -> {
            switch (clickEvent.getClick()) {
                case LEFT:
                case SHIFT_LEFT:
                case RIGHT:
                case SHIFT_RIGHT:
                    break;
                case WINDOW_BORDER_LEFT:
                case WINDOW_BORDER_RIGHT:
                case MIDDLE:
                case NUMBER_KEY:
                case DOUBLE_CLICK:
                case DROP:
                case CONTROL_DROP:
                case CREATIVE:
                case SWAP_OFFHAND:
                case UNKNOWN:
                    return;
            }

            var curr = DataContainerUtil.compute(
                    clickEvent.getCurrentItem(),
                    key,
                    PersistentDataType.BYTE,
                    aByte -> DataContainerUtil.booleanToByte(!DataContainerUtil.byteToBoolean(aByte)));
            var b = DataContainerUtil.byteToBoolean(curr);
            ItemStackBuilder.of(clickEvent.getCurrentItem(), false).withLore(b ? "§2true" : "§cfalse");
        };
    }
}
