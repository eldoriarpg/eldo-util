/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.builder;

import de.eldoria.eldoutilities.utils.ObjUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A class which allows the creation of ItemStacks with a builder like pattern.
 *
 * @since 1.1.0
 */
@SuppressWarnings("unused")
public final class ItemStackBuilder {
    private final ItemStack itemStack;

    private ItemStackBuilder(Material material, int amount) {
        itemStack = new ItemStack(material, amount);
    }

    public ItemStackBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Creates a new item stack builder
     *
     * @param material material of item stack
     * @return builder instance
     */
    public static ItemStackBuilder of(Material material) {
        return new ItemStackBuilder(material, 1);
    }

    /**
     * Creates a new item stack builder
     *
     * @param material material of item stack
     * @param amount   size of item stack.
     * @return builder instance
     */
    public static ItemStackBuilder of(Material material, int amount) {
        return new ItemStackBuilder(material, amount);
    }

    /**
     * Load a item stack into a item stack builder.
     * <p>
     * The item stack will be cloned to provide immutability.
     *
     * @param stack item stack to load
     * @return builder instance
     */
    public static ItemStackBuilder of(ItemStack stack) {
        return of(stack, true);
    }

    /**
     * Load a item stack into a item stack builder.
     *
     * @param stack item stack to load
     * @param clone true if the item should be cloned
     * @return builder instance
     */
    public static ItemStackBuilder of(ItemStack stack, boolean clone) {
        return new ItemStackBuilder(clone ? stack.clone() : stack);
    }

    /**
     * Applies an item meta consumer on the item meta, if a item meta is present.
     *
     * @param itemMetaConsumer consumer for non null item meta.
     * @return builder instance
     */
    public ItemStackBuilder withMetaValue(Consumer<@NotNull ItemMeta> itemMetaConsumer) {
        var meta = itemStack.getItemMeta();
        ObjUtil.nonNull(meta, itemMetaConsumer);
        itemStack.setItemMeta(meta);
        return this;
    }

    /**
     * Clone the underlying itemstack and return a cloned instance.
     *
     * @return new item stack instance
     */
    public ItemStack build() {
        return itemStack.clone();
    }

