package com.TBK.servants_mod.action;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import org.jspecify.annotations.NonNull;

public class ActionTeleportToSlot extends ActionBase {
    public final int slot;

    public ActionTeleportToSlot(@NonNull BuilderActionTeleportToMine builder, BuilderSupport support) {
        super(builder);
        this.slot = support.getTargetSlot("Pos");
    }

    @Override
    public boolean execute(@NonNull Ref<EntityStore> ref, @NonNull Role role, InfoProvider sensorInfo, double dt, @NonNull Store<EntityStore> store) {
        if (!super.execute(ref, role, sensorInfo, dt, store)) return false;
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());

        if (npc == null || transform == null) return false;

        Vector3d target = role.getMarkedEntitySupport().getStoredPosition(slot);

        if (target == null || target.equals(Vector3d.MIN)) return false;

        World world = npc.getWorld();

        int x = (int) target.x;
        int y = (int) target.y;
        int z = (int) target.z;

        int safeY = y;


        npc.moveTo(ref,x + 0.5, y+1.5, z + 0.5,store);
        world.getNotificationHandler().sendBlockParticle(x+0.5, (y + 1.5),z+0.5, BlockType.getBlockIdOrUnknown("Rock_Crystal_Green_Block"," "), BlockParticleEvent.Break);

        return true;
    }
}