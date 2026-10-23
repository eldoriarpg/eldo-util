/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import com.sun.net.httpserver.HttpServer;
import de.eldoria.eldoutilities.debug.DebugUpload.Failed;
import de.eldoria.eldoutilities.debug.DebugUpload.Reason;
import de.eldoria.eldoutilities.debug.DebugUpload.Uploaded;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * What a Lyna instance is sent, and what each of its answers becomes.
 */
class DebugUploadTest {
    private HttpServer server;
    private final AtomicReference<String> received = new AtomicReference<>();
    private final AtomicReference<String> encoding = new AtomicReference<>();
    private int status;
    private String body;
    private String retryAfter;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/debug/submit", exchange -> {
            encoding.set(exchange.getRequestHeaders().getFirst("Content-Encoding"));
            try (var in = new GZIPInputStream(exchange.getRequestBody())) {
                received.set(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
            if (retryAfter != null) exchange.getResponseHeaders().add("Retry-After", retryAfter);
            byte[] answer = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, answer.length);
            exchange.getResponseBody().write(answer);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private DebugUpload.Result send() {
        return DebugUpload.send("http://127.0.0.1:" + server.getAddress().getPort(), "{\"v\":2}")
                .join();
    }

    private void answer(int status, String body) {
        this.status = status;
        this.body = body;
    }

    @Test
    void aStoredReportGivesItsLinks() {
        answer(200, """
                {"readKey":"r","deleteKey":"d","readUrl":"https://lyna/debug/r","deleteUrl":"https://lyna/debug/delete/d","expires":"2026-10-29T12:00:00Z"}""");

        var uploaded = assertInstanceOf(Uploaded.class, send());

        assertEquals(
                new Uploaded(
                        "https://lyna/debug/r", "https://lyna/debug/delete/d", Instant.parse("2026-10-29T12:00:00Z")),
                uploaded);
        assertEquals("{\"v\":2}", received.get());
        assertEquals("gzip", encoding.get());
    }

    @Test
    void aRateLimitSaysHowLongToWait() {
        answer(429, "{\"error\":\"rate_limited\",\"message\":\"One report per 10 seconds.\"}");
        retryAfter = "10";

        assertEquals(new Failed(Reason.RATE_LIMITED, "One report per 10 seconds.", OptionalLong.of(10)), send());
    }

    @Test
    void tooLargeAndRefusedCarryLynasMessage() {
        answer(413, "{\"error\":\"too_large\",\"message\":\"At most 16 bytes.\"}");
        assertEquals(new Failed(Reason.TOO_LARGE, "At most 16 bytes.", OptionalLong.empty()), send());

        answer(422, "{\"error\":\"invalid\",\"message\":\"The plugin has no name.\"}");
        assertEquals(new Failed(Reason.REFUSED, "The plugin has no name.", OptionalLong.empty()), send());
    }

    @Test
    void anythingElseIsUnreachable() {
        answer(500, "boom");
        assertEquals(Reason.UNREACHABLE, assertInstanceOf(Failed.class, send()).reason());

        answer(200, "not json");
        assertEquals(Reason.UNREACHABLE, assertInstanceOf(Failed.class, send()).reason());

        answer(200, "{}");
        assertEquals(Reason.UNREACHABLE, assertInstanceOf(Failed.class, send()).reason());
    }

    @Test
    void aHostNobodyListensOnIsUnreachable() {
        server.stop(0);
        var failed = assertInstanceOf(
                Failed.class,
                DebugUpload.send("http://127.0.0.1:" + server.getAddress().getPort(), "{}")
                        .join());
        assertEquals(Reason.UNREACHABLE, failed.reason());
    }
}
