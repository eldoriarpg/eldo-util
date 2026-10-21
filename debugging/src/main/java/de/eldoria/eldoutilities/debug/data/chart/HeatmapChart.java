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
 * A grid of values, colored by size. Filled either row by row or cell by cell, not both.
 */
public final class HeatmapChart extends ChartData<HeatmapChart> {
    private final List<List<Number>> rows = new ArrayList<>();
    private final List<Map<String, Object>> cells = new ArrayList<>();
    private final Map<String, Object> x = new LinkedHashMap<>();
    private final Map<String, Object> y = new LinkedHashMap<>();

    private HeatmapChart(String name) {
        super(name, "heatmap");
    }

    /**
     * A new heatmap.
     *
     * @param name name shown above the chart
     * @return new chart
     */
    public static HeatmapChart heatmap(String name) {
        return new HeatmapChart(name);
    }

    /**
     * Adds the next row; the position in it is the column.
     *
     * @param values values of the row; null leaves a cell empty
     * @return this chart
     * @throws IllegalStateException if the heatmap is already filled by cells
     */
    public HeatmapChart row(List<? extends Number> values) {
        if (!cells.isEmpty()) throw new IllegalStateException("This heatmap is filled by cells");
        rows.add(new ArrayList<>(values));
        return this;
    }

    /**
     * Sets one cell, placed by the labels or positions of its column and row.
     *
     * @param x     column label or position
     * @param y     row label or position
     * @param value value of the cell
     * @return this chart
     * @throws IllegalStateException if the heatmap is already filled by rows
     */
    public HeatmapChart cell(Object x, Object y, Number value) {
        if (!rows.isEmpty()) throw new IllegalStateException("This heatmap is filled by rows");
        var cell = new LinkedHashMap<String, Object>();
        cell.put("x", x);
        cell.put("y", y);
        cell.put("value", value);
        cells.add(cell);
        return this;
    }

    /**
     * Names the columns, replacing their positions.
     *
     * @param labels one label per column
     * @return this chart
     */
    public HeatmapChart xLabels(List<String> labels) {
        x.put("labels", List.copyOf(labels));
        return this;
    }

    /**
     * Names the rows, replacing their positions.
     *
     * @param labels one label per row
     * @return this chart
     */
    public HeatmapChart yLabels(List<String> labels) {
        y.put("labels", List.copyOf(labels));
        return this;
    }

    /**
     * Sets the label of the x axis.
     *
     * @param label axis label
     * @return this chart
     */
    public HeatmapChart xLabel(String label) {
        x.put("label", label);
        return this;
    }

    /**
     * Sets the label of the y axis.
     *
     * @param label axis label
     * @return this chart
     */
    public HeatmapChart yLabel(String label) {
        y.put("label", label);
        return this;
    }

    @Override
    protected Object data() {
        return cells.isEmpty() ? rows : cells;
    }

    @Override
    protected void hints(Map<String, Object> chart) {
        if (!x.isEmpty()) chart.put("x", x);
        if (!y.isEmpty()) chart.put("y", y);
    }
}
