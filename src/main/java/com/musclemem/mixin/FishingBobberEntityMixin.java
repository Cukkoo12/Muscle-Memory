package com.musclemem.mixin;

import com.musclemem.SkillEvents;
import com.musclemem.SkillPersistence;
import com.musclemem.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class FishingBobberEntityMixin {

    @Shadow
    public abstract Player getPlayerOwner();

    @Inject(method = "retrieve", at = @At("RETURN"))
    private void muscleMemory$onFishCaught(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() <= 0) return;
        Player owner = getPlayerOwner();
        if (owner instanceof ServerPlayer player) {
            SkillPersistence.get(player).increment(SkillType.FISHING);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void muscleMemory$accelerateFishing(CallbackInfo ci) {
        Player owner = getPlayerOwner();
        if (!(owner instanceof ServerPlayer player)) return;
        double multiplier = SkillEvents.getFishingWaitMultiplier(player);
        if (multiplier >= 1.0) return;
        FishingBobberEntityAccessor accessor = (FishingBobberEntityAccessor) this;
        int waitCountdown = accessor.getWaitCountdown();
        if (waitCountdown > 0) {
            double extraChance = (1.0 / multiplier) - 1.0;
            if (owner.level().getRandom().nextFloat() < extraChance) {
                accessor.setWaitCountdown(waitCountdown - 1);
            }
        }
        int hookCountdown = accessor.getHookCountdown();
        if (hookCountdown > 0) {
            double extraChance = (1.0 / multiplier) - 1.0;
            if (owner.level().getRandom().nextFloat() < extraChance) {
                accessor.setHookCountdown(hookCountdown - 1);
            }
        }
    }
}