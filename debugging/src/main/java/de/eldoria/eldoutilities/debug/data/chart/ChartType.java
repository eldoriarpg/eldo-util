/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data.chart;

/**
 * The chart types the report viewer draws. A report holding another type has that chart left out.
 */
public enum ChartType {
    LINE("line"),
    AREA("area"),
    BAR("bar"),
    STACKED_BAR("stacked-bar"),
    SCATTER("scatter"),
    PIE("pie"),
    DONUT("donut"),
    HEATMAP("heatmap"),
    GAUGE("gauge");

    private final String wireName;

    ChartType(String wireName) {
        this.wireName = wireName;
    }

    /**
     * The name the report sends for this type.
     *
     * @return the name, e.g. {@code stacked-bar}
     */
    public String wireName() {
        return wireName;
    }
}
