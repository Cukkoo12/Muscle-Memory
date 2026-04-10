package com.musclemem.gui;

import com.musclemem.SkillData;
import com.musclemem.SkillPersistence;
import com.musclemem.SkillType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumMap;
import java.util.Map;

/**
 * Data payload sent from server to client when opening the skill screen.
 */
public record SkillScreenData(Map<SkillType, int[]> skills) {

    // Artık parametre tiplerini açıkça belirttik ve metodları statik yaptık
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillScreenData> PACKET_CODEC = StreamCodec.of(
            SkillScreenData::write,
            SkillScreenData::read
    );

    public static SkillScreenData fromPlayer(ServerPlayer player) {
        SkillData data = SkillPersistence.get(player);
        Map<SkillType, int[]> skills = new EnumMap<>(SkillType.class);
        for (SkillType type : SkillType.values()) {
            skills.put(type, new int[]{data.getCount(type), data.getLevel(type)});
        }
        return new SkillScreenData(skills);
    }

    // 1. DÜZELTME: Metodu static yaptık ve parametre sırasını (Buffer, Data) olarak ayarladık
    private static void write(RegistryFriendlyByteBuf buf, SkillScreenData data) {
        for (SkillType type : SkillType.values()) {
            int[] values = data.skills().getOrDefault(type, new int[]{0, 0});
            buf.writeInt(values[0]); // count
            buf.writeInt(values[1]); // level
        }
    }

    // 2. DÜZELTME: Bu zaten statikti ama tip uyumu için kontrol edildi
    private static SkillScreenData read(RegistryFriendlyByteBuf buf) {
        Map<SkillType, int[]> skills = new EnumMap<>(SkillType.class);
        for (SkillType type : SkillType.values()) {
            int count = buf.readInt();
            int level = buf.readInt();
            skills.put(type, new int[]{count, level});
        }
        return new SkillScreenData(skills);
    }

    public int getCount(SkillType type) {
        int[] values = skills.getOrDefault(type, new int[]{0, 0});
        return values[0];
    }

    public int getLevel(SkillType type) {
        int[] values = skills.getOrDefault(type, new int[]{0, 0});
        return values[1];
    }
}