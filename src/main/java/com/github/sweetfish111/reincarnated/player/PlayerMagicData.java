package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;

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
    private Set<String> unlockedSkills = new HashSet<>();

    private UUID uniqueSkillId = null;

    private double predatorScore = 0.0;    // 生体キル 捕食者蓄積
    private double scavengerScore = 0.0;  // 食事（とくに生肉）　飢餓者蓄積
    private double greedScore = 0.0;      // 経験値取得量　強欲者蓄積
    private double usurpScore = 0.0;      // オーバーチャージ（媽祖過剰状態）時間　簒奪者蓄積

    private final Map<EditorTab, Set<MagiculeNodeType>> unlockedNodeTypes = new EnumMap<>(EditorTab.class);

    public PlayerMagicData(){
        for(EditorTab tab : EditorTab.values()){
            circuits.put(tab, new MagiculeCircuit());
            unlockedNodeTypes.put(tab, new HashSet<>());
        }
        uniqueSkillId = DefaultCircuitBuilder.buildDefaultSkillCircuit(circuits.get(EditorTab.SKILL));

        // 👑 初期状態のデフォルトノードを付与
        addDefaultUnlockedNodes(EditorTab.SKILL);
    }

    /**
     * デフォルトで解放しておく計算ノード群を登録する共通メソッド
     */
    private void addDefaultUnlockedNodes(EditorTab tab) {
        Set<MagiculeNodeType> types = unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>());
        types.add(MagiculeNodeType.ADD);
        types.add(MagiculeNodeType.AND);
        types.add(MagiculeNodeType.DIVIDE);
        types.add(MagiculeNodeType.EQUAL);
        types.add(MagiculeNodeType.GREATER_THAN);
        types.add(MagiculeNodeType.GRATER_OR_EQUAL);
        types.add(MagiculeNodeType.LESS_THAN);
        types.add(MagiculeNodeType.LESS_OR_EQUAL);
        types.add(MagiculeNodeType.MODULO);
        types.add(MagiculeNodeType.MULTIPLY);
        types.add(MagiculeNodeType.NOT);
        types.add(MagiculeNodeType.OR);
        types.add(MagiculeNodeType.SUBTACT);
    }

    public String getCurrentUniqueSkill() {
        return currentUniqueSkill;
    }
    public MagiculeCircuit getCircuit(EditorTab tab){
        MagiculeCircuit circuit = circuits.get(tab);

        if(this.currentUniqueSkill.equals("greedy") && tab == EditorTab.SKILL && uniqueSkillId == null){
            uniqueSkillId = DefaultCircuitBuilder.buildDefaultSkillCircuit(circuit);
        }

        return circuit;
    }

    public float getMaxMaso(){
        double scaledInput = this.totalConsumedMaso / 100.0;
        return (float) (BASE_MAX_MASO + MAX_SCALE_FACTOR * Math.log(1.0 + scaledInput));
    }

    public float getMasoRegenRate(){
        double scaledInput = this.totalRegeneratedMaso / 100.0;
        return (float)(BASE_REGEN_RATE + REGEN_SCALE_FACTOR * Math.log(1.0 + scaledInput));
    }

    public boolean hasUnlockedUniqueSkills(String key){
        if(currentUniqueSkill.equals(key)){
            return unlockedSkills.contains(key + "_welcom");
        }
        return false;
    }

    public void setCircuits(EditorTab tab, MagiculeCircuit circuit){
        this.circuits.put(tab,circuit);
    }

    public void unlockUniqueSkills(String key){
        if(key.equals(currentUniqueSkill)){
            unlockedSkills.add(key + "_welcom");
        }
    }

    public boolean isNodeTypeUnlocked(EditorTab tab, MagiculeNodeType nodeType) {
        Set<MagiculeNodeType> types = unlockedNodeTypes.get(tab);
        return types != null && types.contains(nodeType);
    }

    public void unlockNodeType(EditorTab tab, MagiculeNodeType nodeType) {
        unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>()).add(nodeType);
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
        if (!currentUniqueSkill.equals("greedy")) return;

        double threshold = 100.0;

        if (predatorScore >= threshold) {
            currentUniqueSkill = "predator";
        } else if (scavengerScore >= threshold) {
            currentUniqueSkill = "scavenger";
        } else if (greedScore >= threshold) {
            currentUniqueSkill = "greed";
        } else if (usurpScore >= threshold) {
            currentUniqueSkill = "usurper";
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

        rootTag.putString("currentUniqueSkill", currentUniqueSkill);
        if (uniqueSkillId != null) {
            rootTag.putString("uniqueskillId", uniqueSkillId.toString());
        }

        ListTag unlockedKeys = new ListTag();
        for (String key : this.unlockedConditionKeys){
            unlockedKeys.add(StringTag.valueOf(key));
        }

        rootTag.put("unlockedKeys", unlockedKeys);

        CompoundTag unlockedNodesTag = new CompoundTag();
        for (Map.Entry<EditorTab, Set<MagiculeNodeType>> entry : unlockedNodeTypes.entrySet()) {
            ListTag listTag = new ListTag();
            for (MagiculeNodeType nodeType : entry.getValue()) {
                listTag.add(StringTag.valueOf(nodeType.name()));
            }
            unlockedNodesTag.put(entry.getKey().name(), listTag);
        }
        rootTag.put("unlockedNodeTypes", unlockedNodesTag);

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

        if (rootTag.contains("currentUniqueSkill")) {
            currentUniqueSkill = rootTag.getStringOr("currentUniqueSkill", "greedy");
        }

        if (rootTag.contains("uniqueskillId")) {
            String idStr = rootTag.getStringOr("uniqueskillId", null);
            if (idStr != null && !idStr.isEmpty()) {
                uniqueSkillId = UUID.fromString(idStr);
            }
        }

        ListTag unlockedKeys = rootTag.getListOrEmpty("unlockedKeys");
        if(unlockedKeys != null){
            for (int i = 0; i < unlockedKeys.size(); i++){
                unlockedConditionKeys.add(unlockedKeys.getStringOr(i, null));
            }
        }

        boolean hasLoadedUnlockedNodes = false;
        if (rootTag.contains("unlockedNodeTypes")) {
            CompoundTag unlockedNodesTag = rootTag.getCompound("unlockedNodeTypes").orElse(new CompoundTag());
            for (EditorTab tab : EditorTab.values()) {
                if (unlockedNodesTag.contains(tab.name())) {
                    hasLoadedUnlockedNodes = true;
                    ListTag listTag = unlockedNodesTag.getListOrEmpty(tab.name());
                    Set<MagiculeNodeType> set = unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>());
                    for (int i = 0; i < listTag.size(); i++) {
                        try {
                            String typeName = listTag.getStringOr(i, null);
                            if (typeName != null) {
                                set.add(MagiculeNodeType.valueOf(typeName));
                            }
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }
        }

        // 👑 【マイグレーション】もしアプデ前のデータで未解放ノードのタグが存在しない場合は、デフォルトを自動付与
        if (!hasLoadedUnlockedNodes) {

            addDefaultUnlockedNodes(EditorTab.SKILL);
        }

        MagiculeCircuit skillCircuit = this.circuits.get(EditorTab.SKILL);
        if (this.currentUniqueSkill.equals("greedy") && uniqueSkillId == null) {
            this.uniqueSkillId = DefaultCircuitBuilder.buildDefaultSkillCircuit(skillCircuit);
        }
    }
}