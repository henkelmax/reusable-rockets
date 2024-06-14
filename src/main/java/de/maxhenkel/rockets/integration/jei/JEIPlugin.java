package de.maxhenkel.rockets.integration.jei;

import de.maxhenkel.rockets.Main;
import de.maxhenkel.rockets.recipe.RefuelRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Main.MODID, "reusable_rockets");
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        IExtendableCraftingRecipeCategory craftingCategory = registration.getCraftingCategory();
        craftingCategory.addExtension(RefuelRecipe.class, new RefuelExtension<>());
    }

}