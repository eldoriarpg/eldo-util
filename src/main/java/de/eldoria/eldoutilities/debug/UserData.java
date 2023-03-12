/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.debug;

import com.google.common.hash.Hashing;
import de.eldoria.eldoutilities.utils.Plugins;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class UserData {

    public final String type;
    private final Map<String, String> buildProperties;
    public final String user = "%%__USER__%%";
    public final String resource = "%%__RESOURCE__%%";
    public final String nonce = "%%__NONCE__%%";

    private UserData(Map<String, String> buildProperties) {
        this.buildProperties = buildProperties;
        type = buildProperties.getOrDefault("type", "LOCAL");
    }

    public static UserData get(Plugin plugin) {
        Map<String, String> buildProperties = new LinkedHashMap<>();
        try (var in = plugin.getResource("build.data")) {
            if (in != null) {
                buildProperties = Arrays.stream(new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n"))
                        .filter(r -> !r.isBlank())
                        .map(e -> e.split("=", 2))
                        .filter(e -> e.length == 2)
                        .collect(Collectors.toMap(e -> e[0], e -> e[1]));
            }
            Path pluginFile = Plugins.getPluginFile(plugin).get().toPath();
            String md5 = Hashing.md5()
                    .hashBytes(Files.readAllBytes(pluginFile))
                    .toString();
            String sha256 = Hashing.sha256()
                    .hashBytes(Files.readAllBytes(pluginFile))
                    .toString();
            buildProperties.put("md5", md5);
            buildProperties.put("sha256", sha256);
        } catch (IOException e) {

        }

        return new UserData(buildProperties);
    }

    public Optional<String> property(String property) {
        return Optional.ofNullable(buildProperties.get(property));
    }

    public Optional<String> property(DefaultProperties property) {
        return property(property.key());
    }

    public String resource() {
        return resource;
    }

    public boolean isPremium() {
        return "PUBLIC".equalsIgnoreCase(type);
    }

    public String user() {
        return user;
    }

    public String asString() {
        List<String> properties = new ArrayList<>();
        properties.add("Premium: " + isPremium());
        properties.add("User: " + user());
        properties.add("Nonce: " + nonce);
        buildProperties.entrySet().stream().map(e -> "%s: %s".formatted(e.getKey(), e.getValue())).forEach(properties::add);
        return String.join("\n", properties);
    }
}
