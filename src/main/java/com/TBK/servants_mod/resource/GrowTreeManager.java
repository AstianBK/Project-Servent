package com.TBK.servants_mod.resource;

import com.TBK.servants_mod.component.LumberjackZoneComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

public class GrowTreeManager implements Resource<EntityStore> {
    public static final BuilderCodec<GrowTreeManager> CODEC =
            BuilderCodec.builder(GrowTreeManager.class, GrowTreeManager::new)
                    .versioned()
                    .codecVersion(1)
                    .build();
    public Vector3i saplingPos;

    public void addPos(Vector3i vector3i){
        saplingPos.add(vector3i);
    }

    @Override
    public @Nullable Resource<EntityStore> clone() {
        GrowTreeManager growTreeManager = new GrowTreeManager();
        growTreeManager.saplingPos = saplingPos;
        return growTreeManager;
    }
}
