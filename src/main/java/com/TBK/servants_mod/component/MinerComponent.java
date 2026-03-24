package com.TBK.servants_mod.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

public class MinerComponent implements Component<EntityStore> {
    public static final BuilderCodec<MinerComponent> CODEC =
            BuilderCodec.builder(MinerComponent.class, MinerComponent::new)
                    .versioned()
                    .codecVersion(1)

                    .append(
                            new KeyedCodec<>("TargetPos", Vector3i.CODEC),
                            (o, v) -> o.targetPos = v,
                            o -> o.targetPos
                    ).add()

                    .append(
                            new KeyedCodec<>("Progress", Codec.FLOAT),
                            (o, v) -> o.progress = v,
                            o -> o.progress
                    ).add()

                    .append(
                            new KeyedCodec<>("BreakTime", Codec.FLOAT),
                            (o, v) -> o.breakTime = v,
                            o -> o.breakTime
                    ).add()

                    .append(
                            new KeyedCodec<>("Range", Codec.FLOAT),
                            (o, v) -> o.range = v,
                            o -> o.range
                    ).add()

                    .append(
                            new KeyedCodec<>("Mining", Codec.BOOLEAN),
                            (o, v) -> o.mining = v,
                            o -> o.mining
                    ).add()

                    .build();
    /** Posición del bloque que está minando */
    public Vector3i targetPos;

    /** Progreso actual de minado (segundos) */
    public float progress;

    /** Tiempo total necesario para romper el bloque */
    public float breakTime;

    /** Alcance máximo del minero */
    public float range = 4.5f;

    /** Está minando actualmente */
    public boolean mining;

    public MinerComponent() {
        this.progress = 0f;
        this.breakTime = 1.5f;
        this.mining = false;
    }    @Override
    public @Nullable Component<EntityStore> clone() {
        MinerComponent minerComponent = new MinerComponent();
        minerComponent.mining = this.mining;
        minerComponent.targetPos = this.targetPos;
        minerComponent.breakTime = this.breakTime;
        minerComponent.progress = this.progress;
        return minerComponent;
    }
}