    /**
     * Adds the specified enchantments to this item stack.
     * <p>
     * This method is the same as calling {@link
     * #withEnchantment(Enchantment, int)} for each
     * element of the map.
     *
     * @param enchantments Enchantments to add
     * @return builder instance
     * @throws IllegalArgumentException if the specified enchantments is null
     * @throws IllegalArgumentException if any specific enchantment or level
     *                                  is null. <b>Warning</b>: Some enchantments may be added before this
     *                                  exception is thrown.
     */
    public ItemStackBuilder withEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
        itemStack.addEnchantments(enchantments);
        return this;
    }

    /**
     * Adds the specified {@link Enchantment} to this item stack.
     * <p>
     * If this item stack already contained the given enchantment (at any
     * level), it will be replaced.
     *
     * @param ench  Enchantment to add
     * @param level Level of the enchantment
     * @return builder instance
     * @throws IllegalArgumentException if enchantment null, or enchantment is
     *                                  not applicable
     */
    public ItemStackBuilder withEnchantment(@NotNull Enchantment ench, int level) {
        itemStack.addEnchantment(ench, level);
        return this;
    }

    /**
     * Adds the specified enchantments to this item stack in an unsafe manner.
     * <p>
     * This method is the same as calling {@link
     * #withUnsafeEnchantment(Enchantment, int)} for
     * each element of the map.
     *
     * @param enchantments Enchantments to add
     * @return builder instance
     */
    public ItemStackBuilder withUnsafeEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
        itemStack.addUnsafeEnchantments(enchantments);
        return this;
    }

    /**
     * Adds the specified {@link Enchantment} to this item stack.
     * <p>
     * If this item stack already contained the given enchantment (at any
     * level), it will be replaced.
     * <p>
     * This method is unsafe and will ignore level restrictions or item type.
     * Use at your own discretion.
     *
     * @param ench  Enchantment to add
     * @param level Level of the enchantment
     * @return builder instance
     */
    public ItemStackBuilder withUnsafeEnchantment(@NotNull Enchantment ench, int level) {
        itemStack.addUnsafeEnchantment(ench, level);
        return this;
    }

    /**
     * Sets the amount of items in this stack
     *
     * @param amount New amount of items in this stack
     * @return builder instance
     */
    public ItemStackBuilder ofAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Sets the display name.
     *
     * @param name the name to set
     * @return builder instance
     */
    public ItemStackBuilder withDisplayName(@Nullable String name) {
        withMetaValue(m -> m.setDisplayName(name));
        return this;
    }

    /**
     * Sets the localized name.
     *
     * @param name the name to set
     * @return builder instance
     */
    public ItemStackBuilder withLocalizedName(@Nullable String name) {
        withMetaValue(m -> m.setLocalizedName(name));
        return this;
    }

    /**
     * Sets the lore for this item.
     *
     * @param lore the lore that will be set
     * @return builder instance
     */
    public ItemStackBuilder withLore(@Nullable List<String> lore) {
        withMetaValue(m -> m.setLore(lore));
        return this;
    }

    /**
     * Sets the lore for this item.
     *
     * @param lore the lore that will be set
     * @return builder instance
     */
    public ItemStackBuilder withLore(String... lore) {
        return withLore(Arrays.asList(lore));
    }

    /**
     * Sets the custom model data.
     * <p>
     * CustomModelData is an integer that may be associated client side with a
     * custom item model.
     *
     * @param data the data to set, or null to clear
     * @return builder instance
     */
    public ItemStackBuilder withCustomModelData(@Nullable Integer data) {
        withMetaValue(m -> m.setCustomModelData(data));
        return this;
    }

    /**
     * Adds the specified enchantment to this item meta.
     *
     * @param ench                   Enchantment to add
     * @param level                  Level for the enchantment
     * @param ignoreLevelRestriction this indicates the enchantment should be
     *                               applied, ignoring the level limit
     * @return builder instance
     */
    public ItemStackBuilder withEnchant(@NotNull Enchantment ench, int level, boolean ignoreLevelRestriction) {
        withMetaValue(m -> m.addEnchant(ench, level, ignoreLevelRestriction));
        return this;
    }

    /**
     * Set itemflags which should be ignored when rendering a ItemStack in the Client. This Method does silently ignore double set itemFlags.
     *
     * @param itemFlags The hideflags which shouldn't be rendered
     * @return builder instance
     */
    public ItemStackBuilder withItemFlags(@NotNull ItemFlag... itemFlags) {
        withMetaValue(m -> m.addItemFlags(itemFlags));
        return this;
    }

    /**
     * Sets the unbreakable tag. An unbreakable item will not lose durability.
     *
     * @return builder instance
     */
    public ItemStackBuilder asUnbreakable() {
        withMetaValue(m -> m.setUnbreakable(true));
        return this;
    }

    /**
     * Sets the breakable tag. An breakable item will lose durability.
     * This is the default value.
     *
     * @return builder instance
     */
    public ItemStackBuilder asBreakable() {
        withMetaValue(m -> m.setUnbreakable(false));
        return this;
    }

    /**
     * Add an Attribute and it's Modifier.
     * AttributeModifiers can now support {@link EquipmentSlot}s.
     * If not set, the {@link AttributeModifier} will be active in ALL slots.
     * <br>
     * Two {@link AttributeModifier}s that have the same {@link java.util.UUID}
     * cannot exist on the same Attribute.
     *
     * @param attribute the {@link Attribute} to modify
     * @param modifier  the {@link AttributeModifier} specifying the modification
     * @return builder instance
     * @throws NullPointerException     if Attribute is null
     * @throws NullPointerException     if AttributeModifier is null
     * @throws IllegalArgumentException if AttributeModifier already exists
     */
    public ItemStackBuilder withAttributeModifier(@NotNull Attribute attribute, @NotNull AttributeModifier modifier) {
        withMetaValue(m -> m.addAttributeModifier(attribute, modifier));
        return this;
    }

    /**
     * Sets the damage
     *
     * @param damage item damage
     * @return builder instance
     */
    public ItemStackBuilder withDurability(int damage) {
        withMetaValue(m -> {
            if (m instanceof Damageable) {
                ((Damageable) m).setDamage(damage);
            }
        });
        return this;
    }

    /**
     * Applies a consumer on the {@link ItemMeta#getPersistentDataContainer()}.
     * The consumer will only be executed if a item meta is present
     *
     * @param dataConsumer consumer for data container
     * @return builder instance
     */
    public ItemStackBuilder withNBTData(Consumer<@NotNull PersistentDataContainer> dataConsumer) {
        withMetaValue(m -> dataConsumer.accept(m.getPersistentDataContainer()));
        return this;
    }

    /**
     * Casts the item meta to the defined class if possible.
     * <p>
     * Will apply the consumer on the meta if the cast was successful.
     *
     * @param clazz    class of item meta
     * @param consumer consumer for item meta
     * @param <T>      type of meta
     * @return builder instance
     */
    @SuppressWarnings("unchecked")
    public <T extends ItemMeta> ItemStackBuilder withMetaValue(Class<T> clazz, Consumer<@NotNull T> consumer) {
        var itemMeta = itemStack.getItemMeta();
        ObjUtil.nonNull(itemMeta, m -> {
            if (clazz.isAssignableFrom(m.getClass())) {
                consumer.accept((T) m);
            }
        });
        itemStack.setItemMeta(itemMeta);
        return this;
    }
}
