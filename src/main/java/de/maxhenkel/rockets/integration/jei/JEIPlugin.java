package de.maxhenkel.rockets.integration.jei;

import de.maxhenkel.rockets.Main;
import de.maxhenkel.rockets.recipe.RefuelRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.ResourceLocation;

@mezz.jei.api.JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Main.MODID, "reusable_rockets");
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        IExtendableRecipeCategory<ICraftingRecipe, ICraftingCategoryExtension> craftingCategory = registration.getCraftingCategory();
        craftingCategory.addCategoryExtension(RefuelRecipe.class, RefuelExtension::new);
    }

}