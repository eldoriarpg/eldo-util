/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data.chart;

import de.eldoria.eldoutilities.debug.DebugSettings;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A chart in a debug report.
 * <p>
 * Each chart type has its own class with the options that apply to it: {@link SeriesChart}, {@link ScatterChart},
 * {@link CategoryChart}, {@link HeatmapChart} and {@link GaugeChart}. {@link #of(String, ChartType, Object)} sends data
 * of any shape for a {@link ChartType}, for charts the classes do not cover; the report viewer shows the data as sent
 * when it cannot draw it.
 *
 * @param <T> the chart class, returned by the fluent setters
 */
public abstract class ChartData<T extends ChartData<T>> {
    private final String name;
    private final ChartType type;
    private String description = "";
    @Nullable
    private String unit;

    protected ChartData(String name, ChartType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * A chart with data of any shape, sent as it is.
     *
     * @param name name shown above the chart
     * @param type chart type
     * @param data maps, lists, numbers, strings, booleans and null
     * @return new chart
     */
    public static CustomChart of(String name, ChartType type, Object data) {
        return new CustomChart(name, type, data);
    }

    /**
     * Sets a text shown with the chart.
     *
     * @param description text explaining the chart
     * @return this chart
     */
    public T description(String description) {
        this.description = description;
        return self();
    }

    /**
     * Sets the unit shown with values.
     *
     * @param unit unit, e.g. {@code ms}
     * @return this chart
     */
    public T unit(String unit) {
        this.unit = unit;
        return self();
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    /**
     * The chart as the report sends it: type, data and the hints that are set.
     *
     * @param settings settings whose filter is applied to every text in the chart
     * @return map of the chart
     */
    public Map<String, Object> toChart(DebugSettings settings) {
        var chart = new LinkedHashMap<String, Object>();
        chart.put("type", type.wireName());
        chart.put("data", data());
        if (unit != null) chart.put("unit", unit);
        hints(chart);
        @SuppressWarnings("unchecked")
        var filtered = (Map<String, Object>) filtered(chart, settings);
        return filtered;
    }

    /**
     * The data of the chart in the shape the viewer reads for its type.
     *
     * @return data
     */
    protected abstract Object data();

    /**
     * Adds the hints of the chart type, e.g. axis labels.
     *
     * @param chart chart map to add to
     */
    protected void hints(Map<String, Object> chart) {
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    private static Object filtered(Object value, DebugSettings settings) {
        if (value instanceof String text) return settings.applyFilter(text);
        if (value instanceof Map<?, ?> map) {
            var copy = new LinkedHashMap<String, Object>();
            map.forEach((key, entry) -> copy.put(settings.applyFilter(String.valueOf(key)), filtered(entry, settings)));
            return copy;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(entry -> filtered(entry, settings)).toList();
        }
        if (value instanceof Object[] array) {
            return filtered(Arrays.asList(array), settings);
        }
        return value;
    }
}
