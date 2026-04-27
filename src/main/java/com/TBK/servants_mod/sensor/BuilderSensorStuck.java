package com.TBK.servants_mod.sensor;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorStuck extends BuilderSensorBase {

    protected final DoubleHolder maxMoveDistance = new DoubleHolder();
    protected final DoubleHolder timeToStuck = new DoubleHolder();

    @Nonnull
    @Override
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.getDouble(data, "MaxMoveDistance", this.maxMoveDistance, 0.1, DoubleSingleValidator.greater0(), BuilderDescriptorState.Experimental, "Minimum movement distance to consider the entity moving", "ss");
        this.getDouble(data, "TimeToStuck", this.timeToStuck, 4.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Experimental, "Time in seconds before considering the entity stuck", "ss");
        return this;
    }

    public double getMaxMoveDistance(@Nonnull BuilderSupport support) {
        return this.maxMoveDistance.get(support.getExecutionContext());
    }

    public double getTimeToStuck(@Nonnull BuilderSupport support) {
        return this.timeToStuck.get(support.getExecutionContext());
    }

    @Override
    public @Nullable String getShortDescription() {
        return "Detects when an entity is not moving";
    }

    @Override
    public @Nullable String getLongDescription() {
        return "Triggers when the entity hasn't moved enough for a given amount of time";
    }

    @Override
    public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Experimental;
    }

    @Override
    public @Nullable Sensor build(BuilderSupport builderSupport) {
        return new SensorStuck(this,builderSupport);
    }
}