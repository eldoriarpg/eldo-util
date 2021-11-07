package de.eldoria.eldoutilities.crossversion.builder;

import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.function.BiVersionFunction;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * A builder for a  {@link BiVersionFunction} with version sensitive context.
 *
 * @param <A> first Input Type
 * @param <B> second Input Type
 * @param <R> result Type
 */
public class BiFunctionBuilder<A, B, R> {
    private final Map<ServerVersion, BiFunction<A, B, R>> functions = new EnumMap<>(ServerVersion.class);

    protected BiFunctionBuilder() {
    }

    /**
     * Add a version function which should be used on one or more versions.
     *
     * @param function function to execute
     * @param version  versions which should use this function
     * @return builder instance with function applied for versions
     */
    public BiFunctionBuilder<A, B, R> addVersionFunctionBetween(BiFunction<A, B, R> function, ServerVersion... version) {
        for (var serverVersion : version) {
            functions.put(serverVersion, function);
        }
        return this;
    }

    /**
     * Add a version functions for all versions between two versions.
     *
     * @param oldest   oldest version (inclusive)
     * @param newest   newest version (inclusive)
     * @param function function to execute
     * @return builder instance with function applied for versions
     */
    public BiFunctionBuilder<A, B, R> addVersionFunctionBetween(ServerVersion oldest, ServerVersion newest, BiFunction<A, B, R> function) {
        addVersionFunctionBetween(function, ServerVersion.versionsBetween(oldest, newest));
        return this;
    }

    /**
     * Build the version function.
     *
     * @return version functions with applied functions for versions.
     */
    public BiVersionFunction<A, B, R> build() {
        return new BiVersionFunction<>(functions);
    }
}
