package com.TBK.servants_mod.ui.commands;

import com.TBK.servants_mod.ui.pages.HelloWorldPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class HelloWorldCommand extends AbstractPlayerCommand {

    public HelloWorldCommand() {
        super("helloworld", "sample command", false);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext commandContext,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        HelloWorldPage page = new HelloWorldPage(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, "Test Test");
        assert player != null;
        player.getPageManager().openCustomPage(ref, store, page);
    }
}
