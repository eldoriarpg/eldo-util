/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data.chart;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A chart of points, each placed by its own x and y value, in one or more series.
 */
public final class ScatterChart extends AxisChart<ScatterChart> {
    private final Map<String, List<List<Number>>> points = new LinkedHashMap<>();

    private ScatterChart(String name) {
        super(name, ChartType.SCATTER);
    }

    /**
     * A new scatter chart.
     *
     * @param name name shown above the chart
     * @return new chart
     */
    public static ScatterChart scatter(String name) {
        return new ScatterChart(name);
    }

    /**
     * Adds a point to a series.
     *
     * @param series series name, shown in the legend when there are several
     * @param x      x value
     * @param y      y value
     * @return this chart
     */
    public ScatterChart point(String series, Number x, Number y) {
        points.computeIfAbsent(series, key -> new ArrayList<>()).add(List.of(x, y));
        return this;
    }

    @Override
    protected Object data() {
        return points;
    }
}
