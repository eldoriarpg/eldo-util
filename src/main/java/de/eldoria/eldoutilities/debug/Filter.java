/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import java.util.regex.Pattern;

public class Filter {
    private final Pattern pattern;
    private final String replacement;

    public Filter(Pattern pattern, String replacement) {
        this.pattern = pattern;
        this.replacement = replacement;
    }

    public String apply(String text) {
        return pattern.matcher(text).replaceAll(replacement);
    }
}
