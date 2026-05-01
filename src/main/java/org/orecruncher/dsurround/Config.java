package org.orecruncher.dsurround;

import org.orecruncher.dsurround.lib.config.ConfigurationData;

/**
 * Static accessor for configuration values.
 * Provides convenient access to all configuration sections.
 */
public final class Config {

    private static Configuration configuration;

    public static Configuration.Logging logging;
    public static Configuration.SoundSystem soundSystem;
    public static Configuration.EnhancedSounds enhancedSounds;
    public static Configuration.SoundOptions soundOptions;
    public static Configuration.BlockEffects blockEffects;
    public static Configuration.EntityEffects entityEffects;
    public static Configuration.Footsteps footsteps;
    public static Configuration.FootstepAccents footstepAccents;
    public static Configuration.ParticleTweaks particleTweaks;
    public static Configuration.CompassAndClockOptions compassAndClockOptions;
    public static Configuration.WeatherEffects weather;
    public static Configuration.AuroraEffects aurora;
    public static Configuration.OtherOptions otherOptions;

    private Config() {
        // Utility class
    }

    /**
     * Initialize the configuration system.
     * Must be called during mod initialization.
     */
    public static void initialize() {
        configuration = ConfigurationData.getConfig(Configuration.class);
        refresh();
    }

    /**
     * Refresh all configuration references.
     * Call this after configuration changes.
     */
    public static void refresh() {
        if (configuration == null) {
            throw new IllegalStateException("Configuration not initialized");
        }

        logging = configuration.logging;
        soundSystem = configuration.soundSystem;
        enhancedSounds = configuration.enhancedSounds;
        soundOptions = configuration.soundOptions;
        blockEffects = configuration.blockEffects;
        entityEffects = configuration.entityEffects;
        footsteps = configuration.footsteps;
        footstepAccents = configuration.footstepAccents;
        particleTweaks = configuration.particleTweaks;
        compassAndClockOptions = configuration.compassAndClockOptions;
        weather = configuration.weather;
        aurora = configuration.aurora;
        otherOptions = configuration.otherOptions;
    }

    /**
     * Get the raw configuration object.
     *
     * @return The configuration object
     */
    public static Configuration getConfiguration() {
        return configuration;
    }
}
