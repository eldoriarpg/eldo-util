package de.eldoria.eldoutilities.debug;


import de.eldoria.eldoutilities.updater.butlerupdater.ButlerUpdateData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class DebugSettings {
    public static final List<Filter> DEFAULT_FILTER = new ArrayList<>() {{
        add(new Filter(Pattern.compile("/([0-9]{1,3}\\.){3}[0-9]{1,3}(:[0-9]{1,5})"), "/127.0.0.1"));
        add(new Filter(Pattern.compile("(?i)(password:).*?$", Pattern.MULTILINE), "$1 *******"));
        add(new Filter(Pattern.compile("(?i)(user:).*?$", Pattern.MULTILINE), "$1 *****"));
    }};

    public static final DebugSettings DEFAULT = new DebugSettings(ButlerUpdateData.HOST, DEFAULT_FILTER);
    private final String host;

    private final List<Filter> filters = new ArrayList<>();

    private DebugSettings(String host, List<Filter> filters) {
        this.host = host;
        this.filters.addAll(filters);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getHost() {
        return host;
    }

    public void addFilter(Filter... filter) {
        filters.addAll(Arrays.asList(filter));
    }

    public String applyFilter(String text) {
        for (var filter : filters) {
            text = filter.apply(text);
        }
        return text;
    }

    public static class Builder {
        private final List<Filter> filters = new ArrayList<>();
        private String host = ButlerUpdateData.HOST;

        public Builder forHost(String host) {
            this.host = host;
            return this;
        }

        public Builder withFilter(Filter... filter) {
            this.filters.addAll(Arrays.asList(filter));
            return this;
        }

        public Builder withDefaultFilter() {
            this.filters.addAll(DEFAULT_FILTER);
            return this;
        }

        public DebugSettings build() {
            return new DebugSettings(host, filters);
        }
    }
}
