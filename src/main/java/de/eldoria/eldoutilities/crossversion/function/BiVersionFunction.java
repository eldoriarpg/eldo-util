package de.eldoria.eldoutilities.crossversion.function;

import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.UnsupportedVersionException;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * A {@link BiFunction} with version sensitive context.
 *
 * @param <A> first Input Type
 * @param <B> second Input Type
 * @param <R> result Type
 */
public class BiVersionFunction<A, B, R> {
    private final Map<ServerVersion, BiFunction<A, B, R>> functions;

    public BiVersionFunction(Map<ServerVersion, BiFunction<A, B, R>> functions) {
        this.functions = functions;
    }

    /**
     * Execute the function for the current version.
     *
     * @param a first parameter of the function.
     * @param b second parameter of the function
     * @return value of the function
     * @throws UnsupportedVersionException when no function is defined for the server version.
     */
    public R apply(A a, B b) {
        BiFunction<A, B, R> function = functions.get(ServerVersion.CURRENT_VERSION);
        if (function == null) {
            throw new UnsupportedVersionException();
        }
        return function.apply(a, b);
    }
}
