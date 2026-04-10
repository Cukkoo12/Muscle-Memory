package com.musclemem.gui;

import com.musclemem.SkillType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class SkillScreenHandler extends AbstractContainerMenu {

    public static MenuType<SkillScreenHandler> TYPE;
    private final SkillScreenData data;

    // Yalnızca ağ paketinden (packet) gelen datayı kabul eden yapıcı metot
    public SkillScreenHandler(int syncId, Inventory playerInventory, SkillScreenData data) {
        super(TYPE, syncId);
        this.data = data;
    }

    public int getSkillCount(SkillType type) {
        return data.getCount(type);
    }

    public int getSkillLevel(SkillType type) {
        return data.getLevel(type);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}