package org.orecruncher.dsurround.effects.blocks;

/**
 * Enum defining the types of block effects available in the system.
 * Each type corresponds to a specific visual or audio effect.
 */
public enum BlockEffectType {
    /**
     * Steam particles where hot blocks (lava) meet water
     */
    STEAM_JET,

    /**
     * Flame particles rising from hot blocks like lava
     */
    FIRE_JET,

    /**
     * Bubble particles rising from underwater blocks
     */
    BUBBLE_JET,

    /**
     * Dust particles falling from blocks
     */
    DUST_JET,

    /**
     * Water fountain particles from water sources
     */
    FOUNTAIN_JET,

    /**
     * Water splash effects when water flows over edges
     */
    WATERFALL,

    /**
     * Glowing firefly particles around plants in certain biomes
     */
    FIREFLY
}
