/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages;

public interface MessageType {
    /**
     * Default implementation for a normal message type
     */
    MessageType NORMAL = () -> "§2";
    /**
     * Default implementation for a error message type.
     */
    MessageType ERROR = () -> "§c";

    /**
     * Default implementation for a message without default color.
     */
    MessageType BLANK = () -> "";

    /**
     * Get the default color of the channel.
     * <p>
     * The color code should use the § notation.
     *
     * @return color code in § notation.
     */
    String getDefaultColor();

    /**
     * Tranform a message to force the correct color code where needed.
     * <p>
     * By default the §r mark will be replaced by "§r{@link #getDefaultColor()}".
     * <p>
     * The default color code will also be appended at the begin of the message
     *
     * @param message message to send
     * @return message with forced color codes.
     */
    default String forceColor(String message) {
        return "§r" + getDefaultColor() + message.replaceAll("§r", "§r" + getDefaultColor());
    }
}
