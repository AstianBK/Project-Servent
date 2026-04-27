package com.TBK.servants_mod.component;

import com.TBK.servants_mod.data.TreeData;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class TreeManagerComponent implements Resource<EntityStore> {
    public static final BuilderCodec<TreeManagerComponent> CODEC =
            BuilderCodec.builder(TreeManagerComponent.class, TreeManagerComponent::new)
                    .versioned()
                    .codecVersion(1)
//                    .append(
//                            new KeyedCodec<>("TreeList",
//                                    ArrayCodec.ofBuilderCodec(TreeData.CODEC, TreeData[]::new)
//                            ),
//                            (o, v) -> {
//                                if (o != null) {
//                                    o.treeList = new ArrayList<>(Arrays.asList(v));
//                                }
//                            },
//                            o -> o != null && o.treeList != null
//                                    ? o.treeList.toArray(TreeData[]::new)
//                                    : new TreeData[0]
//                    )
//                    .add()
                    .build();
    public List<TreeData> treeList = new ArrayList<>();
    public final Map<Vector3i, TreeData> trees = new HashMap<>();

    private final Map<Long, List<TreeData>> treesByChunk = new HashMap<>();

    public void addTree(TreeData tree) {
        trees.put(tree.center, tree);

        long chunkKey = chunkKey(tree.center);
        treesByChunk.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(tree);
    }

    public void removeTree(Vector3i center) {
        TreeData tree = trees.remove(center);
        if (tree == null) return;

        long key = chunkKey(center);
        List<TreeData> list = treesByChunk.get(key);
        if (list != null) list.remove(tree);
    }
    public void rebuild() {

        trees.clear();
        treesByChunk.clear();

        for (TreeData tree : treeList) {
            if (tree == null|| tree.center==null)continue;
            trees.put(tree.center, tree);

            long key = chunkKey(tree.center);
            treesByChunk
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(tree);
        }
    }
    public TreeData requestTree(Vector3i npcPos) {

        TreeData best = null;
        double bestDist = Double.MAX_VALUE;

        for (TreeData tree : getNearbyTrees(npcPos)) {

            if (tree.reserved) continue;
            if (tree.surfaceLogs.isEmpty()) continue;

            double dist = tree.distanceTo(npcPos);

            if (dist < bestDist) {
                bestDist = dist;
                best = tree;
            }
        }

        return best;
    }

    public void reserveTree(TreeData tree, Ref<EntityStore> npc) {
        tree.reserved = true;
        tree.assignedNpc = npc;
    }

    public void releaseTree(TreeData tree) {
        tree.reserved = false;
        tree.assignedNpc = null;
    }

    private List<TreeData> getNearbyTrees(Vector3i pos) {

        List<TreeData> result = new ArrayList<>();

        int cx = pos.x >> 4;
        int cz = pos.z >> 4;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long key = chunkKey(cx + dx, cz + dz);
                List<TreeData> list = treesByChunk.get(key);
                if (list != null) result.addAll(list);
            }
        }

        return result;
    }

    private long chunkKey(Vector3i pos) {
        return chunkKey(pos.x >> 4, pos.z >> 4);
    }

    private long chunkKey(int cx, int cz) {
        return (((long) cx) << 32) | (cz & 0xffffffffL);
    }

    @Override
    public @Nullable Resource<EntityStore> clone() {
        TreeManagerComponent copy = new TreeManagerComponent();

        copy.treeList = new ArrayList<>(this.treeList);

        copy.rebuild();

        return copy;
    }
}