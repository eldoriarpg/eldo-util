package de.eldoria.eldoutilities.permissions;

import de.eldoria.eldoutilities.utils.Parser;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;

public final class PermUtil {
    private PermUtil() {
    }

    /**
     * Find a permission which indicates a number in the format {@code permission.node.<number>}.
     *
     * @param player       player to check
     * @param prefix       prefix of permission. E.g. permission.node.
     * @param defaultValue default value which should be returned when no value is present
     *                     This will also be the lowest possible returned value.
     * @return highest permission value or default value
     */
    public static int findHighestIntPermission(Player player, String prefix, int defaultValue) {
        var permission = findPermissions(player, prefix, true, (string) -> {
            var optionalInt = Parser.parseInt(string);
            return optionalInt.orElse(null);
        });

        var max = defaultValue;
        for (int num : permission) {
            max = Math.max(max, num);
        }
        return max;
    }

    /**
     * Find a permission which indicates a number in the format {@code permission.node.<number>}.
     *
     * @param player       player to check
     * @param prefix       prefix of permission. E.g. permission.node.
     * @param defaultValue default value which should be returned when no value is present
     *                     This will also be the lowest possible returned value.
     * @return highest permission value or default value
     */
    public static double findHighestDoublePermission(Player player, String prefix, double defaultValue) {
        var permission = findPermissions(player, prefix, true, (string) -> {
            var optionalInt = Parser.parseDouble(string);
            return optionalInt.orElse(null);
        });

        var max = defaultValue;
        for (double num : permission) {
            max = Math.max(max, num);
        }
        return max;
    }

    /**
     * Finds all permissions which start with the provided prefix.
     *
     * @param player   player to check
     * @param prefix   prefix of permission. E.g. permission.node.
     * @param truncate if true the prefix will be removed.
     * @return collection with all matchin permissions
     */
    public static Collection<String> findPermissions(Player player, String prefix, boolean truncate) {
        return findPermissions(player, prefix, truncate, s -> s);
    }

    /**
     * Finds all permissions which start with the provided prefix and maps them to the requested value.
     *
     * @param player   player to check
     * @param prefix   prefix of permission. E.g. permission.node.
     * @param truncate if true the prefix will be removed before parsing.
     * @param parse    parse the permission to the requested value. Return null to remove permission
     * @param <T>      requested value
     * @return collection which contains all matchin permission. Contains no null values.
     */
    public static <T> Collection<@NotNull T> findPermissions(Player player, String prefix, boolean truncate, Function<String, T> parse) {
        var permissions = player.getEffectivePermissions();

        Set<T> matches = new HashSet<>();

        for (var permission : permissions) {
            if (!permission.getValue()) continue;
            var perm = permission.getPermission();
            if (perm.toLowerCase().startsWith(prefix)) {
                if (truncate) {
                    perm = perm.replace(prefix, "");
                }
                matches.add(parse.apply(perm));
            }
        }
        matches.removeIf(Objects::isNull);
        return matches;
    }
}
