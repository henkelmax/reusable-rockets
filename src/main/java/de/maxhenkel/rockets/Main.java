package de.maxhenkel.rockets;

import de.maxhenkel.corelib.CommonRegistry;
import de.maxhenkel.rockets.item.ItemReusableRocket;
import de.maxhenkel.rockets.recipe.RefuelRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "reusable_rockets";

    public static ServerConfig SERVER_CONFIG;

    public static RefuelRecipe.RecipeRefuelSerializer CRAFTING_REFUEL;

    public static ItemReusableRocket REUSABLE_ROCKET_TIER_1;
    public static ItemReusableRocket REUSABLE_ROCKET_TIER_2;
    public static ItemReusableRocket REUSABLE_ROCKET_TIER_3;

    public static ITag<Item> ROCKET_FUEL = ItemTags.bind(new ResourceLocation(Main.MODID, "rocket_fuel").toString());

    public Main() {

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(IRecipeSerializer.class, this::registerRecipes);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        SERVER_CONFIG = CommonRegistry.registerConfig(ModConfig.Type.SERVER, ServerConfig.class);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            clientStart();
        });
    }

    @OnlyIn(Dist.CLIENT)
    public void clientStart() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Main.this::clientSetup);
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        CRAFTING_REFUEL = new RefuelRecipe.RecipeRefuelSerializer();
        CRAFTING_REFUEL.setRegistryName(new ResourceLocation(MODID, "refuel"));
        event.getRegistry().register(CRAFTING_REFUEL);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        REUSABLE_ROCKET_TIER_1 = new ItemReusableRocket("reusable_rocket_tier_1", SERVER_CONFIG.tier1MaxUses::get, SERVER_CONFIG.tier1MaxDuration::get);
        REUSABLE_ROCKET_TIER_2 = new ItemReusableRocket("reusable_rocket_tier_2", SERVER_CONFIG.tier2MaxUses::get, SERVER_CONFIG.tier2MaxDuration::get);
        REUSABLE_ROCKET_TIER_3 = new ItemReusableRocket("reusable_rocket_tier_3", SERVER_CONFIG.tier3MaxUses::get, SERVER_CONFIG.tier3MaxDuration::get);
        event.getRegistry().registerAll(
                REUSABLE_ROCKET_TIER_1,
                REUSABLE_ROCKET_TIER_2,
                REUSABLE_ROCKET_TIER_3
        );
    }

}
