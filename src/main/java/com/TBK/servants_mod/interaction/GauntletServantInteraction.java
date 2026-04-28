package com.TBK.servants_mod.interaction;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.ServantUtil;
import com.TBK.servants_mod.component.*;
import com.TBK.servants_mod.data.TreeData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GauntletServantInteraction  extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC = BuilderCodec.builder(
            GauntletServantInteraction.class, GauntletServantInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@NonNull InteractionType interactionType, @NonNull InteractionContext interactionContext, @NonNull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            ServantMod.LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }
        World world = commandBuffer.getExternalData().getWorld();
        Store<EntityStore> store = commandBuffer.getExternalData().getStore();
        Ref ref = interactionContext.getEntity();
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        ItemStack stack = player.getInventory().getItemInHand();
        BsonDocument meta = stack.getMetadata();



        if(meta==null)meta = new BsonDocument();

        BsonDocument gauntletData;
        String type = "Miner";
        if(meta.containsKey("GauntletType")){
            type = meta.getString("GauntletType").getValue();
            gauntletData = meta.getDocument(type,new BsonDocument());
        }else {
            gauntletData = new BsonDocument();
            meta.append("GauntletType",Codec.STRING.encode(type));
            meta.append(type,Codec.BSON_DOCUMENT.encode(gauntletData));
        }
        BsonDocument finalMeta = meta;
        CompletableFuture.runAsync(()-> player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(finalMeta)),world);

        ServantMod.LOGGER.atInfo().log("Type :%s",type);
        switch (type){
            case "Miner"->{
                summonMinerInteraction(world,player,ref,interactionContext,store,meta,gauntletData);
            }
            case "LumberJack"->{
                summonLumberJackInteraction(world,player,ref,interactionContext,store,meta,gauntletData);
            }
            case "Collect"->{
                summonCollectInteraction(world,player,ref,interactionContext,store,meta,gauntletData);
            }
            default -> {

            }
        }
    }

    public void summonLumberJackInteraction (World world,Player player,Ref<EntityStore> ref, @NonNull InteractionContext interactionContext,Store<EntityStore> store,BsonDocument stackData,BsonDocument meta){
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            ServantMod.LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }

        AreaOrderComponent areaOrderComponent = store.getComponent(ref,ServantMod.AREA_COMPONENT);
        if (areaOrderComponent==null){
            areaOrderComponent = new AreaOrderComponent();
        }



        int width =(int) areaOrderComponent.width;
        MinerComponent component = new MinerComponent();
        component.targetPos = new Vector3i(width,1,width);


        boolean summoning;
        if (meta != null) {
            if(!meta.containsKey("Summoning")){
                meta.append("Summoning", Codec.BOOLEAN.encode(true));
                summoning = true;
            }else{
                boolean flag = meta.getBoolean("Summoning").getValue();
                meta.put("Summoning",Codec.BOOLEAN.encode(!flag));
                summoning = !flag;
            }
        }else {
            summoning = false;
            meta = new BsonDocument();
        }

        ServantMod.LOGGER.atInfo().log("Data %s",meta);

        RootInteraction rootInteraction = null;
        if(ref!=null){
            rootInteraction = RootInteraction.getRootInteractionOrUnknown("Root_Summon_Demon");
            if (summoning && interactionContext.getTargetBlock()!=null){

                double x = interactionContext.getTargetBlock().x;
                double y = interactionContext.getTargetBlock().y;
                double z = interactionContext.getTargetBlock().z;
                Vector3d posOrigin = new Vector3d(x,y+1.5D,z);
                if (meta.containsKey("OriginX")){
                    meta.put("OriginX",Codec.DOUBLE.encode(x));
                    meta.put("OriginY",Codec.DOUBLE.encode(y));
                    meta.put("OriginZ",Codec.DOUBLE.encode(z));
                }else {
                    meta.append("OriginX",Codec.DOUBLE.encode(x));
                    meta.append("OriginY",Codec.DOUBLE.encode(y));
                    meta.append("OriginZ",Codec.DOUBLE.encode(z));
                }
                BsonDocument finalMeta = meta;
                Blackboard blackboard = (Blackboard) store.getResource(Blackboard.getResourceType());

                ResourceView resourceView = (ResourceView) blackboard.getView(ResourceView.class, ResourceView.indexViewFromWorldPosition(posOrigin));
                TreeManagerComponent managerComponent = store.getResource(ServantMod.TREE_MANAGER_COMPONENT);

                scanArea(world,posOrigin.toVector3i(),25,managerComponent,resourceView);
                ArrayList<String> uuids = new ArrayList<>();
                CompletableFuture.runAsync(()->{
                    Pair<Ref<EntityStore>, INonPlayerCharacter> pair = NPCPlugin.get().spawnNPC(store,"miner_orden_entity",null,posOrigin,new Vector3f(90,0));
                    Ref<EntityStore> ref1 = pair.first();
                    LumberjackZoneComponent component1 = new LumberjackZoneComponent();
                    component1.center = posOrigin.toVector3i();
                    component1.dirty = true;


                    store.addComponent(ref1,ServantMod.MINER_COMPONENT,component);
                    store.addComponent(ref1, ServantMod.LUMBERJACK_COMPONENT,component1);
                    uuids.add(((Entity)pair.value()).getUuid().toString());
                },world);

                for (int i = 0 ; i < 2 ; i++){
                    CompletableFuture.runAsync(()->{
                        Entity entity = (Entity) NPCPlugin.get().spawnNPC(store,"ServantLumberJack",null,posOrigin,new Vector3f(90,0)).right();
                        uuids.add(entity.getUuid().toString());
                    },world);
                }
                CompletableFuture.runAsync(()->{
                    finalMeta.append("list",Codec.STRING_ARRAY.encode(uuids.toArray(new String[3])));
                    ItemStack stack = player.getInventory().getItemInHand();
                    stackData.put("LumberJack",Codec.BSON_DOCUMENT.encode(finalMeta));
                    player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(stackData));
                },world);

            }else {
                rootInteraction=RootInteraction.getRootInteractionOrUnknown("Root_Desummon_Demon");

                BsonDocument finalMeta = meta;
                CompletableFuture.runAsync(()->{
                    if (finalMeta.containsKey("list")){
                        for (BsonValue uuid : finalMeta.getArray("list").asArray().getValues()){
                            Ref<EntityStore> ref1=world.getEntityStore().getRefFromUUID(UUID.fromString(uuid.asString().getValue()));
                            if(ref1==null || !ref1.isValid())continue;
                            NPCEntity entity = store.getComponent(ref1, NPCEntity.getComponentType());
                            Vector3d pos = entity.getOldPosition();
                            world.getNotificationHandler().sendBlockParticle(pos.x+0.5F, (pos.y + 1.5),pos.z+0.5F, BlockType.getBlockIdOrUnknown("Rock_Crystal_Red_Block"," "), BlockParticleEvent.Break);

                            entity.remove();
                        }
                    }
                    ItemStack stack = player.getInventory().getItemInHand();


                    player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(stackData));
                },world);
            }
        }


        if(rootInteraction!=null){
            interactionContext.getChain().pushRoot(rootInteraction,false);
        }
    }

    public void summonCollectInteraction (World world,Player player,Ref<EntityStore> ref, @NonNull InteractionContext interactionContext,Store<EntityStore> store,BsonDocument stackData,BsonDocument meta){
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            ServantMod.LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }
        ItemStack stack = player.getInventory().getItemInHand();
        boolean summoning = false;
        RootInteraction rootInteraction = null;

        if (interactionContext.getTargetBlock()==null)return;
        BlockPosition pos = interactionContext.getTargetBlock();
        BlockType type = world.getBlockType(pos.x,pos.y,pos.z);
        MovementStatesComponent states = commandBuffer.getComponent(ref, MovementStatesComponent.getComponentType());
        ServantMod.LOGGER.atInfo().log("Data %s",meta);

        if(states.getMovementStates().crouching){
            if (type!=null && type.getBlockEntity()!=null && type.getBlockEntity().getComponent(ItemContainerBlock.getComponentType())!=null){
                meta.put("ChestX",Codec.INTEGER.encode(pos.x));
                meta.put("ChestY",Codec.INTEGER.encode(pos.y));
                meta.put("ChestZ",Codec.INTEGER.encode(pos.z));
                meta.put("HasChest",Codec.BOOLEAN.encode(true));
                player.sendMessage(Message.raw("Chest Selected!").color(Color.GREEN));
            }

            CompletableFuture.runAsync(()->{
                stackData.put("Collect",Codec.BSON_DOCUMENT.encode(meta));
                player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(stackData));
            },world);
        }else {
            if(meta.containsKey("HasChest")){
                if(meta.containsKey("Summoning")){
                    if (meta.getBoolean("Summoning").getValue()){
                        summoning = false;
                        meta.put("Summoning", Codec.BOOLEAN.encode(false));
                    }else {
                        summoning = true;
                        meta.put("Summoning", Codec.BOOLEAN.encode(true));
                    }
                }else {
                    meta.append("Summoning", Codec.BOOLEAN.encode(true));
                    summoning = true;
                }
            }
            BsonDocument finalMeta = meta;
            if (summoning){
                rootInteraction=RootInteraction.getRootInteractionOrUnknown("Root_Desummon_Demon");

                CompletableFuture.runAsync(()->{
                    if (finalMeta.containsKey("list")){
                        for (BsonValue uuid : finalMeta.getArray("list").asArray().getValues()){
                            Ref<EntityStore> ref1=world.getEntityStore().getRefFromUUID(UUID.fromString(uuid.asString().getValue()));
                            if(ref1==null || !ref1.isValid())continue;
                            NPCEntity entity = store.getComponent(ref1, NPCEntity.getComponentType());
                            Vector3d pos1 = entity.getOldPosition();
                            world.getNotificationHandler().sendBlockParticle(pos1.x+0.5F, (pos1.y + 1.5),pos1.z+0.5F, BlockType.getBlockIdOrUnknown("Rock_Crystal_Red_Block"," "), BlockParticleEvent.Break);

                            entity.remove();
                        }
                    }

                    player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(stackData));
                },world);
            }else {
                rootInteraction = RootInteraction.getRootInteractionOrUnknown("Root_Summon_Demon");

                List<String> uuids = new ArrayList<>();
                CompletableFuture.runAsync(()->{
                    if (finalMeta.containsKey("ChestX")){
                        int x = finalMeta.getInt32("ChestX").getValue();
                        int y = finalMeta.getInt32("ChestY").getValue();
                        int z = finalMeta.getInt32("ChestZ").getValue();
                        Pair<Ref<EntityStore>, INonPlayerCharacter> pair = NPCPlugin.get().spawnNPC(store,"ServantCollect",null,new Vector3d(pos.x,pos.y+1.5D,pos.z),new Vector3f(90,0));

                        RecollectComponent component = new RecollectComponent();
                        component.targetPos = new Vector3i(x,y,z);
                        component.originPos = new Vector3i(pos.x,pos.y+1,pos.z);
                        Ref<EntityStore> ref1 = pair.first();
                        store.addComponent(ref1,ServantMod.RECOLLECT_COMPONENT,component);
                        uuids.add( ((Entity)pair.right()).getUuid().toString());
                    }
                },world);

                CompletableFuture.runAsync(()->{
                    finalMeta.append("list",Codec.STRING_ARRAY.encode(uuids.toArray(new String[1])));
                    stackData.put("Collect",Codec.BSON_DOCUMENT.encode(finalMeta));
                    player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(stackData));
                },world);
            }
            if(rootInteraction!=null){
                interactionContext.getChain().pushRoot(rootInteraction,false);
            }
        }

    }

    public void summonMinerInteraction (World world,Player player,Ref<EntityStore> ref, @NonNull InteractionContext interactionContext,Store<EntityStore> store,BsonDocument stackData,BsonDocument meta){
        AreaOrderComponent areaOrderComponent = store.getComponent(ref,ServantMod.AREA_COMPONENT);
        if (areaOrderComponent==null){
            areaOrderComponent = new AreaOrderComponent();
        }



        int width =(int) areaOrderComponent.width;
        MinerComponent component = new MinerComponent();
        component.targetPos = new Vector3i(width,1,width);

        ServantMod.LOGGER.atInfo().log("Data %s",meta);
        boolean summoning;
        if (meta != null) {
            if(!meta.containsKey("Summoning")){
                meta.append("Summoning", Codec.BOOLEAN.encode(true));
                summoning = true;
            }else{
                boolean flag = meta.getBoolean("Summoning").getValue();
                ServantMod.LOGGER.atInfo().log("Bandera :%s",flag);
                meta.put("Summoning",Codec.BOOLEAN.encode(!flag));
                summoning = !flag;
            }
        }else {
            summoning = false;
            meta = new BsonDocument();
        }



        RootInteraction rootInteraction = null;
        if(ref!=null){
            rootInteraction = RootInteraction.getRootInteractionOrUnknown("Root_Summon_Demon");
            if (summoning && interactionContext.getTargetBlock()!=null){

                double x = interactionContext.getTargetBlock().x;
                double y = interactionContext.getTargetBlock().y;
                double z = interactionContext.getTargetBlock().z;
                Vector3d posOrigin = new Vector3d(x,y+1.5D,z);
                BsonDocument finalMeta = meta;
                ArrayList<String> uuids = new ArrayList<>();
                CompletableFuture.runAsync(()->{
                    Pair<Ref<EntityStore>, INonPlayerCharacter> pair = NPCPlugin.get().spawnNPC(store,"miner_orden_entity",null,posOrigin, Vector3f.NaN);

                    Ref<EntityStore> ref1 = pair.first();
                    store.addComponent(ref1,ServantMod.MINER_COMPONENT,component);
                    uuids.add(((Entity) pair.value()).getUuid().toString());
                },world);

                for (int i = 0 ; i < 5 ; i++){
                    CompletableFuture.runAsync(()->{
                        Entity entity = (Entity) NPCPlugin.get().spawnNPC(store,"ServantMiner",null,posOrigin,new Vector3f(90,0)).right();
                        uuids.add(entity.getUuid().toString());
                    },world);
                }
                CompletableFuture.runAsync(()->{
                    finalMeta.append("list",Codec.STRING_ARRAY.encode(uuids.toArray(new String[6])));
                    ItemStack stack = player.getInventory().getItemInHand();
                    stackData.put("Miner",Codec.BSON_DOCUMENT.encode(finalMeta));
                    player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(stackData));
                },world);
                if (meta.containsKey("debug_x")) {
                    meta.put("debug_x",Codec.DOUBLE.encode((double) x));
                    meta.put("debug_y",Codec.DOUBLE.encode((double) y));
                    meta.put("debug_z",Codec.DOUBLE.encode((double) z));
                }else {
                    meta.append("debug_x", Codec.DOUBLE.encode((double) x));
                    meta.append("debug_y", Codec.DOUBLE.encode((double) y));
                    meta.append("debug_z", Codec.DOUBLE.encode((double) z));
                }
            }else {
                rootInteraction=RootInteraction.getRootInteractionOrUnknown("Root_Desummon_Demon");

                BsonDocument finalMeta = meta;
                CompletableFuture.runAsync(()->{
                    if (finalMeta.containsKey("list")){
                        for (BsonValue uuid : finalMeta.getArray("list").asArray().getValues()){
                            Ref<EntityStore> ref1=world.getEntityStore().getRefFromUUID(UUID.fromString(uuid.asString().getValue()));
                            if(ref1==null || !ref1.isValid())continue;
                            NPCEntity entity = store.getComponent(ref1, NPCEntity.getComponentType());
                            Vector3d pos = entity.getOldPosition();
                            world.getNotificationHandler().sendBlockParticle(pos.x+0.5F, (pos.y + 1.5),pos.z+0.5F, BlockType.getBlockIdOrUnknown("Rock_Crystal_Red_Block"," "), BlockParticleEvent.Break);

                            entity.remove();
                        }
                        ItemStack stack = player.getInventory().getItemInHand();

                        player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(stackData));
                    }
                },world);
            }
        }


        if(rootInteraction!=null){
            interactionContext.getChain().pushRoot(rootInteraction,false);
        }

    }

    public void scanArea(World world, Vector3i center, int radius, TreeManagerComponent data,ResourceView resourceView) {
        data.treeList.clear();
        for (int x = center.x - radius; x <= center.x + radius; x++) {
            for (int z = center.z - radius; z <= center.z + radius; z++) {
                for (int y = center.y - 5; y <= center.y + 20; y++) {
                    if (!isTreeBase(world, x, y, z)) continue;

                    TreeData tree = ServantUtil.detectTree(world,resourceView,new Vector3i(x,y,z));

                    data.treeList.add(tree);
                }
            }
        }
    }

    private boolean isTreeBase(World world, int x, int y, int z) {

        Vector3i vector3i = new Vector3i(x,y,z);
        if (!ServantUtil.isLog(world,vector3i)) return false;

        // 👇 evita raíces
        if (!isGround(world, x,y-1,z)) return false;

        // 👇 asegura que es tronco real
        if (!ServantUtil.isLog(world, vector3i.add(0,1,0))) return false;

        return true;
    }

    private boolean isGround(World world, int x, int y, int z) {
        BlockType t = world.getBlockType(x, y, z);
        if (t == null) return false;

        String id = t.getId();
        return id.contains("Dirt") || id.contains("Grass") || id.contains("Soil") || t.getId().contains("Wood");
    }
}
