package com.TBK.servants_mod.component;

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
                    .append(
                            new KeyedCodec<>("OriginPos", Vector3i.CODEC),
                            (o, v) -> o.originPos = v,
                            o -> o.originPos
                    ).add()
                    .build();
    public Vector3i targetPos;
    public Vector3i originPos;
    public RecollectComponent() {

    }    @Override
    public @Nullable Component<EntityStore> clone() {
        RecollectComponent recollectComponent = new RecollectComponent();
        recollectComponent.targetPos = this.targetPos;
        recollectComponent.originPos = this.originPos;
        return recollectComponent;
    }
}
