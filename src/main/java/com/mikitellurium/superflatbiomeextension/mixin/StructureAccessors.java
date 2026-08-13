package com.mikitellurium.superflatbiomeextension.mixin;

import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

public class StructureAccessors {
    @Mixin(OceanMonumentPieces.MonumentBuilding.class)
    public interface OceanMonumentBase {
        @Accessor List<StructurePiece> getChildPieces();
    }
}
