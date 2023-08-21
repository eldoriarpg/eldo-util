/*
 *     SPDX-License-Identifier: LGPL-3.0-or-later
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.utils;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Class which holds utility methods to handle and manage attributes.
 *
 * @since 1.1.0
 */
public final class AttributeUtil {
    private AttributeUtil() {
    }

    /**
     * Sets the {@link AttributeInstance#getBaseValue()} to the value which is required to get the target value on
     * {@link AttributeInstance#getValue()}
     *
     * @param attribute attribute to set
     * @param target    target value which should be retrieved via {@link AttributeInstance#getValue()}
     * @throws NullPointerException when the attribute is null
     * @deprecated Use {@link #setAttributeValue(LivingEntity, Attribute, double)} for better and safer assignment.
     */
    @Deprecated
    public static void setAttributeValue(AttributeInstance attribute, double target) throws NullPointerException {
        attribute.setBaseValue(target / (attribute.getValue() / Math.max(attribute.getBaseValue(), 1)));
    }

    /**
     * Sets the {@link AttributeInstance#getBaseValue()} to the value which is required to get the target value on
     * {@link AttributeInstance#getValue()} for the requested {@link Attribute}
     *
     * @param entity    to set the attribute for
     * @param attribute attribute type to set if present
     * @param target    target value which should be retrieved via {@link AttributeInstance#getValue()}
     */
    public static void setAttributeValue(@NotNull LivingEntity entity, @NotNull Attribute attribute, @NotNull double target) {
        var a = entity.getAttribute(attribute);
        if (a == null) {
            Bukkit.getLogger().log(Level.WARNING, "[EldoUtilities] Attempted to set attribute "
                                                  + attribute + " for " + entity.getType() + ", but Attribute is not present on this type.",
                    new IllegalArgumentException("A not present attribute was requested to change"));
            return;
        }
        setAttributeValue(a, target);
    }

    /**
     * Syncs to attributes to the same value.
     * <p> If the {@link Attribute} is {@link Attribute#GENERIC_MAX_HEALTH} the health of the
     * entity health will be set to the new max health value.
     *
     * @param source    source of the attribute
     * @param target    target of the source attribute value
     * @param attribute attribute to change
     */
    public static void syncAttributeValue(LivingEntity source, LivingEntity target, Attribute attribute) {
        var sourceAttribute = source.getAttribute(attribute);
        var targetAttribute = target.getAttribute(attribute);

        if (sourceAttribute == null) {
            Bukkit.getLogger().log(Level.WARNING, "Attempted to sync attribute "
                                                  + attribute + " between source " + source.getType() + " and target " + target.getType()
                                                  + ", but Attribute is not present on source",
                    new IllegalArgumentException("A not present attribute was requested to change"));
            return;
        }
        if (targetAttribute == null) {
            Bukkit.getLogger().log(Level.WARNING, "Attempted to sync attribute "
                                                  + attribute + " between source " + source.getType() + " and target " + target.getType()
                                                  + ", but Attribute is not present on target",
                    new IllegalArgumentException("A not present attribute was requested to change"));
            return;
        }

        setAttributeValue(target, attribute, sourceAttribute.getValue());

        if (attribute == Attribute.GENERIC_MAX_HEALTH) {
            target.setHealth(targetAttribute.getValue());
        }
    }

    public static double getAttributeValue(LivingEntity entity, Attribute attribute) {
        var entityAttribute = entity.getAttribute(attribute);
        if (entityAttribute == null) {
            return 0;
        }
        return entityAttribute.getValue();
    }
}
