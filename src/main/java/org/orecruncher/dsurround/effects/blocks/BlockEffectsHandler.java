package org.orecruncher.dsurround.effects.blocks;

import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.lib.Library;

/**
 * Main handler for block effects system.
 * Coordinates the scanner and particle system manager.
 */
public class BlockEffectsHandler {
    private static BlockEffectsHandler INSTANCE;

    private final BlockEffectScanner scanner;
    private final ParticleSystemManager particleManager;

    private boolean initialized = false;

    private BlockEffectsHandler() {
        this.scanner = new BlockEffectScanner();
        this.particleManager = ParticleSystemManager.getInstance();
    }

    @NotNull
    public static BlockEffectsHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BlockEffectsHandler();
        }
        return INSTANCE;
    }

    /**
     * Initialize the block effects system.
     * Registers all default effects.
     */
    public void initialize() {
        if (this.initialized) {
            return;
        }

        Library.LOGGER.info("Initializing Block Effects System");

        // Register default effects
        registerDefaultEffects();

        this.initialized = true;

        Library.LOGGER.info("Block Effects System initialized with {} effects",
            this.scanner.getEffectCount());
    }

    /**
     * Register all default block effects
     */
    private void registerDefaultEffects() {
        // Register steam jets
        this.scanner.registerEffect(new SteamJetEffect());

        // Register fire jets
        this.scanner.registerEffect(new FireJetEffect());

        // Register waterfalls
        this.scanner.registerEffect(new WaterfallEffect());

        // Register bubble jets
        this.scanner.registerEffect(new BubbleJetEffect());

        // Register dust jets
        this.scanner.registerEffect(new DustJetEffect());

        // Register fountain jets
        this.scanner.registerEffect(new FountainJetEffect());

        // Register fireflies
        this.scanner.registerEffect(new FireFlyEffect());
    }

    /**
     * Update the block effects system.
     * Called every client tick.
     */
    public void onTick() {
        if (!this.initialized) {
            return;
        }

        // Scan for new effects
        this.scanner.onTick();

        // Update existing particle systems
        this.particleManager.onTick();
    }

    /**
     * Clear all active effects.
     * Called when disconnecting or changing dimensions.
     */
    public void clear() {
        this.particleManager.clear();
        Library.LOGGER.info("Cleared all block effects");
    }

    /**
     * Get the block effect scanner
     */
    @NotNull
    public BlockEffectScanner getScanner() {
        return this.scanner;
    }

    /**
     * Get the particle system manager
     */
    @NotNull
    public ParticleSystemManager getParticleManager() {
        return this.particleManager;
    }

    /**
     * Get statistics about active effects
     */
    public String getStats() {
        return String.format("Active particle systems: %d, Registered effects: %d",
            this.particleManager.getActiveCount(),
            this.scanner.getEffectCount());
    }
}
