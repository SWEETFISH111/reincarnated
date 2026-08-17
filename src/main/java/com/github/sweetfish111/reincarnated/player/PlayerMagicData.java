package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.magic.skill.SkillAccessLevel;
import com.github.sweetfish111.reincarnated.magic.skill.unique.Hoarder;
import com.github.sweetfish111.reincarnated.magic.skill.unique.Predator;
import com.github.sweetfish111.reincarnated.magic.skill.unique.Scavenger;
import com.github.sweetfish111.reincarnated.magic.skill.unique.Usurper;
import com.github.sweetfish111.reincarnated.system.MessageScheduler;
import com.github.sweetfish111.reincarnated.system.ReincarnatedPlaySound;
import com.github.sweetfish111.reincarnated.system.VoiceOfWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class PlayerMagicData {
    private static final int CURRENT_DATA_VERSION = 5;

    private final Map<EditorTab, MagiculeCircuit> circuits = new EnumMap<>(EditorTab.class);
    private final boolean[] magicSlotEnabled = new boolean[MAGIC_SLOT_COUNT];
    public static final int MAGIC_SLOT_COUNT = 6;
    private final MagiculeCircuit[] magicSlots = new MagiculeCircuit[MAGIC_SLOT_COUNT];
    private int activeMagicSlot = 0;

    private final MasoEconomy masoEconomy = new MasoEconomy();
    private final BarrierState barrier = new BarrierState();
    private final UniqueSkillProgress skillProgress = new UniqueSkillProgress();
    private final NodeUnlockState nodeUnlocks = new NodeUnlockState();
    private final SkillAccessControl accessControl = new SkillAccessControl();

    private final Set<String> unlockedConditionKeys = new HashSet<>();

    public boolean hasUnlocked(String key){
        return this.unlockedConditionKeys.contains(key);
    }
    public void unlock(String key){
        if (key != null && !key.isEmpty()) {
            this.unlockedConditionKeys.add(key);
        }
    }

    public PlayerMagicData(){
        for(EditorTab tab : EditorTab.values()){
            circuits.put(tab, new MagiculeCircuit());
        }
        for (int i = 0; i < MAGIC_SLOT_COUNT; i++) {
            magicSlots[i] = new MagiculeCircuit();
        }

        ensureUniqueSkillCircuit();
        nodeUnlocks.addDefaultUnlockedNodes(EditorTab.SKILL);
        nodeUnlocks.addDefaultUnlockedNodes(EditorTab.MAGIC);
        nodeUnlocks.addDefaultUnlockedNodes(EditorTab.ARTS);
    }

    public void addDefaultUnlockedNodes(EditorTab tab) {
        nodeUnlocks.addDefaultUnlockedNodes(tab);
    }

    // ===== ユニークスキル進化 =====
    public Set<String> getEvolvableUniqueSkills(){ return skillProgress.getEvolvableUniqueSkills(); }
    public String getCurrentUniqueSkill(){ return skillProgress.getCurrentUniqueSkill(); }
    public UUID getUniqueSkillId(){ return skillProgress.getUniqueSkillId(); }
    public void setUniqueSkillId(UUID uniqueSkillId){ skillProgress.setUniqueSkillId(uniqueSkillId); }

    public boolean hasUnlockedUniqueSkills(String key){ return skillProgress.hasUnlockedUniqueSkills(key); }
    public void unlockUniqueSkills(String key){ skillProgress.unlockUniqueSkills(key); }

    public boolean evolveUniqueSkillTo(String skillId) {
        if (skillId == null || !skillProgress.isEvolvable(skillId)) {
            return false;
        }
        skillProgress.setCurrentUniqueSkill(skillId);
        skillProgress.clearEvolvableUniqueSkills();
        accessControl.setSkillAccessLevel(skillId, SkillAccessLevel.DENIED);

        triggerMasoStageEvolutionAttempt();
        return true;
    }

    public void addGreedyScore(double amount, ServerPlayer player){
        skillProgress.addGreedyScore(amount);
        checkEvolution(player);
    }
    public void addPredatorScore(double amount, ServerPlayer player) {
        if(skillProgress.isCompleteGreedy()){
            skillProgress.addPredatorScore(amount);
            checkEvolution(player);
        }
    }
    public void addScavengerScore(double amount, ServerPlayer player) {
        if(skillProgress.isCompleteGreedy()){
            skillProgress.addScavengerScore(amount);
            checkEvolution(player);
        }
    }
    public void addhoarderScore(double amount, ServerPlayer player) {
        if(skillProgress.isCompleteGreedy()){
            skillProgress.addHoarderScore(amount);
            checkEvolution(player);
        }
    }
    public void addUsurperScore(double amount, ServerPlayer player){
        if(skillProgress.isCompleteGreedy()){
            skillProgress.addUsurperScore(amount);
            checkEvolution(player);
        }
    }

    private void checkEvolution(ServerPlayer player) {
        double threshold = BalanceConfig.UNIQUE_SKILL_EVOLUTION_THRESHOLD.get();
        boolean stillGreedy = skillProgress.getCurrentUniqueSkill().equals("greedy");

        if (stillGreedy && !skillProgress.isCompleteGreedy() && skillProgress.getGreedyScore() >= threshold) {
            accessControl.setSkillAccessLevel("greedy", SkillAccessLevel.READ_ONLY);

            if (masoEconomy.getMasoStage() == MasoEvolutionStage.STAGE0) {
                triggerMasoStageEvolutionAttempt();
            }

            Set<MagiculeNodeType> unlockNodeSet = new HashSet<>();
            unlockNodeSet.add(MagiculeNodeType.ADD_MASO);
            unlockNodeSet.add(MagiculeNodeType.CONBERS_XP_TO_MASO);

            List<Component> messages = List.of(
                    Component.translatable("message.reincarnated.voice_of_world.greedy_Establishment", Component.literal(player.getName().getString())),
                    VoiceOfWorld.sendEvolvedStage1(player)
            );
            MessageScheduler.scheduleMessages(player, messages, 3);

            nodeUnlocks.unlockNodeTypes(EditorTab.MAGIC, unlockNodeSet);
            nodeUnlocks.unlockNodeTypes(EditorTab.SKILL, unlockNodeSet);
            nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.ON_XP_PICKUP);
            nodeUnlocks.unlockNodeTypes(EditorTab.ARTS, unlockNodeSet);

            skillProgress.setCompleteGreedy(true);
        }
        if (!skillProgress.isCompleteGreedy()) return;

        if (skillProgress.getPredatorScore() >= threshold) {
            if (stillGreedy) unlockEvolutionCandidate(player, "predator");
            nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.ON_KILL);
            nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.COMBERS_KILL_TO_MASO);
            nodeUnlocks.unlockNodeType(EditorTab.MAGIC, MagiculeNodeType.ON_KILL);
            nodeUnlocks.unlockNodeType(EditorTab.MAGIC, MagiculeNodeType.COMBERS_KILL_TO_MASO);
            unlockBarrierNode();
        }
        if (skillProgress.getScavengerScore() >= threshold) {
            if (stillGreedy) unlockEvolutionCandidate(player, "scavenger");
            nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.ON_EAT);
            nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.COMBERS_SATIETY_TO_MASO);
            nodeUnlocks.unlockNodeType(EditorTab.MAGIC, MagiculeNodeType.ON_EAT);
            nodeUnlocks.unlockNodeType(EditorTab.MAGIC, MagiculeNodeType.COMBERS_SATIETY_TO_MASO);
            unlockBarrierNode();
        }
        if (skillProgress.getHoarderScore() >= threshold) {
            if (stillGreedy) unlockEvolutionCandidate(player, "hoarder");
            nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.ON_OVERCHARGE);
            nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.ABSORPTION);
            nodeUnlocks.unlockNodeType(EditorTab.MAGIC, MagiculeNodeType.ON_OVERCHARGE);
            nodeUnlocks.unlockNodeType(EditorTab.MAGIC, MagiculeNodeType.ABSORPTION);
            unlockBarrierNode();
        }
        if (skillProgress.getUsurperScore() >= threshold) {
            if (stillGreedy) unlockEvolutionCandidate(player, "usurper");
            nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.ON_ATTACK_STRONGER);
            nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.COMBERS_POWERGAP_TO_MASO);
            nodeUnlocks.unlockNodeType(EditorTab.MAGIC, MagiculeNodeType.ON_ATTACK_STRONGER);
            nodeUnlocks.unlockNodeType(EditorTab.MAGIC, MagiculeNodeType.COMBERS_POWERGAP_TO_MASO);
            unlockBarrierNode();
        }
    }

    private void unlockBarrierNode(){
        nodeUnlocks.unlockNodeType(EditorTab.MAGIC, MagiculeNodeType.ON_TICK);
        nodeUnlocks.unlockNodeType(EditorTab.MAGIC, MagiculeNodeType.BARRIER);
        nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.ON_TICK);
        nodeUnlocks.unlockNodeType(EditorTab.SKILL, MagiculeNodeType.BARRIER);
    }

    private void unlockEvolutionCandidate(ServerPlayer player, String skillName) {
        if(!skillProgress.isEvolvable(skillName)){
            skillProgress.addEvolvableUniqueSkill(skillName);
            setMaxBarrierPoint(60);

            player.sendSystemMessage(Component.translatable("message.reincarnated.voice_of_world.greedy_factor_analyzed", Component.literal(player.getName().getString())));

            String translatedSkillName = Component.translatable("name.reincarnated.uniqueSkill." + skillName).getString();
            player.sendSystemMessage(Component.translatable("message.reincarnated.voice_of_world.greedy_evolution_available", translatedSkillName));
        }
    }

    public MasoEvolutionStage pollStageEvolutionEvent(){ return masoEconomy.pollStageEvolutionEvent(); }

    public boolean performEvolution(String skillId, ServerPlayer player) {
        if (skillId == null || !skillProgress.isEvolvable(skillId)) {
            return false;
        }

        UUID oldGreedyId = skillProgress.getUniqueSkillId();

        // 先に状態遷移（進化資格の最終確定＋魔素進化ステージのトリガー起動）を済ませる。
        // これで万一の検証失敗時も回路を汚さずに済む。
        if (!evolveUniqueSkillTo(skillId)) {
            return false;
        }

        MagiculeCircuit circuit = getCircuit(EditorTab.SKILL);
        UUID newSkillId = switch (skillId) {
            case "predator"  -> Predator.getPredator(circuit);
            case "scavenger" -> Scavenger.getScavenger(circuit);
            case "hoarder"   -> Hoarder.getHoarder(circuit);
            case "usurper"   -> Usurper.getUsurper(circuit);
            default -> null;
        };
        if (newSkillId == null) return false; // 通常到達しない防御的ガード

        skillProgress.setUniqueSkillId(newSkillId);
        circuit.getCNode(newSkillId).setSkillId(skillId);

        accessControl.setSkillAccessLevel("greedy", SkillAccessLevel.EDITABLE);
        if (oldGreedyId != null) {
            circuit.removeNodeAndWires(oldGreedyId);
        }

        ReincarnatedPlaySound.playEvolutionSound(player);
        player.sendSystemMessage(VoiceOfWorld.sendEvolvedStage2(player));

        return true;
    }

    // ===== ノードアンロック =====
    public boolean isNodeTypeUnlocked(EditorTab tab, MagiculeNodeType nodeType) {
        return nodeUnlocks.isNodeTypeUnlocked(tab, nodeType);
    }
    public void unlockNodeType(EditorTab tab, MagiculeNodeType nodeType) {
        nodeUnlocks.unlockNodeType(tab, nodeType);
    }

    // ===== アクセス制御 =====
    public SkillAccessLevel getSkillAccessLevel(String skillId) {
        return accessControl.getSkillAccessLevel(skillId);
    }
    public void setSkillAccessLevel(String skillId, SkillAccessLevel level) {
        accessControl.setSkillAccessLevel(skillId, level);
    }
    public CompoundTag savePermissionsNBT() {
        CompoundTag tag = new CompoundTag();
        accessControl.saveToNBT(tag);
        return tag;
    }
    public void loadPermissionsNBT(CompoundTag permTag) {
        accessControl.loadFromNBT(permTag);
    }

    //===== ユニークスキルスコア =====
    public double getGreedyScore(){ return skillProgress.getGreedyScore(); }
    public double getPredatorScore(){ return skillProgress.getPredatorScore(); }
    public double getScavengerScore(){ return skillProgress.getScavengerScore(); }
    public double getHoarderScore(){ return skillProgress.getHoarderScore(); }
    public double getUsurperScore(){ return skillProgress.getUsurperScore(); }
    public boolean isCompleteGreedy(){ return skillProgress.isCompleteGreedy(); }

    // ===== 魔素経済 =====
    public MasoEvolutionStage getMasoStage(){ return masoEconomy.getMasoStage(); }
    public float getMaxMaso(){ return masoEconomy.getMaxMaso(); }
    public float getMasoRegenRate(){ return masoEconomy.getMasoRegenRate(); }
    public void triggerMasoStageEvolutionAttempt(){ masoEconomy.triggerMasoStageEvolutionAttempt(); }
    public void addCurretMaso(double d){ masoEconomy.addCurrentMaso((float) d); }
    public float getCurrentMaso(){ return masoEconomy.getCurrentMaso(); }
    public void addMasoAmount(float amount){ masoEconomy.addCurrentMaso(amount); }
    public void setCurrentMaso(float value){ masoEconomy.setCurrentMaso(value); }
    public void consumeMasoAmount(float amount, long currentTick){ masoEconomy.consumeMaso(amount, currentTick); }
    public void consumeMasoAmountPassive(float amount){ masoEconomy.consumePassive(amount); }
    public double getMasoStylePreference(){ return masoEconomy.getStylePreference(); }
    public void addTotalRegeneratedMaso(float amount){ masoEconomy.addTotalRegeneratedMaso(amount); }

    //==== 演算能力 ====
    private final ComputeCapacity computeCapacity = new ComputeCapacity();
    public void recordCastTime(double castTimeTicks){ computeCapacity.recordCastTime(castTimeTicks); }
    public double getTotalCastTimeSpent(){ return computeCapacity.getTotalCastTimeSpent(); }
    public double getMaxComputeCapacity(){ return computeCapacity.getMaxComputeCapacity(masoEconomy.getMasoStage()); }

    // ===== バリア =====
    public float getBarrierPoint(){ return barrier.getCurrentPoint(); }
    public void setBarrierPoint(float point){ barrier.setCurrentPoint(point); }
    public float getMaxBarrierPoint(){ return barrier.getMaxBarrierPoint(); }
    public void setMaxBarrierPoint(float max){ barrier.setMaxBarrierPoint(max); }
    public float getBarrierDamageReduction(){ return barrier.getBarrierDamageReduction(); }
    public void recordBarrierHit(float rawDamage, boolean barrierBroke, long currentTick){
        barrier.recordBarrierHit(rawDamage, barrierBroke, currentTick);
    }
    public double getBarrierStylePreference(){ return barrier.getAdaptR(); }

    // ===== 回路ストレージ =====
    private void ensureUniqueSkillCircuit(){
        if (!skillProgress.getCurrentUniqueSkill().equals("greedy")) return;

        MagiculeCircuit skillCircuit = circuits.get(EditorTab.SKILL);
        UUID uniqueSkillId = skillProgress.getUniqueSkillId();
        var node = (uniqueSkillId != null) ? skillCircuit.getCNode(uniqueSkillId) : null;

        if (node == null) {
            uniqueSkillId = DefaultCircuitBuilder.buildDefaultSkillCircuit(skillCircuit);
            skillProgress.setUniqueSkillId(uniqueSkillId);
            node = skillCircuit.getCNode(uniqueSkillId);
        }

        if (node.getSkillId() == null || node.getSkillId().isEmpty()) {
            node.setSkillId("greedy");
            accessControl.setSkillAccessLevel("greedy", SkillAccessLevel.DENIED);
        }
    }

    public MagiculeCircuit getMagicSlot(int index){
        if (index < 0 || index >= MAGIC_SLOT_COUNT) index = 0;
        return magicSlots[index];
    }
    public void setMagicSlot(int index, MagiculeCircuit circuit){
        if (index < 0 || index >= MAGIC_SLOT_COUNT) return;
        magicSlots[index] = circuit;
    }
    public int getActiveMagicSlot(){ return activeMagicSlot; }
    public void setActiveMagicSlot(int index){
        if (index < 0 || index >= MAGIC_SLOT_COUNT) return;
        this.activeMagicSlot = index;
    }

    public MagiculeCircuit getCircuit(EditorTab tab){
        if (tab == EditorTab.MAGIC) {
            return getMagicSlot(activeMagicSlot);
        }
        return circuits.get(tab);
    }

    public boolean isMagicSlotEnabled(int index){
        if (index < 0 || index >= MAGIC_SLOT_COUNT) return false;
        return magicSlotEnabled[index];
    }
    public void setMagicSlotEnabled(int index, boolean enabled){
        if (index < 0 || index >= MAGIC_SLOT_COUNT) return;
        magicSlotEnabled[index] = enabled;
    }

    public void setCircuits(EditorTab tab, MagiculeCircuit circuit){
        if (tab == EditorTab.MAGIC) {
            setMagicSlot(activeMagicSlot, circuit);
            return;
        }
        this.circuits.put(tab, circuit);
    }

    // ===== NBT =====
    public CompoundTag saveToNBT(){
        CompoundTag rootTag = new CompoundTag();
        rootTag.putInt("data_version", CURRENT_DATA_VERSION);

        for(Map.Entry<EditorTab, MagiculeCircuit> entry : this.circuits.entrySet()){
            if(entry.getKey() == EditorTab.MAGIC) continue;
            rootTag.put(entry.getKey().name(), entry.getValue().saveToNBT());
        }

        CompoundTag enabledSlotsTag = new CompoundTag();
        for (int i = 0; i < MAGIC_SLOT_COUNT; i++) {
            enabledSlotsTag.putBoolean("slot" + i, magicSlotEnabled[i]);
        }
        rootTag.put("MagicSlotEnabled", enabledSlotsTag);

        ListTag magicSlotsTag = new ListTag();
        for (int i = 0; i < MAGIC_SLOT_COUNT; i++) {
            magicSlotsTag.add(magicSlots[i].saveToNBT());
        }
        rootTag.put("MagicSlots", magicSlotsTag);
        rootTag.putInt("ActiveMagicSlot", activeMagicSlot);

        CompoundTag masoTag = new CompoundTag();
        masoEconomy.saveToNBT(masoTag);
        rootTag.put("maso", masoTag);

        CompoundTag computeTag = new CompoundTag();
        computeCapacity.saveToNBT(computeTag);
        rootTag.put("computeCapacity", computeTag);

        CompoundTag barrierTag = new CompoundTag();
        barrier.saveToNBT(barrierTag);
        rootTag.put("barrier", barrierTag);

        CompoundTag permTag = new CompoundTag();
        accessControl.saveToNBT(permTag);
        rootTag.put("permission", permTag);

        skillProgress.saveToNBT(rootTag); // フラット構造のためrootTagに直接書く

        ListTag unlockedKeys = new ListTag();
        for (String key : this.unlockedConditionKeys){
            unlockedKeys.add(StringTag.valueOf(key));
        }
        rootTag.put("unlockedKeys", unlockedKeys);

        CompoundTag unlockedNodesTag = new CompoundTag();
        nodeUnlocks.saveToNBT(unlockedNodesTag);
        rootTag.put("unlockedNodeTypes", unlockedNodesTag);

        return rootTag;
    }

    public void loadFromNBT(CompoundTag rootTag){
        if(rootTag == null || rootTag.isEmpty()) return;

        int version = rootTag.getInt("data_version").orElse(0);

        for(EditorTab tab : EditorTab.values()){
            if(tab == EditorTab.MAGIC) continue;
            if(rootTag.contains(tab.name())){
                rootTag.getCompound(tab.name()).ifPresent(tabTag -> {
                    MagiculeCircuit circuit = new MagiculeCircuit();
                    circuit.loadFromNBT(tabTag);
                    this.circuits.put(tab, circuit);
                });
            }
        }

        if (rootTag.contains("MagicSlots")) {
            ListTag magicSlotsTag = rootTag.getListOrEmpty("MagicSlots");
            for (int i = 0; i < MAGIC_SLOT_COUNT; i++) {
                MagiculeCircuit circuit = new MagiculeCircuit();
                if (i < magicSlotsTag.size()) {
                    magicSlotsTag.getCompound(i).ifPresent(circuit::loadFromNBT);
                }
                magicSlots[i] = circuit;
            }
        }
        activeMagicSlot = rootTag.getInt("ActiveMagicSlot").orElse(0);

        if (rootTag.contains("MagicSlotEnabled")) {
            CompoundTag enabledSlotsTag = rootTag.getCompoundOrEmpty("MagicSlotEnabled");
            for (int i = 0; i < MAGIC_SLOT_COUNT; i++) {
                magicSlotEnabled[i] = enabledSlotsTag.getBooleanOr("slot" + i, false);
            }
        }

        if(rootTag.contains("maso")){
            masoEconomy.loadFromNBT(rootTag.getCompoundOrEmpty("maso"));
        }

        if (rootTag.contains("computeCapacity")) {
            computeCapacity.loadFromNBT(rootTag.getCompoundOrEmpty("computeCapacity"));
        }

        if(rootTag.contains("barrier")){
            barrier.loadFromNBT(rootTag.getCompoundOrEmpty("barrier"));
        }

        if(rootTag.contains("permission")){
            accessControl.loadFromNBT(rootTag.getCompoundOrEmpty("permission"));
        }

        skillProgress.loadFromNBT(rootTag); // フラット構造のためrootTagから直接読む

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
            nodeUnlocks.loadFromNBT(rootTag.getCompoundOrEmpty("unlockedNodeTypes"));
        }

        ensureUniqueSkillCircuit();
        if(version < 1){ migrateV0toV1(); }
        if(version < 2){ migrateV1toV2(); }
        if(version < 3){ migrateV2toV3(); }
        if(version < 4){ migratev3tov4(); }
        if(version < 5){ migrateV4toV5(rootTag); }
    }

    private void migrateV0toV1(){
        //tokuninasi
    }
    private void migrateV1toV2(){
        skillProgress.setCompleteGreedy(false);
        skillProgress.setGreedyScore(50);
        skillProgress.setPredatorScore(0);
        skillProgress.setScavengerScore(0);
        skillProgress.setHoarderScore(0);
        skillProgress.setUsurperScore(0);
        skillProgress.clearEvolvableUniqueSkills();
    }
    private void migrateV2toV3(){
        nodeUnlocks.addDefaultUnlockedNodes(EditorTab.MAGIC);
    }
    private void migratev3tov4(){
        nodeUnlocks.addDefaultUnlockedNodes(EditorTab.MAGIC);
    }
    private void migrateV4toV5(CompoundTag rootTag){
        if (rootTag.contains("MAGIC")) {
            rootTag.getCompound("MAGIC").ifPresent(oldMagicTag -> {
                MagiculeCircuit migrated = new MagiculeCircuit();
                migrated.loadFromNBT(oldMagicTag);
                magicSlots[0] = migrated;
            });
        }
    }
}