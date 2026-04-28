package com.TBK.servants_mod.interaction;

import com.TBK.servants_mod.ui.pages.SelectServantPage;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class OpenOrdenMenu extends SimpleInstantInteraction {

    public static final BuilderCodec<OpenOrdenMenu> CODEC =
            BuilderCodec.builder(OpenOrdenMenu.class, OpenOrdenMenu::new, SimpleInstantInteraction.CODEC)
                    .build();

    @Override
    protected void firstRun(@NotNull InteractionType interactionType, @NotNull InteractionContext interactionContext, @NotNull CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = interactionContext.getEntity();
        Store<EntityStore> store = ref.getStore();
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        Player player = store.getComponent(ref, Player.getComponentType());

        if (playerRef==null || player == null)return;


        CompletableFuture.runAsync(
                () -> {
                    if (player.getPageManager().getCustomPage()==null){
                        SelectServantPage page = new SelectServantPage(playerRef," "," ");
                        player.getPageManager().openCustomPage(ref, store, page);
                    }
                });


    }


}