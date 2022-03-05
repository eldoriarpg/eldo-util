/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.localization;

import java.util.Map;

/**
 * A dummy localizer which serves as a default localizer.
 * <p>
 * Does return the locale code.
 *
 * @since 1.0.0
 */
public class DummyLocalizer implements ILocalizer {
    @Override
    public void setLocale(String language) {

    }

    @Override
    public String getMessage(String key, Replacement... replacements) {
        return key;
    }

    @Override
    public String[] getIncludedLocales() {
        return new String[0];
    }

    @Override
    public void addLocaleCodes(Map<String, String> runtimeLocaleCodes) {
    }

    @Override
    public String localize(String message, Replacement... replacements) {
        return message;
    }
}
