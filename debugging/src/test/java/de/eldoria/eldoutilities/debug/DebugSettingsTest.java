/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * What the default filters hide before a report leaves the server.
 */
class DebugSettingsTest {
    private static String filter(String text) {
        return DebugSettings.DEFAULT.applyFilter(text);
    }

    @Test
    void ipv4KeepsItsFirstTwoOctets() {
        assertEquals("Steve[/203.0.x.x:51234] logged in", filter("Steve[/203.0.113.7:51234] logged in"));
    }

    @Test
    void loopbackStays() {
        assertEquals("bound to 127.0.0.1:25565", filter("bound to 127.0.0.1:25565"));
    }

    @Test
    void ipv6IsHidden() {
        assertEquals("from x:x:x:x:x:x:x:x", filter("from 2001:db8::8a2e:370:7334"));
        assertEquals("[12:00:01 INFO]: Done", filter("[12:00:01 INFO]: Done"));
    }

    @Test
    void urlCredentialsAreHidden() {
        assertEquals(
                "url: jdbc:postgresql://*****:*****@db:5432/mc",
                filter("url: jdbc:postgresql://admin:hunter2@db:5432/mc"));
    }

    @Test
    void secretsInYamlAndPropertiesAreHidden() {
        assertEquals(
                "database:\n  password: *****\n  port: 5432", filter("database:\n  password: hunter2\n  port: 5432"));
        assertEquals("api-key=*****", filter("api-key=abc123"));
        assertEquals("discord:\n  bot-token: \"*****\"", filter("discord:\n  bot-token: \"abc.def\""));
        assertEquals("  user: *****", filter("  user: minecraft"));
    }

    @Test
    void secretsInJsonAreHidden() {
        assertEquals(
                "{\"clientSecret\": \"*****\", \"port\": 1}", filter("{\"clientSecret\": \"s3cr3t\", \"port\": 1}"));
    }

    @Test
    void customHostLosesItsTrailingSlash() {
        assertEquals(
                "https://lyna.example.com",
                DebugSettings.builder()
                        .forHost("https://lyna.example.com/")
                        .build()
                        .getHost());
    }
}
