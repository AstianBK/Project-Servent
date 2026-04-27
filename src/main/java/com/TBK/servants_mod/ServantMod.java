package com.TBK.servants_mod;

import com.TBK.servants_mod.action.BuilderActionTeleportToMine;
import com.TBK.servants_mod.action.BuilderAddItemsToContainerAction;
import com.TBK.servants_mod.action.BuilderMinerAction;
import com.TBK.servants_mod.action.BuilderStoreBeaconPosition;
import com.TBK.servants_mod.component.*;
import com.TBK.servants_mod.interaction.ContractCollectInteraction;
import com.TBK.servants_mod.interaction.ModifyAreaMinerInteraction;
import com.TBK.servants_mod.interaction.SummonLumberJackInteraction;
import com.TBK.servants_mod.interaction.SummonMinerInteraction;
import com.TBK.servants_mod.resource.GrowTreeManager;
import com.TBK.servants_mod.sensor.*;
import com.TBK.servants_mod.ui.commands.InfoCommand;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.ConditionInteraction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
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
    public static ComponentType<EntityStore, MinerSectionComponent> MINER_SECTION_COMPONENT;
    public static ComponentType<EntityStore,LumberjackZoneComponent> LUMBERJACK_COMPONENT;
    public static ResourceType<EntityStore, TreeManagerComponent> TREE_MANAGER_COMPONENT;
    public static ResourceType<EntityStore, GrowTreeManager> GROW_MANAGER_COMPONENT;

    public ServantMod(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void start() {
        GROW_MANAGER_COMPONENT = getEntityStoreRegistry().registerResource(GrowTreeManager.class,"grow_manager",GrowTreeManager.CODEC);
        LUMBERJACK_COMPONENT = getEntityStoreRegistry().registerComponent(LumberjackZoneComponent.class,"Lumberjack_zone",LumberjackZoneComponent.CODEC);
        TREE_MANAGER_COMPONENT = getEntityStoreRegistry().registerResource(TreeManagerComponent.class,"lumberjack_manager",TreeManagerComponent.CODEC);
        MINER_SECTION_COMPONENT = getEntityStoreRegistry().registerComponent(MinerSectionComponent.class,"miner_section_component",MinerSectionComponent.CODEC);
        MINER_COMPONENT = getEntityStoreRegistry().registerComponent(MinerComponent.class,"miner_component",MinerComponent.CODEC);
        AREA_COMPONENT = getEntityStoreRegistry().registerComponent(AreaOrderComponent.class,"area_component",AreaOrderComponent.CODEC);
        RECOLLECT_COMPONENT = getEntityStoreRegistry().registerComponent(RecollectComponent.class,"recollect_component",RecollectComponent.CODEC);
        getCommandRegistry().registerCommand(new InfoCommand());
        this.getEntityStoreRegistry().registerSystem(new RegisterComponent());
        this.getEntityStoreRegistry().registerSystem(new TickPlayerSystem());
        this.getEntityStoreRegistry().registerSystem(new TickServantCollect());
        this.getEntityStoreRegistry().registerSystem(new ManagerSystem());
        this.getEntityStoreRegistry().registerSystem(new ManagerSystem.PlaceBlockEventSystem());

        this.getEntityStoreRegistry().registerSystem(new ManagerSystem.TPCooldown());
    }



    @Override
    protected void setup() {
        this.getCodecRegistry(Interaction.CODEC).register("SummonLumberJack", SummonLumberJackInteraction.class,SummonLumberJackInteraction.CODEC);

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
        factory.add("SearchLumberJack",BuilderLumberJackSensor::new);
        factory.add("SearchPathLumberJack",BuilderPathLumberJackSensor::new);
        factory.add("BlockMiner", SensorMinerBuilder::new);
        factory.add("StoreBeaconEntity", BuilderSensorEntityMarker::new);
        factory.add("Stuck",BuilderSensorStuck::new);
    }
}
