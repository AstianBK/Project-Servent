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
import com.hypixel.hytale.server.core.io.PacketHandler;
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
        PacketHandler connection = player.getPlayerConnection();
        if (stack == null || !stack.getItemId().equals("Contract_Item")){
            connection.write(new ClearDebugShapes());
            return;
        }




        HeadRotation headComponent = store.getComponent(ref,HeadRotation.getComponentType());

        if(headComponent==null)return;


        com.hypixel.hytale.math.vector.Vector3f pos = com.hypixel.hytale.math.vector.Vector3f.add(player.getTransformComponent().getPosition().toVector3f(),new com.hypixel.hytale.math.vector.Vector3f(0,1.6F,0)) ;

        BsonDocument meta = stack.getMetadata();
        if (meta == null) meta = new BsonDocument();

        if(meta.containsKey("Summoning")){
            if (!meta.getBoolean("Summoning").getValue()){
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
                        width-0.01F,0,0,0,
                        0,1,0,0,
                        0,0,width-0.01F,0,
                        finalX, finalY+0.1F, finalZ,1
                };
                float[] matrix1 = new float[]{
                        1,0,0,0,
                        0,1,0,0,
                        0,0,1,0,
                        finalX, finalY+0.1F, finalZ,1
                };

                DisplayDebug debug1 = new DisplayDebug(DebugShape.Cube, matrix1, new com.hypixel.hytale.protocol.Vector3f(1,0,0), 9999f, (byte) DebugFlags.NoSolid.getValue(), null, 0.05f);

                DisplayDebug debug = new DisplayDebug(DebugShape.Cube, matrix, new com.hypixel.hytale.protocol.Vector3f(1,1,1), 9999f, (byte) DebugFlags.NoSolid.getValue(), null, 0.25f);
                connection.write(debug);
                connection.write(debug1);

            }else {
                connection.write(new ClearDebugShapes());
            }

        }

//        ServantMod.LOGGER.atInfo().log("Meta :%s",debug);
        player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(meta));

    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
