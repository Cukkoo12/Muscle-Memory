package com.musclemem.client;

import com.musclemem.gui.SkillScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class MuscleMemoryClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Link MenuType → Screen. The SkillScreenHandler(syncId, inv, FriendlyByteBuf)
        // constructor is called by vanilla when the server's openMenu() packet arrives.
        MenuScreens.register(SkillScreenHandler.TYPE, SkillScreen::new);
    }
}
