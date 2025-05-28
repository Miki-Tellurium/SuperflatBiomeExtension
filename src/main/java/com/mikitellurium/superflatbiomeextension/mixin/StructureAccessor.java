package com.mikitellurium.superflatbiomeextension.mixin;

import net.minecraft.structure.OceanMonumentGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.JigsawStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Optional;

public class StructureAccessor {
    @Mixin(JigsawStructure.class)
    public interface Jigsaw {
        @Accessor Optional<Identifier> getStartJigsawName();
    }
    @Mixin(OceanMonumentGenerator.Base.class)
    public interface OceanMonumentBase {
        @Accessor List<OceanMonumentGenerator.Piece> getChildren();
    }
}
