package org.orecruncher.dsurround.forge.services;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.orecruncher.dsurround.Constants;
import org.orecruncher.dsurround.config.libraries.AssetLibraryEvent;
import org.orecruncher.dsurround.config.libraries.IReloadEvent;
import org.orecruncher.dsurround.eventing.ClientState;
import org.orecruncher.dsurround.lib.GameUtils;
import org.orecruncher.dsurround.lib.Library;
import org.orecruncher.dsurround.lib.config.ConfigurationData;
import org.orecruncher.dsurround.lib.config.IScreenFactory;
import org.orecruncher.dsurround.lib.platform.IPlatform;
import org.orecruncher.dsurround.lib.platform.ModInformation;
import org.orecruncher.dsurround.lib.resources.ResourceLookupHelper;
import org.orecruncher.dsurround.lib.resources.ResourceUtilities;
import org.orecruncher.dsurround.lib.version.SemanticVersion;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ForgeServiceImpl implements IPlatform {

    private final ResourceLookupHelper lookupHelper;
    private final List<KeyMapping> pendingKeyMappings = new ArrayList<>();

    public ForgeServiceImpl() {
        this.lookupHelper = new ResourceLookupHelper(PackType.SERVER_DATA);

        // Register to Forge event bus for game events
        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler(this));

        // Note: WeatherFogHandler will be registered later by the mod initialization
        // to avoid circular dependency issues during service loading
    }

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public Optional<ModInformation> getModInformation(String modId) {
        var container = ModList.get().getModContainerById(modId);
        if (container.isPresent()) {
            try {
                var modInfo = container.get().getModInfo();
                var displayName = modInfo.getDisplayName();
                var version = SemanticVersion.parse(modInfo.getVersion().toString());

                // Forge doesn't have custom metadata like Fabric, so we'll use defaults
                var updateURL = "";
                var curseForgeLink = "";
                var modrinthLink = "";

                var result = new ModInformation(modId, displayName, version, updateURL, curseForgeLink, modrinthLink);
                return Optional.of(result);
            } catch (Exception ex) {
                Library.LOGGER.error(ex, "Error getting mod information for " + modId);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getModDisplayName(String namespace) {
        var container = ModList.get().getModContainerById(namespace);
        return container.map(modContainer -> modContainer.getModInfo().getDisplayName());
    }

    @Override
    public Optional<SemanticVersion> getModVersion(String namespace) {
        var container = ModList.get().getModContainerById(namespace);
        if (container.isPresent()) {
            try {
                return Optional.of(SemanticVersion.parse(container.get().getModInfo().getVersion().toString()));
            } catch (Exception ignored) {
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isModLoaded(String namespace) {
        return ModList.get().isLoaded(namespace);
    }

    @Override
    public Collection<String> getModIdList(boolean loadedOnly) {
        return ModList.get().getMods()
                .stream()
                .map(modInfo -> modInfo.getModId())
                .filter(name -> !loadedOnly || ModList.get().isLoaded(name))
                .collect(Collectors.toList());
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Path getConfigPath(String modId) {
        return getConfigPath().resolve(modId);
    }

    @Override
    public KeyMapping registerKeyBinding(String translationKey, int code, String category) {
        var keyMapping = new KeyMapping(translationKey, code, category);
        this.pendingKeyMappings.add(keyMapping);
        return keyMapping;
    }

    @Override
    public Collection<Path> findResourcePaths(String fileNamePattern) {
        return this.lookupHelper.findResourcePaths(fileNamePattern);
    }

    @Override
    public Collection<Path> getResourceRootPaths(PackType packType) {
        var pathPrefix = packType.getDirectory();
        return ModList.get().getMods()
                .stream()
                .map(modInfo -> modInfo.getOwningFile().getFile().findResource(pathPrefix))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Optional<IScreenFactory<?>> getModConfigScreenFactory(Class<? extends ConfigurationData> configClass) {
        return Optional.of(new org.orecruncher.dsurround.forge.config.ForgeConfigScreenFactory());
    }

    // Mod event bus handler for key mappings
    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModEventHandler {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            var platform = (ForgeServiceImpl) org.orecruncher.dsurround.lib.platform.Services.PLATFORM;
            for (var keyMapping : platform.pendingKeyMappings) {
                event.register(keyMapping);
            }
            platform.pendingKeyMappings.clear();
        }
    }

    // Forge event bus handler for game events (client only)
    @OnlyIn(Dist.CLIENT)
    public static class ForgeEventHandler {
        private final ForgeServiceImpl platform;

        public ForgeEventHandler(ForgeServiceImpl platform) {
            this.platform = platform;
        }

        @SubscribeEvent
        public void onResourceReload(AddReloadListenerEvent event) {
            event.addListener((preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) ->
                    preparationBarrier.wait(null).thenRunAsync(() -> {
                        if (GameUtils.getMC().isSameThread()) {
                            Library.LOGGER.info("Refreshing lookup helper");
                            platform.lookupHelper.refresh(platform);

                            Library.LOGGER.info("Resource reload - resetting configuration caches");
                            var resourceUtilities = ResourceUtilities.createForResourceManager(resourceManager);
                            AssetLibraryEvent.RELOAD.raise().onReload(resourceUtilities, IReloadEvent.Scope.RESOURCES);
                        }
                    }, gameExecutor)
            );
        }

        @SubscribeEvent
        public void onTagsUpdated(TagsUpdatedEvent event) {
            if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
                ClientState.TAG_SYNC.raise().onTagSync(event.getRegistryAccess());
            }
        }
    }
}
