package de.maxhenkel.rockets.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.maxhenkel.rockets.Main;
import de.maxhenkel.rockets.item.ItemReusableRocket;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class RefuelRecipe extends CustomRecipe {

    private ItemStack rocket;
    private Ingredient fuel;

    public RefuelRecipe(ItemStack rocket, Ingredient fuel) {
        super(CraftingBookCategory.MISC);
        this.rocket = rocket;
        this.fuel = fuel;
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        return craft(inv) != null;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        CraftingResult craft = craft(inv);
        if (craft == null) {
            return null;
        }
        return craft.remaining;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        CraftingResult craft = craft(inv);
        if (craft == null) {
            return null;
        }
        return craft.result;
    }

    public ItemStack getRocket() {
        return rocket;
    }

    public Ingredient getFuel() {
        return fuel;
    }


    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return Main.CRAFTING_REFUEL.get();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public static class RecipeRefuelSerializer implements RecipeSerializer<RefuelRecipe> {

        private static final MapCodec<RefuelRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder.group(
                BuiltInRegistries.ITEM.byNameCodec().xmap(ItemStack::new, ItemStack::getItem)
                        .fieldOf("rocket")
                        .forGetter((recipe) -> recipe.rocket),
                Ingredient.CODEC
                        .fieldOf("fuel")
                        .forGetter((recipe) -> recipe.fuel)
        ).apply(builder, RefuelRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, RefuelRecipe> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC,
                RefuelRecipe::getRocket,
                Ingredient.CONTENTS_STREAM_CODEC,
                RefuelRecipe::getFuel,
                RefuelRecipe::new
        );

        public RecipeRefuelSerializer() {

        }

        @Override
        public MapCodec<RefuelRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RefuelRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    protected CraftingResult craft(CraftingInput inv) {
        ItemStack rocket = null;
        List<Integer> gunpowderSlotIndices = new ArrayList<>();

        for (int i = 0; i < inv.size(); i++) {
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

        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

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
