package de.eldoria.eldoutilities.container;

/**
 * Contains a immutable triple of three values.
 *
 * @param <A> value A
 * @param <B> value B
 * @param <C> value C
 * @since 1.0.0
 */
public class Triple<A, B, C> {
    public final A first;
    public final B second;
    public final C third;

    /**
     * Create a new tripple.
     *
     * @param first  first value
     * @param second second value
     * @param third  third value
     */
    public Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Create a new tripple.
     *
     * @param x   first value
     * @param y   second value
     * @param z   third value
     * @param <X> type of first value
     * @param <Y> type of second value
     * @param <Z> type of third value
     * @return new tripple
     */
    public static <X, Y, Z> Triple<X, Y, Z> of(X x, Y y, Z z) {
        return new Triple<>(x, y, z);
    }
}
