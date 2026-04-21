package com.TBK.servants_mod.action;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionTeleportToMine extends BuilderActionBase {
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