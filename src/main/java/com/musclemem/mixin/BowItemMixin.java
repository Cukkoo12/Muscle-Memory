package com.musclemem.mixin;

import com.musclemem.SkillData;
import com.musclemem.SkillEvents;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable; // Değişen İçe Aktarma

@Mixin(BowItem.class)
public abstract class BowItemMixin {

    // CallbackInfo yerine CallbackInfoReturnable<Boolean> kullanıyoruz
    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void muscleMemory$onBowShot(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks, CallbackInfoReturnable<Boolean> cir) {
        if (level.isClientSide()) return;
        if (!(user instanceof ServerPlayer player)) return;

        int useTime = stack.getUseDuration(user) - remainingUseTicks;
        if (useTime < 3) return; // Ok çok kısa sürede bırakıldıysa sayma

        SkillData data = SkillPersistence.get(player);
        if (data.increment(SkillType.BOW_SHOOT)) {
            SkillEvents.sendLevelUpMessage(player, SkillType.BOW_SHOOT, data.getLevel(SkillType.BOW_SHOOT));
        }
    }
}