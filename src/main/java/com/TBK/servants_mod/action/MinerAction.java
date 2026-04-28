package com.TBK.servants_mod.action;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.MinerSectionComponent;
import com.TBK.servants_mod.component.TreeManagerComponent;
import com.TBK.servants_mod.data.TreeData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MinerAction extends ActionBase {
    public int slot;
    public MinerAction(@NonNull BuilderActionBase builderActionBase, BuilderSupport builderSupport) {
        super(builderActionBase);
        slot = builderSupport.getTargetSlot("Pos");
    }

    @Override
    public boolean canExecute(@NonNull Ref<EntityStore> ref, @NonNull Role role, InfoProvider sensorInfo, double dt, @NonNull Store<EntityStore> store) {
        return super.canExecute(ref, role, sensorInfo, dt, store);
    }

    @Override
    public boolean execute(@NonNull Ref<EntityStore> ref, @NonNull Role role, InfoProvider sensorInfo, double dt, @NonNull Store<EntityStore> store) {
        if (!super.execute(ref, role, sensorInfo, dt, store))return false;

        NPCEntity npc = store.getComponent(ref,NPCEntity.getComponentType());
        if(npc==null){
            return false;
        }


        MinerSectionComponent sectionComponent  = store.getComponent(ref, ServantMod.MINER_SECTION_COMPONENT);

        if(sectionComponent!=null){
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

            TreeManagerComponent component = store.getResource(ServantMod.TREE_MANAGER_COMPONENT);

            if(type!=null && !type.getId().equals("Empty")){
                BlockHarvestUtils.performBlockDamage(npc,ref,lol,npc.getInventory().getItemInHand(),null,null,false,2.0F,3,chunkReference,store,chunkStoreStore);
            }else{
                int newSize = Math.max(sectionComponent.targetPos.length-1,0);
                if(newSize>0){
                    List<Vector3i> nextPositions = new ArrayList<>();

                    for (int i = 0 ; i < sectionComponent.targetPos.length ; i++){
                        Vector3i vector3i = sectionComponent.targetPos[i];
                        if(vector3i==null)continue;
                        if (vector3i.x != lol.x || vector3i.y != lol.y || vector3i.z != lol.z){
                            nextPositions.add(vector3i);
                        }
                    }

                    if (newSize>1){
                        Vector3i[] newVectorTarget = new Vector3i[newSize];
                        int i = 0;
                        nextPositions = nextPositions.stream()
                                .sorted(Comparator
                                        .comparingInt((Vector3i v) -> v.y)
                                        .thenComparingDouble(v -> v.distanceTo(transformComponent.getPosition().toVector3i()))
                                )
                                .toList();
                        for (Vector3i v : nextPositions){
                            if(i<newSize){
                                newVectorTarget[i]= v;
                                i++;
                            }
                        }

                        sectionComponent.targetPos = newVectorTarget;
                        if (sectionComponent.targetPos[0]!=null){
                            role.getMarkedEntitySupport().getStoredPosition(slot).assign(sectionComponent.targetPos[0]);
                            npc.getRole().getStateSupport().setState(ref,"Go_Mine",null,store);
                        }else {
                            removeTreeData(lol,component);
                            npc.getRole().getStateSupport().setState(ref,"SearchNextBlock",null,store);
                            resourceView.clearReservation(ref);
                        }
                    }else {
                        removeTreeData(lol,component);
                        npc.getRole().getStateSupport().setState(ref,"SearchNextBlock",null,store);
                        resourceView.clearReservation(ref);
                    }

                }else {
                    removeTreeData(lol,component);
                    npc.getRole().getStateSupport().setState(ref,"SearchNextBlock",null,store);
                    resourceView.clearReservation(ref);
                }
            }
            return true;
        }


        return false;
    }

    public void removeTreeData(Vector3i pos,TreeManagerComponent manager){
        List<TreeData> removeList = new ArrayList<>();
        for (TreeData data : manager.treeList){
            if (data!=null && data.center == pos){
                removeList.add(data);
            }
        }
        List<TreeData> finalList = new ArrayList<>();
        for (TreeData data : manager.treeList){
            if (!removeList.contains(data)){
                finalList.add(data);
            }
        }

        manager.treeList = finalList;
        manager.rebuild();
    }
}
