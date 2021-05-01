package de.eldoria.eldoutilities.crossversion.functionbuilder;

/**
 * Interface to create different function builders.
 */
public interface VersionFunctionBuilder {
    /**
     * Get a function builder.
     *
     * @param a   first input type class
     * @param r   result type class
     * @param <A> first input type
     * @param <R> result type
     * @return a new function builder instance
     */
    static <A, R> FunctionBuilder<A, R> functionBuilder(Class<A> a, Class<R> r) {
        return new FunctionBuilder<>();
    }

    /**
     * Get a bi function builder
     *
     * @param a   first input type class
     * @param b   second input type class
     * @param r   result type class
     * @param <A> first input type
     * @param <B> second input type
     * @param <R> result type
     * @return a new bi function builder instance
     */
    static <A, B, R> BiFunctionBuilder<A, B, R> biFunctionBuilder(Class<A> a, Class<B> b, Class<R> r) {
        return new BiFunctionBuilder<>();
    }

    /**
     * Get a tri function builder.
     *
     * @param a   first input type class
     * @param b   second input type class
     * @param c   third input type class
     * @param r   result type class
     * @param <A> first input type
     * @param <B> second input type
     * @param <C> third input type
     * @param <R> result type
     * @return new tri function builder instance
     */
    static <A, B, C, R> TriFunctionBuilder<A, B, C, R> triFunctionBuilder(Class<A> a, Class<B> b, Class<C> c, Class<R> r) {
        return new TriFunctionBuilder<>();
    }

    /**
     * Get a quad function builder.
     *
     * @param a   first input type class
     * @param b   second input type class
     * @param c   third input type class
     * @param d   fourth input type class
     * @param r   result type class
     * @param <A> first input type
     * @param <B> second input type
     * @param <C> third input type
     * @param <D> fourth input type
     * @param <R> result type
     * @return new quad function builder instance
     */
    static <A, B, C, D, R> QuadFunctionBuilder<A, B, C, D, R> quadFunctionBuilder(
            Class<A> a, Class<B> b, Class<C> c, Class<D> d, Class<R> r) {
        return new QuadFunctionBuilder<>();
    }
}
