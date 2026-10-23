/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPOutputStream;

/**
 * Sends a debug report to a Lyna instance, gzipped, and reads what it answers.
 * <p>
 * Nothing here blocks: the request runs on the HTTP client's own threads, so it works the same on Paper and Folia.
 */
public final class DebugUpload {
    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final HttpClient CLIENT =
            HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
    private static final Gson GSON = new Gson();

    private DebugUpload() {}

    /**
     * What became of an upload.
     */
    public sealed interface Result permits Uploaded, Failed {}

    /**
     * @param readUrl   the viewer, for whoever helps
     * @param deleteUrl deletes the report
     * @param expires   when Lyna deletes it on its own
     */
    public record Uploaded(String readUrl, String deleteUrl, Instant expires) implements Result {}

    /**
     * @param reason     why
     * @param message    what Lyna said, or what went wrong on the way
     * @param retryAfter seconds to wait before trying again, when Lyna said so
     */
    public record Failed(Reason reason, String message, OptionalLong retryAfter) implements Result {}

    public enum Reason {
        /**
         * Too many reports from this address; wait {@link Failed#retryAfter()}.
         */
        RATE_LIMITED,
        /**
         * The report is larger than Lyna takes.
         */
        TOO_LARGE,
        /**
         * Lyna does not take the report as it is.
         */
        REFUSED,
        /**
         * Lyna could not be reached or answered something unexpected.
         */
        UNREACHABLE
    }

    private record Receipt(String readKey, String deleteKey, String readUrl, String deleteUrl, String expires) {}

    private record Problem(String error, String message) {}

    /**
     * Sends a report.
     *
     * @param host the Lyna instance, e.g. {@link DebugSettings#DEFAULT_HOST}
     * @param json the report, as {@link DebugReport} serialised
     * @return the answer; the future never completes exceptionally
     */
    public static CompletableFuture<Result> send(String host, String json) {
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(URI.create(host + "/api/v1/debug/submit"))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Content-Encoding", "gzip")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(gzip(json)))
                    .build();
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(
                    new Failed(Reason.UNREACHABLE, "Invalid host " + host, OptionalLong.empty()));
        }
        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(DebugUpload::read)
                .exceptionally(e -> new Failed(Reason.UNREACHABLE, describe(e), OptionalLong.empty()));
    }

    static Result read(HttpResponse<String> response) {
        try {
            return switch (response.statusCode()) {
                case 200 -> uploaded(GSON.fromJson(response.body(), Receipt.class));
                case 429 ->
                    new Failed(
                            Reason.RATE_LIMITED,
                            problem(response),
                            retryAfter(
                                    response.headers().firstValue("Retry-After").orElse("")));
                case 413 -> new Failed(Reason.TOO_LARGE, problem(response), OptionalLong.empty());
                case 400, 422 -> new Failed(Reason.REFUSED, problem(response), OptionalLong.empty());
                default ->
                    new Failed(
                            Reason.UNREACHABLE, "Lyna answered " + response.statusCode() + ".", OptionalLong.empty());
            };
        } catch (JsonParseException | DateTimeParseException e) {
            return new Failed(Reason.UNREACHABLE, "Lyna answered something unexpected.", OptionalLong.empty());
        }
    }

    private static Result uploaded(Receipt receipt) {
        if (receipt == null || receipt.readUrl() == null || receipt.deleteUrl() == null || receipt.expires() == null) {
            return new Failed(Reason.UNREACHABLE, "Lyna answered without links.", OptionalLong.empty());
        }
        return new Uploaded(receipt.readUrl(), receipt.deleteUrl(), Instant.parse(receipt.expires()));
    }

    private static String problem(HttpResponse<String> response) {
        try {
            Problem problem = GSON.fromJson(response.body(), Problem.class);
            if (problem != null && problem.message() != null) return problem.message();
        } catch (JsonParseException e) {
            return response.body();
        }
        return response.body();
    }

    private static OptionalLong retryAfter(String header) {
        try {
            return OptionalLong.of(Long.parseLong(header.strip()));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

    private static String describe(Throwable error) {
        Throwable cause = error.getCause() != null ? error.getCause() : error;
        String message = cause.getMessage();
        return cause.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }

    static byte[] gzip(String json) {
        var bytes = new ByteArrayOutputStream();
        try (var out = new GZIPOutputStream(bytes)) {
            out.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Writing to memory failed", e);
        }
        return bytes.toByteArray();
    }
}
