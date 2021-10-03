package de.eldoria.eldoutilities.crossversion.function;

import de.eldoria.eldoutilities.crossversion.ServerVersion;
import de.eldoria.eldoutilities.crossversion.UnsupportedVersionException;
import de.eldoria.eldoutilities.functions.TriFunction;

import java.util.Map;

/**
 * A {@link TriFunction} with version sensitive context.
 *
 * @param <A> first Input Type
 * @param <B> second Input Type
 * @param <C> third Input Type
 * @param <R> result Type
 */
public class TriVersionFunction<A, B, C, R> {
    private final Map<ServerVersion, TriFunction<A, B, C, R>> functions;

    public TriVersionFunction(Map<ServerVersion, TriFunction<A, B, C, R>> functions) {
        this.functions = functions;
    }

    /**
     * Execute the function for the current version.
     *
     * @param a first parameter of the function.
     * @param b second parameter of the function
     * @param c third parameter of the function
     * @return value of the function
     * @throws UnsupportedVersionException when no function is defined for the server version.
     */
    public R apply(A a, B b, C c) {
        TriFunction<A, B, C, R> function = functions.get(ServerVersion.CURRENT_VERSION);
        if (function == null) {
            throw new UnsupportedVersionException();
        }
        return function.apply(a, b, c);
    }
}
