package com.TBK.servants_mod.sensor;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class SensorStuck extends SensorBase {
    private final double maxMoveDistance;

    private final double timeToStuck;

    private Vector3d lastPos = null;

    private double stuckTimer = 0;
    private final PositionProvider positionProvider = new PositionProvider();

    public SensorStuck(BuilderSensorStuck sensorBase, BuilderSupport builderSupport) {
        super(sensorBase);
        this.maxMoveDistance = sensorBase.getMaxMoveDistance(builderSupport);
        this.timeToStuck = sensorBase.getTimeToStuck(builderSupport);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return false;

        Vector3d currentPos = transform.getPosition();

        if (lastPos == null) {
            lastPos = new Vector3d(currentPos);
            return false;
        }

        double dist = currentPos.distanceTo(lastPos);

        if (dist < maxMoveDistance) {
            stuckTimer += dt;
        } else {
            stuckTimer = 0;
        }

        lastPos.assign(currentPos);

        this.positionProvider.setTarget(this.lastPos);
        return stuckTimer >= timeToStuck;
    }

    @Override
    public @Nullable InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}