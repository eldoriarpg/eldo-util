/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.eldoria.eldoutilities.debug.DebugReport.Kind;
import de.eldoria.eldoutilities.debug.DebugReport.Section;
import de.eldoria.eldoutilities.debug.DebugUpload.Failed;
import de.eldoria.eldoutilities.debug.DebugUpload.Reason;
import de.eldoria.eldoutilities.debug.DebugUpload.Uploaded;
import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.eldoutilities.debug.data.ExceptionData;
import de.eldoria.eldoutilities.debug.data.LogData;
import de.eldoria.eldoutilities.debug.data.chart.CategoryChart;
import de.eldoria.eldoutilities.debug.data.chart.ChartData;
import de.eldoria.eldoutilities.debug.data.chart.GaugeChart;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The sections of a report, the shape Lyna reads, and what the sender is told.
 */
class DebugReportTest {
    private static final LogData LOG = new LogData("the log", "",
            new ExceptionData[]{new ExceptionData("npe", 3, "12:00:01", "12:30:00"), new ExceptionData(" ", 1, null, null)},
            new ExceptionData[]{new ExceptionData("other", 1, "12:00:02", "12:00:02")});

    @Test
    void sectionsComeInTheOrderTheViewerShowsThem() {
        var sections = DebugReport.sections(
                List.of(HealthCheck.error("Database", "password: hunter2 refused")),
                new EntryData[]{new EntryData("Hooks", "token: abc"), new EntryData("Storage", Map.of("url", "jdbc:mysql://root:pw@db/mc"))},
                List.of(CategoryChart.bar("Storage types").value("yaml", 3)),
                LOG,
                List.of(new ConfigFiles.Read("config.yml", "password: hunter2"), new ConfigFiles.Read("mobs/a.json", "{}")),
                DebugSettings.DEFAULT);

        assertEquals(List.of(
                Section.check("Database", HealthCheck.Level.ERROR, "password: *****"),
                Section.meta("Hooks", "token: *****", Map.of()),
                Section.meta("Storage", "", Map.of("url", "jdbc:mysql://*****:*****@db/mc")),
                Section.chart("Storage types", "", Map.of("type", "bar", "data", Map.of("yaml", 3))),
                Section.text(Kind.LOG, "latest.log", "the log", "log"),
                Section.exception(Kind.INTERNAL_EXCEPTION, "Plugin exception 1", new ExceptionData("npe", 3, "12:00:01", "12:30:00")),
                Section.exception(Kind.EXTERNAL_EXCEPTION, "Other exception 1", new ExceptionData("other", 1, "12:00:02", "12:00:02")),
                Section.text(Kind.CONFIG, "config.yml", "password: *****", "yaml"),
                Section.text(Kind.CONFIG, "mobs/a.json", "{}", "json")), sections);
    }

    @Test
    void sectionsSerialiseAsLynaReadsThem() {
        JsonObject config = new Gson().toJsonTree(Section.text(Kind.CONFIG, "config.yml", "x", "yaml")).getAsJsonObject();
        assertEquals("CONFIG", config.get("kind").getAsString());
        assertEquals("config.yml", config.get("name").getAsString());
        assertEquals("x", config.get("content").getAsString());
        assertEquals("yaml", config.get("format").getAsString());
        assertFalse(config.has("level"));
        assertFalse(config.has("count"));

        JsonObject check = new Gson().toJsonTree(Section.check("Db", HealthCheck.Level.WARN, "slow")).getAsJsonObject();
        assertEquals("CHECK", check.get("kind").getAsString());
        assertEquals("WARN", check.get("level").getAsString());

        JsonObject meta = new Gson().toJsonTree(Section.meta("Hooks", "", Map.of("Vault", "1.7"))).getAsJsonObject();
        assertEquals("Vault", meta.getAsJsonArray("entries").get(0).getAsJsonObject().get("key").getAsString());

        JsonObject exception = new Gson().toJsonTree(Section.exception(Kind.INTERNAL_EXCEPTION, "e",
                new ExceptionData("npe", 2, "12:00:00", "12:01:00"))).getAsJsonObject();
        assertEquals(2, exception.get("count").getAsInt());
        assertEquals("12:00:00", exception.get("firstSeen").getAsString());
        assertEquals("12:01:00", exception.get("lastSeen").getAsString());
    }

    @Test
    void chartsKeepTheirLooseDataAndPassTheFilter() {
        var chart = ChartData.of("Pool", "line", Map.of("password: hunter2", List.of(1, "2.5", Map.of("x", 3))))
                             .unit("ms")
                             .hint("marks", List.of(Map.of("y", 25, "label", "budget")))
                             .description("token: abc")
                             .toChart(DebugSettings.DEFAULT);

        assertEquals("line", chart.get("type"));
        assertEquals(Map.of("password: *****", List.of(1, "2.5", Map.of("x", 3))), chart.get("data"));
        assertEquals("ms", chart.get("unit"));
        assertEquals(List.of(Map.of("y", 25, "label", "budget")), chart.get("marks"));
        assertFalse(chart.containsKey("x"));
    }

    @Test
    void chartsSerialiseAsLynaReadsThem() {
        var gauge = GaugeChart.gauge("Usage", 42).unit("percent").toChart(DebugSettings.DEFAULT);
        JsonObject section = new Gson().toJsonTree(Section.chart("Usage", "", gauge)).getAsJsonObject();

        assertEquals("CHART", section.get("kind").getAsString());
        JsonObject chart = section.getAsJsonObject("chart");
        assertEquals("gauge", chart.get("type").getAsString());
        assertEquals(42, chart.getAsJsonObject("data").get("value").getAsInt());
        assertEquals(100, chart.getAsJsonObject("data").get("max").getAsInt());
        assertEquals("percent", chart.get("unit").getAsString());
        assertFalse(section.has("level"));
    }

    @Test
    void formatsFollowTheFileName() {
        assertEquals("yaml", DebugReport.formatOf("mobs/zombie.YML"));
        assertEquals("toml", DebugReport.formatOf("a.toml"));
        assertEquals("text", DebugReport.formatOf("notes"));
    }

    @Test
    void anUploadIsAnsweredWithBothLinks() {
        String text = plain(new Uploaded("https://lyna/debug/r", "https://lyna/debug/delete/d", Instant.parse("2026-10-29T12:00:00Z")));

        assertTrue(text.contains("Read: https://lyna/debug/r"), text);
        assertTrue(text.contains("Delete: https://lyna/debug/delete/d"), text);
    }

    @Test
    void aRateLimitNamesTheWait() {
        String text = plain(new Failed(Reason.RATE_LIMITED, "", OptionalLong.of(7)));

        assertTrue(text.contains("Try again in 7 seconds."), text);
    }

    private static String plain(DebugUpload.Result result) {
        return PlainTextComponentSerializer.plainText().serialize(DebugUtil.message(result, DebugSettings.DEFAULT));
    }
}
