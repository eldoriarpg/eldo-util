package de.eldoria.eldoutilities.database.builder;

import de.eldoria.eldoutilities.database.builder.exception.QueryExecutionException;

public class QueryBuilderConfig {
    /**
     * Contains the default configuration.
     */
    public static QueryBuilderConfig DEFAULT = builder().build();

    private final boolean throwing;
    private final boolean atomic;

    private QueryBuilderConfig(boolean throwing, boolean atomic) {
        this.throwing = throwing;
        this.atomic = atomic;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isThrowing() {
        return throwing;
    }

    public boolean isAtomic() {
        return atomic;
    }

    public static class Builder {
        boolean throwing;
        boolean atomic = true;

        /**
         * Sets the query builder as throwing. This will cause any occuring exception to be wrapped into an {@link QueryExecutionException} and be thrown instead of logged.
         *
         * @return The {@link Builder} with the value set.
         */
        public Builder throwExceptions() {
            throwing = true;
            return this;
        }

        /**
         * Set that the queries are not executed atomic.
         * <p>
         * When the queries are atomic they will be executed in one transaction. This will cause that no data will be changed if any query fails to execute.
         * <p>
         * On default queries will be also executed atomic. This method just exists for convenience. No queries will be executed after one query fails in any way.
         * <p>
         *
         * @return The {@link Builder} in with the atomic value set.
         */
        public Builder notAtomic() {
            atomic = false;
            return this;
        }

        public QueryBuilderConfig build() {
            return new QueryBuilderConfig(throwing, atomic);
        }
    }
}
