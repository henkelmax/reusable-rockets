package de.maxhenkel.rockets.integration.jei;

import de.maxhenkel.rockets.item.ItemReusableRocket;
import de.maxhenkel.rockets.recipe.RefuelRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class RefuelExtension<T extends RefuelRecipe> implements ICraftingCategoryExtension {

    private final T recipe;

    public RefuelExtension(T recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        ItemStack in = recipe.getRocket().copy();
        if (!(in.getItem() instanceof ItemReusableRocket rocket)) {
            return;
        }
        rocket.setUsesLeft(in, rocket.getMaxUses() / 2);

        ItemStack out1 = in.copy();
        rocket.setUsesLeft(out1, Math.min(rocket.getUsesLeft(in) + 8, rocket.getMaxUses()));

        List<ItemStack> fuels = Arrays.asList(recipe.getFuel().getItems());
        craftingGridHelper.setInputs(builder, VanillaTypes.ITEM, Arrays.asList(
                fuels, fuels, fuels,
                fuels, List.of(in), fuels,
                fuels, fuels, fuels
        ), 0, 0);
        craftingGridHelper.setOutputs(builder, VanillaTypes.ITEM, List.of(out1));
    }

}
