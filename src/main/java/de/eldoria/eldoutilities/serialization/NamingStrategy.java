package de.eldoria.eldoutilities.serialization;

/**
 * Interface to define naming strategies.
 *
 * @since 1.0.0
 */
public interface NamingStrategy {

    String adapt(Class<?> type);
}