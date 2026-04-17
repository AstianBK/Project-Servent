package com.TBK.servants_mod.sensor;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.*;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.BlockSetExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderSensorSearchChest extends BuilderSensorBase {

    @Nonnull
    public String getShortDescription() {
        return "Checks for one of a set of blocks in the nearby area";
    }

    @Nonnull
    public String getLongDescription() {
        return "Checks for one of a set of blocks in the nearby area and caches the result until explicitly reset or the targeted block changes/is removed. All block sensors with the same sought blockset share the same targeted block once found";
    }

    @Nonnull
    public Sensor build(@Nonnull BuilderSupport builderSupport) {
        return new SensorSearchChest(this, builderSupport);
    }

    @Nonnull
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Experimental;
    }

    @Nonnull
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        return this;
    }
}
