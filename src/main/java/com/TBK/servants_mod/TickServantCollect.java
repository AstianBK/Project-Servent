package com.TBK.servants_mod;

import com.TBK.servants_mod.component.AreaOrderComponent;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;

import com.hypixel.hytale.server.core.entity.entities.Player;

import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.modules.entity.damage.DeferredCorpseRemoval;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TickServantCollect extends EntityTickingSystem<EntityStore> {
    @Nonnull
    private static final Query<EntityStore> QUERY = Query.and(new Query[]{NPCEntity.getComponentType(), TransformComponent.getComponentType(), HeadRotation.getComponentType(), Query.not(Player.getComponentType()), DeathComponent.getComponentType()});
    @Nonnull
    private static final Set<Dependency<EntityStore>> DEPENDENCIES;
    @Override
    public void tick(float v, int id, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {

        var ref = archetypeChunk.getReferenceTo(id);
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc == null) return;

        if(npc.getNPCTypeId().equals("ServantCollect")){
            List<ItemStack> stackList=new ArrayList<>();
            for (ComponentType<EntityStore, ? extends InventoryComponent> componentType : InventoryComponent.HOTBAR_UTILITY_CONSUMABLE_STORAGE){
                InventoryComponent inventory = store.getComponent(ref,componentType);
                for (int i= 0; i<inventory.getInventory().getCapacity() ;i++){
                    stackList.add(inventory.getInventory().getItemStack((short) i));
                }
                inventory.getInventory().removeAllItemStacks();
            }
            Vector3d dropPosition = npc.getTransformComponent().getPosition().clone().add((double)0.0F, (double)1.0F, (double)0.0F);
            Holder<EntityStore>[] drops = ItemComponent.generateItemDrops(store, stackList, dropPosition, Vector3f.ZERO.clone());
            commandBuffer.addEntities(drops, AddReason.SPAWN);
            ServantMod.LOGGER.atInfo().log("Servant %s",stackList);
        }

    }



    @Nonnull
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Nonnull
    public Set<Dependency<EntityStore>> getDependencies() {
        return DEPENDENCIES;
    }

    static {
        DEPENDENCIES = Set.of(new SystemDependency(Order.AFTER, DeathSystems.TickCorpseRemoval.class), new SystemDependency(Order.BEFORE, DeathSystems.CorpseRemoval.class));
    }
}
