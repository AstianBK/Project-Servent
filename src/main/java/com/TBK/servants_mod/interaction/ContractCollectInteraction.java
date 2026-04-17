package com.TBK.servants_mod.interaction;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.RecollectComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.EntityChunk;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import it.unimi.dsi.fastutil.Pair;
import org.bson.BsonDocument;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ContractCollectInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC = BuilderCodec.builder(
            ContractCollectInteraction.class, ContractCollectInteraction::new, SimpleInstantInteraction.CODEC
    ).build();
    @Override
    protected void firstRun(@NonNull InteractionType interactionType, @NonNull InteractionContext interactionContext, @NonNull CooldownHandler cooldownHandler) {
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
        ItemStack stack = player.getInventory().getItemInHand();
        BsonDocument meta = stack.getMetadata();
        boolean summoning = false;

        if (interactionContext.getTargetBlock()==null)return;
        BlockPosition pos = interactionContext.getTargetBlock();
        BlockType type = world.getBlockType(pos.x,pos.y,pos.z);

        if(meta==null){
            meta = new BsonDocument();
        }
        if(meta.containsKey("HasChest")){
            int x = meta.getInt32("ChestX").getValue();
            int y = meta.getInt32("ChestY").getValue();
            int z = meta.getInt32("ChestZ").getValue();


            if(meta.containsKey("Summoning")){
                if (meta.getBoolean("Summoning").getValue()){
                    summoning = true;
                    meta.put("Summoning", Codec.BOOLEAN.encode(false));
                    //Summoning
                }else {
                    meta.put("Summoning", Codec.BOOLEAN.encode(false));
                    //Unsummoning
                }
            }else {
                meta.append("Summoning", Codec.BOOLEAN.encode(true));
                summoning = true;
            }
        }else if (type!=null && type.getBlockEntity()!=null && type.getBlockEntity().getComponent(ItemContainerBlock.getComponentType())!=null){
            meta.put("ChestX",Codec.INTEGER.encode(pos.x));
            meta.put("ChestY",Codec.INTEGER.encode(pos.y));
            meta.put("ChestZ",Codec.INTEGER.encode(pos.z));
            meta.put("HasChest",Codec.BOOLEAN.encode(true));
        }
        if (summoning){
            BsonDocument finalMeta = meta;
            CompletableFuture.runAsync(()->{
                if (finalMeta.containsKey("ChestX")){
                    int x = finalMeta.getInt32("ChestX").getValue();
                    int y = finalMeta.getInt32("ChestY").getValue();
                    int z = finalMeta.getInt32("ChestZ").getValue();
                    Pair<Ref<EntityStore>, INonPlayerCharacter> pair = NPCPlugin.get().spawnNPC(store,"ServantCollect",null,new Vector3d(pos.x,pos.y+1.5D,pos.z), Vector3f.NaN);

                    RecollectComponent component = new RecollectComponent();
                    component.targetPos = new Vector3i(x,y,z);
                    Ref<EntityStore> ref1 = pair.first();
                    store.addComponent(ref1,ServantMod.RECOLLECT_COMPONENT,component);
                }
                },world);
        }else {

        }
        player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(meta));

    }
}
