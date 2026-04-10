package com.musclemem;

import com.musclemem.gui.SkillScreenData;
import com.musclemem.gui.SkillScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
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

        // 1. Menü tipini ExtendedScreenHandlerType olarak kaydediyoruz.
        // Bu sayede oyun, GUI açılırken SkillScreenData.PACKET_CODEC ile veriyi paketleyip istemciye gönderir.
        SkillScreenHandler.TYPE = Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(MOD_ID, "skills"),
                new ExtendedScreenHandlerType<>(SkillScreenHandler::new, SkillScreenData.PACKET_CODEC)
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

                                // 2. ExtendedScreenHandlerFactory kullanarak ekranı açıyoruz.
                                player.openMenu(new ExtendedScreenHandlerFactory<SkillScreenData>() {
                                    @Override
                                    public SkillScreenData getScreenOpeningData(ServerPlayer player) {
                                        // Paket olarak gönderilecek veriyi burada belirliyoruz
                                        return SkillScreenData.fromPlayer(player);
                                    }

                                    @Override
                                    public Component getDisplayName() {
                                        return Component.literal("Muscle Memory");
                                    }

                                    @Override
                                    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                                        // Sunucu tarafında menüyü oluşturuyoruz
                                        return new SkillScreenHandler(syncId, inv, getScreenOpeningData((ServerPlayer) p));
                                    }
                                });
                                return 1;
                            })
            );
        });

        LOGGER.info("Muscle Memory initialized.");
    }
}