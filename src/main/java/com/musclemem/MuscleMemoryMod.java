package com.musclemem;

import com.musclemem.gui.SkillScreenData;
import com.musclemem.gui.SkillScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuscleMemoryMod implements ModInitializer {

    public static final String MOD_ID = "muscle-memory";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Muscle Memory v1.0.2 initializing...");

        SkillPersistence.init();

        SkillScreenHandler.TYPE = Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(MOD_ID, "skills"),
                new ExtendedMenuType<>(SkillScreenHandler::new, SkillScreenData.PACKET_CODEC)
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

                                SkillScreenData screenData = SkillScreenData.fromPlayer(player);
                                player.openMenu(new ExtendedMenuProvider<SkillScreenData>() {
                                    @Override
                                    public SkillScreenData getScreenOpeningData(ServerPlayer p) {
                                        return screenData;
                                    }

                                    @Override
                                    public Component getDisplayName() {
                                        return Component.literal("Muscle Memory");
                                    }

                                    @Override
                                    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                                        return new SkillScreenHandler(syncId, inv, screenData);
                                    }
                                });
                                return 1;
                            })
            );
        });

        LOGGER.info("Muscle Memory initialized.");
    }
}