package de.eldoria.eldoutilities.crossversion.builder;

import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.function.TriVersionFunction;
import de.eldoria.eldoutilities.functions.TriFunction;

import java.util.EnumMap;
import java.util.Map;

public class TriFunctionBuilder<A, B, C, R> {
    private final Map<ServerVersion, TriFunction<A, B, C, R>> functions = new EnumMap<>(ServerVersion.class);

    protected TriFunctionBuilder() {
    }

    /**
     * Add a version function which should be used on one or more versions.
     *
     * @param function function to execute
     * @param version  versions which should use this function
     * @return builder instance with function applied for versions
     */
    public TriFunctionBuilder<A, B, C, R> addVersionFunction(TriFunction<A, B, C, R> function, ServerVersion... version) {
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
    public TriFunctionBuilder<A, B, C, R> addVersionFunctionBetween(ServerVersion oldest, ServerVersion newest, TriFunction<A, B, C, R> function) {
        addVersionFunction(function, ServerVersion.versionsBetween(oldest, newest));
        return this;
    }

    /**
     * Build the version function.
     *
     * @return version functions with applied functions for versions.
     */
    public TriVersionFunction<A, B, C, R> build() {
        return new TriVersionFunction<>(functions);
    }
}
