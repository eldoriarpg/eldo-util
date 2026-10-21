/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data;

import de.eldoria.eldoutilities.debug.DebugSettings;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A chart in a debug report.
 * <p>
 * The data is loosely typed: it is sent as it is and the report viewer reads it according to the type. The viewer
 * draws {@code line}, {@code area}, {@code bar}, {@code stacked-bar}, {@code scatter}, {@code pie}, {@code donut},
 * {@code heatmap} and {@code gauge}, and shows the data as sent for any other type or for data it cannot read.
 * <p>
 * Shapes the viewer reads:
 * <ul>
 *     <li>a list of numbers, or a map of series name to a list of numbers, for line, area, bar and scatter charts</li>
 *     <li>a list of {@code [x, y]} pairs, or a map of series name to such lists</li>
 *     <li>a map of name to number, or a list of {@code {name, value}} maps, for bar, pie and donut charts</li>
 *     <li>a list of number lists (rows of a matrix), or a list of {@code {x, y, value}} maps, for heatmaps</li>
 *     <li>a number, a {@code {value, min, max}} map, or a map of name to number, for gauges</li>
 * </ul>
 * Strings holding numbers are read as numbers and {@code null} leaves a gap.
 */
public final class ChartData {
    private final String name;
    private final String type;
    private final Object data;
    private final List<Map<String, Object>> marks = new ArrayList<>();
    private final Map<String, Object> x = new LinkedHashMap<>();
    private final Map<String, Object> y = new LinkedHashMap<>();
    @Nullable
    private String unit;
    private String description = "";

    private ChartData(String name, String type, Object data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }

    /**
     * A chart of any type with data of any shape.
     *
     * @param name name shown above the chart
     * @param type chart type, e.g. {@code line}
     * @param data data as the viewer reads it for the type; maps, lists, numbers, strings, booleans and null
     * @return new chart
     */
    public static ChartData of(String name, String type, Object data) {
        return new ChartData(name, type, data);
    }

    /**
     * A line chart with one line per series. Values are placed by their position.
     *
     * @param name   name shown above the chart
     * @param series series name to its values
     * @return new chart
     */
    public static ChartData line(String name, Map<String, ? extends Collection<? extends Number>> series) {
        return of(name, "line", series);
    }

    /**
     * An area chart with one filled line per series. Values are placed by their position.
     *
     * @param name   name shown above the chart
     * @param series series name to its values
     * @return new chart
     */
    public static ChartData area(String name, Map<String, ? extends Collection<? extends Number>> series) {
        return of(name, "area", series);
    }

    /**
     * A bar chart with one bar per category.
     *
     * @param name       name shown above the chart
     * @param categories category name to its value
     * @return new chart
     */
    public static ChartData bar(String name, Map<String, ? extends Number> categories) {
        return of(name, "bar", categories);
    }

    /**
     * A pie chart with one slice per category.
     *
     * @param name       name shown above the chart
     * @param categories category name to its value
     * @return new chart
     */
    public static ChartData pie(String name, Map<String, ? extends Number> categories) {
        return of(name, "pie", categories);
    }

    /**
     * A heatmap. Each inner list is a row; the position in it is the column.
     *
     * @param name name shown above the chart
     * @param rows rows of values
     * @return new chart
     */
    public static ChartData heatmap(String name, List<? extends List<? extends Number>> rows) {
        return of(name, "heatmap", rows);
    }

    /**
     * A gauge showing one value on a scale.
     *
     * @param name  name shown above the chart
     * @param value current value
     * @param min   start of the scale
     * @param max   end of the scale
     * @return new chart
     */
    public static ChartData gauge(String name, Number value, Number min, Number max) {
        var reading = new LinkedHashMap<String, Object>();
        reading.put("value", value);
        reading.put("min", min);
        reading.put("max", max);
        return of(name, "gauge", reading);
    }

    /**
     * Sets the unit shown with values.
     *
     * @param unit unit, e.g. {@code ms}
     * @return this chart
     */
    public ChartData unit(String unit) {
        this.unit = unit;
        return this;
    }

    /**
     * Sets the label of the x axis.
     *
     * @param label axis label
     * @return this chart
     */
    public ChartData xLabel(String label) {
        x.put("label", label);
        return this;
    }

    /**
     * Names the positions on the x axis, replacing their numbers.
     *
     * @param labels one label per position
     * @return this chart
     */
    public ChartData xLabels(List<String> labels) {
        x.put("labels", List.copyOf(labels));
        return this;
    }

    /**
     * Sets the label of the y axis.
     *
     * @param label axis label
     * @return this chart
     */
    public ChartData yLabel(String label) {
        y.put("label", label);
        return this;
    }

    /**
     * Names the positions on the y axis of a heatmap, replacing their numbers.
     *
     * @param labels one label per row
     * @return this chart
     */
    public ChartData yLabels(List<String> labels) {
        y.put("labels", List.copyOf(labels));
        return this;
    }

    /**
     * Fixes the range of the y axis.
     *
     * @param min lowest value shown
     * @param max highest value shown
     * @return this chart
     */
    public ChartData yRange(Number min, Number max) {
        y.put("min", min);
        y.put("max", max);
        return this;
    }

    /**
     * Draws a horizontal reference line, e.g. a limit.
     *
     * @param value y value of the line
     * @param label label of the line
     * @return this chart
     */
    public ChartData mark(Number value, String label) {
        var mark = new LinkedHashMap<String, Object>();
        mark.put("y", value);
        mark.put("label", label);
        marks.add(mark);
        return this;
    }

    /**
     * Sets a text shown with the chart.
     *
     * @param description text explaining the chart
     * @return this chart
     */
    public ChartData description(String description) {
        this.description = description;
        return this;
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
        chart.put("type", type);
        chart.put("data", filtered(data, settings));
        if (unit != null) chart.put("unit", settings.applyFilter(unit));
        if (!x.isEmpty()) chart.put("x", filtered(x, settings));
        if (!y.isEmpty()) chart.put("y", filtered(y, settings));
        if (!marks.isEmpty()) chart.put("marks", filtered(marks, settings));
        return chart;
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
