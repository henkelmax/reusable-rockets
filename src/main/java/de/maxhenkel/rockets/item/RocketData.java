package de.maxhenkel.rockets.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class RocketData {

    public static final Codec<RocketData> CODEC = RecordCodecBuilder.create(i ->
            i.group(
                    Codec.BYTE.fieldOf("flight_duration").forGetter(RocketData::getFlightDuration),
                    Codec.INT.fieldOf("uses_left").forGetter(RocketData::getUsesLeft)
            ).apply(i, RocketData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RocketData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE,
            RocketData::getFlightDuration,
            ByteBufCodecs.VAR_INT,
            RocketData::getUsesLeft,
            RocketData::new
    );

    private final byte flightDuration;
    private final int usesLeft;

    public RocketData(byte flightDuration, int usesLeft) {
        this.flightDuration = flightDuration;
        this.usesLeft = usesLeft;
    }

    public byte getFlightDuration() {
        return flightDuration;
    }

    public int getUsesLeft() {
        return usesLeft;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RocketData that = (RocketData) o;
        return flightDuration == that.flightDuration && usesLeft == that.usesLeft;
    }

    @Override
    public int hashCode() {
        int result = flightDuration;
        result = 31 * result + usesLeft;
        return result;
    }
}
