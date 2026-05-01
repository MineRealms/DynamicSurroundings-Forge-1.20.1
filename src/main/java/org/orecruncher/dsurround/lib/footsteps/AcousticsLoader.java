package org.orecruncher.dsurround.lib.footsteps;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.lib.Library;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads acoustic profiles and block mappings from JSON resource files.
 * Supports resource pack overrides.
 */
public class AcousticsLoader {
    private static final Gson GSON = new Gson();
    private static final ResourceLocation ACOUSTICS_LOCATION = new ResourceLocation("dsurround", "footsteps/acoustics.json");
    private static final ResourceLocation BLOCKS_LOCATION = new ResourceLocation("dsurround", "footsteps/blocks.json");

    /**
     * Load acoustics from resource manager
     */
    public static void loadAcoustics(@NotNull ResourceManager resourceManager, @NotNull AcousticsManager manager) {
        Library.LOGGER.info("Loading footstep acoustics from resources");

        try {
            // Load acoustic profiles
            Map<String, AcousticProfile> profiles = loadAcousticProfiles(resourceManager);
            for (Map.Entry<String, AcousticProfile> entry : profiles.entrySet()) {
                manager.registerProfile(entry.getKey(), entry.getValue());
            }

            // Load block mappings
            Map<String, String> blockMappings = loadBlockMappings(resourceManager);
            applyBlockMappings(manager, blockMappings);

            Library.LOGGER.info("Loaded {} acoustic profiles and {} block mappings from resources",
                    profiles.size(), blockMappings.size());
        } catch (Exception e) {
            Library.LOGGER.error(e, "Failed to load footstep acoustics from resources");
        }
    }

    /**
     * Load acoustic profiles from JSON
     */
    @NotNull
    private static Map<String, AcousticProfile> loadAcousticProfiles(@NotNull ResourceManager resourceManager) {
        Map<String, AcousticProfile> profiles = new HashMap<>();

        try {
            // Get all resources with this location (supports resource pack overrides)
            List<Resource> resources = resourceManager.getResourceStack(ACOUSTICS_LOCATION);

            for (Resource resource : resources) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {

                    JsonObject root = GSON.fromJson(reader, JsonObject.class);
                    if (root == null || !root.has("acoustics")) {
                        continue;
                    }

                    JsonObject acoustics = root.getAsJsonObject("acoustics");
                    for (Map.Entry<String, JsonElement> entry : acoustics.entrySet()) {
                        String name = entry.getKey();
                        JsonObject acousticData = entry.getValue().getAsJsonObject();

                        AcousticProfile profile = parseAcousticProfile(name, acousticData);
                        if (profile != null) {
                            profiles.put(name, profile);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Library.LOGGER.error(e, "Failed to load acoustic profiles");
        }

        return profiles;
    }

    /**
     * Parse an acoustic profile from JSON
     */
    private static AcousticProfile parseAcousticProfile(@NotNull String name, @NotNull JsonObject data) {
        try {
            AcousticProfile.Builder builder = new AcousticProfile.Builder().name(name);

            // Parse walk sounds
            if (data.has("walk")) {
                for (SoundEvent sound : parseSoundArray(data.getAsJsonArray("walk"))) {
                    builder.addWalkSound(sound);
                }
            }

            // Parse run sounds
            if (data.has("run")) {
                for (SoundEvent sound : parseSoundArray(data.getAsJsonArray("run"))) {
                    builder.addRunSound(sound);
                }
            }

            // Parse jump sounds
            if (data.has("jump")) {
                for (SoundEvent sound : parseSoundArray(data.getAsJsonArray("jump"))) {
                    builder.addJumpSound(sound);
                }
            }

            // Parse land sounds
            if (data.has("land")) {
                for (SoundEvent sound : parseSoundArray(data.getAsJsonArray("land"))) {
                    builder.addLandSound(sound);
                }
            }

            // Parse volume
            if (data.has("volume")) {
                builder.volumeScale(data.get("volume").getAsFloat());
            }

            // Parse pitch variation
            if (data.has("pitchVariation")) {
                builder.pitchVariation(data.get("pitchVariation").getAsFloat());
            }

            return builder.build();
        } catch (Exception e) {
            Library.LOGGER.error(e, "Failed to parse acoustic profile: {}", name);
            return null;
        }
    }

    /**
     * Parse an array of sound event names
     */
    @NotNull
    private static List<SoundEvent> parseSoundArray(@NotNull JsonArray array) {
        List<SoundEvent> sounds = new ArrayList<>();

        for (JsonElement element : array) {
            String soundName = element.getAsString();
            ResourceLocation location = new ResourceLocation(soundName);
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(location);

            if (sound != null) {
                sounds.add(sound);
            } else {
                Library.LOGGER.warn("Unknown sound event: {}", soundName);
            }
        }

        return sounds;
    }

    /**
     * Load block mappings from JSON
     */
    @NotNull
    private static Map<String, String> loadBlockMappings(@NotNull ResourceManager resourceManager) {
        Map<String, String> mappings = new HashMap<>();

        try {
            // Get all resources with this location (supports resource pack overrides)
            List<Resource> resources = resourceManager.getResourceStack(BLOCKS_LOCATION);

            for (Resource resource : resources) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {

                    JsonObject root = GSON.fromJson(reader, JsonObject.class);
                    if (root == null || !root.has("blocks")) {
                        continue;
                    }

                    JsonObject blocks = root.getAsJsonObject("blocks");
                    for (Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                        String blockId = entry.getKey();
                        String acousticName = entry.getValue().getAsString();
                        mappings.put(blockId, acousticName);
                    }
                }
            }
        } catch (Exception e) {
            Library.LOGGER.error(e, "Failed to load block mappings");
        }

        return mappings;
    }

    /**
     * Apply block mappings to the acoustics manager
     */
    private static void applyBlockMappings(@NotNull AcousticsManager manager, @NotNull Map<String, String> mappings) {
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String blockId = entry.getKey();
            String acousticName = entry.getValue();

            // Get the block from registry
            ResourceLocation blockLocation = new ResourceLocation(blockId);
            var block = ForgeRegistries.BLOCKS.getValue(blockLocation);

            if (block != null) {
                // Get the acoustic profile
                AcousticProfile profile = manager.getProfile(acousticName);
                manager.getBlockAcoustics().registerBlock(block, profile);
            } else {
                Library.LOGGER.warn("Unknown block: {}", blockId);
            }
        }
    }
}
