package com.mikitellurium.superflatbiomeextension.mixin;

import com.mikitellurium.superflatbiomeextension.mixinutil.FlatSurfaceBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.BlockColumn;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SurfaceBuilder.class)
public abstract class SurfaceBuilderMixin implements FlatSurfaceBuilder {
    @Shadow protected abstract void placeBadlandsPillar(BlockColumn column, int x, int z, int surfaceY, HeightLimitView chunk);
    @Shadow protected abstract void placeIceberg(int minY, Biome biome, BlockColumn column, BlockPos.Mutable mutablePos, int x, int z, int surfaceY);

    @Unique private boolean isFlat = false;

    @Redirect(method = "buildSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/surfacebuilder/SurfaceBuilder;placeBadlandsPillar(Lnet/minecraft/world/gen/chunk/BlockColumn;IIILnet/minecraft/world/HeightLimitView;)V"))
    private void redirect$placeBadlandsPillar(SurfaceBuilder instance, BlockColumn column, int x, int z, int surfaceY, HeightLimitView chunk) {
        if (!((FlatSurfaceBuilder)this).mixin$isFlat()) this.placeBadlandsPillar(column, x, z, surfaceY, chunk);
    }

    @Redirect(method = "buildSurface", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/surfacebuilder/SurfaceBuilder;placeIceberg(ILnet/minecraft/world/biome/Biome;Lnet/minecraft/world/gen/chunk/BlockColumn;Lnet/minecraft/util/math/BlockPos$Mutable;III)V"))
    private void redirect$placeIceberg(SurfaceBuilder instance, int minY, Biome biome, BlockColumn column, BlockPos.Mutable mutablePos, int x, int z, int surfaceY) {
        if (!((FlatSurfaceBuilder)this).mixin$isFlat()) this.placeIceberg(minY, biome, column, mutablePos, x, z, surfaceY);
    }

    @Override
    public boolean mixin$isFlat() {
        return isFlat;
    }

    @Override
    public void mixin$setFlat() {
        this.isFlat = true;
    }
}
