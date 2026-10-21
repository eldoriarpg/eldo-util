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
 * A chart with an x and a y axis: labels for both, a fixed range for y, and horizontal reference lines.
 *
 * @param <T> the chart class, returned by the fluent setters
 */
public abstract class AxisChart<T extends AxisChart<T>> extends ChartData<T> {
    protected final Map<String, Object> x = new LinkedHashMap<>();
    protected final Map<String, Object> y = new LinkedHashMap<>();
    private final List<Map<String, Object>> marks = new ArrayList<>();

    protected AxisChart(String name, String type) {
        super(name, type);
    }

    /**
     * Sets the label of the x axis.
     *
     * @param label axis label
     * @return this chart
     */
    public T xLabel(String label) {
        x.put("label", label);
        return self();
    }

    /**
     * Sets the label of the y axis.
     *
     * @param label axis label
     * @return this chart
     */
    public T yLabel(String label) {
        y.put("label", label);
        return self();
    }

    /**
     * Fixes the range of the y axis.
     *
     * @param min lowest value shown
     * @param max highest value shown
     * @return this chart
     */
    public T yRange(Number min, Number max) {
        y.put("min", min);
        y.put("max", max);
        return self();
    }

    /**
     * Draws a horizontal reference line, e.g. a limit.
     *
     * @param value y value of the line
     * @param label label of the line
     * @return this chart
     */
    public T mark(Number value, String label) {
        var mark = new LinkedHashMap<String, Object>();
        mark.put("y", value);
        mark.put("label", label);
        marks.add(mark);
        return self();
    }

    @Override
    protected void hints(Map<String, Object> chart) {
        if (!x.isEmpty()) chart.put("x", x);
        if (!y.isEmpty()) chart.put("y", y);
        if (!marks.isEmpty()) chart.put("marks", marks);
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }
}
