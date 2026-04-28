package com.TBK.servants_mod;

import com.TBK.servants_mod.component.AreaOrderComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.DebugFlags;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.packets.player.ClearDebugShapes;
import com.hypixel.hytale.protocol.packets.player.DisplayDebug;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.collision.*;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

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


        if (stack == null || !stack.getItemId().equals("Gauntlet")){
            connection.write(new ClearDebugShapes());
            return;
        }
        if (stack.getMetadata()!=null){
            if (stack.getMetadata().containsKey("debug")){
                stack.getMetadata().put("debug",Codec.BOOLEAN.encode(false));
            }else {
                stack.getMetadata().append("debug",Codec.BOOLEAN.encode(false));
            }
            BsonDocument stackData = stack.getMetadata();
            String type = "Miner";
            if (stackData.containsKey("GauntletType")){
                type = ServantUtil.getGauntletType(stackData);
            }

            BsonDocument meta = ServantUtil.getGauntletTypeData(stackData,type);
            if (meta==null)return;
            switch (type){
                case "Miner"->{
                    tickManagerMiner(ref,player.getWorld(),store,stack,stackData,meta,player,areaOrderComponent);
                }
                case "LumberJack"->{
                    tickManagerLumberJack(ref,player.getWorld(),store,stack,stackData,meta,player,areaOrderComponent);
                }
                case "Collect"->{
                    tickManagerCollect(ref,player.getWorld(),store,stack,stackData,meta,player,areaOrderComponent);
                }
                default -> {
                    connection.write(new ClearDebugShapes());

                }
            }

        }
    }

    public static void tickManagerLumberJack(Ref<EntityStore> ref, World world, ComponentAccessor<EntityStore> store, ItemStack stack,BsonDocument stackData,BsonDocument meta, Player player, AreaOrderComponent areaOrderComponent){
        PacketHandler connection = player.getPlayerConnection();
        HeadRotation headComponent = store.getComponent(ref,HeadRotation.getComponentType());

        if(headComponent==null)return;


        if (meta == null) meta = new BsonDocument();


        if(meta.containsKey("Summoning")){
            if (meta.getBoolean("Summoning").getValue()){
                if (meta.containsKey("debug")){
                    if (!meta.getBoolean("debug").getValue()){
                        connection.write(new ClearDebugShapes());
                        meta.put("debug",Codec.BOOLEAN.encode(true));
                    }
                }else {
                    meta.append("debug",Codec.BOOLEAN.encode(true));
                    connection.write(new ClearDebugShapes());
                }

                if (meta.containsKey("OriginZ")) {
                    float lastX = (float) meta.getDouble("OriginX").getValue();
                    float lastY = (float) meta.getDouble("OriginY").getValue();
                    float lastZ = (float) meta.getDouble("OriginZ").getValue();

                    float width = 50;
                    float[] matrix = new float[]{
                            width-0.04F,0,0,0,
                            0,99,0,0,
                            0,0,width-0.04F,0,
                            lastX, lastY+0.1F, lastZ,1
                    };
//                    DisplayDebug debug = new DisplayDebug(DebugShape.Cube, matrix, new com.hypixel.hytale.protocol.Vector3f(0,1,0), 9999f, (byte) DebugFlags.NoSolid.getValue(), null, 0.05f);
//                    connection.write(debug);
                    drawCubeOutline(world,new Vector3d(lastX,lastY-49.5D,lastZ),width+0.05F,99,new Vector3f(1,0,0),0.05, 9999f, DebugFlags.NoSolid.getValue(), connection);
                }
            }else{
                connection.write(new ClearDebugShapes());

            }
        }
        stackData.put("LumberJack",Codec.BSON_DOCUMENT.encode(meta));
        player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(stackData));

    }
    public static void tickManagerCollect(Ref<EntityStore> ref, World world, ComponentAccessor<EntityStore> store, ItemStack stack,BsonDocument stackData,BsonDocument meta, Player player, AreaOrderComponent areaOrderComponent){
        PacketHandler connection = player.getPlayerConnection();
        HeadRotation headComponent = store.getComponent(ref,HeadRotation.getComponentType());

        if(headComponent==null)return;



        if (meta == null) meta = new BsonDocument();


        if(meta.containsKey("Summoning")){
            if (meta.getBoolean("Summoning").getValue()){
                if (meta.containsKey("debug")){
                    if (!meta.getBoolean("debug").getValue()){
                        connection.write(new ClearDebugShapes());
                        meta.put("debug",Codec.BOOLEAN.encode(true));
                    }
                }else {
                    meta.append("debug",Codec.BOOLEAN.encode(true));
                    connection.write(new ClearDebugShapes());
                }
                if (meta.containsKey("ChestX")) {
                    int x = meta.getInt32("ChestX").getValue();
                    int y = meta.getInt32("ChestY").getValue();
                    int z = meta.getInt32("ChestZ").getValue();

                    float width = 1;
//                    float[] matrix = new float[]{
//                            width-0.04F,0,0,0,
//                            0,99,0,0,
//                            0,0,width-0.04F,0,
//                            lastX, lastY+0.1F, lastZ,1
//                    };
//                    DisplayDebug debug = new DisplayDebug(DebugShape.Cube, matrix, new com.hypixel.hytale.protocol.Vector3f(0,1,0), 9999f, (byte) DebugFlags.NoSolid.getValue(), null, 0.1f);
//                    connection.write(debug);
                    drawCubeOutline(world,new Vector3d(x+0.5D,y,z+0.5D),width+0.05F,1,new Vector3f(1,0,0),0.05, 9999f, DebugFlags.NoSolid.getValue(), connection);
                }
            }else {
                connection.write(new ClearDebugShapes());
            }
        }
        stackData.put("Collect",Codec.BSON_DOCUMENT.encode(meta));
        player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(stackData));
    }
    public static void tickManagerMiner(Ref<EntityStore> ref, World world, ComponentAccessor<EntityStore> store, ItemStack stack,BsonDocument stackData,BsonDocument meta, Player player, AreaOrderComponent areaOrderComponent){
        PacketHandler connection = player.getPlayerConnection();
        HeadRotation headComponent = store.getComponent(ref,HeadRotation.getComponentType());

        if(headComponent==null)return;


        com.hypixel.hytale.math.vector.Vector3f pos = com.hypixel.hytale.math.vector.Vector3f.add(player.getTransformComponent().getPosition().toVector3f(),new com.hypixel.hytale.math.vector.Vector3f(0,1.6F,0)) ;

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

                CollisionModule.findBlockCollisionsIterative(player.getWorld(), collider, pos.toVector3d(), dir.scale(10.0F).toVector3d(), true, result);

                if (result.getBlockCollisionCount()>0) {
                    BlockCollisionData hit = result.getBlockCollision(0);

                    Vector3d hitPos = hit.collisionPoint;

                    int bx = MathUtil.floor(hitPos.x);
                    int by = MathUtil.floor(hitPos.y);
                    int bz = MathUtil.floor(hitPos.z);

                    hitX = bx;
                    hitY = by;
                    hitZ = bz;
                }

                float finalX = (float)(hitX + 0.5);
                float finalY = (float)(hitY + 0.5);
                float finalZ = (float)(hitZ + 0.5);

                boolean changed = true;

                if (meta.containsKey("debug")){
                    meta.put("debug",Codec.BOOLEAN.encode(false));
                }else {
                    meta.append("debug",Codec.BOOLEAN.encode(false));
                }
                if (meta.containsKey("debug_x")) {
                    double lastX = meta.getDouble("debug_x").getValue();
                    double lastY = meta.getDouble("debug_y").getValue();
                    double lastZ = meta.getDouble("debug_z").getValue();

                    changed = (lastX != finalX || lastY != finalY || lastZ != finalZ);
                    if (changed){
                        meta.put("debug_x",Codec.DOUBLE.encode((double) finalX));
                        meta.put("debug_y",Codec.DOUBLE.encode((double) finalY));
                        meta.put("debug_z",Codec.DOUBLE.encode((double) finalZ));
                    }
                }else {
                    meta.append("debug_x", Codec.DOUBLE.encode((double) finalX));
                    meta.append("debug_y", Codec.DOUBLE.encode((double) finalY));
                    meta.append("debug_z", Codec.DOUBLE.encode((double) finalZ));
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
                drawCubeOutline(world,new Vector3d(finalX,finalY-0.4F,finalZ),width+0.05F,1,new Vector3f(1,0,0),0.05, 9999f, DebugFlags.NoSolid.getValue(), connection);

            }else {
                if (meta.containsKey("debug")){
                    if (!meta.getBoolean("debug").getValue()){
                        connection.write(new ClearDebugShapes());
                        meta.put("debug",Codec.BOOLEAN.encode(true));
                    }
                }else {
                    meta.append("debug",Codec.BOOLEAN.encode(true));
                    connection.write(new ClearDebugShapes());
                }

                if (meta.containsKey("debug_x")) {
                    float lastX = (float) meta.getDouble("debug_x").getValue();
                    float lastY = (float) meta.getDouble("debug_y").getValue();
                    float lastZ = (float) meta.getDouble("debug_z").getValue();

                    float width = areaOrderComponent.width;
//                    float[] matrix = new float[]{
//                            width-0.04F,0,0,0,
//                            0,99,0,0,
//                            0,0,width-0.04F,0,
//                            lastX, lastY+0.1F, lastZ,1
//                    };
//                    DisplayDebug debug = new DisplayDebug(DebugShape.Cube, matrix, new com.hypixel.hytale.protocol.Vector3f(0,1,0), 9999f, (byte) DebugFlags.NoSolid.getValue(), null, 0.1f);
//                    connection.write(debug);
                    drawCubeOutline(world,new Vector3d(lastX + 0.5F,lastY-49.5D,lastZ + 0.5F),width+0.05F,99,new Vector3f(1,0,0),0.05, 9999f, DebugFlags.NoSolid.getValue(), connection);
                }
            }
        }
        stackData.put("Miner",Codec.BSON_DOCUMENT.encode(meta));
        player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(stackData));

    }
    public static void drawCubeOutline(@Nonnull World world, @Nonnull Vector3d center, float width, float height, @Nonnull Vector3f color, double thickness, float time, int flags,PacketHandler connection) {
        double cx = center.x;
        double cy = center.y;
        double cz = center.z;

        double hw = width / 2.0;


        Vector3d p000 = new Vector3d(cx - hw, cy, cz - hw);
        Vector3d p001 = new Vector3d(cx - hw, cy, cz + hw);
        Vector3d p010 = new Vector3d(cx - hw, cy + height, cz - hw);
        Vector3d p011 = new Vector3d(cx - hw, cy + height, cz + hw);

        Vector3d p100 = new Vector3d(cx + hw, cy, cz - hw);
        Vector3d p101 = new Vector3d(cx + hw, cy, cz + hw);
        Vector3d p110 = new Vector3d(cx + hw, cy + height, cz - hw);
        Vector3d p111 = new Vector3d(cx + hw, cy + height, cz + hw);

        // Base
        addLine(world, p000, p001, color, thickness, time, flags,connection);
        addLine(world, p001, p101, color, thickness, time, flags,connection);
        addLine(world, p101, p100, color, thickness, time, flags,connection);
        addLine(world, p100, p000, color, thickness, time, flags,connection);

        // Top
        addLine(world, p010, p011, color, thickness, time, flags,connection);
        addLine(world, p011, p111, color, thickness, time, flags,connection);
        addLine(world, p111, p110, color, thickness, time, flags,connection);
        addLine(world, p110, p010, color, thickness, time, flags,connection);

        // Verticales
        addLine(world, p000, p010, color, thickness, time, flags,connection);
        addLine(world, p001, p011, color, thickness, time, flags,connection);
        addLine(world, p101, p111, color, thickness, time, flags,connection);
        addLine(world, p100, p110, color, thickness, time, flags,connection);
    }


    public static void addLine(@Nonnull World world, @Nonnull Vector3d start, @Nonnull Vector3d end, @Nonnull Vector3f color, double thickness, float time, int flags,PacketHandler connection) {
        addLine(world, start.x, start.y, start.z, end.x, end.y, end.z, color, thickness, time, flags,connection);
    }

    public static void addLine(@Nonnull World world, double startX, double startY, double startZ, double endX, double endY, double endZ, @Nonnull Vector3f color, double thickness, float time, int flags,PacketHandler connection) {
        double dirX = endX - startX;
        double dirY = endY - startY;
        double dirZ = endZ - startZ;
        double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (!(length < 0.001)) {
            Matrix4d tmp = new Matrix4d();
            Matrix4d matrix = new Matrix4d();
            matrix.identity();
            matrix.translate(startX, startY, startZ);
            double angleY = Math.atan2(dirZ, dirX);
            matrix.rotateAxis(angleY + (Math.PI / 2D), (double)0.0F, (double)1.0F, (double)0.0F, tmp);
            double angleX = Math.atan2(Math.sqrt(dirX * dirX + dirZ * dirZ), dirY);
            matrix.rotateAxis(angleX, (double)1.0F, (double)0.0F, (double)0.0F, tmp);
            matrix.translate((double)0.0F, length / (double)2.0F, (double)0.0F);
            matrix.scale(thickness, length, thickness);
            connection.write(new DisplayDebug(DebugShape.Cylinder, matrix.asFloatData(),new com.hypixel.hytale.protocol.Vector3f(color.x, color.y, color.z), time, (byte) flags,null,0.5F));
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
