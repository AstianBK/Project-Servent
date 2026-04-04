package com.TBK.servants_mod.sensor;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SensorCheckMinerState extends SensorBase {
    protected final PositionProvider positionProvider = new PositionProvider();

    public SensorCheckMinerState(@NonNull BuilderSensorBase builderSensorBase) {
        super(builderSensorBase);
    }

    @Override
    public boolean matches(@NonNull Ref<EntityStore> ref, @NonNull Role role, double dt, @NonNull Store<EntityStore> store) {

        return super.matches(ref, role, dt, store);
    }

    @Override
    public @Nullable InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
