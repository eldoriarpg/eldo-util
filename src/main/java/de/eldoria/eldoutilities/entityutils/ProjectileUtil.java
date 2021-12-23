package de.eldoria.eldoutilities.entityutils;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Util to dtermine the sender of a projectile.
 *
 * @since 1.0.0
 */
public final class ProjectileUtil {
    private ProjectileUtil() {
    }

    /**
     * Check if a entity is a projectile. If the entity is a projectile try to find the projectile sender.
     *
     * @param entity entity
     * @return A {@link ProjectileSender} which is empty if the entity is not a projectile or will hold a block or
     * entity.
     */
    public static ProjectileSender getProjectileSource(Entity entity) {
        // Check if the item is a projectile. If not this is not necessary
        if (!(entity instanceof Projectile)) return new ProjectileSender();

        var projectile = (Projectile) entity;
        var source = projectile.getShooter();

        if (source == null) return new ProjectileSender();

        // Projectile source should normaly be a entity
        if (source instanceof Entity) {
            return new ProjectileSender(((Entity) source));
        }
        // in some cases it could also be a block. Eg. dispenser
        if (source instanceof BlockProjectileSource) {
            var damager = ((BlockProjectileSource) source).getBlock();
            return new ProjectileSender(damager);
        }

        // This should likely never happen
        throw new IllegalArgumentException(source.getClass().getSimpleName() + " is either a block nor an entity");
    }

}
