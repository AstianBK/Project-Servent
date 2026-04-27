package com.TBK.servants_mod.sensor;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.LumberjackZoneComponent;
import com.TBK.servants_mod.component.MinerSectionComponent;
import com.TBK.servants_mod.data.TreeData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
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

public class SensorPathLumberJack extends SensorBase {
    protected final double range;
    protected final double yRange;
    protected final int blockSet;
    protected final boolean pickRandom;
    protected final boolean reserveBlock;
    protected final PositionProvider positionProvider = new PositionProvider();
    protected final int slot;
    public SensorPathLumberJack(@Nonnull BuilderPathLumberJackSensor builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.range = builder.getRange(support);
        this.yRange = builder.getYRange(support);
        this.blockSet = builder.getBlockSet(support);
        this.pickRandom = builder.isPickRandom(support);
        this.reserveBlock = builder.isReserveBlock(support);
        this.slot = support.getTargetSlot("Pos");
        support.requireBlockTypeBlackboard(this.blockSet);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {

        if (!super.matches(ref, role, dt, store)) {
            this.positionProvider.clear();
            return false;
        }

        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (npc == null || transform == null) return false;
        World world = npc.getWorld();

        Vector3i best = npc.getRole().getMarkedEntitySupport().getStoredPosition(slot).toVector3i();

        if (best == null) {
            this.positionProvider.clear();
            return false;
        }
        Vector3d target;
        if (!hasPosPathValid(world,best)){
            Vector3i newTarget = getPosPathValid(world,best);
            if (newTarget!=null){
                target = new Vector3d(newTarget.x + 0.5D,newTarget.y,newTarget.z + 0.5D);
            }else {
                target = new Vector3d(best.x + 0.5, best.y, best.z + 0.5);
            }
        }else {
            target = new Vector3d(best.x + 0.5, best.y, best.z + 0.5);
        }
        this.positionProvider.setTarget(target);
        return true;
    }
    private Vector3i getPosPathValid(World world,Vector3i pos){
        for (int x = -1 ; x <= 1 ; x++){
            for (int z = -1 ; z <= 1; z++){
                int bx = pos.x+x;
                int by = pos.y;
                int bz = pos.z+z;
                Vector3i v = new Vector3i(bx,by,bz);
                if(hasPosPathValid(world,v)){
                    return v;
                }
            }
        }

        return null;
    }
    private boolean hasPosPathValid(World world,Vector3i pos){
        BlockType first = world.getBlockType(pos);
        if (first != null && first.getMaterial() == BlockMaterial.Solid){
            BlockType type = world.getBlockType(new Vector3i(pos.x,pos.y+1,pos.z));

            return type==null || type.getId().equals("Empty");
        }
        BlockType above = world.getBlockType(pos.x,pos.y-1,pos.z);
        return above!=null && above.getMaterial()==BlockMaterial.Solid;
    }


    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
