package de.maxhenkel.rockets;

import de.maxhenkel.corelib.config.ConfigBase;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig extends ConfigBase {

    public final ModConfigSpec.BooleanValue allowRocketSpamming;
    public final ModConfigSpec.IntValue tier1MaxDuration;
    public final ModConfigSpec.IntValue tier1MaxUses;
    public final ModConfigSpec.IntValue tier2MaxDuration;
    public final ModConfigSpec.IntValue tier2MaxUses;
    public final ModConfigSpec.IntValue tier3MaxDuration;
    public final ModConfigSpec.IntValue tier3MaxUses;

    public ServerConfig(ModConfigSpec.Builder builder) {
        super(builder);
        allowRocketSpamming = builder.comment("If the rocket can be used while already getting boosted").define("allow_rocket_spamming", false);
        tier1MaxDuration = builder.defineInRange("rocket.tier_1.max_duration", 2, 1, Integer.MAX_VALUE);
        tier1MaxUses = builder.defineInRange("rocket.tier_1.max_uses", 32, 1, Integer.MAX_VALUE);
        tier2MaxDuration = builder.defineInRange("rocket.tier_2.max_duration", 4, 1, Integer.MAX_VALUE);
        tier2MaxUses = builder.defineInRange("rocket.tier_2.max_uses", 64, 1, Integer.MAX_VALUE);
        tier3MaxDuration = builder.defineInRange("rocket.tier_3.max_duration", 6, 1, Integer.MAX_VALUE);
        tier3MaxUses = builder.defineInRange("rocket.tier_3.max_uses", 128, 1, Integer.MAX_VALUE);
    }

}
