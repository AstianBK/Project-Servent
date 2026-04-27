package com.TBK.servants_mod.interaction;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.component.AreaOrderComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;
import org.jspecify.annotations.NonNull;

public class ModifyAreaMinerInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec CODEC = BuilderCodec.builder(
            ModifyAreaMinerInteraction.class, ModifyAreaMinerInteraction::new, SimpleInstantInteraction.CODEC
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
        AreaOrderComponent areaOrderComponent = commandBuffer.getComponent(ref, ServantMod.AREA_COMPONENT);
        ItemStack stack = player.getInventory().getItemInHand();

        BsonDocument meta = stack.getMetadata();

        if (meta != null) {
            if(!meta.containsKey("Summoning") || !meta.getBoolean("Summoning").getValue()){
                MovementStatesComponent states = commandBuffer.getComponent(ref, MovementStatesComponent.getComponentType());

                if(areaOrderComponent!=null){
                    if(states.getMovementStates().crouching){
                        areaOrderComponent.width = Math.max(1.0F,areaOrderComponent.width-2.0F);
                    }else {
                        areaOrderComponent.width = Math.min(15.0F,areaOrderComponent.width+2.0F);
                    }
                }
            }
        }

    }
}
