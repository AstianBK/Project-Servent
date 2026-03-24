package com.TBK.servants_mod;

import com.TBK.servants_mod.component.MinerComponent;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.Pair;
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

        double x = brushConfigCommandExecutor.getEdit().getAfter().getX();
        double y = brushConfigCommandExecutor.getEdit().getAfter().getY();
        double z = brushConfigCommandExecutor.getEdit().getAfter().getZ();

        MinerComponent component = new MinerComponent();


        int width = prototypeSettings.getBrushConfig().getShapeWidth();
        int height = prototypeSettings.getBrushConfig().getShapeHeight();
        component.targetPos = new Vector3i(width,height,width);
        ServantMod.LOGGER.atInfo().log("x: %s , y: %s",width,height);

        if(commandContext.senderAsPlayerRef()!=null){
            CompletableFuture.runAsync(()->{
                Pair<Ref<EntityStore>, INonPlayerCharacter> pair = NPCPlugin.get().spawnNPC(store,"miner_orden_entity",null,new Vector3d(x,y+1.5D,z), Vector3f.NaN);
                Ref<EntityStore> ref1 = pair.first();
                store.addComponent(ref1,ServantMod.MINER_COMPONENT,component);
                ServantMod.LOGGER.atInfo().log("x: %s , y: %s",width,height);
            },commandContext.senderAsPlayerRef().getStore().getExternalData().getWorld());

        }

    }


}