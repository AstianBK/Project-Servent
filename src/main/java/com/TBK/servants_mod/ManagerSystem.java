package com.TBK.servants_mod;

import com.TBK.servants_mod.action.ActionTeleportToSlot;
import com.TBK.servants_mod.component.LumberjackZoneComponent;
import com.TBK.servants_mod.component.MinerComponent;
import com.TBK.servants_mod.component.TreeManagerComponent;
import com.TBK.servants_mod.data.TreeData;
import com.TBK.servants_mod.resource.GrowTreeManager;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldConfig;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthChunk;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.TBK.servants_mod.action.ActionTeleportToSlot.center;
import static com.TBK.servants_mod.action.ActionTeleportToSlot.isSafe;

public class ManagerSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float v, int id, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {

        var ref = archetypeChunk.getReferenceTo(id);
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc == null) return;

        if(npc.getWorld()==null)return;

        World world = npc.getWorld();
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());

        if (transform != null){
            Vector3d pos = transform.getPosition();

            CollisionResult result = new CollisionResult();


            if (!ActionTeleportToSlot.isValid(pos) || pos.y < -20) {
                rescue(ref, store, world, pos,commandBuffer);
            }

            if (isInsideBlock(world, pos, result)) {
                rescue(ref, store, world, pos,commandBuffer);
            }
        }
        LumberjackZoneComponent lumberjackZoneComponent = store.getComponent(ref,ServantMod.LUMBERJACK_COMPONENT);

        if(lumberjackZoneComponent!=null) {
            TreeManagerComponent managerComponent = store.getResource(ServantMod.TREE_MANAGER_COMPONENT);

            if(lumberjackZoneComponent.dirty){
                rebuildZone(lumberjackZoneComponent,managerComponent);
                lumberjackZoneComponent.dirty = false;
            }
        }


    }
    private static void rescue(Ref<EntityStore> ref, Store<EntityStore> store, World world, Vector3d origin, @NonNull CommandBuffer<EntityStore> commandBuffer) {

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;

        Vector3d safe = findNearestSafe(world, origin);

        Velocity vel = store.getComponent(ref, Velocity.getComponentType());
        if (vel != null) vel.setZero();

        commandBuffer.addComponent(ref, Teleport.getComponentType(), Teleport.createExact(safe, transform.getRotation()));
    }

    private static boolean isInsideBlock(World world, Vector3d pos, CollisionResult result) {

        Box collider = new Box(-1, 0.0, -1, 1, 1.8,  1);

        int validation = CollisionModule.get()
                .validatePosition(world, collider, pos, result);

        return validation == CollisionModule.VALIDATE_INVALID;
    }

    private static Vector3d findNearestSafe(World world, Vector3d origin) {

        int baseX = (int)Math.floor(origin.x);
        int baseY = (int)Math.floor(origin.y);
        int baseZ = (int)Math.floor(origin.z);

        WorldChunk chunk = world.getChunkIfNonTicking(ServantUtil.getChunkIndex(baseX, baseZ));

        if(chunk == null)return new Vector3d(baseX + 0.5, baseY + 5, baseZ + 0.5);
        CollisionResult result = new CollisionResult();
        for (int dy = 1; dy <= 5; dy++) {
            if (isSafe(world,chunk, baseX, baseY + dy, baseZ,result)) {
                return center(baseX, baseY + dy, baseZ);
            }
        }

        for (int r = 1; r <= 3; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {

                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;

                    int x = baseX + dx;
                    int z = baseZ + dz;

                    for (int dy = 0; dy <= 2; dy++) {
                        int y = baseY + dy;

                        if (isSafe(world,chunk, x, y, z,result)) {
                            return center(x, y, z);
                        }
                    }
                }
            }
        }

        return new Vector3d(baseX + 0.5, baseY + 5, baseZ + 0.5);
    }
    public void rebuildZone(LumberjackZoneComponent zone, TreeManagerComponent worldData) {

        zone.trees.clear();

        for (TreeData tree : worldData.treeList) {
            if (tree !=null && isInside(zone, tree.center)) {
                zone.trees.add(tree);
            }
        }

        zone.dirty = false;
    }
    private boolean isInside(LumberjackZoneComponent zone, Vector3i pos) {
        return pos != null && pos.distanceTo(zone.center) <= zone.radius;
    }
    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return ServantMod.LUMBERJACK_COMPONENT;
    }

    public static class TPCooldown extends EntityTickingSystem<EntityStore> {
        @Override
        public void tick(float v, int id, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {

            var ref = archetypeChunk.getReferenceTo(id);
            NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
            if (npc == null) return;

            if(npc.getWorld()==null)return;

            MinerComponent minerComponent = store.getComponent(ref,ServantMod.MINER_COMPONENT);

            if(minerComponent==null)return;
            if(minerComponent.cooldownTP>0){
                minerComponent.cooldownTP--;
            }
        }

        @Override
        public @Nullable Query<EntityStore> getQuery() {
            return ServantMod.MINER_COMPONENT;
        }
    }

    public static class PlaceBlockEventSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
        @Nonnull
        private static final ComponentType<ChunkStore, BlockHealthChunk> BLOCK_HEALTH_CHUNK_COMPONENT_TYPE = BlockHealthModule.get().getBlockHealthChunkComponentType();

        public PlaceBlockEventSystem() {
            super(PlaceBlockEvent.class);
        }

        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull PlaceBlockEvent event) {

        }

        @javax.annotation.Nullable
        public Query<EntityStore> getQuery() {
            return Archetype.empty();
        }
    }
}
