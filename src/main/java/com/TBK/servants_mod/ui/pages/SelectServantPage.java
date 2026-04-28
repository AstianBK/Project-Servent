package com.TBK.servants_mod.ui.pages;

import com.TBK.servants_mod.ServantUtil;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.protocol.packets.player.ClearDebugShapes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;

import javax.annotation.Nonnull;

/**
 * SelectServantPage - A dialog with buttons that can be clicked.
 *
 * EXTENDS: InteractiveCustomUIPage<DialogEventData>
 *   - Use this when you need to handle events (button clicks, inputs, etc.)
 *   - Generic parameter <DialogEventData> defines what data we receive from events
 *
 * LIFETIME: CanDismissOrCloseThroughInteraction
 *   - Player can press ESC to close
 *   - Or the page closes when certain interactions happen
 *
 * This page demonstrates:
 *   1. Event binding with eventBuilder.addEventBinding()
 *   2. Handling events in handleDataEvent()
 *   3. Closing the page programmatically
 */
public class SelectServantPage extends InteractiveCustomUIPage<SelectServantPage.DialogEventData> {

    private final String headline;
    private final String message;

    public static class DialogEventData {
        private String key;

        public static final BuilderCodec<DialogEventData> CODEC =
                BuilderCodec.builder(DialogEventData.class, DialogEventData::new).versioned().codecVersion(1)
                        .append(
                                new KeyedCodec<>("Key", Codec.STRING),
                                (event, value) -> event.key = value,
                                event -> event.key
                        )
                        .add().build();
    }


    public SelectServantPage(@Nonnull PlayerRef playerRef, String headline, String message) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, DialogEventData.CODEC);
        this.headline = headline;
        this.message = message;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {// Load the UI layout
        commandBuilder.append("Pages/SelectServant.ui");


        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MinerButtom",new EventData().append("Key", "Miner"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LumberJackButtom",new EventData().append("Key", "LumberJack"));
        eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CollectButtom",new EventData().append("Key", "Collect"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull DialogEventData data) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());

        ItemStack stack = player.getInventory().getItemInHand();

        BsonDocument meta = stack.getMetadata();

        if (meta==null)meta=new BsonDocument();
        String oldType = ServantUtil.getGauntletType(meta);
        BsonDocument gauntletData = ServantUtil.getGauntletTypeData(meta,oldType);
        if (gauntletData!=null){
            if (gauntletData.containsKey("debug")){
                gauntletData.put("debug",Codec.BOOLEAN.encode(false));
            }else {
                gauntletData.append("debug",Codec.BOOLEAN.encode(false));
            }
            if (meta.containsKey(oldType)){
                meta.put(oldType,Codec.BSON_DOCUMENT.encode(gauntletData));
            }else {
                meta.append(oldType,Codec.BSON_DOCUMENT.encode(gauntletData));
            }
        }



        ServantUtil.setGauntletType(meta, data.key);
        player.getInventory().getHotbar().replaceItemStackInSlot(player.getInventory().getActiveHotbarSlot(),stack,stack.withMetadata(meta));
        player.getPageManager().setPage(ref, store, Page.None);

        player.getPlayerConnection().write(new ClearDebugShapes());

    }
}
