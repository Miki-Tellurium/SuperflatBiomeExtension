package com.mikitellurium.superflatbiomeextension.mixin;

import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.JigsawStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(JigsawStructure.class)
public interface JigsawStructureAccessor {
    @Accessor Optional<Identifier> getStartJigsawName();
}
