package de.maxhenkel.rockets.events;

import de.maxhenkel.rockets.Main;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CreativeTabEvents {

    @SubscribeEvent
    public static void onCreativeModeTabBuildContents(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(new ItemStack(Main.REUSABLE_ROCKET_TIER_1.get()));
            event.accept(new ItemStack(Main.REUSABLE_ROCKET_TIER_2.get()));
            event.accept(new ItemStack(Main.REUSABLE_ROCKET_TIER_3.get()));
        }
    }

}
