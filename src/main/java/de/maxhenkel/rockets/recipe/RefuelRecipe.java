package de.maxhenkel.rockets.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.maxhenkel.rockets.Main;
import de.maxhenkel.rockets.item.ItemReusableRocket;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.IShapedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RefuelRecipe implements CraftingRecipe, IShapedRecipe<CraftingContainer> {

    private ItemStack rocket;
    private Ingredient fuel;

    public RefuelRecipe(ItemStack rocket, Ingredient fuel) {
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
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return craft(inv) != null;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        CraftingResult craft = craft(inv);
        if (craft == null) {
            return null;
        }
        return craft.remaining;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
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
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return rocket;
    }

    public ItemStack getRocket() {
        return rocket;
    }

    public Ingredient getFuel() {
        return fuel;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Main.CRAFTING_REFUEL.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public static class RecipeRefuelSerializer implements RecipeSerializer<RefuelRecipe> {

        private Codec<RefuelRecipe> codec;

        public RecipeRefuelSerializer() {
            codec = RecordCodecBuilder.create((builder) -> builder
                    .group(
                            BuiltInRegistries.ITEM.byNameCodec().xmap(ItemStack::new, ItemStack::getItem)
                                    .fieldOf("rocket")
                                    .forGetter((recipe) -> recipe.rocket),
                            Ingredient.CODEC_NONEMPTY
                                    .fieldOf("fuel")
                                    .forGetter((recipe) -> recipe.fuel)
                    ).apply(builder, RefuelRecipe::new));
        }

        @Override
        public Codec<RefuelRecipe> codec() {
            return codec;
        }

        @Override
        public @Nullable RefuelRecipe fromNetwork(FriendlyByteBuf packetBuffer) {
            return new RefuelRecipe(packetBuffer.readItem(), Ingredient.fromNetwork(packetBuffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf packetBuffer, RefuelRecipe recipe) {
            packetBuffer.writeItem(recipe.rocket);
            recipe.fuel.toNetwork(packetBuffer);
        }
    }

    protected CraftingResult craft(CraftingContainer inv) {
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
            } else if (stack.is(Main.ROCKET_FUEL)) {
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
