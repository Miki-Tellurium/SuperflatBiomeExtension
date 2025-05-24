package com.mikitellurium.superflatbiomeextension.mixin;

import com.mikitellurium.superflatbiomeextension.worldgen.FlatBiomeExtendedChunkGenerator;
import com.mojang.datafixers.DataFixer;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerChunkLoadingManager.class)
public abstract class ServerChunkLoadingManagerMixin {
    @Mutable
    @Shadow @Final private NoiseConfig noiseConfig;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;createStructurePlacementCalculator(Lnet/minecraft/registry/RegistryWrapper;Lnet/minecraft/world/gen/noise/NoiseConfig;J)Lnet/minecraft/world/gen/chunk/placement/StructurePlacementCalculator;"))
    private void inject$setNoiseConfig(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor executor, ThreadExecutor mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier persistentStateManagerFactory, ChunkTicketManager ticketManager, int viewDistance, boolean dsync, CallbackInfo ci) {
        if (chunkGenerator instanceof FlatBiomeExtendedChunkGenerator generator) {
            this.noiseConfig = NoiseConfig.create(generator.getConfig().getSettings(), world.getRegistryManager().getOrThrow(RegistryKeys.NOISE_PARAMETERS), world.getSeed());
        }
    }
}
