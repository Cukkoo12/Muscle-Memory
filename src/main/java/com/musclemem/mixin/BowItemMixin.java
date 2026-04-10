package com.musclemem.mixin;

import com.musclemem.SkillPersistence;
import com.musclemem.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void muscleMemory$onBowShot(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if (level.isClientSide()) return;
        if (!(user instanceof ServerPlayer player)) return;
        int useTime = stack.getUseDuration(user) - remainingUseTicks;
        if (useTime < 3) return;
        SkillPersistence.get(player).increment(SkillType.BOW_SHOOT);
    }
}