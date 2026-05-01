package org.orecruncher.dsurround.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.orecruncher.dsurround.Client;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.lib.Library;

/**
 * Implements the Forge specific binding to initialize the mod
 */
@Mod(Constants.MOD_ID)
public final class ForgeMod {

    private final Client client;

    public ForgeMod() {
        // Only initialize on client side
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
            this.client = new Client();

            // Initialize client immediately to register configuration in DI container
            // This must happen before Minecraft creates the Gui instance
            this.client.initializeClient();

            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            modEventBus.addListener(this::onClientSetup);
        } else {
            this.client = null;
        }
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // Client is already initialized in constructor
        // This event can be used for additional setup if needed
    }
}
