package com.TBK.servants_mod.component;

import com.TBK.servants_mod.data.TreeData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LumberjackZoneComponent implements Component<EntityStore> {

    public Vector3i center;
    public int radius = 50;

    public transient List<TreeData> trees = new ArrayList<>();
    public transient boolean dirty = true;

    public LumberjackZoneComponent() {}


    public static final BuilderCodec<LumberjackZoneComponent> CODEC =
            BuilderCodec.builder(LumberjackZoneComponent.class, LumberjackZoneComponent::new)
                    .versioned()
                    .codecVersion(1)
                    .append(new KeyedCodec<>("Center", Vector3i.CODEC),
                            (o, v) -> o.center = v,
                            o -> o.center
                    ).add()
                    .append(new KeyedCodec<>("Radius", Codec.INTEGER),
                            (o, v) -> o.radius = v,
                            o -> o.radius
                    )
                    .add()
                    .build();

    public void removeTreeData(Vector3i data){
        List<TreeData> aux = new ArrayList<>();
        for (TreeData data1 : trees){
            if (data1.center == data){
                aux.add(data1);
            }
        }
        List<TreeData> replaceList = new ArrayList<>();
        for (TreeData data1 : trees){
            if (!aux.contains(data1)){
                replaceList.add(data1);
            }
        }
        trees = replaceList;
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        LumberjackZoneComponent copy = new LumberjackZoneComponent();
        copy.center = this.center;
        copy.radius = this.radius;

        // runtime reset
        copy.dirty = true;

        return copy;
    }
}