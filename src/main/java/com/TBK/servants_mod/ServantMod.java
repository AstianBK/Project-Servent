package com.TBK.servants_mod;

import com.TBK.servants_mod.action.BuilderActionTeleportToMine;
import com.TBK.servants_mod.action.BuilderAddItemsToContainerAction;
import com.TBK.servants_mod.action.BuilderMinerAction;
import com.TBK.servants_mod.action.BuilderStoreBeaconPosition;
import com.TBK.servants_mod.component.AreaOrderComponent;
import com.TBK.servants_mod.component.MinerComponent;
import com.TBK.servants_mod.component.RecollectComponent;
import com.TBK.servants_mod.interaction.ContractCollectInteraction;
import com.TBK.servants_mod.interaction.ModifyAreaMinerInteraction;
import com.TBK.servants_mod.interaction.SummonMinerInteraction;
import com.TBK.servants_mod.sensor.*;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ConditionInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderFactory;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import org.bson.BsonDocument;

public class ServantMod extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static ComponentType<EntityStore, MinerComponent> MINER_COMPONENT;
    public static ComponentType<EntityStore, AreaOrderComponent> AREA_COMPONENT;
    public static ComponentType<EntityStore, RecollectComponent> RECOLLECT_COMPONENT;

    public static BsonDocument document = new BsonDocument();

    public ServantMod(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void start() {
        MINER_COMPONENT = getEntityStoreRegistry().registerComponent(MinerComponent.class,"miner_component",MinerComponent.CODEC);
        AREA_COMPONENT = getEntityStoreRegistry().registerComponent(AreaOrderComponent.class,"area_component",AreaOrderComponent.CODEC);
        RECOLLECT_COMPONENT = getEntityStoreRegistry().registerComponent(RecollectComponent.class,"recollect_component",RecollectComponent.CODEC);
        this.getCommandRegistry().registerCommand(new MinerCommand(this.getName(), this.getManifest().getVersion().toString()));
        this.getEntityStoreRegistry().registerSystem(new RegisterComponent());
        this.getEntityStoreRegistry().registerSystem(new TickPlayerSystem());
        this.getEntityStoreRegistry().registerSystem(new TickServantCollect());
    }



    @Override
    protected void setup() {
        this.getCodecRegistry(Interaction.CODEC).register("SummonMiner", SummonMinerInteraction.class,SummonMinerInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("ModifyArea", ModifyAreaMinerInteraction.class,ModifyAreaMinerInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("CollectInteraction", ContractCollectInteraction.class, ContractCollectInteraction.CODEC);

        BuilderFactory<Action> factoryAction = NPCPlugin.get().getBuilderManager().getFactory(Action.class);
        BuilderFactory<Sensor> factory = NPCPlugin.get().getBuilderManager().getFactory(Sensor.class);

        factoryAction.add("Miner", BuilderMinerAction::new);
        factoryAction.add("StoreBeaconEntity", BuilderStoreBeaconPosition::new);
        factoryAction.add("AddItemsToInventory", BuilderAddItemsToContainerAction::new);
        factoryAction.add("ActionTeleportToSlot", BuilderActionTeleportToMine::new);

        factory.add("SearchChest", BuilderSensorSearchChest::new);
        factory.add("BlockMiner", SensorMinerBuilder::new);
        factory.add("StoreBeaconEntity", BuilderSensorEntityMarker::new);
        factory.add("Stuck",BuilderSensorStuck::new);
    }
}
