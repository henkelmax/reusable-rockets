package de.maxhenkel.rockets.integration.jei;

import de.maxhenkel.rockets.item.ItemReusableRocket;
import de.maxhenkel.rockets.recipe.RefuelRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Arrays;
import java.util.List;

public class RefuelExtension<T extends RefuelRecipe> implements ICraftingCategoryExtension<T> {

    @Override
    public void setRecipe(RecipeHolder<T> recipeHolder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        T recipe = recipeHolder.value();
        ItemStack in = recipe.getRocket().copy();
        if (!(in.getItem() instanceof ItemReusableRocket rocket)) {
            return;
        }
        rocket.setUsesLeft(in, rocket.getMaxUses() / 2);

        ItemStack out1 = in.copy();
        rocket.setUsesLeft(out1, Math.min(rocket.getUsesLeft(in) + 8, rocket.getMaxUses()));

        List<ItemStack> fuels = recipe.getFuel().getValues().stream().map(Holder::value).map(ItemStack::new).toList();
        craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, Arrays.asList(
                fuels, fuels, fuels,
                fuels, List.of(in), fuels,
                fuels, fuels, fuels
        ), 0, 0);
        craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, List.of(out1));
    }
}
