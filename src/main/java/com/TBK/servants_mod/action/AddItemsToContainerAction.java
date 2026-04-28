package com.TBK.servants_mod.action;

import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.ServantUtil;
import com.TBK.servants_mod.component.RecollectComponent;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AddItemsToContainerAction extends ActionBase {
    public AddItemsToContainerAction(@NonNull BuilderActionBase builderActionBase) {
        super(builderActionBase);
    }

    @Override
    public boolean execute(@NonNull Ref<EntityStore> ref, @NonNull Role role, InfoProvider sensorInfo, double dt, @NonNull Store<EntityStore> store) {
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc == null) return false;
        RecollectComponent recollectComponent = store.getComponent(ref, ServantMod.RECOLLECT_COMPONENT);
        if (recollectComponent==null)return false;
        SimpleItemContainer container = ServantUtil.getContainer(recollectComponent.targetPos.x,recollectComponent.targetPos.y,recollectComponent.targetPos.z,store.getExternalData().getWorld());
        List<ItemStack> stackList=new ArrayList<>();
        for (ComponentType<EntityStore, ? extends InventoryComponent> componentType : InventoryComponent.HOTBAR_UTILITY_CONSUMABLE_STORAGE){
            InventoryComponent inventory = store.getComponent(ref,componentType);
            for (int i= 0; i<inventory.getInventory().getCapacity() ;i++){
                if (inventory.getInventory().getItemStack((short) i)!=null){
                    ItemStack stack = inventory.getInventory().getItemStack((short) i);
                    stackList.add(stack);
                }
            }
        }

        if (container.canAddItemStacks(stackList)){
            container.addItemStacks(stackList);
        }

        for (ComponentType<EntityStore, ? extends InventoryComponent> componentType : InventoryComponent.HOTBAR_UTILITY_CONSUMABLE_STORAGE){
            InventoryComponent inventory = store.getComponent(ref,componentType);
            inventory.getInventory().removeAllItemStacks();
        }

        return super.execute(ref, role, sensorInfo, dt, store);
    }
}
