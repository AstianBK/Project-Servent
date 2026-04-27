package com.TBK.servants_mod.sensor;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.MinerComponent;
import com.TBK.servants_mod.component.MinerSectionComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
            MinerSectionComponent sectionComponent = store.getComponent(ref,ServantMod.MINER_SECTION_COMPONENT);
            if(sectionComponent==null){
                sectionComponent=new MinerSectionComponent();
                store.addComponent(ref,ServantMod.MINER_SECTION_COMPONENT,sectionComponent);
            }
            List<Vector3i> list = new ArrayList<>();
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

                            if(type.getId().equals("Rock_Bedrock")){
                                CompletableFuture.runAsync(npcComponent::remove,world);

                                return false;
                            }
                            if(world.getFluidId(bx,by,bz) > 0){
                                CompletableFuture.runAsync(npcComponent::remove,world);
                                return false;
                            }
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
                    list.add(best);

                    sectionComponent.targetPos = list.toArray(Vector3i[]::new);
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
                double angle = pasoAngular * pos.y;

                int xA = (int) Math.round(sin / max * width);
                int zA = (int) Math.round(cos / max * width);
                double dx = Math.cos(angle);
                double dz = -Math.sin(angle);
                Rotation yaw;

                if (Math.abs(dx) > Math.abs(dz)) {
                    yaw = dx > 0 ? Rotation.TwoSeventy : Rotation.Ninety;
                } else {
                    yaw = dz > 0 ? Rotation.OneEighty : Rotation.None;
                }

                if(minerComponent.heightForSlab<=0 && world.getBlockType((int) (pos.x+xA), (int)(pos.y + 2), (int) (pos.z+zA)).getId().equals("Empty")){
                    int bx = (int) (pos.x + xA);
                    int by = (int) (pos.y + 2);
                    int bz = (int) (pos.z + zA);

                    resourceView.reserveBlock(npcComponent, bx, by, bz);



                    int blockId = BlockType.getAssetMap().getIndex("Rock_Magma_Cooled_Brick_Stairs");;

                    world.getChunk(ChunkUtil.indexChunkFromBlock(bx, bz)).setBlock(bx, by, bz, blockId, BlockType.getAssetMap().getAsset(blockId), RotationTuple.index(yaw, Rotation.None, Rotation.None), 0, 0);

                    world.getNotificationHandler().sendBlockParticle(bx + 0.5, by + 1.5, bz + 0.5, BlockType.getBlockIdOrUnknown("Rock_Crystal_Red_Block"," "), BlockParticleEvent.Break);
                }
                if (minerComponent.heightForSlab>0){
                    minerComponent.heightForSlab--;
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
