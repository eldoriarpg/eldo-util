/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) 2021 EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.serialization.util;

import de.eldoria.eldoutilities.builder.EntityBuilder;
import de.eldoria.eldoutilities.serialization.SerializationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Deprecated
public class ArmorStandWrapper implements ConfigurationSerializable {
    // General Entity Data
    private String customName;
    private boolean customNameVisible;
    private double yaw;
    private double pitch;
    private Vector direction;

    // Armor stand data
    private Vector headPose;
    private Vector bodyPose;
    private Vector leftArmPose;
    private Vector rightArmPose;
    private Vector leftLegPose;
    private Vector rightLegPose;
    private ItemStack helmet;
    private ItemStack chestPlate;
    private ItemStack leggins;
    private ItemStack itemInHand;
    private boolean arms;
    private boolean basePlate;
    private boolean marker;
    private boolean small;
    private boolean visible;

    public ArmorStandWrapper(Map<String, Object> objectMap) {
        SerializationUtil.mapOnObject(objectMap, this);
    }

    private ArmorStandWrapper(ArmorStand armorStand) {
        this.customName = armorStand.getCustomName();
        this.customNameVisible = armorStand.isCustomNameVisible();
        this.yaw = armorStand.getLocation().getYaw();
        this.pitch = armorStand.getLocation().getPitch();
        this.direction = armorStand.getLocation().getDirection();

        this.headPose = eulerAngleToVector(armorStand.getHeadPose());
        this.bodyPose = eulerAngleToVector(armorStand.getBodyPose());
        this.leftArmPose = eulerAngleToVector(armorStand.getLeftArmPose());
        this.rightArmPose = eulerAngleToVector(armorStand.getRightArmPose());
        this.leftLegPose = eulerAngleToVector(armorStand.getLeftLegPose());
        this.rightLegPose = eulerAngleToVector(armorStand.getRightLegPose());

        var equipment = armorStand.getEquipment();
        this.helmet = equipment.getHelmet();
        this.chestPlate = equipment.getChestplate();
        this.leggins = equipment.getLeggings();
        this.itemInHand = equipment.getItemInMainHand();

        this.arms = armorStand.hasArms();
        this.basePlate = armorStand.hasBasePlate();
        this.marker = armorStand.isMarker();
        this.small = armorStand.isSmall();
        this.visible = armorStand.isVisible();
    }

    /**
     * Wraps the armor stand into an serializable object.
     *
     * @param armorStand armor stand to wrap
     * @return wrapped armor stand object.
     */
    public static ArmorStandWrapper serialize(ArmorStand armorStand) {
        return new ArmorStandWrapper(armorStand);
    }

    private static Vector eulerAngleToVector(EulerAngle euler) {
        return new Vector(euler.getX(), euler.getY(), euler.getZ());
    }

    private static EulerAngle vectorToEulerAngle(Vector vector) {
        return new EulerAngle(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.objectToMap(this);
    }

    /**
     * Spawns the wrapped armor stand at the requested location
     *
     * @param location location of armor stand
     * @return spawned entity
     */
    public ArmorStand spawn(Location location) {
        return EntityBuilder.of(EntityType.ARMOR_STAND, location)
                .withCustomName(customName)
                .withVisibleCustomName(customNameVisible)
                .with(e -> {
                    e.setRotation((float) yaw, (float) pitch);
                    e.getLocation().setDirection(direction);
                })
                .with(ArmorStand.class, e -> {
                    e.setHeadPose(vectorToEulerAngle(headPose));
                    e.setBodyPose(vectorToEulerAngle(bodyPose));
                    e.setLeftArmPose(vectorToEulerAngle(leftArmPose));
                    e.setRightArmPose(vectorToEulerAngle(rightArmPose));
                    e.setLeftLegPose(vectorToEulerAngle(leftLegPose));
                    e.setRightLegPose(vectorToEulerAngle(rightLegPose));
                })
                .with(e -> {
                    var eq = e.getEquipment();
                    eq.setHelmet(helmet);
                    eq.setChestplate(chestPlate);
                    eq.setLeggings(leggins);
                    eq.setItemInMainHand(itemInHand);
                })
                .with(ArmorStand.class, e -> {
                    e.setArms(arms);
                    e.setBasePlate(basePlate);
                    e.setMarker(marker);
                    e.setSmall(small);
                    e.setVisible(visible);
                })
                .build();
    }
}
