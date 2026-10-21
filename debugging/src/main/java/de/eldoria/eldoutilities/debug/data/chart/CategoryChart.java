/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data.chart;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A bar, pie or donut chart with one named value per category.
 */
public final class CategoryChart extends ChartData<CategoryChart> {
    private final Map<String, Number> values = new LinkedHashMap<>();

    private CategoryChart(String name, ChartType type) {
        super(name, type);
    }

    /**
     * A chart with one bar per category.
     *
     * @param name name shown above the chart
     * @return new chart
     */
    public static CategoryChart bar(String name) {
        return new CategoryChart(name, ChartType.BAR);
    }

    /**
     * A chart with one slice per category.
     *
     * @param name name shown above the chart
     * @return new chart
     */
    public static CategoryChart pie(String name) {
        return new CategoryChart(name, ChartType.PIE);
    }

    /**
     * A ring with one segment per category.
     *
     * @param name name shown above the chart
     * @return new chart
     */
    public static CategoryChart donut(String name) {
        return new CategoryChart(name, ChartType.DONUT);
    }

    /**
     * Sets the value of a category. Setting a category again replaces its value.
     *
     * @param category category name
     * @param value    its value
     * @return this chart
     */
    public CategoryChart value(String category, Number value) {
        values.put(category, value);
        return this;
    }

    @Override
    protected Object data() {
        return values;
    }
}
