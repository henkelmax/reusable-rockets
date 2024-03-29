package de.maxhenkel.rockets.events;

import de.maxhenkel.rockets.Main;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class CreativeTabEvents {

    @SubscribeEvent
    public static void onCreativeModeTabBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(new ItemStack(Main.REUSABLE_ROCKET_TIER_1.get()));
            event.accept(new ItemStack(Main.REUSABLE_ROCKET_TIER_2.get()));
            event.accept(new ItemStack(Main.REUSABLE_ROCKET_TIER_3.get()));
        }
    }

}
