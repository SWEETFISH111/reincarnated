package com.github.sweetfish111.reincarnated.player;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.EditorTab;
import com.github.sweetfish111.reincarnated.circuit.MagiculeNodeType;
import com.github.sweetfish111.reincarnated.magic.slill.SkillAccessLevel;
import com.github.sweetfish111.reincarnated.reincarnated;
import com.github.sweetfish111.reincarnated.system.MessageScheduler;
import com.github.sweetfish111.reincarnated.system.VoiceOfWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;

import java.util.*;

public class PlayerMagicData {
    private static final int CURRENT_DATA_VERSION = 4;
    private final Map<EditorTab, MagiculeCircuit> circuits = new EnumMap<>(EditorTab.class);

    private final boolean[] magicSlotEnabled = new boolean[MAGIC_SLOT_COUNT];

    public static final int MAGIC_SLOT_COUNT = 6;
    private final MagiculeCircuit[] magicSlots = new MagiculeCircuit[MAGIC_SLOT_COUNT];
    private int activeMagicSlot = 0;

    private final MasoEconomy masoEconomy = new MasoEconomy();
    private final BarrierState barrier = new BarrierState();




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

    private boolean completeGreedy = false;

    private double greedyScore = 0.0;
    private double predatorScore = 0.0;    // 生体キル 捕食者蓄積
    private double scavengerScore = 0.0;  // 食事（とくに生肉）　飢餓者蓄積
    private double hoarderScore = 0.0;// オーバーチャージ（魔素過剰状態）時間　強欲者蓄積
    private double usurperScore = 0.0; //格上（より攻撃力の高い相手）への攻撃回数　簒奪者蓄積

    private final Map<EditorTab, Set<MagiculeNodeType>> unlockedNodeTypes = new EnumMap<>(EditorTab.class);

