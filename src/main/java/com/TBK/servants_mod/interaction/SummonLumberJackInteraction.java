package com.TBK.servants_mod.interaction;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.ServantUtil;
import com.TBK.servants_mod.component.AreaOrderComponent;
import com.TBK.servants_mod.component.LumberjackZoneComponent;
import com.TBK.servants_mod.component.MinerComponent;
import com.TBK.servants_mod.component.TreeManagerComponent;
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
import com.hypixel.hytale.protocol.Interaction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
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

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SummonLumberJackInteraction extends SimpleInstantInteraction{
    public static final BuilderCodec CODEC = BuilderCodec.builder(
            SummonLumberJackInteraction.class, SummonLumberJackInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    protected void firstRun(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext, @Nonnull CooldownHandler cooldownHandler) {

    }

    public void scanArea(World world, Vector3i center, int radius, TreeManagerComponent data,ResourceView resourceView) {
        data.treeList.clear();
        for (int x = center.x - radius; x <= center.x + radius; x++) {
            for (int z = center.z - radius; z <= center.z + radius; z++) {
                for (int y = center.y - 5; y <= center.y + 20; y++) {
                    if (!isTreeBase(world, x, y, z)) continue;

                    TreeData tree =ServantUtil.detectTree(world,resourceView,new Vector3i(x,y,z));

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
    @Override
    public boolean needsRemoteSync() {
        return true;
    }

    @Override
    protected void configurePacket(Interaction packet) {
        super.configurePacket(packet);
    }
}
