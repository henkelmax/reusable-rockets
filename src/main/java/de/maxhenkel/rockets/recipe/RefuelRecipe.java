package de.maxhenkel.rockets.recipe;

import com.google.gson.JsonObject;
import de.maxhenkel.rockets.Main;
import de.maxhenkel.rockets.item.ItemReusableRocket;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;

public class RefuelRecipe implements ICraftingRecipe, net.minecraftforge.common.crafting.IShapedRecipe<CraftingInventory> {

    private ResourceLocation id;
    private ItemStack rocket;
    private Ingredient fuel;

    public RefuelRecipe(ResourceLocation id, ItemStack rocket, Ingredient fuel) {
        this.id = id;
        this.rocket = rocket;
        this.fuel = fuel;
    }

    @Override
    public int getRecipeWidth() {
        return 1;
    }

    @Override
    public int getRecipeHeight() {
        return 1;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(rocket), fuel);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        return craft(inv) != null;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
        CraftingResult craft = craft(inv);
        if (craft == null) {
            return null;
        }
        return craft.remaining;
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        CraftingResult craft = craft(inv);
        if (craft == null) {
            return null;
        }
        return craft.result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width > 1 && height > 1;
    }

    @Override
    public ItemStack getResultItem() {
        return rocket;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public ItemStack getRocket() {
        return rocket;
    }

    public Ingredient getFuel() {
        return fuel;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return Main.CRAFTING_REFUEL;
    }

    public static class RecipeRefuelSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<RefuelRecipe> {

        public RecipeRefuelSerializer() {

        }

        @Override
        public RefuelRecipe fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            return new RefuelRecipe(resourceLocation, ShapedRecipe.itemFromJson(jsonObject.getAsJsonObject("rocket")), Ingredient.fromJson(jsonObject.getAsJsonObject("fuel")));
        }

        @Override
        public RefuelRecipe fromNetwork(ResourceLocation resourceLocation, PacketBuffer packetBuffer) {
            return new RefuelRecipe(packetBuffer.readResourceLocation(), packetBuffer.readItem(), Ingredient.fromNetwork(packetBuffer));
        }

        @Override
        public void toNetwork(PacketBuffer packetBuffer, RefuelRecipe recipe) {
            packetBuffer.writeResourceLocation(recipe.getId());
            packetBuffer.writeItem(recipe.rocket);
            recipe.fuel.toNetwork(packetBuffer);
        }
    }

    protected CraftingResult craft(CraftingInventory inv) {
        ItemStack rocket = null;
        List<Integer> gunpowderSlotIndices = new ArrayList<>();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() instanceof ItemReusableRocket) {
                if (rocket != null) {
                    return null;
                }
                rocket = stack;
            } else if (stack.getItem().is(Main.ROCKET_FUEL)) {
                gunpowderSlotIndices.add(i);
            }
        }

        if (rocket == null) {
            return null;
        }

        ItemReusableRocket r = (ItemReusableRocket) rocket.getItem();
        int usesLeft = r.getUsesLeft(rocket);
        int maxUses = r.getMaxUses();

        if (gunpowderSlotIndices.size() <= 0 || usesLeft >= maxUses) {
            return null;
        }

        ItemStack rocketOut = rocket.copy();

        int count = Math.min(maxUses - usesLeft, gunpowderSlotIndices.size());

        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        if (count < gunpowderSlotIndices.size()) {
            int reAddCount = gunpowderSlotIndices.size() - count;
            for (Integer index : gunpowderSlotIndices) {
                if (reAddCount <= 0) {
                    break;
                }
                ItemStack gp = inv.getItem(index).copy();
                gp.setCount(1);
                remaining.set(index, gp);
                reAddCount--;
            }
        }


        r.setUsesLeft(rocketOut, usesLeft + count);

        return new CraftingResult(rocketOut, remaining);
    }

    private static class CraftingResult {
        public final ItemStack result;
        public final NonNullList<ItemStack> remaining;

        public CraftingResult(ItemStack result, NonNullList<ItemStack> remaining) {
            this.result = result;
            this.remaining = remaining;
        }
    }
}
