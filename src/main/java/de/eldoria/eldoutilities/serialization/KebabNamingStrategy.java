package de.eldoria.eldoutilities.serialization;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Naming strategie to define field names on serialization.
 *
 * @since 1.0.0
 */
public final class KebabNamingStrategy implements NamingStrategy {
    private static final Map<Class<?>, String> KEY_LOOKUP_CACHE = new HashMap<>();
    private static final Pattern PATTERN = Pattern.compile("([a-z0-9])([A-Z])");

    private static Optional<ConfigKey> annotation(Class<?> type) {
        return Optional.ofNullable(type.getAnnotation(ConfigKey.class));
    }

    @Override
    public String adapt(Class<?> type) {
        var actualType = type;
        if (type.isAnonymousClass()) {
            actualType = type.getSuperclass();
        }
        return KEY_LOOKUP_CACHE.computeIfAbsent(actualType, clazz -> {
            var configKey = annotation(clazz).map(ConfigKey::value).orElse(clazz.getSimpleName());
            return PATTERN.matcher(configKey).replaceAll("$1-$2").toLowerCase();
        });
    }
}
