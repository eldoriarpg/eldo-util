/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A line, area or bar chart of one or more series. The values of a series are placed by their position; a
 * {@code null} value leaves a gap.
 */
public final class SeriesChart extends AxisChart<SeriesChart> {
    private final Map<String, List<Number>> series = new LinkedHashMap<>();

    private SeriesChart(String name, String type) {
        super(name, type);
    }

    /**
     * A chart with one line per series.
     *
     * @param name name shown above the chart
     * @return new chart
     */
    public static SeriesChart line(String name) {
        return new SeriesChart(name, "line");
    }

    /**
     * A chart with one filled line per series.
     *
     * @param name name shown above the chart
     * @return new chart
     */
    public static SeriesChart area(String name) {
        return new SeriesChart(name, "area");
    }

    /**
     * A chart with the bars of the series side by side.
     *
     * @param name name shown above the chart
     * @return new chart
     */
    public static SeriesChart bar(String name) {
        return new SeriesChart(name, "bar");
    }

    /**
     * A chart with the bars of the series stacked on each other.
     *
     * @param name name shown above the chart
     * @return new chart
     */
    public static SeriesChart stackedBar(String name) {
        return new SeriesChart(name, "stacked-bar");
    }

    /**
     * Adds a series. Adding a series with a name again replaces it.
     *
     * @param name   series name, shown in the legend when there are several
     * @param values values in order; null leaves a gap
     * @return this chart
     */
    public SeriesChart series(String name, Collection<? extends Number> values) {
        series.put(name, new ArrayList<>(values));
        return this;
    }

    /**
     * Names the positions on the x axis, replacing their numbers.
     *
     * @param labels one label per position
     * @return this chart
     */
    public SeriesChart xLabels(List<String> labels) {
        x.put("labels", List.copyOf(labels));
        return this;
    }

    @Override
    protected Object data() {
        return series;
    }
}
