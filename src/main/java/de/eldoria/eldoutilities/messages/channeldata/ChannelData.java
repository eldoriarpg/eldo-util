/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.messages.channeldata;

import de.eldoria.eldoutilities.localization.ILocalizer;
import de.eldoria.eldoutilities.localization.Replacement;
import de.eldoria.eldoutilities.messages.MessageChannel;
import de.eldoria.eldoutilities.messages.MessageType;

public interface ChannelData {
    /**
     * Localize any externa component beside the message itself.
     *
     * @param localizer    localizer instance
     * @param replacements replacements to apply
     */
    default void localized(ILocalizer localizer, Replacement... replacements) {
    }

    /**
     * Format the text to suit the channel requirements
     *
     * @param type    type of message
     * @param channel channel
     * @param prefix  prefix
     */
    default void formatText(MessageType type, MessageChannel<? extends ChannelData> channel, String prefix) {
    }
}
