package de.maxhenkel.rockets;

import de.maxhenkel.corelib.CommonRegistry;
import de.maxhenkel.rockets.events.CreativeTabEvents;
import de.maxhenkel.rockets.item.ItemReusableRocket;
import de.maxhenkel.rockets.item.RocketData;
import de.maxhenkel.rockets.recipe.RefuelRecipe;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "reusable_rockets";

    public static ServerConfig SERVER_CONFIG;

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTER = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Main.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<? extends CustomRecipe>> CRAFTING_REFUEL = RECIPE_REGISTER.register("refuel", RefuelRecipe.RecipeRefuelSerializer::new);

    private static final DeferredRegister.Items ITEM_REGISTER = DeferredRegister.createItems(Main.MODID);

    public static final DeferredHolder<Item, ItemReusableRocket> REUSABLE_ROCKET_TIER_1 = ITEM_REGISTER.registerItem("reusable_rocket_tier_1", p ->
            new ItemReusableRocket(p, SERVER_CONFIG.tier1MaxUses::get, SERVER_CONFIG.tier1MaxDuration::get)
    );
    public static final DeferredHolder<Item, ItemReusableRocket> REUSABLE_ROCKET_TIER_2 = ITEM_REGISTER.registerItem("reusable_rocket_tier_2", p ->
            new ItemReusableRocket(p, SERVER_CONFIG.tier2MaxUses::get, SERVER_CONFIG.tier2MaxDuration::get)
    );
    public static final DeferredHolder<Item, ItemReusableRocket> REUSABLE_ROCKET_TIER_3 = ITEM_REGISTER.registerItem("reusable_rocket_tier_3", p ->
            new ItemReusableRocket(p, SERVER_CONFIG.tier3MaxUses::get, SERVER_CONFIG.tier3MaxDuration::get)
    );

    public static TagKey<Item> ROCKET_FUEL = ItemTags.create(ResourceLocation.fromNamespaceAndPath(Main.MODID, "rocket_fuel"));

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE_REGISTER = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Main.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RocketData>> ROCKET_DATA_COMPONENT = DATA_COMPONENT_TYPE_REGISTER.register("rocket", () -> DataComponentType.<RocketData>builder().persistent(RocketData.CODEC).networkSynchronized(RocketData.STREAM_CODEC).build());

    public Main(IEventBus eventBus) {
        eventBus.addListener(CreativeTabEvents::onCreativeModeTabBuildContents);

        SERVER_CONFIG = CommonRegistry.registerConfig(MODID, ModConfig.Type.SERVER, ServerConfig.class);

        ITEM_REGISTER.register(eventBus);
        RECIPE_REGISTER.register(eventBus);
        DATA_COMPONENT_TYPE_REGISTER.register(eventBus);
    }

}
