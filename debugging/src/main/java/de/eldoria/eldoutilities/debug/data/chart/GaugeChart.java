/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data.chart;

import java.util.LinkedHashMap;

/**
 * One value shown on a scale, e.g. how much of a limit is used.
 */
public final class GaugeChart extends ChartData<GaugeChart> {
    private final Number value;
    private Number min = 0;
    private Number max = 100;

    private GaugeChart(String name, Number value) {
        super(name, "gauge");
        this.value = value;
    }

    /**
     * A gauge on a scale from 0 to 100.
     *
     * @param name  name shown above the chart
     * @param value current value
     * @return new chart
     */
    public static GaugeChart gauge(String name, Number value) {
        return new GaugeChart(name, value);
    }

    /**
     * Sets the scale.
     *
     * @param min start of the scale
     * @param max end of the scale
     * @return this chart
     */
    public GaugeChart range(Number min, Number max) {
        this.min = min;
        this.max = max;
        return this;
    }

    @Override
    protected Object data() {
        var reading = new LinkedHashMap<String, Object>();
        reading.put("value", value);
        reading.put("min", min);
        reading.put("max", max);
        return reading;
    }
}
