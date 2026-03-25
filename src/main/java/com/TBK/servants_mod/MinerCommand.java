package com.TBK.servants_mod;

import com.TBK.servants_mod.component.MinerComponent;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigEditStore;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.operations.system.BrushOperation;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.buildertools.BrushShape;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BrushData;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BuilderToolData;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.Pair;
import org.bson.BsonDocument;
import org.jspecify.annotations.NonNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * This is an example command that will simply print the name of the plugin in chat when used.
 */
public class MinerCommand extends CommandBase {
    public MinerCommand(String pluginName, String pluginVersion) {
        super("minerCommand", "Ordena minar de acuerdo al brush obtenido del item.");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP
    }

    @Override
    protected void executeSync(@NonNull CommandContext commandContext) {
        UUID playerUUID = commandContext.sender().getUuid();

        PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(playerUUID);
        BrushConfigCommandExecutor brushConfigCommandExecutor = ToolOperation.getOrCreatePrototypeSettings(playerUUID).getBrushConfigCommandExecutor();
        Store<EntityStore> store = commandContext.senderAsPlayerRef().getStore();



        double x = brushConfigCommandExecutor.getEdit().getBefore().getX();
        double y = brushConfigCommandExecutor.getEdit().getBefore().getY();
        double z = brushConfigCommandExecutor.getEdit().getBefore().getZ();

        MinerComponent component = new MinerComponent();
        component.targetPos = new Vector3i(3,1,3);

        if(commandContext.senderAsPlayerRef()!=null){
            CompletableFuture.runAsync(()->{
                Player player = store.getComponent(commandContext.senderAsPlayerRef(),Player.getComponentType());
                BsonDocument meta = player.getInventory().getItemInHand().getMetadata();

                if (meta != null) {
                    BsonDocument brushData = meta.getDocument("BrushData");

                    int w = brushData.getInt32("Width").getValue();
                    int h = brushData.getInt32("Height").getValue();

                    ServantMod.LOGGER.atInfo().log("Width: " + w + " Height: " + h);
                    component.targetPos = new Vector3i(w,h,w);
                }
                Pair<Ref<EntityStore>, INonPlayerCharacter> pair = NPCPlugin.get().spawnNPC(store,"miner_orden_entity",null,new Vector3d(x,y+1.5D,z), Vector3f.NaN);
                Ref<EntityStore> ref1 = pair.first();
                store.addComponent(ref1,ServantMod.MINER_COMPONENT,component);
            },commandContext.senderAsPlayerRef().getStore().getExternalData().getWorld());

        }

    }


}