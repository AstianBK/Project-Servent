package com.TBK.servants_mod.sensor;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.ServantUtil;
import com.TBK.servants_mod.component.LumberjackZoneComponent;
import com.TBK.servants_mod.component.MinerComponent;
import com.TBK.servants_mod.component.MinerSectionComponent;
import com.TBK.servants_mod.data.TreeData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SensorLumberJack extends SensorBase {
    protected final double range;
    protected final double yRange;
    protected final int blockSet;
    protected final boolean pickRandom;
    protected final boolean reserveBlock;
    protected final PositionProvider positionProvider = new PositionProvider();
    protected final Sensor sensor;
    protected final int slot;
    protected final int slotPath;
    protected final int slotMessage;
    public SensorLumberJack(@Nonnull BuilderLumberJackSensor builder, @Nonnull BuilderSupport support, Sensor sensor) {
        super(builder);
        this.range = builder.getRange(support);
        this.yRange = builder.getYRange(support);
        this.blockSet = builder.getBlockSet(support);
        this.pickRandom = builder.isPickRandom(support);
        this.reserveBlock = builder.isReserveBlock(support);
        this.sensor = sensor;
        this.slot = support.getTargetSlot("LumberJackCenter");
        this.slotPath = support.getTargetSlot("Pos");
        this.slotMessage = support.getBeaconMessageSlot("MineRequest");
        support.requireBlockTypeBlackboard(this.blockSet);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, role, dt, store)) {
            this.positionProvider.clear();
            return false;
        }

        if (!this.sensor.matches(ref, role, dt, store)) {
            return false;
        }

        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (npc == null || transform == null) return false;
        World world = npc.getWorld();

        Blackboard blackboard = store.getResource(Blackboard.getResourceType());

        BeaconSupport message = store.getComponent(ref,BeaconSupport.getComponentType());

        if (message == null) {
            this.positionProvider.clear();
            return false;
        }

        Ref<EntityStore> senderRef = message.getMessageSlots()[slotMessage].getTarget();

        if (senderRef == null || !senderRef.isValid()) {
            this.positionProvider.clear();
            return false;
        }

        LumberjackZoneComponent zone = store.getComponent(senderRef, ServantMod.LUMBERJACK_COMPONENT);

        if (zone == null || zone.trees == null || zone.trees.isEmpty()) {
            this.positionProvider.clear();
            return false;
        }

        Vector3i best = null;
        double bestDist = Double.MAX_VALUE;

        Vector3i npcPos = transform.getPosition().toVector3i();


        for (TreeData tree : zone.trees) {

            Vector3i base = tree.center;
            ResourceView resourceView = (ResourceView) blackboard.getView(
                    ResourceView.class,
                    ResourceView.indexViewFromWorldPosition(base.toVector3d())
            );
            if (!resourceView.isBlockReserved(base.x,base.y,base.z) && !ServantUtil.isAir(world,base.x,base.y,base.z)){
                double dist = npcPos.distanceTo(base);

                if (dist < bestDist) {
                    bestDist = dist;
                    best = base;
                }
            }
        }

        if (best == null) {
            this.positionProvider.clear();
            return false;
        }
        ResourceView resourceView = (ResourceView) blackboard.getView(ResourceView.class, ResourceView.indexViewFromWorldPosition(best.toVector3d()));

        if (resourceView != null) {
            resourceView.reserveBlock(npc, best.x, best.y, best.z);
        }

        MinerSectionComponent section = store.getComponent(ref, ServantMod.MINER_SECTION_COMPONENT);
        if (section == null) {
            section = new MinerSectionComponent();
            store.addComponent(ref, ServantMod.MINER_SECTION_COMPONENT, section);
        }


        List<Vector3i> list = new ArrayList<>();
        searchLogs(world,resourceView,best,list,best.y);
        list.add(best);
        section.targetPos = list.toArray(Vector3i[]::new);

        zone.removeTreeData(best);
        Vector3d target = new Vector3d(best.x + 0.5, best.y, best.z + 0.5);


        this.positionProvider.setTarget(target);
        return true;
    }

    private boolean isLog(World world, int x, int y, int z) {
        BlockType t = world.getBlockType(x, y, z);

        return t != null && t.getId().contains("Wood") && !t.getId().contains("Sticks") ;
    }

    private void searchLogs(World world,ResourceView resourceView,Vector3i pos,List<Vector3i> list,int yOrigin){
        int maxY = yOrigin + 1;
        for (int x = -1; x <= 1;x++){
            for (int z=-1 ; z<=1 ; z++){
                for (int y = 0 ; y<2 ; y++){
                    if(pos.y+1 > maxY)return;
                    if (x==0 && z==0 && y==0)continue;
                    int bx = (int) pos.x + x;
                    int by = (int) pos.y + y;
                    int bz = (int) pos.z + z;
                    Vector3i base = new Vector3i( bx, by, bz);
                    if (!isLog(world, bx, by, bz) || list.contains(base)) continue;


                    if (resourceView.isBlockReserved(base.x, base.y, base.z)) continue;

                    list.add(new Vector3i(bx,by,bz));
                    searchLogs(world,resourceView,base,list,yOrigin);
                }

            }
        }
    }

//    private boolean isLeaves(World world, int x, int y, int z) {
//        BlockType t = world.getBlockType(x, y, z);
//        return t != null && t.getId().contains("Leaves");
//    }

//    private boolean isTree(World world, int x, int y, int z) {
//
//        if (!isLog(world, x, y, z)) return false;
//
//        Vector3i base = getTreeBase(world, x, y, z);
//        if (base == null) return false;
//
//        // medir altura desde base
//        int height = 0;
//        int topY = base.y;
//
//        while (isLog(world, x, topY, z) && height < 30) {
//            topY++;
//            height++;
//        }
//
//        return true;
//    }


    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
