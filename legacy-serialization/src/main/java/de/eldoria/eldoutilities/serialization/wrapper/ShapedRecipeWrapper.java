/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) EldoriaRPG Team and Contributor
 */

package de.eldoria.eldoutilities.serialization.wrapper;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShapedRecipeWrapper implements ConfigurationSerializable {
    private final ItemStack result;
    private final String group;
    private final NamespacedKey key;
    private final List<String> shape;
    private final Map<Character, RecipeChoiceWrapper> choices;

    public ShapedRecipeWrapper(ShapedRecipe recipe) {
        result = recipe.getResult();
        group = recipe.getGroup();
        key = recipe.getKey();
        shape = Arrays.asList(recipe.getShape());
        choices = new HashMap<>();
        for (var entry : recipe.getChoiceMap().entrySet()) {
            choices.put(entry.getKey(), RecipeChoiceWrapper.wrap(entry.getValue()));
        }
    }

    public ShapedRecipeWrapper(Map<String, Object> objectMap) {
        var map = SerializationUtil.mapOf(objectMap);
        result = map.getValue("result");
        group = map.getValue("group");
        key = new NamespacedKey((String) map.getValue("namespace"), map.getValue("key"));
        shape = map.getValue("shape");
        choices = map.getMap("choices", (k, v) -> k.charAt(0));
    }

    public ShapedRecipe getRecipe() {
        var recipe = new ShapedRecipe(key, result);
        recipe.shape(shape.toArray(new String[0]));
        recipe.setGroup(group);
        for (var entry : choices.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue().toChoice());
        }
        return recipe;
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .add("result", result)
                .add("group", group)
                .add("namespace", key.getNamespace())
                .add("key", key.getKey())
                .addMap("choices", choices, (k, v) -> k.toString())
                .build();
    }

    public interface RecipeChoiceWrapper extends ConfigurationSerializable {
        static RecipeChoiceWrapper wrap(RecipeChoice choice) {
            if (choice instanceof RecipeChoice.MaterialChoice) {
                return new MaterialChoiceWrapper((RecipeChoice.MaterialChoice) choice);
            }
            if (choice instanceof RecipeChoice.ExactChoice) {
                return new ExactChoiceWrapper((RecipeChoice.ExactChoice) choice);
            }
            throw new IllegalStateException("Unknown subclass " + choice.getClass().getName());
        }

        RecipeChoice toChoice();

        class MaterialChoiceWrapper implements RecipeChoiceWrapper {
            List<Material> materials;

            public MaterialChoiceWrapper(RecipeChoice.MaterialChoice choice) {
                materials = choice.getChoices();
            }

            public MaterialChoiceWrapper(Map<String, Object> objectMap) {
                var map = SerializationUtil.mapOf(objectMap);
                materials = map.getValueOrDefault("materials", Collections.emptyList(), Material.class);
            }


            @Override
            @NotNull
            public Map<String, Object> serialize() {
                return SerializationUtil.newBuilder()
                        .build();
            }

            @Override
            public RecipeChoice toChoice() {
                return new RecipeChoice.MaterialChoice(materials);
            }
        }

        class ExactChoiceWrapper implements RecipeChoiceWrapper {
            List<ItemStack> choices;

            public ExactChoiceWrapper(RecipeChoice.ExactChoice choice) {
                choices = choice.getChoices();
            }

            public ExactChoiceWrapper(Map<String, Object> objectMap) {
                var map = SerializationUtil.mapOf(objectMap);
                choices = map.getValue("choices");
            }

            @Override
            public RecipeChoice toChoice() {
                return new RecipeChoice.ExactChoice(choices);
            }

            @Override
            @NotNull
            public Map<String, Object> serialize() {
                return SerializationUtil.newBuilder()
                        .add("choices", choices)
                        .build();
            }
        }
    }
}
