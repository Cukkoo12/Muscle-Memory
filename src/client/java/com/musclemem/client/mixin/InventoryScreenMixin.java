package com.musclemem.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {
    private InventoryScreenMixin() {
        super(null, null, null, 0, 0);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void muscleMemory$addSkillsTab(CallbackInfo ci) {
        int buttonX = this.leftPos - 26;
        int buttonY = this.topPos + 66;

        WidgetSprites sprites = new WidgetSprites(
                Identifier.fromNamespaceAndPath("muscle-memory", "gui/skills_button"),
                Identifier.fromNamespaceAndPath("muscle-memory", "gui/skills_button")
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("M"), button -> {
                            Minecraft mc = Minecraft.getInstance();
                            if (mc.player != null && mc.getConnection() != null) {
                                mc.getConnection().sendCommand("skills-gui");
                            }
                        })
                        .tooltip(net.minecraft.client.gui.components.Tooltip.create(
                                Component.literal("Muscle Memory Skills")))
                        .bounds(this.leftPos - 26, this.topPos + 66, 20, 18)
                        .build()
        );
    }
}