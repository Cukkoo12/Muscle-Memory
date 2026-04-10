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
            }
        }
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float muscleMemory$modifyDamage(float amount, DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (source.getEntity() instanceof ServerPlayer player) {
            float modified = amount;

            // Per-mob-type kill bonus
            double mobMultiplier = SkillEvents.getMobKillDamageMultiplier(player, self);
            modified *= (float) mobMultiplier;

            // Arrow damage bonus — use DamageType tag instead of AbstractArrow instanceof
            // (class hierarchy may shift between MC versions; tag is stable)
            if (source.is(DamageTypes.ARROW)) {
                double bowMultiplier = SkillEvents.getBowDamageMultiplier(player);
                modified *= (float) bowMultiplier;
            }

            return modified;
        }
        return amount;
    }
}
