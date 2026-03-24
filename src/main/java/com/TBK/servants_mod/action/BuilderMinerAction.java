package com.TBK.servants_mod.action;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class BuilderMinerAction extends BuilderActionBase {
    @Override
    public @Nullable String getShortDescription() {
        return "";
    }

    @Override
    public @Nullable String getLongDescription() {
        return "";
    }

    @Override
    public @Nullable Action build(BuilderSupport builderSupport) {
        return new MinerAction(this);
    }

    @Nonnull
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }
}
