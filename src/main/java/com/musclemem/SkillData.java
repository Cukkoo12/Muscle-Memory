package com.musclemem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class SkillData {

    public static final Codec<SkillData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT)
                    .fieldOf("skills")
                    .forGetter(SkillData::skillsToMap),
            Codec.unboundedMap(Codec.STRING, Codec.INT)
                    .fieldOf("mob_kills")
                    .forGetter(SkillData::mobKillsToMap)
    ).apply(instance, SkillData::fromMaps));

    private final EnumMap<SkillType, Integer> counts = new EnumMap<>(SkillType.class);
    private final Map<String, Integer> mobKillCounts = new HashMap<>();
    private final EnumMap<SkillType, Integer> previousLevels = new EnumMap<>(SkillType.class);

    public SkillData() {
        for (SkillType type : SkillType.values()) {
            counts.put(type, 0);
            previousLevels.put(type, 0);
        }
    }

    private Map<String, Integer> skillsToMap() {
        Map<String, Integer> map = new HashMap<>();
        for (var entry : counts.entrySet()) {
            map.put(entry.getKey().name(), entry.getValue());
        }
        return map;
    }

    private Map<String, Integer> mobKillsToMap() {
        return new HashMap<>(mobKillCounts);
    }

    private static SkillData fromMaps(Map<String, Integer> skills, Map<String, Integer> mobKills) {
        SkillData data = new SkillData();
        for (var entry : skills.entrySet()) {
            SkillType type = SkillType.fromString(entry.getKey());
            if (type != null) {
                data.counts.put(type, entry.getValue());
                data.previousLevels.put(type, type.getLevel(entry.getValue()));
            }
        }
        data.mobKillCounts.putAll(mobKills);
        return data;
    }

    public int getCount(SkillType type) {
        return counts.getOrDefault(type, 0);
    }

    public int getLevel(SkillType type) {
        return type.getLevel(getCount(type));
    }

    public int getPreviousLevel(SkillType type) {
        return previousLevels.getOrDefault(type, 0);
    }

    public boolean increment(SkillType type) {
        int oldCount = getCount(type);
        int oldLevel = type.getLevel(oldCount);
        counts.put(type, oldCount + 1);
        int newLevel = type.getLevel(oldCount + 1);
        if (newLevel > oldLevel) {
            previousLevels.put(type, newLevel);
            return true;
        }
        return false;
    }

    public void incrementMobKill(EntityType<?> entityType) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        String key = id.toString();
        mobKillCounts.merge(key, 1, Integer::sum);
        increment(SkillType.MOB_KILL);
    }

    public int getMobKillCount(EntityType<?> entityType) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return mobKillCounts.getOrDefault(id.toString(), 0);
    }

    public int getTotalMobKills() {
        return getCount(SkillType.MOB_KILL);
    }

    public Map<String, Integer> getMobKillCounts() {
        return mobKillCounts;
    }

    public String formatForChat() {
        StringBuilder sb = new StringBuilder();
        sb.append("§6§l--- Muscle Memory ---§r\n");
        for (SkillType type : SkillType.values()) {
            int count = getCount(type);
            int level = getLevel(type);
            String stars = "★".repeat(level) + "☆".repeat(3 - level);
            int next = type.getNextThreshold(count);
            String progress = level >= 3 ? "§aMAX" : "§7" + count + " / " + next;
            sb.append("§e").append(type.getDisplayName())
                    .append(" §f").append(stars)
                    .append(" ").append(progress)
                    .append("§r\n");
        }
        return sb.toString();
    }
}