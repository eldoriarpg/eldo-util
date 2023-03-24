/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import java.time.Duration;

public class Durations {
    public static String simpleDurationFormat(Duration duration) {
        if (duration.toDays() > 0) {
            if (duration.toDays() == 1) {
                return "1 day";
            }
            return "%d days".formatted(duration.toDays());
        }

        if (duration.toHours() > 0) {
            if (duration.toHours() == 1) {
                return "1 hour";
            }
            return "%d hours".formatted(duration.toHours());
        }

        return "%d minutes".formatted(duration.toMinutes());
    }
}
