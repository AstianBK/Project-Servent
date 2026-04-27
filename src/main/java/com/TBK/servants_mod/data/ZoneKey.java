package com.TBK.servants_mod.data;

import com.hypixel.hytale.math.vector.Vector3i;

public record ZoneKey(int x, int z) {

    public static ZoneKey from(Vector3i pos) {
        return new ZoneKey(pos.x / 50, pos.z / 50);
    }
}