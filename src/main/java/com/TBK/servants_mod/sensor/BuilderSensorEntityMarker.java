package com.TBK.servants_mod.sensor;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class BuilderSensorEntityMarker extends BuilderSensorBase {
    protected final StringHolder message = new StringHolder();
    protected final DoubleHolder range = new DoubleHolder();
    protected String targetSlot;
    protected boolean consume;

    @Nonnull
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.requireString(data, "Message", this.message, StringNotEmptyValidator.get(), BuilderDescriptorState.Experimental, "The message to listen for", (String)null);
        this.getDouble(data, "Range", this.range, (double)64.0F, DoubleSingleValidator.greater0(), BuilderDescriptorState.Experimental, "The max distance beacons should be received from", (String)null);
        this.getString(data, "TargetSlot", (s) -> this.targetSlot = s, (String)null, StringNullOrNotEmptyValidator.get(), BuilderDescriptorState.Stable, "A slot to store the sender as a target. If omitted no target will be stored", (String)null);
        this.getBoolean(data, "ConsumeMessage", (b) -> this.consume = b, true, BuilderDescriptorState.Stable, "Whether the message should be consumed by this sensor", (String)null);
        this.provideFeature(Feature.AnyEntity);
        return this;
    }

    public int getMessageSlot(@Nonnull BuilderSupport builderSupport) {
        String name = this.message.get(builderSupport.getExecutionContext());
        return builderSupport.getBeaconMessageSlot(name);
    }

    public double getRange(@Nonnull BuilderSupport builderSupport) {
        return this.range.get(builderSupport.getExecutionContext());
    }

    public int getTargetSlot(@Nonnull BuilderSupport support) {
        return this.targetSlot == null ? Integer.MIN_VALUE : support.getTargetSlot(this.targetSlot);
    }

    public boolean isConsume() {
        return this.consume;
    }


    @Override
    public @Nullable String getShortDescription() {
        return "";
    }

    @Override
    public @Nullable String getLongDescription() {
        return "";
    }

    @Override
    public @Nullable BuilderDescriptorState getBuilderDescriptorState() {
        return null;
    }

    @Nonnull
    public Class<Sensor> category() {
        return Sensor.class;
    }

    public boolean getOnce() {
        return this.once;
    }

    public void setOnce(boolean once) {
        this.once = once;
    }

    public boolean isEnabled(ExecutionContext context) {
        return this.enabled.get(context);
    }

    @Override
    public @Nullable Sensor build(BuilderSupport builderSupport) {
        return new SensorEntityMarker(this,builderSupport);
    }

    public boolean validate(String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors) {
        validationHelper.updateParentSensorOnce(this.once);
        return super.validate(configName, validationHelper, context, globalScope, errors);
    }
}
