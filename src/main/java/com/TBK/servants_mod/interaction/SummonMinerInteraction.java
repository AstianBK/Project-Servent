package com.TBK.servants_mod.interaction;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.AreaOrderComponent;
import com.TBK.servants_mod.component.MinerComponent;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SummonMinerInteraction extends SimpleInstantInteraction{
    public static final BuilderCodec CODEC = BuilderCodec.builder(
            SummonMinerInteraction.class, SummonMinerInteraction::new, SimpleInstantInteraction.CODEC
    ).build();
    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            ServantMod.LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }

        World world = commandBuffer.getExternalData().getWorld(); // just to show how to get the world if needed
        Store<EntityStore> store = commandBuffer.getExternalData().getStore(); // just to show how to get the store if needed
        Ref ref = interactionContext.getEntity();
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        UUID playerUUID = player.getUuid();
        BsonDocument meta = player.getInventory().getItemInHand().getMetadata();
        PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(playerUUID);
        BrushConfigCommandExecutor brushConfigCommandExecutor = prototypeSettings.getBrushConfigCommandExecutor();

        AreaOrderComponent areaOrderComponent = store.getComponent(ref,ServantMod.AREA_COMPONENT);
        if (areaOrderComponent==null){
            areaOrderComponent = new AreaOrderComponent();
        }

//        double x = brushConfigCommandExecutor.getEdit().getBefore().getX();
//        double y = brushConfigCommandExecutor.getEdit().getBefore().getY();
//        double z = brushConfigCommandExecutor.getEdit().getBefore().getZ();




        int width =(int) areaOrderComponent.width;
        MinerComponent component = new MinerComponent();
        component.targetPos = new Vector3i(width,1,width);

        ServantMod.LOGGER.atInfo().log("Data :%s",player.getInventory().getItemInHand());
        boolean summoning = false;
        if (meta != null) {

            if(meta.containsKey("ToolData")){
                BsonDocument brushData = meta.getDocument("ToolData");

                int w = brushData.getInt32("builtin_Width").getValue();
                int h = brushData.getInt32("builtin_Height").getValue();

                component.targetPos = new Vector3i(w,h,w);
            }

        }else {
            meta = new BsonDocument();
        }
        if(!meta.containsKey("Summoning")){
            meta.append("Summoning", Codec.BOOLEAN.encode(true));
            summoning = true;
        }else{
            boolean flag = meta.getBoolean("Summoning").getValue();
            ServantMod.LOGGER.atInfo().log("Bandera :%s",flag);
            meta.put("Summoning",Codec.BOOLEAN.encode(!flag));
            summoning = !flag;
        }

        if(ref!=null){

            if (summoning && interactionContext.getTargetBlock()!=null){
                double x = interactionContext.getTargetBlock().x;
                double y = interactionContext.getTargetBlock().y;
                double z = interactionContext.getTargetBlock().z;
                Vector3d posOrigin = new Vector3d(x,y+1.5D,z);
                BsonDocument finalMeta = meta;
                CompletableFuture.runAsync(()->{
                    ServantMod.LOGGER.atInfo().log("pre-Summon");
                    Pair<Ref<EntityStore>, INonPlayerCharacter> pair = NPCPlugin.get().spawnNPC(store,"miner_orden_entity",null,posOrigin, Vector3f.NaN);

                    Ref<EntityStore> ref1 = pair.first();
                    store.addComponent(ref1,ServantMod.MINER_COMPONENT,component);
                    ServantMod.LOGGER.atInfo().log("pre encode Summon");

                },world);
                ArrayList<String> uuids = new ArrayList<>();
                for (int i = 0 ; i < 5 ; i++){
                    int is = i  ;
                    CompletableFuture.runAsync(()->{
                        Entity entity = (Entity) NPCPlugin.get().spawnNPC(store,"ServantMiner",null,posOrigin, Vector3f.NaN).right();
                        uuids.add(entity.getUuid().toString());
                        ServantMod.LOGGER.atInfo().log("Summon %s",is);
                    },world);

                }
                CompletableFuture.runAsync(()->{
                    ServantMod.LOGGER.atInfo().log("Summon %s",uuids);
                    finalMeta.append("list",Codec.STRING_ARRAY.encode(uuids.toArray(new String[5])));
                    ItemStack stack = player.getInventory().getItemInHand();
                    ServantMod.LOGGER.atInfo().log("Summon %s",uuids);
                    player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(finalMeta));
                },world);

            }else {
                BsonDocument finalMeta = meta;
                CompletableFuture.runAsync(()->{
                    if (finalMeta.containsKey("list")){
                        for (BsonValue uuid : finalMeta.getArray("list").asArray().getValues()){
                            ServantMod.LOGGER.atInfo().log("Entity %s:",uuid.asString().getValue());

                            UUID id = UUID.fromString(uuid.asString().getValue());
                            ServantMod.LOGGER.atInfo().log("Id %s:",id);
//                            if (UUID.fromString(uuid.asString().getValue())==null)continue;
                            Ref<EntityStore> ref1=world.getEntityStore().getRefFromUUID(UUID.fromString(uuid.asString().getValue()));
                            if(ref1==null || !ref1.isValid())continue;
                            NPCEntity entity = store.getComponent(ref1, NPCEntity.getComponentType());
                            Vector3d pos = entity.getOldPosition();
                            world.getNotificationHandler().sendBlockParticle(pos.x+0.5F, (pos.y + 1.5),pos.z+0.5F, BlockType.getBlockIdOrUnknown("Rock_Crystal_Red_Block"," "), BlockParticleEvent.Break);

                            entity.remove();
                        }
                    }
                },world);



                ItemStack stack = player.getInventory().getItemInHand();

                player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(meta));

            }
        }
    }
}
