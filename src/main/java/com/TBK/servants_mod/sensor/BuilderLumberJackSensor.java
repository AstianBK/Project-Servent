package com.TBK.servants_mod.sensor;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.npc.asset.builder.*;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.BlockSetExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderLumberJackSensor extends BuilderSensorBase {
    protected final DoubleHolder range = new DoubleHolder();
    protected final DoubleHolder yRange = new DoubleHolder();
    protected final AssetHolder blockSet = new AssetHolder();
    protected final BooleanHolder pickRandom = new BooleanHolder();
    protected final BooleanHolder reserveBlock = new BooleanHolder();
    protected final BuilderObjectReferenceHelper<Sensor> sensor = new BuilderObjectReferenceHelper<>(Sensor.class, this);

    @Nonnull
    public String getShortDescription() {
        return "Checks for one of a set of blocks in the nearby area";
    }
    @Nullable
    public Sensor getSensor(@Nonnull BuilderSupport support) {
        return (Sensor)this.sensor.build(support);
    }
    @Nonnull
    public String getLongDescription() {
        return "Checks for one of a set of blocks in the nearby area and caches the result until explicitly reset or the targeted block changes/is removed. All block sensors with the same sought blockset share the same targeted block once found";
    }

    @Nonnull
    public Sensor build(@Nonnull BuilderSupport builderSupport) {
        Sensor sensor1 = getSensor(builderSupport);
        return sensor1 !=null ? new SensorLumberJack(this, builderSupport,sensor1) : null;
    }

    @Nonnull
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Experimental;
    }

    @Nonnull
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.requireDouble(data, "Range", this.range, DoubleRangeValidator.fromExclToIncl((double)0.0F, Double.MAX_VALUE), BuilderDescriptorState.Stable, "The range to search for the blocks in", (String)null);
        this.getDouble(data, "MaxHeight", this.yRange, (double)4.0F, DoubleRangeValidator.fromExclToIncl((double)0.0F, Double.MAX_VALUE), BuilderDescriptorState.Stable, "The vertical range to search for the blocks in", (String)null);
        this.requireAsset(data, "Blocks", this.blockSet, BlockSetExistsValidator.required(), BuilderDescriptorState.Stable, "The set of blocks to search for", (String)null);
        this.getBoolean(data, "Random", this.pickRandom, false, BuilderDescriptorState.Stable, "Whether to pick at random from within the matched blocks or pick the closest", (String)null);
        this.getBoolean(data, "Reserve", this.reserveBlock, false, BuilderDescriptorState.Stable, "Whether to reserve the found block to prevent other NPCs selecting it", (String)null);
        this.provideFeature(Feature.Position);
        this.requireObject(data, "Sensor", this.sensor, BuilderDescriptorState.Stable, "Sensor to wrap", (String)null, this.validationHelper);

        return this;
    }

    public double getRange(@Nonnull BuilderSupport support) {
        return this.range.get(support.getExecutionContext());
    }

    public double getYRange(@Nonnull BuilderSupport support) {
        return this.yRange.get(support.getExecutionContext());
    }

    public int getBlockSet(@Nonnull BuilderSupport support) {
        String key = this.blockSet.get(support.getExecutionContext());
        int index = BlockSet.getAssetMap().getIndex(key);
        if (index == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Unknown key! " + key);
        } else {
            return index;
        }
    }

    public boolean isPickRandom(@Nonnull BuilderSupport support) {
        return this.pickRandom.get(support.getExecutionContext());
    }

    public boolean isReserveBlock(@Nonnull BuilderSupport support) {
        return this.reserveBlock.get(support.getExecutionContext());
    }
}
