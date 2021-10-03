package de.eldoria.eldoutilities.consumer;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts two input arguments and returns no result.  This is the quad-arity
 * specialization of {@link Consumer}. Unlike most other functional interfaces, {@code TriCondumer} is expected to
 * operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object, Object, Object)}.
 *
 * @param <T1> the type of the first argument to the operation
 * @param <T2> the type of the second argument to the operation
 * @param <T3> the type of the third argument to the operation
 * @param <T4> the type of the fourth argument to the operation
 * @see Consumer
 * @since 1.0.0
 */
@FunctionalInterface
public interface QuadConsumer<T1, T2, T3, T4> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t1 the first input argument
     * @param t2 the second input argument
     * @param t3 the third input argument
     * @param t4 the fourth input argument
     */
    void accept(T1 t1, T2 t2, T3 t3, T4 t4);

    /**
     * Returns a composed {@code QuadConsumer} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
     * operation.  If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code QuadConsumer} that performs in sequence this operation followed by the {@code after}
     * operation
     * @throws NullPointerException if {@code after} is null
     */
    default QuadConsumer<T1, T2, T3, T4> andThen(QuadConsumer<? super T1, ? super T2, ? super T3, ? super T4> after) {
        Objects.requireNonNull(after);

        return (t1, t2, t3, t4) -> {
            accept(t1, t2, t3, t4);
            after.accept(t1, t2, t3, t4);
        };
    }
}
