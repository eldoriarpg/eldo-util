/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug.data.chart;

import de.eldoria.eldoutilities.debug.DebugSettings;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChartDataTest {
    private static Map<String, Object> sent(ChartData<?> chart) {
        return chart.toChart(DebugSettings.DEFAULT);
    }

    @Test
    void seriesChartsSendNamedSeriesWithTheirHints() {
        var chart = sent(SeriesChart.line("Render time")
                .series("render", Arrays.asList(1.5, null, 3))
                .xLabels(List.of("a", "b", "c"))
                .yLabel("ms")
                .yRange(0, 50)
                .mark(25, "budget")
                .unit("ms"));

        assertEquals("line", chart.get("type"));
        assertEquals(Map.of("render", Arrays.asList(1.5, null, 3)), chart.get("data"));
        assertEquals(Map.of("labels", List.of("a", "b", "c")), chart.get("x"));
        assertEquals(Map.of("label", "ms", "min", 0, "max", 50), chart.get("y"));
        assertEquals(List.of(Map.of("y", 25, "label", "budget")), chart.get("marks"));
        assertEquals("ms", chart.get("unit"));
        assertEquals("stacked-bar", sent(SeriesChart.stackedBar("x")).get("type"));
        assertEquals("area", sent(SeriesChart.area("x")).get("type"));
    }

    @Test
    void scatterChartsSendPointsPerSeries() {
        var chart = sent(
                ScatterChart.scatter("Points").point("a", 1, 2).point("a", 3, 4).point("b", 0, 1));

        assertEquals("scatter", chart.get("type"));
        assertEquals(
                Map.of("a", List.of(List.of(1, 2), List.of(3, 4)), "b", List.of(List.of(0, 1))), chart.get("data"));
        assertFalse(chart.containsKey("marks"));
    }

    @Test
    void categoryChartsSendNameToValue() {
        var chart = sent(CategoryChart.donut("Storage").value("yaml", 12).value("mysql", 3));

        assertEquals("donut", chart.get("type"));
        assertEquals(Map.of("yaml", 12, "mysql", 3), chart.get("data"));
        assertEquals("pie", sent(CategoryChart.pie("x")).get("type"));
        assertEquals("bar", sent(CategoryChart.bar("x")).get("type"));
    }

    @Test
    void heatmapsSendRowsOrCellsButNotBoth() {
        var rows = sent(HeatmapChart.heatmap("Load")
                .row(List.of(1, 2))
                .row(List.of(3, 4))
                .yLabels(List.of("mon", "tue")));
        assertEquals(List.of(List.of(1, 2), List.of(3, 4)), rows.get("data"));
        assertEquals(Map.of("labels", List.of("mon", "tue")), rows.get("y"));

        var cells = sent(HeatmapChart.heatmap("Load").cell("noon", "mon", 5));
        assertEquals(List.of(Map.of("x", "noon", "y", "mon", "value", 5)), cells.get("data"));

        assertThrows(
                IllegalStateException.class,
                () -> HeatmapChart.heatmap("x").row(List.of(1)).cell(0, 0, 1));
        assertThrows(
                IllegalStateException.class,
                () -> HeatmapChart.heatmap("x").cell(0, 0, 1).row(List.of(1)));
    }

    @Test
    void gaugesSendValueAndScale() {
        assertEquals(
                Map.of("value", 42, "min", 0, "max", 100),
                sent(GaugeChart.gauge("Usage", 42)).get("data"));
        assertEquals(
                Map.of("value", 7, "min", 5, "max", 10),
                sent(GaugeChart.gauge("Usage", 7).range(5, 10)).get("data"));
    }

    @Test
    void everyTextPassesTheFilter() {
        var chart = sent(CategoryChart.bar("x").value("password: hunter2", 1).unit("token: abc"));

        assertEquals(Map.of("password: *****", 1), chart.get("data"));
        assertEquals("token: *****", chart.get("unit"));
    }
}
