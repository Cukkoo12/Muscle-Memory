package com.musclemem;

import com.musclemem.gui.SkillScreenData;
import com.musclemem.gui.SkillScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class MuscleMemoryMod implements ModInitializer {

    public static final String MOD_ID = "muscle-memory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Muscle Memory initializing...");

        SkillPersistence.init();

        SkillScreenHandler.TYPE = Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(MOD_ID, "skills"),
                new MenuType<>((syncId, inv) -> new SkillScreenHandler(syncId, inv), FeatureFlags.DEFAULT_FLAGS)
        );

        SkillEvents.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            SkillEvents.applyAllBonuses(player);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal("skills")
                            .executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayer();
                                if (player == null) return 0;
                                SkillData data = SkillPersistence.get(player);
                                ctx.getSource().sendSuccess(() ->
                                        Component.literal(data.formatForChat()), false);
                                return 1;
                            })
            );
            dispatcher.register(
                    Commands.literal("skills-gui")
                            .executes(ctx -> {
                                ServerPlayer player = ctx.getSource().getPlayer();
                                if (player == null) return 0;
                                // Data'yı static cache'e koy, client okusun
                                com.musclemem.gui.SkillScreenData screenData =
                                        com.musclemem.gui.SkillScreenData.fromPlayer(player);
                                SkillScreenHandler.lastOpenedData = screenData;
                                player.openMenu(new net.minecraft.world.MenuProvider() {
                                    @Override
                                    public Component getDisplayName() {
                                        return Component.literal("Muscle Memory");
                                    }
                                    @Override
                                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                                            int syncId, net.minecraft.world.entity.player.Inventory inv,
                                            net.minecraft.world.entity.player.Player p) {
                                        return new com.musclemem.gui.SkillScreenHandler(syncId, inv, screenData);
                                    }
                                });
                                return 1;
                            })
            );
        });

        LOGGER.info("Muscle Memory initialized.");
    }
}