package com.mikitellurium.superflatbiomeextension.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mikitellurium.superflatbiomeextension.client.ModPresetEditors;
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
        PresetEditor editor = original.call(map, key);
        if (editor == null) {
            editor = ModPresetEditors.EDITORS.get((Optional<ResourceKey<WorldPreset>>) key);
        }
        return (V) editor;
    }
}
