package com.TBK.servants_mod;

import com.TBK.servants_mod.component.MinerComponent;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;

public class RegisterComponent extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float v, int i, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {
        var ref = archetypeChunk.getReferenceTo(i);

        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());

        if (!npc.getRole().getRoleName().contains("ServantMiner")) {
            return;
        }

        commandBuffer.addComponent(ref, ServantMod.MINER_COMPONENT, new MinerComponent());
    }

    @Override
    public @Nonnull Query<EntityStore> getQuery() {
        return Query.and(
                NPCEntity.getComponentType(),
                Query.not(ServantMod.MINER_COMPONENT)
        );
    }
}
