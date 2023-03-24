/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public record Version(String version, List<Integer> nums) implements Comparable<Version> {
    public static final int MAJOR = 0;
    public static final int MINOR = 1;
    public static final int PATCH = 2;
    private static final Pattern NUMBER = Pattern.compile("([0-9]+)");

    public static Version parse(String version) {
        List<Integer> nums = new ArrayList<>();

        Matcher matcher = NUMBER.matcher(version);
        while (matcher.find()) {
            nums.add(Integer.parseInt(matcher.group(1)));
        }

        return new Version(version, nums);
    }

    public static Version of(Integer... nums) {
        return of(Arrays.stream(nums).toList());
    }

    public static Version of(List<Integer> nums) {
        return new Version(nums.stream().map(String::valueOf).collect(Collectors.joining(".")), nums);
    }

    @Override
    public List<Integer> nums() {
        return Collections.unmodifiableList(nums);
    }

    public boolean isOlder(Version version) {
        return compareTo(version) < 0;
    }

    public boolean isOlderOrEqual(Version version) {
        return compareTo(version) <= 0;
    }

    public boolean isNewer(Version version) {
        return compareTo(version) > 0;
    }

    public boolean isNewerOrEqual(Version version) {
        return compareTo(version) >= 0;
    }

    public boolean isEqual(Version version) {
        return compareTo(version) == 0;
    }

    public boolean isBetweenInclusive(Version lower, Version upper) {
        return isNewer(lower) && isOlder(upper);
    }

    /**
     * Compares the version to a lower and upper version
     *
     * @param lower lower version (inclusive)
     * @param upper upper version (exclusive)
     * @return true if the version is between
     */
    public boolean isBetween(Version lower, Version upper) {
        return isNewerOrEqual(lower) && isOlder(upper);
    }

    public Comparator<String> comparator() {
        return Comparator.comparing(Version::parse);
    }

    public Version trim(int num) {
        return Version.of(nums.subList(0, Math.min(nums.size(), num)).toArray(Integer[]::new));
    }

    public Version set(int index, int value) {
        var newNums = new ArrayList<>(nums);
        if (newNums.size() < index) {
            newNums.set(index, value);
        } else {
            newNums.add(value);
        }
        return Version.of(newNums);
    }

    public Version increase(int index, int value) {
        var newNums = new ArrayList<>(nums);
        newNums.set(index, newNums.get(index) + value);
        return Version.of(newNums);
    }

    public Version decrease(int index, int value) {
        var newNums = new ArrayList<>(nums);
        newNums.set(index, Math.max(newNums.get(index) - value, 0));
        return Version.of(newNums);
    }

    @Override
    public String toString() {
        return version;
    }

    public int size() {
        return nums.size();
    }

    @Override
    public int compareTo(@NotNull Version version) {
        int numbers = Math.max(version.nums().size(), nums().size());
        for (int i = 0; i < numbers; i++) {
            int compare = Integer.compare(nums().size() > i ? nums().get(i) : 0,
                    version.nums().size() > i ? version.nums().get(i) : 0);
            if (compare != 0) return compare;
        }
        return 0;
    }
}
