package com.TBK.servants_mod;

import com.TBK.servants_mod.data.TreeData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.npc.blackboard.view.resource.ResourceView;
import org.bson.BsonDocument;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class ServantUtil {
    public static void setGauntletType(BsonDocument meta,String type){
        if(meta.containsKey("GauntletType")){
            meta.put("GauntletType",Codec.STRING.encode(type));
        }else {
            meta.append("GauntletType", Codec.STRING.encode(type));
        }
    }
    public static BsonDocument getGauntletTypeData(BsonDocument meta,String type){
        if (meta.containsKey(type)){
            return meta.getDocument(type);
        }
        return null;
    }
    public static String getGauntletType(BsonDocument meta){
        if(meta.containsKey("GauntletType")){
            return meta.getString("GauntletType").getValue();
        }
        return null;
    }
    public static SimpleItemContainer getContainer(int x, int y, int z, World world){
        int chunkX = ChunkUtil.chunkCoordinate(x);
        int chunkZ = ChunkUtil.chunkCoordinate(z);

        long chunkIndex = ChunkUtil.indexChunk(chunkX, chunkZ);

        Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(chunkIndex);

        if (chunkRef == null) {
            ServantMod.LOGGER.atInfo().log("No chunk");
            return null;
        }

        BlockComponentChunk blockChunk = world.getChunkStore().getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());

        if (blockChunk == null) {
            ServantMod.LOGGER.atInfo().log("No BlockComponentChunk");
            return null;
        }


        int index = ChunkUtil.indexBlockInColumn(x, y, z);


        ItemContainerBlock chest = blockChunk.getComponent(index, ItemContainerBlock.getComponentType());

        if (chest == null) {
            return null;
        }


        SimpleItemContainer container = chest.getItemContainer();

        return container;
    }

    public static long getChunkIndex(int x,int z){
        int chunkX = ChunkUtil.chunkCoordinate(x);
        int chunkZ = ChunkUtil.chunkCoordinate(z);

        return ChunkUtil.indexChunk(chunkX, chunkZ);
    }
    public static TreeData detectTree(World world, ResourceView view, Vector3i start) {

        if (!isLog(world, start)) return null;

        Set<Long> visited = new HashSet<>();
        Deque<Vector3i> stack = new ArrayDeque<>();

        stack.push(start);

        int maxY = start.y;
        int count = 0;

        TreeData tree = new TreeData();

        int[][] directions = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };

        while (!stack.isEmpty()) {
            Vector3i pos = stack.pop();

            long key = toLong(pos);
            if (!visited.add(key)) continue;

            if (!isLog(world, pos)) continue;
            if (view.isBlockReserved(pos.x, pos.y, pos.z)) continue;


            count++;
            if (count > 100) {
                break;
            }

            if (pos.y > maxY) {
                maxY = pos.y;
            }

            if (hasAirExposure(world, pos)) {
                tree.center = pos;
            }

            for (int[] dir : directions) {
                int nx = pos.x + dir[0];
                int ny = pos.y + dir[1];
                int nz = pos.z + dir[2];

                long neighborKey = toLong(nx, ny, nz);
                if (!visited.contains(neighborKey)) {
                    stack.push(new Vector3i(nx, ny, nz));
                }
            }
        }

        if (visited.isEmpty()) return null;

        tree.topY = maxY;
        return tree;
    }
    private static long toLong(Vector3i v) {
        return (((long) v.x & 0x3FFFFFF) << 38) |
                (((long) v.z & 0x3FFFFFF) << 12) |
                ((long) v.y & 0xFFF);
    }

    private static long toLong(int x, int y, int z) {
        return (((long) x & 0x3FFFFFF) << 38) |
                (((long) z & 0x3FFFFFF) << 12) |
                ((long) y & 0xFFF);
    }
    public static boolean isLog(World world, Vector3i pos) {
        BlockType t = world.getBlockType(pos.x, pos.y, pos.z);
        return t != null && t.getId().contains("Wood") && !t.getId().contains("Sticks");
    }

    public static boolean hasAirExposure(World world, Vector3i pos) {
        int air = 0;

        if (isAir(world, pos.x+1,pos.y,pos.z)) air++;
        if (isAir(world, pos.x-1,pos.y,pos.z)) air++;
        if (isAir(world, pos.x,pos.y,pos.z+1)) air++;
        if (isAir(world, pos.x,pos.y,pos.z-1)) air++;

        if (isAir(world, pos.x+1,pos.y,pos.z+1)) air++;
        if (isAir(world, pos.x-1,pos.y,pos.z-1)) air++;
        if (isAir(world, pos.x-1,pos.y,pos.z+1)) air++;
        if (isAir(world, pos.x+1,pos.y,pos.z-1)) air++;
        return air >= 3 && isLog(world,new Vector3i(pos.x,pos.y+1,pos.z)) && isGround(world,pos.x,pos.y-1,pos.z);
    }

    private static boolean isGround(World world, int x, int y, int z) {
        BlockType t = world.getBlockType(x, y, z);
        int dirt = 0;
        if (isDirt(world, x+1,y,z)) dirt++;
        if (isDirt(world, x-1,y,z)) dirt++;
        if (isDirt(world, x,y,z+1)) dirt++;
        if (isDirt(world, x,y,z-1)) dirt++;

        if (isDirt(world, x+1,y,z+1)) dirt++;
        if (isDirt(world, x-1,y,z-1)) dirt++;
        if (isDirt(world, x-1,y,z+1)) dirt++;
        if (isDirt(world, x+1,y,z-1)) dirt++;

        return t!=null && (t.getId().contains("Wood") || t.getId().contains("Soil") || t.getId().contains("Grass")) && dirt > 1;
    }

    private static boolean isDirt(World world, int x ,int y, int z) {
        BlockType t = world.getBlockType(x, y, z);

        return t!=null && (t.getId().contains("Soil") || t.getId().contains("Grass"));

    }

    public static boolean isAir(World world, int x, int y, int z) {
        BlockType t = world.getBlockType(x, y, z);
        return t == null || (t.getId().equals("Empty") || t.getMaterial() == BlockMaterial.Empty);
    }
    public static boolean isSolid(World world, int x, int y, int z) {
        BlockType t = world.getBlockType(x, y, z);
        return t == null || t.getMaterial() == BlockMaterial.Solid;
    }

    public static boolean isAir(WorldChunk world, int x, int y, int z) {
        BlockType t = world.getBlockType(x, y, z);
        return t == null || (t.getId().equals("Empty") || t.getMaterial() == BlockMaterial.Empty);
    }
    public static boolean isSolid(WorldChunk world, int x, int y, int z) {
        BlockType t = world.getBlockType(x, y, z);
        return t == null || t.getMaterial() == BlockMaterial.Solid;
    }
}
