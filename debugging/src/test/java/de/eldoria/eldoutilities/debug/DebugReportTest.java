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
import de.eldoria.eldoutilities.debug.data.LogData;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The sections of a report, the shape Lyna reads, and what the sender is told.
 */
class DebugReportTest {
    private static final LogData LOG = new LogData("the log", "", new String[]{"npe", " "}, new String[]{"other"});

    @Test
    void sectionsComeInTheOrderTheViewerShowsThem() {
        var sections = DebugReport.sections(
                new EntryData[]{new EntryData("Hooks", "token: abc")},
                LOG,
                List.of(new ConfigFiles.Read("config.yml", "password: hunter2")),
                DebugSettings.DEFAULT);

        assertEquals(List.of(
                new Section(Kind.META, "Hooks", "token: *****"),
                new Section(Kind.LOG, "latest.log", "the log"),
                new Section(Kind.INTERNAL_EXCEPTION, "Plugin exception 1", "npe"),
                new Section(Kind.EXTERNAL_EXCEPTION, "Other exception 1", "other"),
                new Section(Kind.CONFIG, "config.yml", "password: *****")), sections);
    }

    @Test
    void sectionsSerialiseAsLynaReadsThem() {
        JsonObject json = new Gson().toJsonTree(new Section(Kind.CONFIG, "config.yml", "x")).getAsJsonObject();

        assertEquals("CONFIG", json.get("kind").getAsString());
        assertEquals("config.yml", json.get("name").getAsString());
        assertEquals("x", json.get("content").getAsString());
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
