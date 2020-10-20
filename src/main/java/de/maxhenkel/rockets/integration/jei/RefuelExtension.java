package de.maxhenkel.rockets.integration.jei;

import de.maxhenkel.rockets.recipe.RefuelRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICustomCraftingCategoryExtension;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RefuelExtension<T extends RefuelRecipe> implements ICustomCraftingCategoryExtension {

    private final T recipe;

    public RefuelExtension(T recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = layout.getItemStacks();

        ItemStack in = recipe.getRocket().copy();
        in.setDamage(in.getMaxDamage() / 2);

        ItemStack out = in.copy();
        out.setDamage(Math.max(0, out.getDamage() - 8));

        List<ItemStack> fuels = Arrays.asList(recipe.getFuel().getMatchingStacks());

        guiItemStacks.set(0, out);
        guiItemStacks.set(1, fuels);
        guiItemStacks.set(2, fuels);
        guiItemStacks.set(3, fuels);
        guiItemStacks.set(4, fuels);
        guiItemStacks.set(5, in);
        guiItemStacks.set(6, fuels);
        guiItemStacks.set(7, fuels);
        guiItemStacks.set(8, fuels);
        guiItemStacks.set(9, fuels);
    }

    @Override
    public void setIngredients(IIngredients ingredients) {
        List<ItemStack> inputs = new ArrayList<>(Arrays.asList(recipe.getFuel().getMatchingStacks()));
        inputs.add(recipe.getRocket());

        ingredients.setInputs(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getRocket());
    }
}
