package de.eldoria.eldoutilities.crossversion.function;

import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.UnsupportedVersionException;
import de.eldoria.eldoutilities.functions.QuadFunction;

import java.util.Map;
import java.util.function.Function;

/**
 * A {@link Function} with version sensitive context.
 *
 * @param <A> input Type
 * @param <R> result Type
 */
public class VersionFunction<A, R> {
    private final Map<ServerVersion, Function<A, R>> functions;

    public VersionFunction(Map<ServerVersion, Function<A, R>> functions) {
        this.functions = functions;
    }

    /**
     * Execute the function for the current version.
     *
     * @param a first parameter of the function.
     * @return value of the function
     * @throws UnsupportedVersionException when no function is defined for the server version.
     */
    public R apply(A a) {
        Function<A, R> function = functions.get(ServerVersion.CURRENT_VERSION);
        if (function == null) {
            throw new UnsupportedVersionException();
        }
        return function.apply(a);
    }
}
