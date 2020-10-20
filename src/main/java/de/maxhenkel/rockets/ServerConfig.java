package de.maxhenkel.rockets;

import de.maxhenkel.corelib.config.ConfigBase;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig extends ConfigBase {

    public final ForgeConfigSpec.BooleanValue allowRocketSpamming;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        super(builder);
        allowRocketSpamming = builder.comment("If the rocket can be used while already getting boosted").define("allow_rocket_spamming", false);
    }

}
