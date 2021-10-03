package de.eldoria.eldoutilities.container;

/**
 * Contains a immutable pair of two values
 *
 * @param <A> value A
 * @param <B> value B
 * @since 1.0.0
 */
public class Pair<A, B> {
    public final A first;
    public final B second;

    /**
     * Create a new pair.
     *
     * @param first  first value
     * @param second second value
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Create a new pair.
     *
     * @param x   first value
     * @param y   second value
     * @param <X> type of first value
     * @param <Y> type of second value
     * @return new pair
     */
    public static <X, Y> Pair<X, Y> of(X x, Y y) {
        return new Pair<>(x, y);
    }

}
