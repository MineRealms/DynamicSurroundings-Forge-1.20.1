package org.orecruncher.dsurround.lib.resources;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.orecruncher.dsurround.lib.collections.ObjectArray;
import org.orecruncher.dsurround.lib.logging.IModLog;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.orecruncher.dsurround.Configuration.Flags.RESOURCE_LOADING;

public class ModConfigResourceFinder extends AbstractResourceFinder {

    private final Map<ResourceLocation, List<Resource>> resources;

    public ModConfigResourceFinder(IModLog logger, ResourceManager resourceManager, String configPath) {
        super(logger);
        // In Forge, listResourceStacks expects a path relative to assets/<namespace>/
        // We need to search for resources in the format: assets/<namespace>/dsconfigs/...
        // The method signature is: listResourceStacks(String path, Predicate<ResourceLocation> filter)
        // where path is relative to the namespace root (e.g., "dsconfigs" will look in assets/*/dsconfigs/)
        this.resources = resourceManager.listResourceStacks(configPath, location -> {
            this.logger.info("[ModConfigResourceFinder] Checking location: %s (path: %s)", location, location.getPath());
            return true;
        });
        this.logger.info("[ModConfigResourceFinder] Found %d resources for path '%s'", this.resources.size(), configPath);
        for (var kvp : this.resources.entrySet()) {
            this.logger.info("[ModConfigResourceFinder] - Resource: %s (path: %s, namespace: %s)",
                kvp.getKey(), kvp.getKey().getPath(), kvp.getKey().getNamespace());
        }
    }

    public <T> Collection<DiscoveredResource<T>> find(Codec<T> codec, String path) {
        if (!path.endsWith(".json"))
            path = path + ".json";

        this.logger.info("[ModConfigResourceFinder] find() called with path: %s", path);
        var result = new ObjectArray<DiscoveredResource<T>>();
        for (var kvp : this.resources.entrySet()) {
            var resourcePath = kvp.getKey().getPath();
            if (resourcePath.endsWith(path)) {
                this.logger.info("[ModConfigResourceFinder] MATCH: %s ends with %s", resourcePath, path);
                this.logger.debug(RESOURCE_LOADING, "[%s] - Processing %s", resourcePath, kvp.getKey());
                for (var r : kvp.getValue()) {
                    try (var inputStream = r.open()) {
                        var assetBytes = inputStream.readAllBytes();
                        var assetString = new String(assetBytes, Charset.defaultCharset());
                        this.logger.info("[ModConfigResourceFinder] Read %d bytes from %s", assetBytes.length, kvp.getKey());
                        var entity = this.decode(kvp.getKey(), assetString, codec);
                        if (entity.isPresent()) {
                            result.add(new DiscoveredResource<>(kvp.getKey().getNamespace(), entity.get()));
                            this.logger.info("[ModConfigResourceFinder] Successfully decoded %s", kvp.getKey());
                        } else {
                            this.logger.warn("[ModConfigResourceFinder] Decode returned empty for %s", kvp.getKey());
                        }
                        this.logger.debug(RESOURCE_LOADING, "[%s] - Completed decode of %s", resourcePath, kvp.getKey());
                    } catch (Throwable t) {
                        this.logger.error(t, "[%s] - Unable to read resource stream for path %s", resourcePath, kvp.getKey());
                    }
                }
            }
        }

        this.logger.info("[ModConfigResourceFinder] find() returning %d results for path: %s", result.size(), path);
        return result;
    }
}
