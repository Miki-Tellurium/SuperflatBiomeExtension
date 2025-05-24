package com.mikitellurium.superflatbiomeextension.util;

import com.mikitellurium.superflatbiomeextension.SuperflatBiomeExtension;
import net.minecraft.util.Identifier;

public class FastId {
    public static Identifier of(String namespace, String id) {
        return Identifier.of(namespace, id);
    }

    public static Identifier ofMod(String id) {
        return of(modId(), id);
    }

    public static Identifier ofMc(String id) {
        return Identifier.of(id);
    }

    public static String modId() {
        return SuperflatBiomeExtension.modId();
    }
}
