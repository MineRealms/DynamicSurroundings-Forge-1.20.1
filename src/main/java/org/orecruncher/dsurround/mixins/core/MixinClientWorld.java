package org.orecruncher.dsurround.mixins.core;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.orecruncher.dsurround.mixinutils.IClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Stream;

@Mixin(ClientLevel.class)
public class MixinClientWorld implements IClientWorld {

    @Final
    @Shadow
    private ClientChunkCache chunkSource;

    @Unique
    public Stream<LevelChunk> dsurround_getLoadedChunks() {
        // Use reflection to access ClientChunkCache.Storage since we removed the accessor mixin
        try {
            Field storageField = ClientChunkCache.class.getDeclaredField("storage");
            storageField.setAccessible(true);
            Object storage = storageField.get(this.chunkSource);

            Field chunksField = storage.getClass().getDeclaredField("chunks");
            chunksField.setAccessible(true);
            AtomicReferenceArray<LevelChunk> chunks = (AtomicReferenceArray<LevelChunk>) chunksField.get(storage);

            List<LevelChunk> resultChunks = new ArrayList<>();
            for (int i = 0; i < chunks.length(); i++) {
                var chunk = chunks.get(i);
                if (chunk != null)
                    resultChunks.add(chunk);
            }

            return resultChunks.stream();
        } catch (Exception e) {
            // Fallback to empty stream if reflection fails
            return Stream.empty();
        }
    }
}
