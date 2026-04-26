package com.TBK.servants_mod.ui.types.type1;


import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

/**
 * Type 1: Static Display
 *
 * The simplest possible custom UI page.
 * - Extends BasicCustomUIPage (no event handling)
 * - Just loads a .ui file and displays it
 */
public class Type1Page extends BasicCustomUIPage {

    public Type1Page(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    @Override
    public void build(@Nonnull UICommandBuilder cmd) {
        // Load the UI file
        // Path is relative to: src/main/resources/Common/UI/Custom/
        cmd.append("Pages/Type1Page.ui");
    }
}
