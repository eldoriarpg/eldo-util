package de.eldoria.eldoutilities.builder;

import de.eldoria.eldoutilities.utils.AttributeUtil;
import de.eldoria.eldoutilities.utils.EMath;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A class to build entities with a builder pattern.
 *
 * @since 1.1.0
 */
@SuppressWarnings("unused")
public final class EntityBuilder {
    private final LivingEntity entity;

    private EntityBuilder(LivingEntity entity) {
        this.entity = entity;
    }

    /**
     * Returns a new EntityBuilder with an entity at the requested position.
     *
     * @param entity entity to wrap
     * @return new EntityBuilder instance with entity
     */
    public static EntityBuilder of(LivingEntity entity) {
        return new EntityBuilder(entity);
    }

    /**
     * Returns a new EntityBuilder with an entity at the requested position.
     *
     * @param entity entity type to spawn
     * @param location location to spawn
     * @return new EntityBuilder instance with spawned entity
     */
    public static EntityBuilder of(EntityType entity, Location location) {
        return new EntityBuilder((LivingEntity) location.getWorld().spawnEntity(location, entity));
    }

    /**
     * Add a passenger to the vehicle.
     *
     * @param passengers One or more passengers to add
     * @return builder instance
     * @since 1.1.0
     */
    public EntityBuilder withPassenger(@NotNull Entity... passengers) {
        for (Entity passenger : passengers) {
            entity.addPassenger(passenger);
        }
        return this;
    }

    /**
     * Add a tag to this entity.
     * <br>
     * Entities can have no more than 1024 tags.
     *
     * @param tag the tag to add
     * @return builder instance
     * @since 1.1.0
     */
    public EntityBuilder withScoreboardTag(@NotNull String tag) {
        entity.addScoreboardTag(tag);
        return this;
    }

    /**
     * Sets this entity's velocity
     *
     * @param velocity New velocity to travel with
     * @return builder instance
     * @since 1.1.0
     */
    public EntityBuilder withVelocity(@NotNull Vector velocity) {
        entity.setVelocity(velocity);
        return this;
    }

    /**
     * Sets the entity's rotation.
     * <p>
     * Note that if the entity is affected by AI, it may override this rotation.
     *
     * @param yaw   the yaw
     * @param pitch the pitch
     * @return builder instance
     * @throws UnsupportedOperationException if used for players
     * @since 1.1.0
     */
    public EntityBuilder withRotation(float yaw, float pitch) {
        entity.setRotation(yaw, pitch);
        return this;
    }

    /**
     * Sets the entity's current fire ticks (ticks before the entity stops
     * being on fire).
     *
     * @param ticks Current ticks remaining
     * @return builder instance
     */
    public EntityBuilder withFireTicks(int ticks) {
        entity.setFireTicks(ticks);
        return this;
    }

    /**
     * Sets whether or not the entity gets persisted.
     *
     * @param persistent the persistence status
     * @return builder instance
     * @see Entity#isPersistent()
     */
    public EntityBuilder asPersistent(boolean persistent) {
        entity.setPersistent(persistent);
        return this;
    }

    /**
     * Sets the fall distance for this entity
     *
     * @param distance The new distance.
     * @return builder instance
     */
    public EntityBuilder withFallDistance(float distance) {
        entity.setFallDistance(distance);
        return this;
    }

    /**
     * Sets the amount of ticks this entity has lived for.
     * <p>
     * This is the equivalent to "age" in entities. May not be less than one
     * tick.
     *
     * @param value Age of entity
     * @return builder instance
     */
    public EntityBuilder withTicksLived(int value) {
        entity.setTicksLived(value);
        return this;
    }

    /**
     * Sets whether or not to display the mob's custom name client side. The
     * name will be displayed above the mob similarly to a player.
     * <p>
     * This value has no effect on players, they will always display their
     * name.
     *
     * @param flag custom name or not
     * @return builder instance
     */
    public EntityBuilder withVisibleCustomName(boolean flag) {
        entity.setCustomNameVisible(flag);
        return this;
    }

    /**
     * Sets whether or not to display the mob's custom name client side. The
     * name will be displayed above the mob similarly to a player.
     * <p>
     * This value has no effect on players, they will always display their
     * name.
     *
     * @param flag custom name or not
     * @return builder instance
     */
    public EntityBuilder withGlowing(boolean flag) {
        entity.setGlowing(flag);
        return this;
    }

    /**
     * Sets whether the entity is invulnerable or not.
     * <p>
     * When an entity is invulnerable it can only be damaged by players in
     * creative mode.
     *
     * @param flag if the entity is invulnerable
     * @return builder instance
     */
    public EntityBuilder asInvulnerable(boolean flag) {
        entity.setInvulnerable(flag);
        return this;
    }

    /**
     * Sets whether the entity is silent or not.
     * <p>
     * When an entity is silent it will not produce any sound.
     *
     * @param flag if the entity is silent
     * @return builder instance
     */
    public EntityBuilder asSilent(boolean flag) {
        entity.setSilent(flag);
        return this;
    }

