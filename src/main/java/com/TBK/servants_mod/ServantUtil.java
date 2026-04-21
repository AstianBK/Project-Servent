package com.TBK.servants_mod;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class ServantUtil {
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
            ServantMod.LOGGER.atInfo().log("No chest component");
            return null;
        }


        SimpleItemContainer container = chest.getItemContainer();

        ServantMod.LOGGER.atInfo().log("Capacity: %s", container.getCapacity());

        for (int i = 0; i < container.getCapacity(); i++) {
            ServantMod.LOGGER.atInfo().log("Slot %s: %s", i, container.getItemStack((short) i));
        }
        return container;
    } 
}
