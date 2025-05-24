package com.mikitellurium.superflatbiomeextension.mixin;

import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StructurePoolBasedGenerator.class)
public class StructurePoolBasedGeneratorMixin {
//    @Redirect(method = "generate(Lnet/minecraft/world/gen/structure/Structure$Context;Lnet/minecraft/registry/entry/RegistryEntry;Ljava/util/Optional;ILnet/minecraft/util/math/BlockPos;ZLjava/util/Optional;ILnet/minecraft/structure/pool/alias/StructurePoolAliasLookup;Lnet/minecraft/world/gen/structure/DimensionPadding;Lnet/minecraft/structure/StructureLiquidSettings;)Ljava/util/Optional;",
//    at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/BlockPos;subtract(Lnet/minecraft/util/math/Vec3i;)Lnet/minecraft/util/math/BlockPos;"))
//    private static BlockPos redirect$change(BlockPos instance, Vec3i vec3i) {
//        return new BlockPos(instance.getX(), -5, instance.getZ());
//    }
}