    /**
     * Sets whether gravity applies to this entity.
     *
     * @param gravity whether gravity should apply
     * @return builder instance
     */
    public EntityBuilder withGravity(boolean gravity) {
        entity.setGravity(gravity);
        return this;
    }

    /**
     * Sets a metadata value in the implementing object's metadata store.
     *
     * @param metadataKey      A unique key to identify this metadata.
     * @param newMetadataValue The metadata value to apply.
     * @return builder instance
     * @throws IllegalArgumentException If value is null, or the owning plugin
     *                                  is null
     */
    public EntityBuilder withMetadata(@NotNull String metadataKey, @NotNull MetadataValue newMetadataValue) {
        entity.setMetadata(metadataKey, newMetadataValue);
        return this;
    }

    /**
     * Sets a custom name on a mob or block. This name will be used in death
     * messages and can be sent to the client as a nameplate over the mob.
     * <p>
     * Setting the name to null or an empty string will clear it.
     * <p>
     * This value has no effect on players, they will always use their real
     * name.
     *
     * @param name the name to set
     * @return builder instance
     */
    public EntityBuilder withCustomName(@Nullable String name) {
        entity.setCustomName(name);
        return this;
    }

    /**
     * Applies changes to the persistent data container of the entity
     *
     * @param change conumer for data container
     * @return builder instance
     */
    public EntityBuilder withNBT(Consumer<PersistentDataContainer> change) {
        change.accept(entity.getPersistentDataContainer());
        return this;
    }

    /**
     * Sets the {@link AttributeInstance#getBaseValue()} to the value which is required to get the target value on
     * {@link AttributeInstance#getValue()} for the requested {@link Attribute}
     *
     * @param attribute attribute type to set if present
     * @param value     target value which should be retrieved via {@link AttributeInstance#getValue()}
     * @return builder instance
     */
    public EntityBuilder withAttribute(Attribute attribute, double value) {
        AttributeUtil.setAttributeValue(entity, attribute, value);
        return this;
    }

    /**
     * Adds the given {@link PotionEffect} to the living entity.
     *
     * @param effect PotionEffect to be added
     * @return builder instance
     */
    public EntityBuilder withPotionEffect(@NotNull PotionEffect effect) {
        entity.addPotionEffect(effect);
        return this;
    }

    /**
     * Sets whether an entity will have AI.
     * <p>
     * The entity will be completely unable to move if it has no AI.
     *
     * @param ai whether the mob will have AI or not.
     * @return builder instance
     */
    public EntityBuilder withAI(boolean ai) {
        entity.setAI(ai);
        return this;
    }

    /**
     * Applies a consumer on this entity
     *
     * @param entityConsumer consumer
     * @return builder instance
     */
    public EntityBuilder with(Consumer<LivingEntity> entityConsumer) {
        entityConsumer.accept(entity);
        return this;
    }

    /**
     * Applies a consumer on this entity after casting it to the requested type.
     *
     * @param clazz          class of entity
     * @param entityConsumer consumer
     * @param <T>            type of entity
     * @return builder instance
     */
    public <T extends LivingEntity> EntityBuilder with(Class<T> clazz, Consumer<T> entityConsumer) {
        entityConsumer.accept((T) entity);
        return this;
    }

    /**
     * Set if this entity will be subject to collisions with other entities.
     * <p>
     * Exemptions to this rule can be managed with
     * {@link LivingEntity#getCollidableExemptions()}
     *
     * @param collidable collision status
     * @return builder instance
     */
    public EntityBuilder setCollidable(boolean collidable) {
        entity.setCollidable(collidable);
        return this;
    }

    /**
     * Sets whether the entity is invisible or not.
     *
     * @param invisible If the entity is invisible
     * @return builder instance
     */
    public EntityBuilder setInvisible(boolean invisible) {
        entity.setInvisible(invisible);
        return this;
    }

    /**
     * Sets the entity's health from 0 to {@link LivingEntity#getMaxHealth()}, where 0 is
     * dead.
     *
     * @param health New health represented from 0 to max
     * @return builder instance
     */
    public EntityBuilder withHealth(double health) {
        entity.setHealth(EMath.clamp(0, AttributeUtil.getAttributeValue(entity, Attribute.GENERIC_MAX_HEALTH), health));
        return this;
    }

    /**
     * Sets the entity's absorption amount.
     *
     * @param amount new absorption amount from 0
     * @return builder instance
     * @throws IllegalArgumentException thrown if health is {@literal < 0} or
     *                                  non-finite.
     */
    public EntityBuilder withAbsorptionAmount(double amount) {
        entity.setAbsorptionAmount(amount);
        return this;
    }

    /**
     * Returns the builded entity and casts it to the requested type.
     *
     * @param <T> type of entity
     * @return entity instance
     */
    @SuppressWarnings("unchecked")
    public <T extends LivingEntity> T build() {
        return (T) entity;
    }
}
