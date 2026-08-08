package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import com.github.sweetfish111.reincarnated.magic.slill.SkillAccessLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.*;

public class PlayerMagicData {
    private static final int CURRENT_DATA_VERSION = 1;
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
        if (key != null && !key.isEmpty()) {
            this.unlockedConditionKeys.add(key);
        }
    }

    private String currentUniqueSkill = "greedy";
    private Set<String> evolvableUniqueSkills = new HashSet<>();
    private final Map<String, SkillAccessLevel> skillPermissions = new HashMap<>();

    private Set<String> unlockedSkills = new HashSet<>();

    private UUID uniqueSkillId = null;

    private double greedyScore = 0.0;
    private double predatorScore = 0.0;    // 生体キル 捕食者蓄積
    private double scavengerScore = 0.0;  // 食事（とくに生肉）　飢餓者蓄積
    private double greedScore = 0.0;// オーバーチャージ（魔素過剰状態）時間　強欲者蓄積
    private double usurperScore = 0.0; //格上（より攻撃力の高い相手）への攻撃回数　簒奪者蓄積

    private final Map<EditorTab, Set<MagiculeNodeType>> unlockedNodeTypes = new EnumMap<>(EditorTab.class);

    public PlayerMagicData(){
        for(EditorTab tab : EditorTab.values()){
            circuits.put(tab, new MagiculeCircuit());
            unlockedNodeTypes.put(tab, new HashSet<>());
        }

        ensureUniqueSkillCircuit();
        // 👑 初期状態のデフォルトノードを付与
        addDefaultUnlockedNodes(EditorTab.SKILL);
        addDefaultUnlockedNodes(EditorTab.MAGIC);
        addDefaultUnlockedNodes(EditorTab.ARTS);
    }

    /**
     * デフォルトで解放しておくノード群を登録するメソッド
     */
    public void addDefaultUnlockedNodes(EditorTab tab) {
        if(tab == EditorTab.SKILL) {
            Set<MagiculeNodeType> types = unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>());
            //trigger

            //Action

            //math
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
            //生成物

            //proxy
        } else if (tab == EditorTab.MAGIC) {
            Set<MagiculeNodeType> types = unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>());
            //action
            types.add(MagiculeNodeType.DAMAGE);
            types.add(MagiculeNodeType.EXPLOSION);
            types.add(MagiculeNodeType.HEALING);
            types.add(MagiculeNodeType.LIGHTNING);
            types.add(MagiculeNodeType.DIG);
            //control
            types.add(MagiculeNodeType.DELAY);
            types.add(MagiculeNodeType.IF);
            types.add(MagiculeNodeType.REPEAT);
            types.add(MagiculeNodeType.TOGGLE);
            types.add(MagiculeNodeType.WHILE);
            //conversion
            types.add(MagiculeNodeType.COMBERS_LOOK_DIRECTION);
            types.add(MagiculeNodeType.COMBERS_TARGET_POS);
            types.add(MagiculeNodeType.OFFSET);
            //sensor
            types.add(MagiculeNodeType.GET_LOOK_FORWARD);
            types.add(MagiculeNodeType.GET_LOOK_TARGET);
            types.add(MagiculeNodeType.RETURN_CASTER);
            //trigger
            types.add(MagiculeNodeType.EVENT_KEY_ONE);
            types.add(MagiculeNodeType.NUMBER);
            types.add(MagiculeNodeType.BOOLEAN);
            //math
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
            //生成物
            types.add(MagiculeNodeType.SHOOT_PROJECTILE);
            //proxy
            types.add(MagiculeNodeType.INPUT_PROXY);
            types.add(MagiculeNodeType.OUTPUT_PROXY);
        } else if (tab == EditorTab.ARTS) {
            Set<MagiculeNodeType> types = unlockedNodeTypes.computeIfAbsent(tab, k -> new HashSet<>());
            //trigger

            //actioon

            //math
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
            //生成物

            //proxy
        }
    }

    public String getCurrentUniqueSkill() {
        return currentUniqueSkill;
    }
    public MagiculeCircuit getCircuit(EditorTab tab){
        MagiculeCircuit circuit = circuits.get(tab);
        return circuit;
    }

    private void ensureUniqueSkillCircuit(){
        if (!this.currentUniqueSkill.equals("greedy")) return;

        MagiculeCircuit skillCircuit = circuits.get(EditorTab.SKILL);
        var node = (uniqueSkillId != null) ? skillCircuit.getCNode(uniqueSkillId) : null;

        if (node == null) {
            // 未構築、またはID残存だがノード実体が消えた壊れたデータ → 再構築
            uniqueSkillId = DefaultCircuitBuilder.buildDefaultSkillCircuit(skillCircuit);
            node = skillCircuit.getCNode(uniqueSkillId);
        }

        if (node.getSkillId() == null) {
            node.setSkillId("greedy");
            setSkillAccessLevel("greedy", SkillAccessLevel.DENIED);
        }
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

    public void addGreedyScore(double amount){
        this.greedyScore += amount;
        checkEvolution();
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

    public void addUsurperScore(double amount){
        this.usurperScore += amount;
        checkEvolution();
    }

    public void addCurretMaso(double d){
        currentMaso += (float) d;
    }


    private void checkEvolution() {
        if (!currentUniqueSkill.equals("greedy")) return;

        double threshold = 100;

        if(greedyScore >= threshold){
            MagiculeCircuit skillCircuit = this.getCircuit(EditorTab.SKILL);
            setSkillAccessLevel("greedy", SkillAccessLevel.READ_ONLY);

            Set<MagiculeNodeType> unlockNodeSet = new HashSet<>();
            unlockNodeSet.add(MagiculeNodeType.ADD_MASO);
            unlockNodeSet.add(MagiculeNodeType.CONBERS_XP_TO_MASO);

            unlockedNodeTypes.get(EditorTab.MAGIC).addAll(unlockNodeSet);
            unlockedNodeTypes.get(EditorTab.SKILL).addAll(unlockNodeSet);
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.ON_XP_PICKUP);
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.HEALING);
            unlockedNodeTypes.get(EditorTab.ARTS).addAll(unlockNodeSet);

            if (predatorScore >= threshold) {
                evolvableUniqueSkills.add("predator");
            } else if (scavengerScore >= threshold) {
                evolvableUniqueSkills.add("scavenger");
            } else if (greedScore >= threshold) {
                evolvableUniqueSkills.add("greed");
            } else if (usurperScore >= threshold){
                evolvableUniqueSkills.add("usurper");
            }
        }
    }

    public SkillAccessLevel getSkillAccessLevel(String skillId) {
        return skillPermissions.getOrDefault(skillId, SkillAccessLevel.DENIED);
    }

    /** スキルのアクセスレベルを昇格/降格（例: 解析完了で READ_ONLY -> EDITABLE へ） */
    public void setSkillAccessLevel(String skillId, SkillAccessLevel level) {
        skillPermissions.put(skillId, level);
    }

    // NBTセーブ処理
    public CompoundTag savePermissionsNBT() {
        CompoundTag permTag = new CompoundTag();
        skillPermissions.forEach((skillId, level) -> {
            permTag.putInt(skillId, level.getLevel());
        });
        return permTag;
    }

    // NBTロード処理
    public void loadPermissionsNBT(CompoundTag permTag) {
        skillPermissions.clear();
        for (String skillId : permTag.keySet()) {
            skillPermissions.put(skillId, SkillAccessLevel.fromIndex(permTag.getInt(skillId).orElse(0)));
        }
    }


    public CompoundTag saveToNBT(){
        CompoundTag rootTag = new CompoundTag();

        rootTag.putInt("data_version", CURRENT_DATA_VERSION);

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
        rootTag.put("permission", savePermissionsNBT());

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

        int version = rootTag.getInt("data_version").orElse(0);

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

        if(rootTag.contains("permission")){
            loadPermissionsNBT(rootTag.getCompoundOrEmpty("permission"));
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
                String key = unlockedKeys.getStringOr(i, null);
                if (key != null && !key.isEmpty()) {
                    unlockedConditionKeys.add(key);
                }
            }
        }

        if (rootTag.contains("unlockedNodeTypes")) {
            CompoundTag unlockedNodesTag = rootTag.getCompound("unlockedNodeTypes").orElse(new CompoundTag());
            for (EditorTab tab : EditorTab.values()) {
                if (unlockedNodesTag.contains(tab.name())) {
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


        ensureUniqueSkillCircuit();
        if(version < 1){
            migrateV0toV1();
        }
    }
    private void migrateV0toV1(){
        //tokuninasi
    }
}