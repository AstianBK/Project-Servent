package com.TBK.servants_mod.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

public class AreaOrderComponent implements Component<EntityStore> {
    public static final BuilderCodec<AreaOrderComponent> CODEC =
            BuilderCodec.builder(AreaOrderComponent.class, AreaOrderComponent::new)
                    .versioned()
                    .codecVersion(1)

                    .append(
                            new KeyedCodec<>("Width", Codec.FLOAT),
                            (o, v) -> o.width = v,
                            o -> o.width
                    ).add()

                    .build();

    /** Ancho (por ejemplo de brush, mining area, etc) */
    public float width;

    public AreaOrderComponent() {
        this.width = 9.0f; // default
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        AreaOrderComponent c = new AreaOrderComponent();
        c.width = this.width;
        return c;
    }
}
