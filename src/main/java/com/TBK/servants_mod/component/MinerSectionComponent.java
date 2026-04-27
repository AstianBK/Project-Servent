package com.TBK.servants_mod.component;

import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.VoxelBuffer;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

public class MinerSectionComponent implements Component<EntityStore> {

    public static final BuilderCodec<MinerSectionComponent> CODEC =
            BuilderCodec.builder(MinerSectionComponent.class, MinerSectionComponent::new)
                    .versioned()
                    .codecVersion(1).append(new KeyedCodec<>("TargetsPos",  ArrayCodec.ofBuilderCodec(Vector3i.CODEC, Vector3i[]::new)),
                            (o, v) -> o.targetPos = v, o -> o.targetPos
                    ).add()
                    .build();
    public Vector3i[] targetPos;

    public MinerSectionComponent() {

    }    @Override
    public @Nullable Component<EntityStore> clone() {
        MinerSectionComponent minerComponent = new MinerSectionComponent();
        minerComponent.targetPos = this.targetPos;
        return minerComponent;
    }
}
