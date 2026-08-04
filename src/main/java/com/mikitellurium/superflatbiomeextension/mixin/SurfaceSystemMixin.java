package com.mikitellurium.superflatbiomeextension.mixin;

import com.mikitellurium.superflatbiomeextension.mixinutil.FlatSurfaceBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SurfaceSystem.class)
public abstract class SurfaceSystemMixin implements FlatSurfaceBuilder {
    @Unique private boolean isFlat = false;
    /*
     * This two redirects are used to prevent the vanilla SurfaceSystem from placing
     *  badlands pillars and icebergs in custom flat worlds.
     */

    @Redirect(method = "buildSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/SurfaceSystem;erodedBadlandsExtension(Lnet/minecraft/world/level/chunk/BlockColumn;IIILnet/minecraft/world/level/LevelHeightAccessor;)V"))
    private void redirect$placeBadlandsPillar(SurfaceSystem instance, BlockColumn column, int blockX, int blockZ, int height, LevelHeightAccessor protoChunk) {
        if (!((FlatSurfaceBuilder)this).mixin$isFlat()) this.erodedBadlandsExtension(column, blockX, blockZ, height, protoChunk);
    }

    @Shadow
    protected abstract void erodedBadlandsExtension(BlockColumn column, int blockX, int blockZ, int height, LevelHeightAccessor protoChunk);

    @Redirect(method = "buildSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/SurfaceSystem;frozenOceanExtension(ILnet/minecraft/world/level/biome/Biome;Lnet/minecraft/world/level/chunk/BlockColumn;Lnet/minecraft/core/BlockPos$MutableBlockPos;III)V"))
    private void redirect$placeIceberg(SurfaceSystem instance, int minSurfaceLevel, Biome biome, BlockColumn column, BlockPos.MutableBlockPos mutablePos, int x, int z, int surfaceY) {
        if (!((FlatSurfaceBuilder)this).mixin$isFlat()) this.frozenOceanExtension(minSurfaceLevel, biome, column, mutablePos, x, z, surfaceY);
    }

    @Shadow
    protected abstract void frozenOceanExtension(int minSurfaceLevel, Biome biome, BlockColumn column, BlockPos.MutableBlockPos mutablePos, int x, int z, int surfaceY);

    @Override
    public boolean mixin$isFlat() {
        return isFlat;
    }

    @Override
    public void mixin$setFlat() {
        this.isFlat = true;
    }
}
