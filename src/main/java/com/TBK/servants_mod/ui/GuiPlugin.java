package com.TBK.servants_mod.ui;


import com.TBK.servants_mod.ui.commands.*;
import com.TBK.servants_mod.ui.types.type1.Type1Command;
import com.TBK.servants_mod.ui.types.type2.Type2Command;
import com.TBK.servants_mod.ui.types.type3.Type3Command;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import javax.annotation.Nonnull;
import java.util.logging.Level;

public class GuiPlugin extends JavaPlugin {

    public GuiPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("GuiPlugin loaded!");

        // Register all UI demo commands
        getCommandRegistry().registerCommand(new TestUICommand());
        getCommandRegistry().registerCommand(new DialogCommand());
        getCommandRegistry().registerCommand(new FormCommand());
        getCommandRegistry().registerCommand(new InfoCommand());
        getCommandRegistry().registerCommand(new HelloWorldCommand());

        // Register tutorial commands
        getCommandRegistry().registerCommand(new Type1Command());
        getCommandRegistry().registerCommand(new Type2Command());
        getCommandRegistry().registerCommand(new Type3Command());

        getLogger().at(Level.INFO).log("Commands registered: /testui, /dialog, /form, /info, /tutorial1, /tutorial2, /tutorial3");
    }
}