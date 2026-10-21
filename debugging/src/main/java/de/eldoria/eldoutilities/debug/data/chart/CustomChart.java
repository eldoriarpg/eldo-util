/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data.chart;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A chart of any type with data of any shape. Created by {@link ChartData#of(String, String, Object)}.
 */
public final class CustomChart extends ChartData<CustomChart> {
    private final Object data;
    private final Map<String, Object> hints = new LinkedHashMap<>();

    CustomChart(String name, String type, Object data) {
        super(name, type);
        this.data = data;
    }

    /**
     * Adds a hint for the viewer, e.g. {@code x} with a map holding {@code label}.
     *
     * @param key   hint name
     * @param value hint value; maps, lists, numbers, strings, booleans and null
     * @return this chart
     */
    public CustomChart hint(String key, Object value) {
        hints.put(key, value);
        return this;
    }

    @Override
    protected Object data() {
        return data;
    }

    @Override
    protected void hints(Map<String, Object> chart) {
        hints.forEach(chart::putIfAbsent);
    }
}
