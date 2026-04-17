package com.TBK.servants_mod.action;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.MinerComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jspecify.annotations.NonNull;

public class MinerAction extends ActionBase {
    public MinerAction(@NonNull BuilderActionBase builderActionBase) {
        super(builderActionBase);
    }

    @Override
    public boolean canExecute(@NonNull Ref<EntityStore> ref, @NonNull Role role, InfoProvider sensorInfo, double dt, @NonNull Store<EntityStore> store) {
        return super.canExecute(ref, role, sensorInfo, dt, store);
    }

    @Override
    public boolean execute(@NonNull Ref<EntityStore> ref, @NonNull Role role, InfoProvider sensorInfo, double dt, @NonNull Store<EntityStore> store) {
        if (!super.execute(ref, role, sensorInfo, dt, store))return false;
        MinerComponent miner = store.getComponent(ref, ServantMod.MINER_COMPONENT);
        if(miner == null){
            return false;
        }
        NPCEntity npc = store.getComponent(ref,NPCEntity.getComponentType());
        if(npc==null){
            return false;
        }


        TransformComponent transformComponent = store.getComponent(ref,TransformComponent.getComponentType());

        World world = npc.getWorld();
        if (!sensorInfo.hasPosition()) return false;
        if(npc.getRole() == null)return false;



        Vector3i lol = new Vector3i(MathUtil.floor(sensorInfo.getPositionProvider().getX()), MathUtil.floor( sensorInfo.getPositionProvider().getY()), MathUtil.floor(sensorInfo.getPositionProvider().getZ()));
        Blackboard blackboard = (Blackboard) store.getResource(Blackboard.getResourceType());

        ResourceView resourceView = (ResourceView) blackboard.getView(ResourceView.class, ResourceView.indexViewFromWorldPosition(lol.toVector3d()));

        ChunkStore chunkStore = world.getChunkStore();
        Store<ChunkStore> chunkStoreStore = chunkStore.getStore();
        long chunkIndex = ChunkUtil.indexChunkFromBlock(lol.x, lol.z);
        Ref<ChunkStore> chunkReference = chunkStore.getChunkReference(chunkIndex);

        BlockType type = world.getBlockType(lol.x,lol.y,lol.z);



        if(transformComponent.getPosition().distanceTo(lol)<5.0F){
            if(type!=null && !type.getId().equals("Empty")){
                BlockHarvestUtils.performBlockDamage(npc,ref,lol,npc.getInventory().getItemInHand(),null,null,false,10.0F,3,chunkReference,store,chunkStoreStore);
            }else{
                npc.getRole().getStateSupport().setState(ref,"SearchNextBlock",null,store);
                resourceView.clearReservation(ref);
            }
        }else {
            npc.getRole().getStateSupport().setState(ref,"Go_Mine",null,store);
            resourceView.clearReservation(ref);
        }

        return true;
    }
}
