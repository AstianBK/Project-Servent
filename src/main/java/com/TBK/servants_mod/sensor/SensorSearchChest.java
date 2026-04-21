package com.TBK.servants_mod.sensor;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.MinerComponent;
import com.TBK.servants_mod.component.RecollectComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;

import javax.annotation.Nonnull;

public class SensorSearchChest extends SensorBase {
    protected final PositionProvider positionProvider = new PositionProvider();
    protected final int slot;

    public SensorSearchChest(@Nonnull BuilderSensorSearchChest builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.slot = support.getTargetSlot("ChestPos");
    }

    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
        ServantMod.LOGGER.atInfo().log("Match");
        if (!super.matches(ref, role, dt, store)) {
            this.positionProvider.clear();
            return false;
        } else {
            ServantMod.LOGGER.atInfo().log("primer test pasado");
            RecollectComponent chestComponent = store.getComponent(ref, ServantMod.RECOLLECT_COMPONENT);
            assert chestComponent != null;

            Vector3i pos = chestComponent.targetPos;
            ServantMod.LOGGER.atInfo().log("chestComponent existe : %s",chestComponent);

            if (pos!=null){
                ServantMod.LOGGER.atInfo().log("LO %s",pos);
                this.positionProvider.setTarget(pos.toVector3d());

                role.getMarkedEntitySupport().getStoredPosition(slot).assign(pos.x,pos.y,pos.z);
                return true;
            }

        }
        this.positionProvider.clear();

        return false;
    }

    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
