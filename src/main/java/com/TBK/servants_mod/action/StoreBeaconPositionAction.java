package com.TBK.servants_mod.action;

import com.TBK.servants_mod.ServantMod;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import com.hypixel.hytale.server.npc.role.support.PositionCache;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class StoreBeaconPositionAction extends ActionBase {
    protected final String message;
    protected final double range;
    protected final int[] targetGroups;
    protected final int targetToSendSlot;
    protected final double expirationTime;
    protected final int sendCount;
    @Nullable
    protected final List<Ref<EntityStore>> sendList;
    public StoreBeaconPositionAction(@Nonnull BuilderStoreBeaconPosition builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.message = builder.getMessage(support);
        this.range = builder.getRange();
        this.targetGroups = builder.getTargetGroups(support);
        this.targetToSendSlot = builder.getTargetToSendSlot(support);
        this.expirationTime = builder.getExpirationTime();
        this.sendCount = builder.getSendCount();
        this.sendList = this.sendCount > 0 ? new ObjectArrayList(this.sendCount) : null;
    }

    public void registerWithSupport(@Nonnull Role role) {
        role.getPositionCache().requireEntityDistanceUnsorted(this.range);
    }

    public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.canExecute(ref, role, sensorInfo, dt, store)) {
            return false;
        } else {
            return this.targetToSendSlot == Integer.MIN_VALUE || role.getMarkedEntitySupport().hasMarkedEntityInSlot(this.targetToSendSlot);
        }
    }

    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(ref, role, sensorInfo, dt, store);
        Ref<EntityStore> target = this.targetToSendSlot >= 0 ? role.getMarkedEntitySupport().getMarkedEntityRef(this.targetToSendSlot) : ref;
        PositionCache positionCache = role.getPositionCache();
        if (this.sendCount <= 0) {
            positionCache.forEachNPCUnordered(this.range, StoreBeaconPositionAction::filterNPCs, (_ref, _this, _target, _self) -> _this.sendNPCMessage(_self, _ref, _target, _self.getStore()), this, role, target, ref, store);
            return true;
        } else {
            positionCache.forEachNPCUnordered(this.range, StoreBeaconPositionAction::filterNPCs, (npcEntity, _this, _sendList, _self) -> RandomExtra.reservoirSample(npcEntity, _this.sendCount, _sendList), this, role, this.sendList, ref, store);

            for(int i = 0; i < this.sendList.size(); ++i) {
                this.sendNPCMessage(ref, (Ref)this.sendList.get(i), target, store);
            }

            this.sendList.clear();
            return true;
        }
    }

    protected static boolean filterNPCs(@Nonnull Ref<EntityStore> ref, @Nonnull StoreBeaconPositionAction _this, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        return ref.getStore().getComponent(ref, BeaconSupport.getComponentType()) != null && WorldSupport.isGroupMember(role.getRoleIndex(), ref, _this.targetGroups, componentAccessor);
    }

    protected void sendNPCMessage(@Nonnull Ref<EntityStore> self, @Nonnull Ref<EntityStore> targetRef, @Nonnull Ref<EntityStore> target, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
        NPCEntity npcComponent = (NPCEntity)componentAccessor.getComponent(self, NPCEntity.getComponentType());

        assert npcComponent != null;

        Role role = npcComponent.getRole();
        if (role.getDebugSupport().isDebugFlagSet(RoleDebugFlags.BeaconMessages)) {
            ((HytaleLogger.Api) NPCPlugin.get().getLogger().atInfo()).log("ID %d sent message '%s' with target ID %d to ID %d", self.getIndex(), this.message, target.getIndex(), targetRef.getIndex());
            ThreadLocalRandom random = ThreadLocalRandom.current();
            Vector3f color = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            Matrix4d matrix = new Matrix4d();
            matrix.identity();
            Matrix4d tmp = new Matrix4d();
            TransformComponent transformComponent = (TransformComponent)componentAccessor.getComponent(self, TransformComponent.getComponentType());

            assert transformComponent != null;

            Vector3d pos = transformComponent.getPosition();
            ModelComponent modelComponent = (ModelComponent)componentAccessor.getComponent(self, ModelComponent.getComponentType());

            assert modelComponent != null;

            Model model = modelComponent.getModel();
            double x = pos.x;
            double y = pos.y + (double)(model != null ? model.getEyeHeight(self, componentAccessor) : 0.0F);
            double z = pos.z;
            matrix.translate(x, y + (double)random.nextFloat() * (double)0.5F - (double)0.25F, z);
            TransformComponent targetTransformComponent = (TransformComponent)componentAccessor.getComponent(targetRef, TransformComponent.getComponentType());

            assert targetTransformComponent != null;

            Vector3d targetPos = targetTransformComponent.getPosition();
            ModelComponent targetModelComponent = (ModelComponent)componentAccessor.getComponent(targetRef, ModelComponent.getComponentType());
            float targetEyeHeight = targetModelComponent != null ? targetModelComponent.getModel().getEyeHeight(targetRef, componentAccessor) : 0.0F;
            x -= targetPos.getX();
            y -= targetPos.getY() + (double)targetEyeHeight;
            z -= targetPos.getZ();
            double angleY = Math.atan2(-z, -x);
            matrix.rotateAxis(angleY + (double)((float)Math.PI / 2F), (double)0.0F, (double)1.0F, (double)0.0F, tmp);
            double angleX = Math.atan2(Math.sqrt(x * x + z * z), -y);
            matrix.rotateAxis(angleX, (double)1.0F, (double)0.0F, (double)0.0F, tmp);
            DebugUtils.addArrow(((EntityStore)componentAccessor.getExternalData()).getWorld(), matrix, color, pos.distanceTo(targetPos), 5.0F, true);
        }

        BeaconSupport beaconSupportComponent = (BeaconSupport)componentAccessor.getComponent(targetRef, BeaconSupport.getComponentType());
        if (beaconSupportComponent != null) {
            beaconSupportComponent.postMessage(this.message, target, this.expirationTime);
        }

    }
}
