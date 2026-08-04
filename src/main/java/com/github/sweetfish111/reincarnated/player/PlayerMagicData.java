package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.*;

public class PlayerMagicData {
    private final Map<EditorTab, MagiculeCircuit> circuits = new EnumMap<>(EditorTab.class);
    public float currentMaso = 20f;

    public float totalRegeneratedMaso = 0;
    public float totalConsumedMaso = 0;

    private static final double BASE_MAX_MASO = 20f;
    private static final double BASE_REGEN_RATE = 0.1f;

    // 成長係数（チューニング用）
    private static final double MAX_SCALE_FACTOR = 5.0;
    private static final double REGEN_SCALE_FACTOR = 0.5;

    private final Set<String> unlockedConditionKeys = new HashSet<>();

    public boolean hasUnlocked(String key){
        return this.unlockedConditionKeys.contains(key);
    }

    public void unlock(String key){
        this.unlockedConditionKeys.add(key);
    }

    private String currentUniqueSkill = "greedy";

    private double predatorScore = 0.0;    // 生体キル・直接捕食の蓄積
    private double scavengerScore = 0.0;  // 落ちている死体・残滓の回収蓄積
    private double greedScore = 0.0;      // 魔素の搾取・リソース循環の蓄積
    private double usurpScore = 0.0;      // 上位者への挑戦・回路解析・トレースの蓄積

    public PlayerMagicData(){
        for(EditorTab tab : EditorTab.values()){
            circuits.put(tab, new MagiculeCircuit());
        }
    }

    public String getCurrentUniqueSkill() {
        return currentUniqueSkill;
    }
    public MagiculeCircuit getCircuit(EditorTab tab){
        return circuits.computeIfAbsent(tab, k -> new MagiculeCircuit());
    }

    public float getMaxMaso(){
        double scaledInput = this.totalConsumedMaso / 100.0;
        return (float) (BASE_MAX_MASO + MAX_SCALE_FACTOR * Math.log(1.0 + scaledInput));
    }

    public float getMasoRegenRate(){
        double scaledInput = this.totalRegeneratedMaso / 100.0;
        return (float)(BASE_REGEN_RATE + REGEN_SCALE_FACTOR * Math.log(1.0 + scaledInput));
    }

    public void setCircuits(EditorTab tab, MagiculeCircuit circuit){
        this.circuits.put(tab,circuit);
    }


    public void addPredatorScore(double amount) {
        this.predatorScore += amount;
        checkEvolution();
    }

    public void addScavengerScore(double amount) {
        this.scavengerScore += amount;
        checkEvolution();
    }

    public void addGreedScore(double amount) {
        this.greedScore += amount;
        checkEvolution();
    }

    public void addUsurpScore(double amount) {
        this.usurpScore += amount;
        checkEvolution();
    }

    public void addCurretMaso(double d){
        currentMaso += (float) d;
    }


    private void checkEvolution() {
        // すでに貪欲者から進化している場合はスキップ
        if (!currentUniqueSkill.equals("greedy")) return;

        double threshold = 100.0; // 覚醒に必要な閾値（調整可能）

        if (predatorScore >= threshold) {
            currentUniqueSkill = "predator";
            // TODO: 「捕食者」覚醒時の演出やスキルタブの特殊ノード解放処理
        } else if (scavengerScore >= threshold) {
            currentUniqueSkill = "scavenger";
            // TODO: 「飢餓者」覚醒時
        } else if (greedScore >= threshold) {
            currentUniqueSkill = "greed";
            // TODO: 「強欲者」覚醒時
        } else if (usurpScore >= threshold) {
            currentUniqueSkill = "usurper";
            // TODO: 「簒奪者」覚醒時
        }
    }


    public CompoundTag saveToNBT(){
        CompoundTag rootTag = new CompoundTag();

        for(Map.Entry<EditorTab, MagiculeCircuit> entry : this.circuits.entrySet()){
            EditorTab tab = entry.getKey();
            MagiculeCircuit circuit = entry.getValue();

            rootTag.put(tab.name(), circuit.saveToNBT());
        }
        CompoundTag masoTag = new CompoundTag();
        masoTag.putFloat("currentMaso", currentMaso);
        masoTag.putFloat("totalRegeneratedMaso", totalRegeneratedMaso);
        masoTag.putFloat("totalConsumedMaso", totalConsumedMaso);
        rootTag.put("maso", masoTag);

        ListTag unlockedKeys = new ListTag();
        for (String key : this.unlockedConditionKeys){
            unlockedKeys.add(StringTag.valueOf(key));
        }

        rootTag.put("unlockedKeys", unlockedKeys);

        return rootTag;
    }

    public void loadFromNBT(CompoundTag rootTag){
        if(rootTag == null || rootTag.isEmpty()) return;

        for(EditorTab tab : EditorTab.values()){
            if(rootTag.contains(tab.name())){
                rootTag.getCompound(tab.name()).ifPresent(tabTag ->{
                    MagiculeCircuit circuit = new MagiculeCircuit();
                    circuit.loadFromNBT(tabTag);
                    this.circuits.put(tab, circuit);
                });
            }
        }
        if(rootTag.contains("maso")){
            CompoundTag masoTag = rootTag.getCompound("maso").orElse(new CompoundTag());
            currentMaso = masoTag.getFloat("currentMaso").orElse(20f);
            totalRegeneratedMaso = masoTag.getFloat("totalRegeneratedMaso").orElse(0f);
            totalConsumedMaso = masoTag.getFloat("totalConsumedMaso").orElse(0f);
        }
        ListTag unlockedKeys = rootTag.getListOrEmpty("unlockedKeys");
        if(unlockedKeys != null){
            for (int i = 0; i < unlockedKeys.size(); i++){
                unlockedConditionKeys.add(unlockedKeys.getStringOr(i, null));
            }
        }
    }
}
