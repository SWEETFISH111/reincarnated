package com.github.sweetfish111.reincarnated.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * ユニークスキルの進化スコア・現在のスキル・進化候補を管理する。
 * NodeUnlockState/SkillAccessControlへの書き込みはオーケストレーター
 * （PlayerMagicData.checkEvolution/evolveUniqueSkillTo）が担い、
 * このクラス自身は他コンポーネントに依存しない。
 */
public class UniqueSkillProgress implements PersistentComponent {
    private String currentUniqueSkill = "greedy";
    private final Set<String> evolvableUniqueSkills = new HashSet<>();
    private final Set<String> unlockedSkills = new HashSet<>();
    private UUID uniqueSkillId = null;
    private boolean completeGreedy = false;

    private double greedyScore = 0.0;
    private double predatorScore = 0.0;
    private double scavengerScore = 0.0;
    private double hoarderScore = 0.0;
    private double usurperScore = 0.0;

    public String getCurrentUniqueSkill(){ return currentUniqueSkill; }
    public void setCurrentUniqueSkill(String skillId){ this.currentUniqueSkill = skillId; }

    public Set<String> getEvolvableUniqueSkills(){ return evolvableUniqueSkills; }
    public boolean isEvolvable(String skillId){ return evolvableUniqueSkills.contains(skillId); }
    public void addEvolvableUniqueSkill(String skillId){ evolvableUniqueSkills.add(skillId); }
    public void clearEvolvableUniqueSkills(){ evolvableUniqueSkills.clear(); }

    public UUID getUniqueSkillId(){ return uniqueSkillId; }
    public void setUniqueSkillId(UUID id){ this.uniqueSkillId = id; }

    public boolean isCompleteGreedy(){ return completeGreedy; }
    public void setCompleteGreedy(boolean value){ this.completeGreedy = value; }

    public double getGreedyScore(){ return greedyScore; }
    public void addGreedyScore(double amount){ greedyScore += amount; }
    public void setGreedyScore(double value){ greedyScore = value; }

    public double getPredatorScore(){ return predatorScore; }
    public void addPredatorScore(double amount){ predatorScore += amount; }
    public void setPredatorScore(double value){ predatorScore = value; }

    public double getScavengerScore(){ return scavengerScore; }
    public void addScavengerScore(double amount){ scavengerScore += amount; }
    public void setScavengerScore(double value){ scavengerScore = value; }

    public double getHoarderScore(){ return hoarderScore; }
    public void addHoarderScore(double amount){ hoarderScore += amount; }
    public void setHoarderScore(double value){ hoarderScore = value; }

    public double getUsurperScore(){ return usurperScore; }
    public void addUsurperScore(double amount){ usurperScore += amount; }
    public void setUsurperScore(double value){ usurperScore = value; }

    public boolean hasUnlockedUniqueSkills(String key){
        if (currentUniqueSkill.equals(key)) {
            return unlockedSkills.contains(key + "_welcom");
        }
        return false;
    }

    public void unlockUniqueSkills(String key){
        if (key.equals(currentUniqueSkill)) {
            unlockedSkills.add(key + "_welcom");
        }
    }

    @Override
    public void saveToNBT(CompoundTag rootTag) {
        rootTag.putString("currentUniqueSkill", currentUniqueSkill);
        if (uniqueSkillId != null) {
            rootTag.putString("uniqueskillId", uniqueSkillId.toString());
        }

        CompoundTag scoreTag = new CompoundTag();
        scoreTag.putDouble("greedyScore", greedyScore);
        scoreTag.putDouble("predatorScore", predatorScore);
        scoreTag.putDouble("scavengerScore", scavengerScore);
        scoreTag.putDouble("hoarderScore", hoarderScore);
        scoreTag.putDouble("usurperScore", usurperScore);
        rootTag.put("evolutionScores", scoreTag);
        rootTag.putBoolean("completeGreedy", completeGreedy);

        ListTag evolvableSkillsTag = new ListTag();
        for (String skillId : evolvableUniqueSkills) {
            evolvableSkillsTag.add(StringTag.valueOf(skillId));
        }
        rootTag.put("evolvableUniqueSkills", evolvableSkillsTag);

        // 注意：unlockedSkills(_welcomフラグ)は元実装でも保存されていなかった既存の欠落。
        // 挙動を変えないためここでも保存していない（後述）。
    }

    @Override
    public void loadFromNBT(CompoundTag rootTag) {
        if (rootTag.contains("currentUniqueSkill")) {
            currentUniqueSkill = rootTag.getStringOr("currentUniqueSkill", "greedy");
        }

        if (rootTag.contains("evolutionScores")) {
            CompoundTag scoreTag = rootTag.getCompound("evolutionScores").orElse(new CompoundTag());
            greedyScore = scoreTag.getDouble("greedyScore").orElse(0.0);
            predatorScore = scoreTag.getDouble("predatorScore").orElse(0.0);
            scavengerScore = scoreTag.getDouble("scavengerScore").orElse(0.0);
            hoarderScore = scoreTag.getDouble("hoarderScore").orElse(0.0);
            usurperScore = scoreTag.getDouble("usurperScore").orElse(0.0);
        }

        if (rootTag.contains("completeGreedy")) {
            completeGreedy = rootTag.getBooleanOr("completeGreedy", false);
        }

        if (rootTag.contains("evolvableUniqueSkills")) {
            ListTag evolvableSkillsTag = rootTag.getListOrEmpty("evolvableUniqueSkills");
            for (int i = 0; i < evolvableSkillsTag.size(); i++) {
                String skillId = evolvableSkillsTag.getStringOr(i, null);
                if (skillId != null && !skillId.isEmpty()) {
                    evolvableUniqueSkills.add(skillId);
                }
            }
        }

        if (rootTag.contains("uniqueskillId")) {
            String idStr = rootTag.getStringOr("uniqueskillId", null);
            if (idStr != null && !idStr.isEmpty()) {
                uniqueSkillId = UUID.fromString(idStr);
            }
        }
    }
}