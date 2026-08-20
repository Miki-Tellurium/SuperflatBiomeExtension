package com.mikitellurium.superflatbiomeextension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mikitellurium.superflatbiomeextension.client.ModPresetEditors;
import com.mikitellurium.superflatbiomeextension.util.FastId;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Optional;

@Mixin(WorldCreationUiState.class)
public abstract class WorldCreationUiStateMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(method = {"getPresetEditor", "updatePresetLists"}, at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private <V> V wrapOperation$getPresetEditor(Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> map, Object key, Operation<PresetEditor> original) {
        Optional<ResourceKey<WorldPreset>> optional = (Optional<ResourceKey<WorldPreset>>) key;
        var finalmap = map;
        if (optional.isPresent() && optional.get().identifier().getNamespace().equals(FastId.modId())) {
            finalmap = ModPresetEditors.EDITORS;
        }
        return (V) original.call(finalmap, key);
    }
}
