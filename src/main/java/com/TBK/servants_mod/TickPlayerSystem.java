package com.TBK.servants_mod;

import com.TBK.servants_mod.component.AreaOrderComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.raycast.RaycastAABB;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.DebugFlags;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.packets.player.ClearDebugShapes;
import com.hypixel.hytale.protocol.packets.player.DisplayDebug;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.collision.*;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class TickPlayerSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float v, int id, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {

        var ref = archetypeChunk.getReferenceTo(id);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        if(player.getWorld()==null)return;

        AreaOrderComponent areaOrderComponent = store.getComponent(ref,ServantMod.AREA_COMPONENT);
        if (areaOrderComponent==null){
            areaOrderComponent = new AreaOrderComponent();
            commandBuffer.addComponent(ref,ServantMod.AREA_COMPONENT,areaOrderComponent);
        }

        ItemStack stack = player.getInventory().getItemInHand();

        if (stack == null || !stack.getItemId().equals("Contract_Item")) return;

        var connection = player.getPlayerConnection();


        HeadRotation headComponent = store.getComponent(ref,HeadRotation.getComponentType());

        if(headComponent==null)return;


        com.hypixel.hytale.math.vector.Vector3f pos = com.hypixel.hytale.math.vector.Vector3f.add(player.getTransformComponent().getPosition().toVector3f(),new com.hypixel.hytale.math.vector.Vector3f(0,1.6F,0)) ;

        BsonDocument meta = stack.getMetadata();
        if (meta == null) meta = new BsonDocument();

        com.hypixel.hytale.math.vector.Vector3f dir = headComponent.getDirection().toVector3f();

        Box collider = new Box(
                -0.001, -0.001, -0.001,
                0.001,  0.001,  0.001
        );
        int hitX = 0, hitY = 0, hitZ = 0;
        CollisionResult result = new CollisionResult();

        CollisionModule.findBlockCollisionsIterative(player.getWorld(), collider, pos.toVector3d(), dir.scale(15.0F).toVector3d(), true, result);

        if (result.getBlockCollisionCount()>0) {
            BlockCollisionData hit = result.getBlockCollision(0);

            Vector3d hitPos = hit.collisionPoint;

            int bx = MathUtil.floor(hitPos.x);
            int by = MathUtil.floor(hitPos.y);
            int bz = MathUtil.floor(hitPos.z);

            hitX = bx;
            hitY = by;
            hitZ = bz;
//            System.out.println("HIT REAL: " + bx + ", " + by + ", " + bz);
        }

        float finalX = (float)(hitX + 0.5);
        float finalY = (float)(hitY + 0.5);
        float finalZ = (float)(hitZ + 0.5);

        boolean changed = true;

        if (meta.containsKey("debug_x")) {
            double lastX = meta.getDouble("debug_x").getValue();
            double lastY = meta.getDouble("debug_y").getValue();
            double lastZ = meta.getDouble("debug_z").getValue();

            changed = (lastX != finalX || lastY != finalY || lastZ != finalZ);
        }

        if (!changed) return;
        connection.write(new ClearDebugShapes());

        float width = areaOrderComponent.width;
        float[] matrix = new float[]{
                width,0,0,0,
                0,1,0,0,
                0,0,width,0,
                finalX, finalY+0.1F, finalZ,1
        };


        DisplayDebug debug = new DisplayDebug(DebugShape.Cube, matrix, new com.hypixel.hytale.protocol.Vector3f(1,1,1), 9999f, (byte) DebugFlags.NoSolid.getValue(), null, 0.05f);
        connection.write(debug);
//        ServantMod.LOGGER.atInfo().log("Meta :%s",debug);
        player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(meta));

    }
    private float intBound(float s, float ds) {
        if (ds == 0) return Float.MAX_VALUE;

        float sIsInteger = MathUtil.floor(s);
        if (ds > 0) {
            return (sIsInteger + 1 - s) / ds;
        } else {
            return (s - sIsInteger) / -ds;
        }
    }

    public static boolean raycastBlocks(Store<EntityStore> store, Vector3f origin, Vector3f dir, double maxDist, Vector3d outPos) {

        // 🔥 normalizar dirección
        float len = (float)Math.sqrt(dir.x * dir.x + dir.y * dir.y + dir.z * dir.z);
        dir = new Vector3f(dir.x / len, dir.y / len, dir.z / len);

        double x = origin.x;
        double y = origin.y;
        double z = origin.z;

        int bx = (int)Math.floor(x);
        int by = (int)Math.floor(y);
        int bz = (int)Math.floor(z);

        int stepX = dir.x > 0 ? 1 : -1;
        int stepY = dir.y > 0 ? 1 : -1;
        int stepZ = dir.z > 0 ? 1 : -1;

        double tMaxX = rayIntersectAxis(x, dir.x);
        double tMaxY = rayIntersectAxis(y, dir.y);
        double tMaxZ = rayIntersectAxis(z, dir.z);

        double tDeltaX = dir.x == 0 ? Double.POSITIVE_INFINITY : Math.abs(1 / dir.x);
        double tDeltaY = dir.y == 0 ? Double.POSITIVE_INFINITY : Math.abs(1 / dir.y);
        double tDeltaZ = dir.z == 0 ? Double.POSITIVE_INFINITY : Math.abs(1 / dir.z);

        double bestT = Double.POSITIVE_INFINITY;
        boolean found = false;

        while (true) {

            int chunkX = bx >> 4;
            int chunkZ = bz >> 4;
            long chunkIndex = ((long)chunkX << 32) | (chunkZ & 0xffffffffL);

            var chunk = store.getExternalData().getWorld().getChunkIfLoaded(chunkIndex);

            if (chunk != null) {
                int localX = bx & 15;
                int localZ = bz & 15;

                RaycastAABB.intersect(bx,by,bz,bx+1,by+1,bz+1,origin.x,origin.y,origin.z,dir.x,dir.y,dir.z,
                        (hit,ox,oy,oz,dx,dy,dz,t,rx,ry,rz)->{
                    if (hit){
                        outPos.assign(ox, oy, oz);
                    }
                });
                BlockType type = chunk.getBlockType(localX, by, localZ);

                if (type != null && type.getMaterial() != BlockMaterial.Empty) {


                    return true;
                }
            }

            // 🔥 avanzar voxel
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    bx += stepX;
                    if (tMaxX > maxDist) break;
                    tMaxX += tDeltaX;
                } else {
                    bz += stepZ;
                    if (tMaxZ > maxDist) break;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    by += stepY;
                    if (tMaxY > maxDist) break;
                    tMaxY += tDeltaY;
                } else {
                    bz += stepZ;
                    if (tMaxZ > maxDist) break;
                    tMaxZ += tDeltaZ;
                }
            }
        }

        return found;
    }
    private static double rayIntersectAxis(double pos, double dir) {
        if (dir > 0) {
            return (Math.floor(pos) + 1 - pos) / dir;
        } else if (dir < 0) {
            return (pos - Math.floor(pos)) / -dir;
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }
    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
