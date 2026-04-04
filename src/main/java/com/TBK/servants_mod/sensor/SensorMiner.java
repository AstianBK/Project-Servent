package com.TBK.servants_mod.sensor;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.MinerComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector2i;
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

                Vector3i best = null;
                double bestDist = Double.MAX_VALUE;
                Blackboard blackboard = (Blackboard) store.getResource(Blackboard.getResourceType());

                ResourceView resourceView = (ResourceView) blackboard.getView(ResourceView.class, ResourceView.indexViewFromWorldPosition(pos));

                for (int r = 0; r <= width; r++) {
                    for (int x = -r; x <= r; x++) {
                        for (int z = -r; z <= r; z++) {

                            if (Math.abs(x) != r && Math.abs(z) != r) continue;

                            int bx = (int) pos.x + x;
                            int by = (int) pos.y;
                            int bz = (int) pos.z + z;

                            BlockType type = world.getBlockType(bx, by, bz);
                            if (type == null || type.getId().equals("Empty")) continue;

                            if (resourceView.isBlockReserved(bx, by, bz)) continue;

                            double dist = entityPos.distanceTo(bx, by, bz);

                            if (dist < bestDist) {
                                bestDist = dist;
                                best = new Vector3i(bx, by, bz);
                            }
                        }
                    }
                }
                if (best != null) {
                    resourceView.reserveBlock(npcComponent, best.x, best.y, best.z);

                    pos.assign(best.x + 0.5, best.y, best.z + 0.5);
                    this.positionProvider.setTarget(pos);

                    return true;
                }
                double bloquesPorVuelta = 4 * (minerComponent.targetPos.x-1);

                double pasoAngular = (2 * Math.PI) / (bloquesPorVuelta);

                double sin = Math.sin(pasoAngular * pos.y) * width;
                double cos = Math.cos(pasoAngular * pos.y) * width;

                double max = Math.max(Math.abs(sin), Math.abs(cos));

                int xA = (int) Math.round(sin / max * width);
                int zA = (int) Math.round(cos / max * width);


                if(world.getBlockType((int) (pos.x+xA), (int)(pos.y + 1), (int) (pos.z+zA)).getId().equals("Empty")){
                    world.setBlock((int) (pos.x+xA), (int)(pos.y + 1), (int) (pos.z+zA), "Rock_Stone_Cobble");
                    world.getNotificationHandler().sendBlockParticle((pos.x+xA)+0.5, (pos.y + 1.5),(pos.z+zA)+0.5,BlockType.getBlockIdOrUnknown("Rock_Crystal_Red_Block"," "), BlockParticleEvent.Break);
                }

                role.getMarkedEntitySupport().getStoredPosition(slot).assign(pos.x,pos.y-1,pos.z);
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
