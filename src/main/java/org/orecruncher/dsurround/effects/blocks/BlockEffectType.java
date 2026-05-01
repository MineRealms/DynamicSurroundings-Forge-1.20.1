package org.orecruncher.dsurround.effects.blocks;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Enum defining the types of block effects available in the system.
 * Each type corresponds to a specific visual or audio effect.
 */
public enum BlockEffectType implements StringRepresentable {
    /**
     * Steam particles where hot blocks (lava) meet water
     */
    STEAM_JET("steam_jet"),

    /**
     * Flame particles rising from hot blocks like lava
     */
    FIRE_JET("fire_jet"),

    /**
     * Bubble particles rising from underwater blocks
     */
    BUBBLE_JET("bubble_jet"),

    /**
     * Dust particles falling from blocks
     */
    DUST_JET("dust_jet"),

    /**
     * Water fountain particles from water sources
     */
    FOUNTAIN_JET("fountain_jet"),

    /**
     * Water splash effects when water flows over edges
     */
    WATERFALL("waterfall"),

    /**
     * Glowing firefly particles around plants in certain biomes
     */
    FIREFLY("firefly");

    public static final Codec<BlockEffectType> CODEC = StringRepresentable.fromEnum(BlockEffectType::values);

    private final String name;

    BlockEffectType(String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return this.name;
    }
}
