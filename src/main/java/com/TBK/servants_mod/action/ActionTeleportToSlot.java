package com.TBK.servants_mod.action;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.ServantUtil;
import com.TBK.servants_mod.component.MinerComponent;
import com.TBK.servants_mod.component.RecollectComponent;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionResult;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jspecify.annotations.NonNull;

public class ActionTeleportToSlot extends ActionBase {
    public final int slot;

    public ActionTeleportToSlot(@NonNull BuilderActionTeleportToMine builder, BuilderSupport support) {
        super(builder);
        this.slot = builder.getTargetSlot(support);
    }

    @Override
    public boolean execute(@NonNull Ref<EntityStore> ref, @NonNull Role role, InfoProvider sensorInfo, double dt, @NonNull Store<EntityStore> store) {
        if (!super.execute(ref, role, sensorInfo, dt, store)) return false;
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());

        if (npc == null || transform == null) return false;

        Vector3d target = role.getMarkedEntitySupport().getStoredPosition(slot);

        if (!isValid(target)) {
            RecollectComponent recollectComponent = store.getComponent(ref, ServantMod.RECOLLECT_COMPONENT);

            if (recollectComponent != null && isValid(recollectComponent.originPos.toVector3d())) {
                target = new Vector3d(recollectComponent.originPos);
            } else {
                return false;
            }
        }

        Vector3d current = transform.getPosition();

        if (current.distanceSquaredTo(target) < 0.05) {
            return false;
        }
        MinerComponent component = store.getComponent(ref,ServantMod.MINER_COMPONENT);
        if(component!=null){

            World world = npc.getWorld();

            int x = (int) Math.floor(target.x);
            int y = (int) Math.floor(target.y);
            int z = (int) Math.floor(target.z);
            world.getNotificationHandler().sendBlockParticle(x, (y + 1.5),z, BlockType.getBlockIdOrUnknown("Rock_Crystal_Green_Block"," "), BlockParticleEvent.Break);
            CollisionResult result = new CollisionResult();

            Vector3d safePos = findBestTeleportPosition(world, target, store, result);

            Box collider = new Box(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3);

            if (CollisionModule.get().validatePosition(world, collider, safePos, result) == CollisionModule.VALIDATE_INVALID) {
                safePos = current.add(0, 1, 0);
            }
            Vector3d pos = safePos;
            Vector3f rotation = transform.getRotation();

            component.breakTime = 0;
            Velocity velocity = store.getComponent(ref,Velocity.getComponentType());
            velocity.setZero();
            velocity.setClient(0.0F,0.0F,0.0F);

            store.addComponent(ref, Teleport.getComponentType(), Teleport.createExact(pos, rotation,rotation));

            return true;
        }else {
            ServantMod.LOGGER.atInfo().log("mimir");

            return false;
        }

    }

    public static boolean isValid(Vector3d v) {
        return v != null && Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z);
    }
    public static Vector3d findBestTeleportPosition(World world, Vector3d target, ComponentAccessor<EntityStore> accessor, CollisionResult result) {

        int baseX = (int) Math.floor(target.x);
        int baseY = (int) Math.floor(target.y);
        int baseZ = (int) Math.floor(target.z);

        for (int dy = 1; dy <= 3; dy++) {
            if (isSafe(world, baseX, baseY + dy, baseZ, result)) {
                return center(baseX, baseY + dy, baseZ);
            }
        }

        int[] offsets = {-1, 0, 1};

        for (int radius = 1; radius <= 2; radius++) {
            for (int dx : offsets) {
                for (int dz : offsets) {

                    if (Math.abs(dx) != radius && Math.abs(dz) != radius) continue;

                    int x = baseX + dx;
                    int z = baseZ + dz;

                    for (int dy = 0; dy <= 2; dy++) {
                        int y = baseY + dy;

                        if (isSafe(world, x, y, z, result)) {
                            return center(x, y, z);
                        }
                    }
                }
            }
        }

        return center(baseX, baseY + 1, baseZ);
    }
    public static boolean isSafe(World world, WorldChunk chunk, int x, int y, int z, CollisionResult result) {

        if (!ServantUtil.isAir(chunk, x, y, z)) return false;
        if (!ServantUtil.isAir(chunk, x, y + 1, z)) return false;
        if (!ServantUtil.isSolid(chunk, x, y - 1, z)) return false;


        Vector3d pos = new Vector3d(x + 0.5, y, z + 0.5);


        Box collider = new Box(
                -0.3, 0.0, -0.3,
                0.3, 1.8,  0.3
        );

        CollisionModule collision = CollisionModule.get();

        int validation = collision.validatePosition(world, collider, pos, result);

        if (validation == CollisionModule.VALIDATE_INVALID) return false;

        if ((validation & CollisionModule.VALIDATE_ON_GROUND) == 0) return false;

        return true;
    }
    public static boolean isSafe(World world, int x, int y, int z, CollisionResult result) {

        if (!ServantUtil.isAir(world, x, y, z)) return false;
        if (!ServantUtil.isAir(world, x, y + 1, z)) return false;
        if (!ServantUtil.isSolid(world, x, y - 1, z)) return false;


        Vector3d pos = new Vector3d(x + 0.5, y, z + 0.5);


        Box collider = new Box(
                -0.3, 0.0, -0.3,
                0.3, 1.8,  0.3
        );

        CollisionModule collision = CollisionModule.get();

        int validation = collision.validatePosition(world, collider, pos, result);

        if (validation == CollisionModule.VALIDATE_INVALID) return false;

        if ((validation & CollisionModule.VALIDATE_ON_GROUND) == 0) return false;

        return true;
    }
    public static Vector3d center(int x, int y, int z) {
        return new Vector3d(x + 0.5 + (Math.random() - 0.5) * 0.1, y, z + 0.5 + (Math.random() - 0.5) * 0.1);
    }
}