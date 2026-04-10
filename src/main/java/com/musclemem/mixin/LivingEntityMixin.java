package com.musclemem.mixin;

import com.musclemem.SkillData;
import com.musclemem.SkillEvents;
import com.musclemem.SkillPersistence;
import com.musclemem.SkillType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void muscleMemory$onJump(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            SkillData data = SkillPersistence.get(player);
            if (data.increment(SkillType.JUMPING)) {
                SkillEvents.applyFallDistanceBonus(player, data);
                // Eksik olan satır:
                SkillEvents.sendLevelUpMessage(player, SkillType.JUMPING, data.getLevel(SkillType.JUMPING));
            }
        }
    }

    // Hedef metodu 'hurtServer' olarak güncelledik.
    // Yeni sistemde bu metot fazladan bir 'ServerLevel' parametresi alır,
    // bu yüzden argsOnly = true ile gelen parametre listemize 'level' nesnesini de ekledik.
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float muscleMemory$modifyDamage(float amount, net.minecraft.server.level.ServerLevel level, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (source.getEntity() instanceof ServerPlayer player) {
            float modified = amount;

            // Per-mob-type kill bonus
            double mobMultiplier = SkillEvents.getMobKillDamageMultiplier(player, self);
            modified *= (float) mobMultiplier;

            // Arrow damage bonus
            if (source.is(DamageTypes.ARROW)) {
                double bowMultiplier = SkillEvents.getBowDamageMultiplier(player);
                modified *= (float) bowMultiplier;
            }

            return modified;
        }
        return amount;
    }
}
