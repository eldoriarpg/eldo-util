/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.updater;

/**
 * What an admin decided when the updater offered automatic updates in chat. A plugin stores it in its config through
 * {@link UpdateDataBuilder#autoUpdateHook(java.util.function.Consumer)}.
 */
public enum AutoUpdateDecision {
    /**
     * Automatic updates were switched on. The plugin should enable auto update in its config.
     */
    ENABLED,
    /**
     * The admin does not want to be asked again. The plugin should pass {@code false} to
     * {@link UpdateDataBuilder#askForAutoUpdate(boolean)} from now on.
     */
    NEVER_ASK
}
