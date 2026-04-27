package com.TBK.servants_mod.action;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionTeleportToMine extends BuilderActionBase {

    protected final StringHolder targetSlot = new StringHolder();
    @Nonnull
    @Override
    public Builder<Action> readConfig(@Nonnull JsonElement data) {
        this.getString(data, "TargetSlot", this.targetSlot, (String)null, StringNullOrNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The target slot of the marked entity to send. Omit to send own position", (String)null);
        return this;
    }

    public int getTargetSlot(@Nonnull BuilderSupport support) {
        String slotName = this.targetSlot.get(support.getExecutionContext());
        return slotName == null ? Integer.MIN_VALUE : support.getTargetSlot(slotName);
    }

    @Override
    public @Nullable String getShortDescription() {
        return "Teleports entity to mining target";
    }

    @Override
    public @Nullable String getLongDescription() {
        return "Teleports the NPC above the target block stored in a slot";
    }

    @Override
    public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Experimental;
    }


    @Override
    public @Nullable Action build(BuilderSupport builderSupport) {
        return new ActionTeleportToSlot(this,builderSupport);
    }
}