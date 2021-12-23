package de.eldoria.eldoutilities.functions;

public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T t) throws E;
}
