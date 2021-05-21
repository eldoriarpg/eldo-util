package de.eldoria.eldoutilities.functions;

public interface ThrowingFunction<R, T, E extends Exception> {
    R apply(T t) throws E;
}
