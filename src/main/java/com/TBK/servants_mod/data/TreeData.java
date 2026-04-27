package com.TBK.servants_mod.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.simple.BooleanCodec;
import com.hypixel.hytale.codec.codecs.simple.IntegerCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TreeData {

    public static final BuilderCodec<TreeData> CODEC =
            BuilderCodec.builder(TreeData.class, TreeData::new)
                    .versioned()
                    .codecVersion(1)
                    .append(new KeyedCodec<>("Center", Vector3i.CODEC),
                            (o, v) -> o.center = v,
                            o -> o.center
                    ).add().append(new KeyedCodec<>("Reserved", BooleanCodec.BOOLEAN),
                            (o, v) -> o.reserved = v,
                            o -> o.reserved
                    ).add().append(new KeyedCodec<>("TopY", IntegerCodec.INTEGER)
                            ,(o,s)->o.topY = s
                            ,o->o.topY)
                    .add().build();
    public Set<Vector3i> logs = new HashSet<>();
    public Set<Vector3i> surfaceLogs = new HashSet<>();
    public int topY;
    public boolean reserved = false;
    public Ref<EntityStore> assignedNpc = null;

    public Vector3i center;

    public double distanceTo(Vector3i pos) {
        return center.distanceTo(pos);
    }
}