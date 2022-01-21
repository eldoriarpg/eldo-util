/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.container;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Triple)) return false;

        var triple = (Triple<?, ?, ?>) o;

        if (!Objects.equals(first, triple.first)) return false;
        if (!Objects.equals(second, triple.second)) return false;
        return Objects.equals(third, triple.third);
    }

    @Override
    public int hashCode() {
        var result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Triple{" +
               "first=" + first +
               ", second=" + second +
               ", third=" + third +
               '}';
    }
}
