package de.eldoria.eldoutilities.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class FlagContainer {
    private static final Pattern FLAG = Pattern.compile("-([a-zA-Z]+?)");
    private static final Pattern NAMED_FLAG = Pattern.compile("--([a-zA-Z-])+?");
    private final Map<String, String> flags = new HashMap<>();

    private final List<String> flagArgs = new LinkedList<>();
    private final String[] args;
    private String currFlag = null;

    private FlagContainer(String[] args) {
        this.args = args;
    }

    /**
     * Create a new flag container based on the arguments
     *
     * @param args args
     *
     * @return new flag container with parsed args
     */
    public static FlagContainer of(String[] args) {
        FlagContainer flagContainer = new FlagContainer(args);
        flagContainer.parse();
        return flagContainer;
    }

    private void parse() {
        for (String arg : args) {
            if (FLAG.matcher(arg).matches()) {
                flushFlag();
                String flag = arg.substring(1);
                if (flag.length() > 1) {
                    addSingleFlags(flag);
                    continue;
                }
                currFlag = flag;
                continue;
            }

            if (NAMED_FLAG.matcher(arg).matches()) {
                flushFlag();
                currFlag = arg.substring(2);
                continue;
            }


            if (currFlag != null) {
                flagArgs.add(arg);
            }
        }
        flushFlag();
    }

    private void flushFlag() {
        if (currFlag != null) {
            flags.put(currFlag, flagArgs.isEmpty() ? null : String.join(" ", flagArgs));
            flagArgs.clear();
            currFlag = null;
        }
    }

    private void addSingleFlags(String flags) {
        for (char c : flags.toCharArray()) {
            this.flags.put(String.valueOf(c), null);
        }
    }

    /**
     * Check if a flag is present in the container.
     *
     * @param flag flag to check
     *
     * @return true if flag is present
     */
    public boolean has(@NotNull String flag) {
        return flags.containsKey(flag);
    }

    /**
     * Check if the flag has a value defined.
     *
     * @param flag flag to check
     *
     * @return true if a value is present
     */
    public boolean hasValue(String flag) {
        return flags.get(flag) != null;
    }

    /**
     * Get the value for a flag.
     *
     * @param flag flag to retrieve
     * @param map  function to map the string
     * @param <T>  type of flag
     *
     * @return flag parsed with the function.
     */
    public <T> T get(@NotNull String flag, Function<@Nullable String, T> map) {
        return map.apply(get(flag));
    }

    /**
     * Get the value for the flag.
     *
     * @param flag flag to retrieve
     *
     * @return flag value
     */
    @Nullable
    public String get(String flag) {
        return flags.get(flag);
    }

    /**
     * Get the flag value if the value is present
     *
     * @param flag flag to retrieve
     *
     * @return flag value in an optional if present
     */
    public Optional<String> getIfPresent(String flag) {
        return Optional.ofNullable(get(flag));
    }

    /**
     * Get the value if present parsed with the mapping function.
     * <p>
     * If the value is not present the mapping function will not be applied and a empty optional will be returned.
     *
     * @param flag flag to retrieve
     * @param map  function to map the flag
     * @param <T>  type of returned optional
     *
     * @return flag value parsed and wrapped into an optional
     */
    public <T> Optional<T> getIfPresent(@NotNull String flag, Function<String, T> map) {
        return getIfPresent(flag).map(map);
    }
}
