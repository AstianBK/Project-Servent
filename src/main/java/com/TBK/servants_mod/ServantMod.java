package com.TBK.servants_mod;

import com.TBK.servants_mod.action.BuilderMinerAction;
import com.TBK.servants_mod.action.BuilderStoreBeaconPosition;
import com.TBK.servants_mod.component.AreaOrderComponent;
import com.TBK.servants_mod.component.MinerComponent;
import com.TBK.servants_mod.interaction.ModifyAreaMinerInteraction;
import com.TBK.servants_mod.interaction.SummonMinerInteraction;
import com.TBK.servants_mod.sensor.BuilderSensorEntityMarker;
import com.TBK.servants_mod.sensor.SensorMinerBuilder;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderFactory;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.instructions.Sensor;

public class ServantMod extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static ComponentType<EntityStore, MinerComponent> MINER_COMPONENT;
    public static ComponentType<EntityStore, AreaOrderComponent> AREA_COMPONENT;

    public ServantMod(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void start() {
        MINER_COMPONENT = getEntityStoreRegistry().registerComponent(MinerComponent.class,"miner_component",MinerComponent.CODEC);
        AREA_COMPONENT = getEntityStoreRegistry().registerComponent(AreaOrderComponent.class,"area_component",AreaOrderComponent.CODEC);
        this.getCommandRegistry().registerCommand(new MinerCommand(this.getName(), this.getManifest().getVersion().toString()));
        this.getEntityStoreRegistry().registerSystem(new RegisterComponent());
        this.getEntityStoreRegistry().registerSystem(new TickPlayerSystem());

    }



    @Override
    protected void setup() {
        this.getCodecRegistry(Interaction.CODEC).register("SummonMiner", SummonMinerInteraction.class,SummonMinerInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("ModifyArea", ModifyAreaMinerInteraction.class,ModifyAreaMinerInteraction.CODEC);

        BuilderFactory<Action> factoryAction = NPCPlugin.get().getBuilderManager().getFactory(Action.class);
        BuilderFactory<Sensor> factory = NPCPlugin.get().getBuilderManager().getFactory(Sensor.class);

        factoryAction.add("Miner", BuilderMinerAction::new);
        factoryAction.add("StoreBeaconEntity", BuilderStoreBeaconPosition::new);
        factory.add("BlockMiner", SensorMinerBuilder::new);
        factory.add("StoreBeaconEntity", BuilderSensorEntityMarker::new);
    }
}
