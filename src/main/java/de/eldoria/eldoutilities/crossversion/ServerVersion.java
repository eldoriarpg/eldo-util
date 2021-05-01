package de.eldoria.eldoutilities.crossversion;

import de.eldoria.eldoutilities.container.Triple;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum to determine and work with multiple versions.
 *
 * @since 1.0.0
 */
public enum ServerVersion {
    MC_UNKOWN(Triple.of(0, 0, 0)),
    MC_1_0(Triple.of(1, 0, 0)),
    MC_1_1(Triple.of(1, 1, 0)),
    MC_1_2(Triple.of(1, 2, 0)),
    MC_1_3(Triple.of(1, 3, 0)),
    MC_1_4(Triple.of(1, 4, 0)),
    MC_1_5(Triple.of(1, 5, 0)),
    MC_1_6(Triple.of(1, 6, 0)),
    MC_1_7(Triple.of(1, 7, 0)),
    MC_1_8(Triple.of(1, 8, 0)),
    MC_1_9(Triple.of(1, 9, 0)),
    MC_1_10(Triple.of(1, 10, 0)),
    MC_1_11(Triple.of(1, 11, 0)),
    MC_1_12(Triple.of(1, 12, 0)),
    MC_1_13(Triple.of(1, 13, 0)),
    MC_1_14(Triple.of(1, 14, 0)),
    MC_1_15(Triple.of(1, 15, 0)),
    MC_1_16(Triple.of(1, 16, 0)),
    MC_1_17(Triple.of(1, 17, 0)),
    MC_1_18(Triple.of(1, 18, 0)),
    MC_1_19(Triple.of(1, 19, 0)),
    MC_1_20(Triple.of(1, 20, 0));

    /**
     * Contains the current version of the server.
     */
    public static final ServerVersion CURRENT_VERSION;
    private static final Pattern VERSION_PATTERN;

    static {
        VERSION_PATTERN = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})(?:\\.([0-9]{0,3}))?");
        CURRENT_VERSION = Bukkit.getServer() != null ? getVersion() : MC_UNKOWN;
    }

    private final Triple<Integer, Integer, Integer> version;

    ServerVersion(Triple<Integer, Integer, Integer> version) {
        this.version = version;
    }

    /**
     * Get the version of the server.
     *
     * @return version of server
     */
    public static ServerVersion getVersion() {
        Optional<Triple<Integer, Integer, Integer>> version = extractVersion();

        if (!version.isPresent()) {
            return MC_UNKOWN;
        }

        for (ServerVersion value : values()) {
            if (value.version.first.equals(version.get().first)) {
                if (value.version.second.equals(version.get().second)) {
                    return value;
                }
            }
        }

        Bukkit.getLogger().warning("Could not determine server Version.");

        return MC_UNKOWN;
    }

    /**
     * Extract the version of the server.
     *
     * @return optional version of server if the version could be determined
     */
    public static Optional<Triple<Integer, Integer, Integer>> extractVersion() {
        Matcher matcher = VERSION_PATTERN.matcher(Bukkit.getServer().getBukkitVersion());

        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = Integer.parseInt(matcher.group(3).isEmpty() ? "0" : matcher.group(3));
            return Optional.of(new Triple<>(major, minor, patch));
        }
        return Optional.empty();
    }

    /**
     * Check if a version is between two versions
     *
     * @param oldest  oldest allowed version
     * @param newest  newest allowed version
     * @param current current version
     * @return true when the version is between oldest and newest version or equal to the oldest or newest.
     */
    public static boolean between(ServerVersion oldest, ServerVersion newest, ServerVersion current) {
        Integer currentMajor = current.version.first;
        Integer oldestMajor = oldest.version.first;
        Integer newestMajor = newest.version.first;
        Integer currentMinor = current.version.second;
        Integer oldestMinor = oldest.version.second;
        Integer newestMinor = newest.version.second;
        if (currentMajor < oldestMajor || currentMajor > newestMajor) return false;
        return currentMinor >= oldestMinor && currentMinor <= newestMinor;
    }

    /**
     * Get versions between two versions.
     *
     * @param oldest oldest version (inclusive)
     * @param newest newest version (exclusive)
     * @return array of versions
     */
    public static ServerVersion[] versionsBetween(ServerVersion oldest, ServerVersion newest) {
        return Arrays.stream(values()).filter(v -> v.between(oldest, newest)).toArray(ServerVersion[]::new);
    }

    /**
     * This method will check if the current version is between the oldest and newest version. Will abort enable of
     * plugin when called on enable.
     *
     * @param oldest oldest version (inclusive)
     * @param newest newest version (inclusive)
     * @throws UnsupportedVersionException when the server version is not between the oldest and newest version.
     */
    public static void forceVersion(ServerVersion oldest, ServerVersion newest) {
        if (CURRENT_VERSION.between(oldest, newest)) return;
        throw new UnsupportedVersionException();
    }

    /**
     * Check if this version is between two versions
     *
     * @param oldest oldest allowed version
     * @param newest newest allowed version
     * @return true when the version is between oldest and newest version or equal to the oldest or newest.
     */
    public boolean between(ServerVersion oldest, ServerVersion newest) {
        return between(oldest, newest, this);
    }

    /**
     * Get the minor version as a string separated by '.'. E.g. 1.15
     *
     * @return version as string
     */
    public String version() {
        return this.version.first + "." + this.version.second;
    }
}
