package com.musclemem;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class SkillPersistence {

    public static final AttachmentType<SkillData> SKILL_DATA = AttachmentRegistry.<SkillData>builder()
            .persistent(SkillData.CODEC)
            .initializer(SkillData::new)
            .copyOnDeath()
            .buildAndRegister(Identifier.fromNamespaceAndPath("muscle-memory", "skill_data"));

    private SkillPersistence() {}

    public static void init() {}

    public static SkillData get(ServerPlayer player) {
        return player.getAttachedOrCreate(SKILL_DATA);
    }
}