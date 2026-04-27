package com.TBK.servants_mod.sensor;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.MinerComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
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

    private final PositionProvider positionProvider = new PositionProvider();

    private final int slot;
    public SensorStuck(BuilderSensorStuck sensorBase, BuilderSupport builderSupport) {
        super(sensorBase);
        this.maxMoveDistance = sensorBase.getMaxMoveDistance(builderSupport);
        this.timeToStuck = sensorBase.getTimeToStuck(builderSupport);
        this.slot = builderSupport.getTargetSlot("Pos");
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

        MinerComponent component = store.getComponent(ref, ServantMod.MINER_COMPONENT);
        if(component!=null && component.cooldownTP==0){
            Vector3d target = role.getMarkedEntitySupport().getStoredPosition(slot);
            Vector3i pos = new Vector3i(MathUtil.floor(target.getX()), MathUtil.floor( target.getY()), MathUtil.floor(target.getZ()));

            double dist = currentPos.distanceTo(lastPos);

            BlockType type = store.getExternalData().getWorld().getBlockType(pos);
            if (type==null || type.getId().equals("Empty") || store.getExternalData().getWorld().getFluidId(pos.x,pos.y,pos.z)>0){
                role.getStateSupport().setState(ref,"SearchNextBlock",null,store);
                return false;
            }
            if (dist < maxMoveDistance) {
                component.breakTime += (float) dt;
            } else {
                component.breakTime = 0;
            }

            lastPos.assign(currentPos);

            this.positionProvider.setTarget(this.lastPos);

            if(component.breakTime >= timeToStuck){
                component.cooldownTP=200;
                return true;
            }
            return false;
        }

        return false;
    }

    @Override
    public @Nullable InfoProvider getSensorInfo() {
        return null;
    }
}