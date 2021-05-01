package de.eldoria.eldoutilities.crossversion.functionbuilder;

import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.function.VersionFunction;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class FunctionBuilder<A, R> {
    private final Map<ServerVersion, Function<A, R>> functions = new EnumMap<>(ServerVersion.class);

    protected FunctionBuilder() {
    }

    /**
     * Add a version function which should be used on one or more versions.
     *
     * @param function function to execute
     * @param version  versions which should use this function
     * @return builder instance with function applied for versions
     */
    public FunctionBuilder<A, R> addVersionFunction(Function<A, R> function, ServerVersion... version) {
        for (ServerVersion serverVersion : version) {
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
    public FunctionBuilder<A, R> addVersionFunctionBetween(ServerVersion oldest, ServerVersion newest, Function<A, R> function) {
        addVersionFunction(function, ServerVersion.versionsBetween(oldest, newest));
        return this;
    }

    /**
     * Build the version function.
     *
     * @return version functions with applied functions for versions.
     */
    public VersionFunction<A, R> build() {
        return new VersionFunction<>(functions);
    }
}
