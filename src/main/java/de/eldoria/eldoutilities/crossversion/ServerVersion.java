/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

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
    MC_1_20(Triple.of(1, 20, 0)),
    MC_1_21(Triple.of(1, 21, 0)),
    MC_1_22(Triple.of(1, 22, 0)),
    MC_1_23(Triple.of(1, 23, 0)),
    MC_1_24(Triple.of(1, 24, 0)),
    MC_1_25(Triple.of(1, 25, 0)),
    MC_1_26(Triple.of(1, 26, 0)),
    MC_1_27(Triple.of(1, 27, 0)),
    MC_1_28(Triple.of(1, 28, 0)),
    MC_1_29(Triple.of(1, 29, 0)),
    MC_1_30(Triple.of(1, 30, 0)),
    MC_1_31(Triple.of(1, 31, 0)),
    MC_1_32(Triple.of(1, 32, 0)),
    MC_1_33(Triple.of(1, 33, 0)),
    MC_1_34(Triple.of(1, 34, 0)),
    MC_1_35(Triple.of(1, 35, 0)),
    MC_1_36(Triple.of(1, 36, 0)),
    MC_1_37(Triple.of(1, 37, 0)),
    MC_1_38(Triple.of(1, 38, 0)),
    MC_1_39(Triple.of(1, 39, 0)),
    MC_1_40(Triple.of(1, 40, 0)),
    MC_1_41(Triple.of(1, 41, 0)),
    MC_1_42(Triple.of(1, 42, 0)),
    MC_1_43(Triple.of(1, 43, 0)),
    MC_1_44(Triple.of(1, 44, 0)),
    MC_1_45(Triple.of(1, 45, 0)),
    MC_1_46(Triple.of(1, 46, 0)),
    MC_1_47(Triple.of(1, 47, 0)),
    MC_1_48(Triple.of(1, 48, 0)),
    MC_1_49(Triple.of(1, 49, 0)),
    MC_1_50(Triple.of(1, 50, 0)),
    MC_1_51(Triple.of(1, 51, 0)),
    MC_1_52(Triple.of(1, 52, 0)),
    MC_1_53(Triple.of(1, 53, 0)),
    MC_1_54(Triple.of(1, 54, 0)),
    MC_1_55(Triple.of(1, 55, 0)),
    MC_1_56(Triple.of(1, 56, 0)),
    MC_1_57(Triple.of(1, 57, 0)),
    MC_1_58(Triple.of(1, 58, 0)),
    MC_1_59(Triple.of(1, 59, 0)),
    MC_1_60(Triple.of(1, 60, 0)),
    MC_1_61(Triple.of(1, 61, 0)),
    MC_1_62(Triple.of(1, 62, 0)),
    MC_1_63(Triple.of(1, 63, 0)),
    MC_1_64(Triple.of(1, 64, 0)),
    MC_1_65(Triple.of(1, 65, 0)),
    MC_1_66(Triple.of(1, 66, 0)),
    MC_1_67(Triple.of(1, 67, 0)),
    MC_1_68(Triple.of(1, 68, 0)),
    MC_1_69(Triple.of(1, 69, 0)),
    MC_1_70(Triple.of(1, 70, 0)),
    MC_1_71(Triple.of(1, 71, 0)),
    MC_1_72(Triple.of(1, 72, 0)),
    MC_1_73(Triple.of(1, 73, 0)),
    MC_1_74(Triple.of(1, 74, 0)),
    MC_1_75(Triple.of(1, 75, 0)),
    MC_1_76(Triple.of(1, 76, 0)),
    MC_1_77(Triple.of(1, 77, 0)),
    MC_1_78(Triple.of(1, 78, 0)),
    MC_1_79(Triple.of(1, 79, 0)),
    MC_1_80(Triple.of(1, 80, 0)),
    MC_1_81(Triple.of(1, 81, 0)),
    MC_1_82(Triple.of(1, 82, 0)),
    MC_1_83(Triple.of(1, 83, 0)),
    MC_1_84(Triple.of(1, 84, 0)),
    MC_1_85(Triple.of(1, 85, 0)),
    MC_1_86(Triple.of(1, 86, 0)),
    MC_1_87(Triple.of(1, 87, 0)),
    MC_1_88(Triple.of(1, 88, 0)),
    MC_1_89(Triple.of(1, 89, 0)),
    MC_1_90(Triple.of(1, 90, 0)),
    MC_1_91(Triple.of(1, 91, 0)),
    MC_1_92(Triple.of(1, 92, 0)),
    MC_1_93(Triple.of(1, 93, 0)),
    MC_1_94(Triple.of(1, 94, 0)),
    MC_1_95(Triple.of(1, 95, 0)),
    MC_1_96(Triple.of(1, 96, 0)),
    MC_1_97(Triple.of(1, 97, 0)),
    MC_1_98(Triple.of(1, 98, 0)),
    MC_1_99(Triple.of(1, 99, 0));

    /**
     * Contains the current version of the server.
     */
    public static final ServerVersion CURRENT_VERSION;
    private static final Pattern VERSION_PATTERN;

    static {
        VERSION_PATTERN = Pattern.compile("^([0-9]{1,3})\\.([0-9]{1,3})(?:\\.([0-9]{1,3}))?");
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
        var version = extractVersion();

        if (!version.isPresent()) {
            return MC_UNKOWN;
        }

        for (var value : values()) {
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
        var matcher = VERSION_PATTERN.matcher(Bukkit.getServer().getBukkitVersion());

        if (matcher.find()) {
            var major = Integer.parseInt(matcher.group(1));
            var minor = Integer.parseInt(matcher.group(2));
            var patch = Integer.parseInt(matcher.group(3) == null ? "0" : matcher.group(3));
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
        var currentMajor = current.version.first;
        var oldestMajor = oldest.version.first;
        var newestMajor = newest.version.first;
        var currentMinor = current.version.second;
        var oldestMinor = oldest.version.second;
        var newestMinor = newest.version.second;
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
