package com.TBK.servants_mod.sensor;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.MinerComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
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

public class SensorMiner extends SensorBase {
    protected final double range;
    protected final double yRange;
    protected final int blockSet;
    protected final boolean pickRandom;
    protected final boolean reserveBlock;
    protected final PositionProvider positionProvider = new PositionProvider();
    protected final Sensor sensor;
    protected final int slot;
    public SensorMiner(@Nonnull SensorMinerBuilder builder, @Nonnull BuilderSupport support,Sensor sensor) {
        super(builder);

        this.range = builder.getRange(support);
        this.yRange = builder.getYRange(support);
        this.blockSet = builder.getBlockSet(support);
        this.pickRandom = builder.isPickRandom(support);
        this.reserveBlock = builder.isReserveBlock(support);
        this.sensor = sensor;
        this.slot = support.getTargetSlot("MineCenter");
        support.requireBlockTypeBlackboard(this.blockSet);
    }

    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, role, dt, store)) {
            this.positionProvider.clear();
            return false;
        } else {
            if (!this.sensor.matches(ref, role, dt, store)){
                return false;
            }

            World world = ((EntityStore)store.getExternalData()).getWorld();
            TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d entityPos = transformComponent.getPosition();
            NPCEntity npcComponent = (NPCEntity)store.getComponent(ref, NPCEntity.getComponentType());

            assert npcComponent != null;
            MinerComponent minerComponent = store.getComponent(ref, ServantMod.MINER_COMPONENT);

            int width = minerComponent.targetPos.x / 2;
            int height = minerComponent.targetPos.y;

            InfoProvider infoProvider = sensor.getSensorInfo();
            if(infoProvider!=null && infoProvider.getPositionProvider()!=null){
                Vector3d pos = new Vector3d(infoProvider.getPositionProvider().getX(),infoProvider.getPositionProvider().getY(),infoProvider.getPositionProvider().getZ());

                if(pos.equals(Vector3d.MIN)){
                    return false;
                }

                for (int x = -width; x < width+1; x++) {
                    for (int z = -width; z < width+1; z++) {

                        int bx = (int) (pos.x + x);
                        int by = (int) pos.y;
                        int bz = (int) (pos.z + z);


                        BlockType type = world.getBlockType(bx, by, bz);

                        if (type == null) {
                            continue;
                        }

                        if (type.getId().equals("Empty")) {
                            continue;
                        }

                        Blackboard blackboard = (Blackboard) store.getResource(Blackboard.getResourceType());

                        ResourceView resourceView = (ResourceView) blackboard.getView(ResourceView.class, ResourceView.indexViewFromWorldPosition(pos));

                        if (resourceView.isBlockReserved(bx, by, bz)) {
                            continue;
                        }

                        resourceView.reserveBlock(npcComponent, bx, by, bz);

                        pos.assign(bx+0.5, by, bz+0.5);

                        this.positionProvider.setTarget(pos);


                        return true;
                    }
                    if(x == width){
                        role.getMarkedEntitySupport().getStoredPosition(slot).assign(pos.x,pos.y-1,pos.z);
                    }
                }
            }
        }
        this.positionProvider.clear();
        this.sensor.getSensorInfo().getPositionProvider().clear();
        return false;
    }

    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
