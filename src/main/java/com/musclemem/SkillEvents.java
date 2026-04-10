package com.musclemem;

import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;

public final class SkillEvents {

    private static final Identifier MOD_REACH = Identifier.fromNamespaceAndPath("muscle-memory", "block_place_reach");
    private static final Identifier MOD_MELEE_DAMAGE = Identifier.fromNamespaceAndPath("muscle-memory", "melee_damage");
    private static final Identifier MOD_FALL_DISTANCE = Identifier.fromNamespaceAndPath("muscle-memory", "fall_distance");
    private static final Identifier MOD_SPRINT_SPEED = Identifier.fromNamespaceAndPath("muscle-memory", "sprint_speed");

    private SkillEvents() {}

    public static void register() {
        registerBlockBreak();
        registerBlockPlace();
        registerMeleeHit();
        registerMobKill();
        registerSleep();
        registerTickBonuses();
    }

    private static void registerBlockBreak() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) return;
            SkillData data = SkillPersistence.get(serverPlayer);
            if (data.increment(SkillType.BLOCK_BREAK)) {
                applyBlockBreakBonus(serverPlayer, data);
                sendLevelUpMessage(serverPlayer, SkillType.BLOCK_BREAK, data.getLevel(SkillType.BLOCK_BREAK));
            }
        });
    }

    private static void registerBlockPlace() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer))
                return InteractionResult.PASS;
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(player.getItemInHand(hand).getItem() instanceof BlockItem))
                return InteractionResult.PASS;
            SkillData data = SkillPersistence.get(serverPlayer);
            if (data.increment(SkillType.BLOCK_PLACE)) {
                applyBlockPlaceBonus(serverPlayer, data);
                sendLevelUpMessage(serverPlayer, SkillType.BLOCK_PLACE, data.getLevel(SkillType.BLOCK_PLACE));
            }
            return InteractionResult.PASS;
        });
    }

    private static void registerMeleeHit() {
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer))
                return InteractionResult.PASS;
            if (!(entity instanceof LivingEntity)) return InteractionResult.PASS;
            SkillData data = SkillPersistence.get(serverPlayer);
            if (data.increment(SkillType.MELEE_HIT)) {
                applyMeleeDamageBonus(serverPlayer, data);
                sendLevelUpMessage(serverPlayer, SkillType.MELEE_HIT, data.getLevel(SkillType.MELEE_HIT));
            }
            return InteractionResult.PASS;
        });
    }

    private static void registerMobKill() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity.level().isClientSide()) return;
            Entity attacker = damageSource.getEntity();
            if (!(attacker instanceof ServerPlayer serverPlayer)) return;
            SkillData data = SkillPersistence.get(serverPlayer);
            data.incrementMobKill(entity.getType());
            if (data.increment(SkillType.MOB_KILL)) {
                sendLevelUpMessage(serverPlayer, SkillType.MOB_KILL, data.getLevel(SkillType.MOB_KILL));
            }
        });
    }

    private static void registerSleep() {
        EntitySleepEvents.START_SLEEPING.register((entity, sleepingPos) -> {
            if (entity.level().isClientSide() || !(entity instanceof ServerPlayer serverPlayer)) return;
            SkillData data = SkillPersistence.get(serverPlayer);
            if (data.increment(SkillType.SLEEPING)) {
                sendLevelUpMessage(serverPlayer, SkillType.SLEEPING, data.getLevel(SkillType.SLEEPING));
            }
        });
    }

    private static void registerTickBonuses() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                SkillData data = SkillPersistence.get(player);

                if (player.isSwimming() && server.getTickCount() % 20 == 0) {
                    if (data.increment(SkillType.SWIMMING)) {
                        applySwimmingBonus(player, data);
                        sendLevelUpMessage(player, SkillType.SWIMMING, data.getLevel(SkillType.SWIMMING));
                    }
                }

                int swimLevel = data.getLevel(SkillType.SWIMMING);
                if (swimLevel > 0 && player.isUnderWater()) {
                    int duration = switch (swimLevel) {
                        case 1 -> 60;
                        case 2 -> 100;
                        case 3 -> 200;
                        default -> 0;
                    };
                    MobEffectInstance current = player.getEffect(MobEffects.WATER_BREATHING);
                    if (current == null || current.getDuration() < 40) {
                        player.addEffect(new MobEffectInstance(
                                MobEffects.WATER_BREATHING, duration, 0, true, false, true
                        ));
                    }
                }

                int breakLevel = data.getLevel(SkillType.BLOCK_BREAK);
                if (breakLevel > 0 && server.getTickCount() % 200 == 0) {
                    applyBlockBreakBonus(player, data);
                }

                if (player.isSprinting() && server.getTickCount() % 20 == 0) {
                    if (data.increment(SkillType.SPRINTING)) {
                        applySprintBonus(player, data);
                        sendLevelUpMessage(player, SkillType.SPRINTING, data.getLevel(SkillType.SPRINTING));
                    }
                }
            }
        });
    }

    private static void sendLevelUpMessage(ServerPlayer player, SkillType skill, int newLevel) {
        String stars = "★".repeat(newLevel) + "☆".repeat(3 - newLevel);
        player.sendSystemMessage(
                Component.literal("§6§l✦ §e" + skill.getDisplayName() + " §6" + stars + " §7Level " + newLevel + "!")
        );
        ((ServerLevel) player.level()).sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.5, 0.5, 0.5, 0.3
        );
    }

    public static void applyAllBonuses(ServerPlayer player) {
        SkillData data = SkillPersistence.get(player);
        applyBlockBreakBonus(player, data);
        applyBlockPlaceBonus(player, data);
        applyMeleeDamageBonus(player, data);
        applyFallDistanceBonus(player, data);
        applySprintBonus(player, data);
    }

    private static void applyBlockBreakBonus(ServerPlayer player, SkillData data) {
        int level = data.getLevel(SkillType.BLOCK_BREAK);
        if (level > 0) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.HASTE, 220, level - 1, true, false, true
            ));
        }
    }

    private static void applyBlockPlaceBonus(ServerPlayer player, SkillData data) {
        int level = data.getLevel(SkillType.BLOCK_PLACE);
        double reach = switch (level) {
            case 1 -> 0.5;
            case 2 -> 1.0;
            case 3 -> 1.5;
            default -> 0.0;
        };
        applyModifier(player, Attributes.BLOCK_INTERACTION_RANGE, MOD_REACH, reach,
                AttributeModifier.Operation.ADD_VALUE);
    }

    private static void applyMeleeDamageBonus(ServerPlayer player, SkillData data) {
        int level = data.getLevel(SkillType.MELEE_HIT);
        double multiplier = switch (level) {
            case 1 -> 0.05;
            case 2 -> 0.15;
            case 3 -> 0.25;
            default -> 0.0;
        };
        applyModifier(player, Attributes.ATTACK_DAMAGE, MOD_MELEE_DAMAGE, multiplier,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static void applySwimmingBonus(ServerPlayer player, SkillData data) {}

    public static void applyFallDistanceBonus(ServerPlayer player, SkillData data) {
        int level = data.getLevel(SkillType.JUMPING);
        double extra = switch (level) {
            case 1 -> 1.0;
            case 2 -> 2.0;
            case 3 -> 3.0;
            default -> 0.0;
        };
        applyModifier(player, Attributes.SAFE_FALL_DISTANCE, MOD_FALL_DISTANCE, extra,
                AttributeModifier.Operation.ADD_VALUE);
    }

    private static void applySprintBonus(ServerPlayer player, SkillData data) {
        int level = data.getLevel(SkillType.SPRINTING);
        double speedBonus = switch (level) {
            case 1 -> 0.02;
            case 2 -> 0.05;
            case 3 -> 0.10;
            default -> 0.0;
        };
        applyModifier(player, Attributes.MOVEMENT_SPEED, MOD_SPRINT_SPEED, speedBonus,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    public static double getBowDamageMultiplier(ServerPlayer player) {
        SkillData data = SkillPersistence.get(player);
        int level = data.getLevel(SkillType.BOW_SHOOT);
        return switch (level) {
            case 1 -> 1.05;
            case 2 -> 1.15;
            case 3 -> 1.25;
            default -> 1.0;
        };
    }

    public static double getMobKillDamageMultiplier(ServerPlayer player, LivingEntity target) {
        SkillData data = SkillPersistence.get(player);
        int kills = data.getMobKillCount(target.getType());
        int level = SkillType.MOB_KILL.getLevel(kills);
        return switch (level) {
            case 1 -> 1.05;
            case 2 -> 1.15;
            case 3 -> 1.20;
            default -> 1.0;
        };
    }

    public static double getFishingWaitMultiplier(ServerPlayer player) {
        SkillData data = SkillPersistence.get(player);
        int level = data.getLevel(SkillType.FISHING);
        return switch (level) {
            case 1 -> 0.90;
            case 2 -> 0.75;
            case 3 -> 0.60;
            default -> 1.0;
        };
    }

    public static double getSleepTimeMultiplier(ServerPlayer player) {
        SkillData data = SkillPersistence.get(player);
        int level = data.getLevel(SkillType.SLEEPING);
        return switch (level) {
            case 1 -> 0.80;
            case 2 -> 0.60;
            case 3 -> 0.40;
            default -> 1.0;
        };
    }

    private static void applyModifier(ServerPlayer player,
                                      Holder<Attribute> attribute,
                                      Identifier id, double value,
                                      AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (value != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(id, value, operation));
        }
    }
}