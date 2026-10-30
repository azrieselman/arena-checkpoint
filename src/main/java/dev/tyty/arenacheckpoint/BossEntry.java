package dev.tyty.arenacheckpoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BossEntry(boolean inside, boolean present, double x, double y, double z, String dimension) {
    public static final BossEntry NONE = new BossEntry(false, false, 0.0D, 0.0D, 0.0D, "");

    public static final Codec<BossEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("inside").forGetter(BossEntry::inside),
            Codec.BOOL.fieldOf("present").forGetter(BossEntry::present),
            Codec.DOUBLE.fieldOf("x").forGetter(BossEntry::x),
            Codec.DOUBLE.fieldOf("y").forGetter(BossEntry::y),
            Codec.DOUBLE.fieldOf("z").forGetter(BossEntry::z),
            Codec.STRING.fieldOf("dimension").forGetter(BossEntry::dimension)
    ).apply(instance, BossEntry::new));

    public BossEntry entered(double x, double y, double z, String dimension) {
        return new BossEntry(true, true, x, y, z, dimension);
    }

    public BossEntry left() {
        return new BossEntry(false, this.present, this.x, this.y, this.z, this.dimension);
    }
}
