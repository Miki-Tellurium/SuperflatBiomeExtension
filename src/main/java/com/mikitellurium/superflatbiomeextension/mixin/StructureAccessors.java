package com.mikitellurium.superflatbiomeextension.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Optional;

public class StructureAccessors {
    @Mixin(JigsawStructure.class)
    public interface Jigsaw {
        @Accessor Optional<Identifier> getStartJigsawName();
    }
    @Mixin(OceanMonumentPieces.MonumentBuilding.class)
    public interface OceanMonumentBase {
        @Accessor List<StructurePiece> getChildPieces();
    }
}
