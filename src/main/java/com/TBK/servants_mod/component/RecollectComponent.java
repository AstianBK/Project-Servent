package com.TBK.servants_mod.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

public class RecollectComponent implements Component<EntityStore> {
    public static final BuilderCodec<RecollectComponent> CODEC =
            BuilderCodec.builder(RecollectComponent.class, RecollectComponent::new)
                    .versioned()
                    .codecVersion(1)

                    .append(
                            new KeyedCodec<>("ChestPos", Vector3i.CODEC),
                            (o, v) -> o.targetPos = v,
                            o -> o.targetPos
                    ).add()
                    .build();
    public Vector3i targetPos;

    public RecollectComponent() {

    }    @Override
    public @Nullable Component<EntityStore> clone() {
        RecollectComponent minerComponent = new RecollectComponent();
        minerComponent.targetPos = this.targetPos;
        return minerComponent;
    }
}
