package org.orecruncher.dsurround.lib.footsteps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.lib.Library;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages acoustic profiles and their mappings to blocks.
 * Handles loading, registration, and lookup of footstep acoustics.
 */
public class AcousticsManager {
    private static AcousticsManager INSTANCE;

    private final Map<String, AcousticProfile> profiles;
    private final BlockAcoustic blockAcoustics;
    private AcousticProfile defaultProfile;

    private AcousticsManager() {
        this.profiles = new HashMap<>();

        // Create a silent default profile as fallback
        this.defaultProfile = new AcousticProfile.Builder()
            .name("silent")
            .addWalkSound(getSoundEvent("minecraft:block.stone.step"))
            .volumeScale(0.0f)
            .build();

        this.blockAcoustics = new BlockAcoustic(this.defaultProfile);
    }

    @NotNull
    public static AcousticsManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AcousticsManager();
        }
        return INSTANCE;
    }

    /**
     * Initialize the acoustics manager with default profiles and mappings
     */
    public void initialize() {
        Library.LOGGER.info("Initializing Acoustics Manager");

        // Clear existing data
        this.profiles.clear();
        this.blockAcoustics.clear();

        // Register default acoustic profiles
        registerDefaultProfiles();

        // Map vanilla blocks to profiles
        registerVanillaBlocks();

        Library.LOGGER.info("Registered {} acoustic profiles and {} block mappings",
            this.profiles.size(), this.blockAcoustics.size());
    }

    /**
     * Register a named acoustic profile
     */
    public void registerProfile(@NotNull String name, @NotNull AcousticProfile profile) {
        this.profiles.put(name, profile);
    }

    /**
     * Get a registered acoustic profile by name
     */
    @NotNull
    public AcousticProfile getProfile(@NotNull String name) {
        return this.profiles.getOrDefault(name, this.defaultProfile);
    }

    /**
     * Get the block acoustics mapper
     */
    @NotNull
    public BlockAcoustic getBlockAcoustics() {
        return this.blockAcoustics;
    }

    /**
     * Register default acoustic profiles for common materials
     */
    private void registerDefaultProfiles() {
        // Stone profile
        AcousticProfile stone = new AcousticProfile.Builder()
            .name("stone")
            .addWalkSound(getSoundEvent("minecraft:block.stone.step"))
            .addRunSound(getSoundEvent("minecraft:block.stone.step"))
            .addJumpSound(getSoundEvent("minecraft:block.stone.step"))
            .addLandSound(getSoundEvent("minecraft:block.stone.fall"))
            .volumeScale(1.0f)
            .pitchVariation(0.1f)
            .build();
        registerProfile("stone", stone);

        // Wood profile
        AcousticProfile wood = new AcousticProfile.Builder()
            .name("wood")
            .addWalkSound(getSoundEvent("minecraft:block.wood.step"))
            .addRunSound(getSoundEvent("minecraft:block.wood.step"))
            .addJumpSound(getSoundEvent("minecraft:block.wood.step"))
            .addLandSound(getSoundEvent("minecraft:block.wood.fall"))
            .volumeScale(1.0f)
            .pitchVariation(0.1f)
            .build();
        registerProfile("wood", wood);

        // Grass profile
        AcousticProfile grass = new AcousticProfile.Builder()
            .name("grass")
            .addWalkSound(getSoundEvent("minecraft:block.grass.step"))
            .addRunSound(getSoundEvent("minecraft:block.grass.step"))
            .addJumpSound(getSoundEvent("minecraft:block.grass.step"))
            .addLandSound(getSoundEvent("minecraft:block.grass.fall"))
            .volumeScale(0.8f)
            .pitchVariation(0.15f)
            .build();
        registerProfile("grass", grass);

        // Gravel profile
        AcousticProfile gravel = new AcousticProfile.Builder()
            .name("gravel")
            .addWalkSound(getSoundEvent("minecraft:block.gravel.step"))
            .addRunSound(getSoundEvent("minecraft:block.gravel.step"))
            .addJumpSound(getSoundEvent("minecraft:block.gravel.step"))
            .addLandSound(getSoundEvent("minecraft:block.gravel.fall"))
            .volumeScale(1.0f)
            .pitchVariation(0.1f)
            .build();
        registerProfile("gravel", gravel);

        // Sand profile
        AcousticProfile sand = new AcousticProfile.Builder()
            .name("sand")
            .addWalkSound(getSoundEvent("minecraft:block.sand.step"))
            .addRunSound(getSoundEvent("minecraft:block.sand.step"))
            .addJumpSound(getSoundEvent("minecraft:block.sand.step"))
            .addLandSound(getSoundEvent("minecraft:block.sand.fall"))
            .volumeScale(0.9f)
            .pitchVariation(0.1f)
            .build();
        registerProfile("sand", sand);

        // Snow profile
        AcousticProfile snow = new AcousticProfile.Builder()
            .name("snow")
            .addWalkSound(getSoundEvent("minecraft:block.snow.step"))
            .addRunSound(getSoundEvent("minecraft:block.snow.step"))
            .addJumpSound(getSoundEvent("minecraft:block.snow.step"))
            .addLandSound(getSoundEvent("minecraft:block.snow.fall"))
            .volumeScale(0.7f)
            .pitchVariation(0.15f)
            .build();
        registerProfile("snow", snow);

        // Metal profile
        AcousticProfile metal = new AcousticProfile.Builder()
            .name("metal")
            .addWalkSound(getSoundEvent("minecraft:block.metal.step"))
            .addRunSound(getSoundEvent("minecraft:block.metal.step"))
            .addJumpSound(getSoundEvent("minecraft:block.metal.step"))
            .addLandSound(getSoundEvent("minecraft:block.metal.fall"))
            .volumeScale(1.0f)
            .pitchVariation(0.05f)
            .build();
        registerProfile("metal", metal);

        // Wool/Carpet profile
        AcousticProfile wool = new AcousticProfile.Builder()
            .name("wool")
            .addWalkSound(getSoundEvent("minecraft:block.wool.step"))
            .addRunSound(getSoundEvent("minecraft:block.wool.step"))
            .addJumpSound(getSoundEvent("minecraft:block.wool.step"))
            .addLandSound(getSoundEvent("minecraft:block.wool.fall"))
            .volumeScale(0.5f)
            .pitchVariation(0.1f)
            .build();
        registerProfile("wool", wool);

        // Set stone as default
        this.defaultProfile = stone;
    }

    /**
     * Map vanilla blocks to acoustic profiles
     */
    private void registerVanillaBlocks() {
        // Stone blocks
        registerBlocksToProfile("stone",
            Blocks.STONE, Blocks.COBBLESTONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE,
            Blocks.POLISHED_ANDESITE, Blocks.POLISHED_DIORITE, Blocks.POLISHED_GRANITE,
            Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS,
            Blocks.BRICKS, Blocks.NETHER_BRICKS, Blocks.RED_NETHER_BRICKS,
            Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERRACK, Blocks.BASALT,
            Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE, Blocks.DEEPSLATE,
            Blocks.COBBLED_DEEPSLATE, Blocks.POLISHED_DEEPSLATE
        );

        // Wood blocks
        registerBlocksToProfile("wood",
            Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS,
            Blocks.JUNGLE_PLANKS, Blocks.ACACIA_PLANKS, Blocks.DARK_OAK_PLANKS,
            Blocks.MANGROVE_PLANKS, Blocks.CHERRY_PLANKS, Blocks.BAMBOO_PLANKS,
            Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG,
            Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG,
            Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG, Blocks.BAMBOO_BLOCK
        );

        // Grass blocks
        registerBlocksToProfile("grass",
            Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL,
            Blocks.MYCELIUM, Blocks.DIRT_PATH, Blocks.FARMLAND, Blocks.ROOTED_DIRT
        );

        // Gravel
        registerBlocksToProfile("gravel",
            Blocks.GRAVEL
        );

        // Sand
        registerBlocksToProfile("sand",
            Blocks.SAND, Blocks.RED_SAND, Blocks.SOUL_SAND, Blocks.SOUL_SOIL
        );

        // Snow
        registerBlocksToProfile("snow",
            Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.ICE, Blocks.PACKED_ICE,
            Blocks.BLUE_ICE, Blocks.FROSTED_ICE
        );

        // Metal
        registerBlocksToProfile("metal",
            Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK,
            Blocks.EMERALD_BLOCK, Blocks.NETHERITE_BLOCK, Blocks.COPPER_BLOCK,
            Blocks.IRON_DOOR, Blocks.IRON_TRAPDOOR, Blocks.ANVIL
        );

        // Wool
        registerBlocksToProfile("wool",
            Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL,
            Blocks.LIGHT_BLUE_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL,
            Blocks.PINK_WOOL, Blocks.GRAY_WOOL, Blocks.LIGHT_GRAY_WOOL,
            Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL,
            Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL, Blocks.BLACK_WOOL,
            Blocks.WHITE_CARPET, Blocks.ORANGE_CARPET, Blocks.MAGENTA_CARPET,
            Blocks.LIGHT_BLUE_CARPET, Blocks.YELLOW_CARPET, Blocks.LIME_CARPET,
            Blocks.PINK_CARPET, Blocks.GRAY_CARPET, Blocks.LIGHT_GRAY_CARPET,
            Blocks.CYAN_CARPET, Blocks.PURPLE_CARPET, Blocks.BLUE_CARPET,
            Blocks.BROWN_CARPET, Blocks.GREEN_CARPET, Blocks.RED_CARPET, Blocks.BLACK_CARPET
        );
    }

    /**
     * Helper method to register multiple blocks to a profile
     */
    private void registerBlocksToProfile(@NotNull String profileName, @NotNull Block... blocks) {
        AcousticProfile profile = getProfile(profileName);
        for (Block block : blocks) {
            this.blockAcoustics.registerBlock(block, profile);
        }
    }

    /**
     * Helper method to get a SoundEvent from a resource location string
     */
    @NotNull
    private static SoundEvent getSoundEvent(@NotNull String location) {
        ResourceLocation resourceLocation = new ResourceLocation(location);
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(resourceLocation);
        if (sound == null) {
            throw new IllegalArgumentException("Unknown sound event: " + location);
        }
        return sound;
    }
}
