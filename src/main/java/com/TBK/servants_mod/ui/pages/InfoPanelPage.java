package com.TBK.servants_mod.ui.pages;


import com.TBK.servants_mod.ServantMod;
import com.TBK.servants_mod.ServantUtil;
import com.TBK.servants_mod.resource.GrowTreeManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.bson.BsonDocument;
import org.bson.types.Code;

import javax.annotation.Nonnull;

/**
 * InfoPanelPage - Displays multiple dynamic values in a panel.
 *
 * This page demonstrates:
 *   1. Passing multiple values to a page via constructor
 *   2. Setting multiple UI elements with different values
 *   3. Converting non-string values to strings for display
 *
 * Use case: Server info panel, player stats, inventory summary, etc.
 */
public class InfoPanelPage extends InteractiveCustomUIPage<InfoPanelPage.InfoEventData> {

    // Data to display - passed via constructor
    private final int playersOnline;
    private final int activeQuests;
    private final String uptime;

    /**
     * Empty EventData - we only need to handle the close button.
     */
    public static class InfoEventData {
        private String key;

        public static final BuilderCodec<InfoEventData> CODEC =
                BuilderCodec.builder(InfoEventData.class, InfoEventData::new).versioned().codecVersion(1)
                        .append(
                                new KeyedCodec<>("Key", Codec.STRING),
                                (event, value) -> event.key = value,
                                event -> event.key
                        )
                        .add().build();
    }

    /**
     * Constructor with multiple data values.
     *
     * @param playerRef     Reference to the player
     * @param playersOnline Number of online players
     * @param activeQuests  Number of active quests
     * @param uptime        Server uptime string
     */
    public InfoPanelPage(@Nonnull PlayerRef playerRef, int playersOnline, int activeQuests, String uptime) {
        super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, InfoEventData.CODEC);
        this.playersOnline = playersOnline;
        this.activeQuests = activeQuests;
        this.uptime = uptime;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        // Load the UI layout
        commandBuilder.append("Pages/InfoPanel.ui");

        // Set each stat value
        // Note: Numbers must be converted to String with String.valueOf()
        // The selector pattern is: #ElementId.Property
        commandBuilder.set("#Stat1Value.Text", String.valueOf(playersOnline));
        commandBuilder.set("#Stat2Value.Text", String.valueOf(activeQuests));
        commandBuilder.set("#Stat3Value.Text", uptime);  // Already a string

        // Bind close button
        eventBuilder.addEventBinding(CustomUIEventBindingType.KeyDown, "#CloseButton",new EventData().append("Key","#Key"));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull InfoEventData data) {
        Player player = (Player) store.getComponent(ref, Player.getComponentType());

        if (data.key != null) {
            ServantMod.LOGGER.atInfo().log("Key : %s",data.key);

        }
//        if (data.action == null) return;
//
//        switch (data.action) {
//            case "toggle_item":
//                toggleItemState(player,ref, store);
//                break;
//
//            case "close":
//                player.getPageManager().setPage(ref, store, Page.None);
//                break;
//        }
    }

    private void toggleItemState(Player player,Ref<EntityStore> ref, Store<EntityStore> store) {
        Inventory inv = player.getInventory();
        if (inv == null) return;

        ItemStack stack = inv.getItemInHand();
        if (stack == null) return;

        BsonDocument meta = stack.getMetadata();
        if (meta == null) meta = new BsonDocument();

        boolean current = meta.containsKey("active") && meta.getBoolean("active").getValue();

        meta.put("active", Codec.BOOLEAN.encode(!current));

    }

}