    public PlayerMagicData(){
        for(EditorTab tab : EditorTab.values()){
            circuits.put(tab, new MagiculeCircuit());
            unlockedNodeTypes.put(tab, new HashSet<>());
        }
        for (int i = 0; i < MAGIC_SLOT_COUNT; i++) {
            magicSlots[i] = new MagiculeCircuit();
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
            types.add(MagiculeNodeType.DIG_ALl);
            types.add(MagiculeNodeType.COLLECT_ITEMS);
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
            types.add(MagiculeNodeType.TO_BLOCK_POS);
            //sensor
            types.add(MagiculeNodeType.GET_LOOK_FORWARD);
            types.add(MagiculeNodeType.GET_LOOK_TARGET);
            types.add(MagiculeNodeType.RETURN_CASTER);
            types.add(MagiculeNodeType.GET_BLOCK_AT_POS);
            types.add(MagiculeNodeType.GET_CURENT_MASO);
            types.add(MagiculeNodeType.GET_MAX_MASO);
            types.add(MagiculeNodeType.GET_CURRENT_HP);
            types.add(MagiculeNodeType.GET_MAX_HP);
            //trigger
            types.add(MagiculeNodeType.EVENT_KEY_ONE);
            types.add(MagiculeNodeType.ON_SLOT_ENABLE);
            //value
            types.add(MagiculeNodeType.NUMBER);
            types.add(MagiculeNodeType.BOOLEAN);
            types.add(MagiculeNodeType.VECTOR);
            types.add(MagiculeNodeType.NULL);
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
    public Set<String> getEvolvableUniqueSkills(){return evolvableUniqueSkills;}

    public String getCurrentUniqueSkill() {
        return currentUniqueSkill;
    }



    public MasoEvolutionStage getMasoStage(){ return masoEconomy.getMasoStage(); }
    public float getMaxMaso(){ return masoEconomy.getMaxMaso(); }
    public float getMasoRegenRate(){ return masoEconomy.getMasoRegenRate(); }
    public void triggerMasoStageEvolutionAttempt(){ masoEconomy.triggerMasoStageEvolutionAttempt(); }
    public void addCurretMaso(double d){ masoEconomy.addCurrentMaso((float) d); }

    // ★新規：PlayerCasterAdapterが直接フィールドを触れなくなる分の窓口
    public float getCurrentMaso(){ return masoEconomy.getCurrentMaso(); }
    public void addMasoAmount(float amount){ masoEconomy.addCurrentMaso(amount); }
    public void setCurrentMaso(float value){ masoEconomy.setCurrentMaso(value); }
    public void consumeMasoAmount(float amount){ masoEconomy.consumeMaso(amount); }
    public void addTotalRegeneratedMaso(float amount){ masoEconomy.addTotalRegeneratedMaso(amount); }



    public float getBarrierPoint(){ return barrier.getCurrentPoint(); }
    public void setBarrierPoint(float point){ barrier.setCurrentPoint(point); }
    public float getMaxBarrierPoint(){ return barrier.getMaxBarrierPoint(); }
    public void setMaxBarrierPoint(float max){ barrier.setMaxBarrierPoint(max); }
    public float getBarrierDamageReduction(){ return barrier.getBarrierDamageReduction(); }
    public void recordBarrierHit(float rawDamage, boolean barrierBroke, long currentTick){
        barrier.recordBarrierHit(rawDamage, barrierBroke, currentTick);
    }

    public UUID getUniqueSkillId(){return this.uniqueSkillId;}
    public void setUniqueSkillId(UUID uniqueSkillId){this.uniqueSkillId = uniqueSkillId;}

    private void ensureUniqueSkillCircuit(){
        if (!this.currentUniqueSkill.equals("greedy")) return;

        MagiculeCircuit skillCircuit = circuits.get(EditorTab.SKILL);
        var node = (uniqueSkillId != null) ? skillCircuit.getCNode(uniqueSkillId) : null;

        if (node == null) {
            // 未構築、またはID残存だがノード実体が消えた壊れたデータ → 再構築
            uniqueSkillId = DefaultCircuitBuilder.buildDefaultSkillCircuit(skillCircuit);
            node = skillCircuit.getCNode(uniqueSkillId);
        }

        if (node.getSkillId() == null || node.getSkillId().isEmpty()) {
            node.setSkillId("greedy");
            setSkillAccessLevel("greedy", SkillAccessLevel.DENIED);
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

    // getCircuit(EditorTab)は「詠唱・compile用に参照する回路」を返す唯一の窓口。
    // MAGICタブだけ、実体を magicSlots[activeMagicSlot] にリダイレクトする。
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

    // エディタが「このタブの回路を書き換えた」ときに呼ぶ既存のsetCircuits互換口
    // MAGICタブは常に「編集中のスロット番号」を明示して呼ぶ必要があるため、専用メソッドを分ける
    public void setCircuits(EditorTab tab, MagiculeCircuit circuit){
        if (tab == EditorTab.MAGIC) {
            setMagicSlot(activeMagicSlot, circuit); // 呼び出し側でactiveを一時的に「編集スロット」に切り替えて使う想定
            return;
        }
        this.circuits.put(tab, circuit);
    }


    /**
     * ユニークスキルの進化を発動する。evolvableUniqueSkills に候補として
     * 登録済みの skillId のみ受け付ける。
     * 成功したら魔素進化ステージの「発動トリガー」を立てる。
     * ※ 現状の checkEvolution() は候補をセットするだけで実際の切り替えを行っていなかったため、
     *   ここが「資格→発動」の発動側の実装になる。
     */
    public boolean evolveUniqueSkillTo(String skillId) {
        if (skillId == null || !evolvableUniqueSkills.contains(skillId)) {
            return false;
        }
        this.currentUniqueSkill = skillId;
        this.evolvableUniqueSkills.clear();
        setSkillAccessLevel(skillId, SkillAccessLevel.DENIED);

        triggerMasoStageEvolutionAttempt();
        return true;
    }


    public boolean hasUnlockedUniqueSkills(String key){
        if(currentUniqueSkill.equals(key)){
            return unlockedSkills.contains(key + "_welcom");
        }
        return false;
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

    public void addGreedyScore(double amount, ServerPlayer player){
        this.greedyScore += amount;
        checkEvolution(player);

    }

    public void addPredatorScore(double amount, ServerPlayer player) {
        if(completeGreedy){
            this.predatorScore += amount;
            checkEvolution(player);
        }
    }

    public void addScavengerScore(double amount, ServerPlayer player) {
        if(completeGreedy){
            this.scavengerScore += amount;
            checkEvolution(player);
        }
    }

    public void addhoarderScore(double amount, ServerPlayer player) {
        if(completeGreedy){
            this.hoarderScore += amount;
            checkEvolution(player);
        }
    }

    public void addUsurperScore(double amount, ServerPlayer player){
        if(completeGreedy){
            this.usurperScore += amount;
            checkEvolution(player);
        }
    }


    private void checkEvolution(ServerPlayer player) {
        double threshold = 100;
        boolean stillGreedy = currentUniqueSkill.equals("greedy");

        if (stillGreedy && !completeGreedy && greedyScore >= threshold) {
            setSkillAccessLevel("greedy", SkillAccessLevel.READ_ONLY);

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

            unlockedNodeTypes.get(EditorTab.MAGIC).addAll(unlockNodeSet);
            unlockedNodeTypes.get(EditorTab.SKILL).addAll(unlockNodeSet);
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.ON_XP_PICKUP);
            unlockedNodeTypes.get(EditorTab.ARTS).addAll(unlockNodeSet);

            this.completeGreedy = true;
        }
        if (!completeGreedy) return;

        if (predatorScore >= threshold) {
            if (stillGreedy) unlockEvolutionCandidate(player, "predator");
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.ON_KILL);
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.COMBERS_KILL_TO_MASO);
            unlockedNodeTypes.get(EditorTab.MAGIC).add(MagiculeNodeType.ON_KILL);
            unlockedNodeTypes.get(EditorTab.MAGIC).add(MagiculeNodeType.COMBERS_KILL_TO_MASO); // 抜けを補完
            unlockBarrierNode();
        }
        if (scavengerScore >= threshold) {
            if (stillGreedy) unlockEvolutionCandidate(player, "scavenger");
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.ON_EAT);
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.COMBERS_SATIETY_TO_MASO);
            unlockedNodeTypes.get(EditorTab.MAGIC).add(MagiculeNodeType.ON_EAT);
            unlockedNodeTypes.get(EditorTab.MAGIC).add(MagiculeNodeType.COMBERS_SATIETY_TO_MASO); // 抜けを補完
            unlockBarrierNode();
        }
        if (hoarderScore >= threshold) {
            if (stillGreedy) unlockEvolutionCandidate(player, "hoarder");
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.ON_OVERCHARGE);
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.ABSORPTION);
            unlockedNodeTypes.get(EditorTab.MAGIC).add(MagiculeNodeType.ON_OVERCHARGE); // 抜けを補完
            unlockedNodeTypes.get(EditorTab.MAGIC).add(MagiculeNodeType.ABSORPTION);
            unlockBarrierNode();
        }
        if (usurperScore >= threshold) {
            if (stillGreedy) unlockEvolutionCandidate(player, "usurper");
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.ON_ATTACK_STRONGER);
            unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.COMBERS_POWERGAP_TO_MASO);
            unlockedNodeTypes.get(EditorTab.MAGIC).add(MagiculeNodeType.ON_ATTACK_STRONGER);
            unlockedNodeTypes.get(EditorTab.MAGIC).add(MagiculeNodeType.COMBERS_POWERGAP_TO_MASO); // 抜けを補完
            unlockBarrierNode();
        }
    }


    private void unlockBarrierNode(){
        unlockedNodeTypes.get(EditorTab.MAGIC).add(MagiculeNodeType.ON_TICK);
        unlockedNodeTypes.get(EditorTab.MAGIC).add(MagiculeNodeType.BARRIER);
        unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.ON_TICK);
        unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.BARRIER);
    }

    private void unlockEvolutionCandidate(ServerPlayer player, String skillName) {
       if(!this.evolvableUniqueSkills.contains(skillName)){
            this.evolvableUniqueSkills.add(skillName);
           setMaxBarrierPoint(60);

            player.sendSystemMessage(Component.translatable("message.reincarnated.voice_of_world.greedy_factor_analyzed", Component.literal(player.getName().getString())));

            String translatedSkillName = Component.translatable("name.reincarnated.uniqueSkill." + skillName).getString();
            player.sendSystemMessage(Component.translatable("message.reincarnated.voice_of_world.greedy_evolution_available", translatedSkillName));
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
            if(entry.getKey() == EditorTab.MAGIC)continue;
            EditorTab tab = entry.getKey();
            MagiculeCircuit circuit = entry.getValue();

            rootTag.put(tab.name(), circuit.saveToNBT());
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
        masoEconomy.saveToNBT(masoTag); // ★修正：丸投げ
        rootTag.put("maso", masoTag);

        CompoundTag barrierTag = new CompoundTag();
        barrier.saveToNBT(barrierTag); // ★修正：BarrierStateに丸投げ
        rootTag.put("barrier", barrierTag);

        rootTag.put("permission", savePermissionsNBT());

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
            if(tab == EditorTab.MAGIC)continue;
            if(rootTag.contains(tab.name())){
                rootTag.getCompound(tab.name()).ifPresent(tabTag ->{
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
            masoEconomy.loadFromNBT(rootTag.getCompoundOrEmpty("maso")); // ★修正：丸投げ
        }

        if(rootTag.contains("barrier")){
            CompoundTag barrierTag = rootTag.getCompoundOrEmpty("barrier");
            barrier.loadFromNBT(barrierTag); // ★修正：BarrierStateに丸投げ
        }

        if(rootTag.contains("permission")){
            loadPermissionsNBT(rootTag.getCompoundOrEmpty("permission"));
        }

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

        if(rootTag.contains("completeGreedy")){
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
        if(version < 2){
            migrateV1toV2();
        }
        if(version < 3){
            migrateV2toV3();
        }if(version < 4){
            migratev3tov4();
        }if(version < 5){
            migrateV4toV5(rootTag);
        }

    }
    private void migrateV0toV1(){
        //tokuninasi
    }
    private void migrateV1toV2(){
        completeGreedy = false;
        greedyScore = 50;
        predatorScore = 0;
        scavengerScore = 0;
        hoarderScore = 0;
        usurperScore = 0;
        evolvableUniqueSkills.clear();
    }
    private void migrateV2toV3(){
        addDefaultUnlockedNodes(EditorTab.MAGIC);
    }
    private void migratev3tov4(){
        addDefaultUnlockedNodes(EditorTab.MAGIC);
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