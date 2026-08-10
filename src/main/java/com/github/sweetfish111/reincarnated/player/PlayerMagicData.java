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
    private static final int CURRENT_DATA_VERSION = 2;
    private final Map<EditorTab, MagiculeCircuit> circuits = new EnumMap<>(EditorTab.class);
    public float currentMaso = 20f;
    private float maxBarrierPoint = 20;
    private float barrierPoint = 0;

    public float totalRegeneratedMaso = 0;
    public float totalConsumedMaso = 0;

    // 魔素上限・回復速度：進化段階ごとに floor/scaleFactor が切り替わる（MasoEvolutionStage参照）
    private MasoEvolutionStage masoStage = MasoEvolutionStage.STAGE0;
    // 現ステージに入った時点での totalConsumedMaso / totalRegeneratedMaso
    // （このステージ内での消費量・回復量の起点。両方とも同じ進化タイミングでリセットされる）
    private double stageStartConsumedMaso = 0.0;
    private double stageStartRegeneratedMaso = 0.0;
    // ステージ移行時、旧ステージでの超過消費分をどれだけ新ステージの起点に持ち越すか（0〜1）
    // = 「非効率な旧スケーリングで頑張った分」を新ステージでのヘッドスタートとして還元する係数
    private static final double STAGE_CARRYOVER_RATIO = 0.3;

    public MasoEvolutionStage getMasoStage() {
        return masoStage;
    }

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
    public Set<String> getEvolvableUniqueSkills(){return evolvableUniqueSkills;}

    public String getCurrentUniqueSkill() {
        return currentUniqueSkill;
    }
    public MagiculeCircuit getCircuit(EditorTab tab){
        MagiculeCircuit circuit = circuits.get(tab);
        return circuit;
    }
    public float getBarrierPoint(){return barrierPoint;}
    public float getMaxBarrierPoint(){return maxBarrierPoint;}
    public void setBarrierPoint(float barrierPoint){this.barrierPoint = barrierPoint;}
    public void setMaxBarrierPoint(float max){this.maxBarrierPoint = max;}

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

    public float getMaxMaso(){
        // 閾値未達で持ち越し中のトリガーがあれば、ここで再チェック（消費が進んで閾値を超えた瞬間に反映される）
        advanceMasoStageIfNeeded();
        double sinceStageStart = Math.max(0.0, this.totalConsumedMaso - stageStartConsumedMaso);
        double scaledInput = sinceStageStart / 100.0;
        return (float) (masoStage.getFloor() + masoStage.getScaleFactor() * Math.log(1.0 + scaledInput));
    }

    /**
     * 「発動」トリガー待ちフラグ。魔素消費量が閾値を超えているだけでは進化せず、
     * このフラグが立った状態で閾値も満たしていて初めてステージが進む。
     * ユニークスキル進化（evolveUniqueSkillTo）が発火元。
     * トリガーが先に立って閾値がまだの場合はここに保持され、後から
     * totalConsumedMaso が伸びて閾値に届いた時点（＝次のgetMaxMaso呼び出し時）で反映される。
     */
    private boolean pendingMasoEvolutionTrigger = false;

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
        setSkillAccessLevel(skillId, SkillAccessLevel.READ_ONLY);

        triggerMasoStageEvolutionAttempt();
        return true;
    }

    /**
     * 魔素進化ステージを進める試みを外部から起動するための汎用フック。
     * 将来的に「二段階目のユニークスキル進化」など別の発動条件を実装する際も、
     * ここを呼び出すだけで同じ持ち越しロジックに乗せられる。
     */
    public void triggerMasoStageEvolutionAttempt() {
        this.pendingMasoEvolutionTrigger = true;
        advanceMasoStageIfNeeded();
    }

    /**
     * pendingMasoEvolutionTrigger が立っていて、かつ totalConsumedMaso が
     * 現ステージの evolutionThreshold を超えていれば次ステージへ進化させる。
     * どちらか一方だけでは進化しない（発動トリガー × 消費量の両方が必要）。
     *
     * 旧セーブ移行時：totalConsumedMaso 自体は引き継がれるため、
     * 移行後に初めてユニークスキル進化が発動した瞬間、既に閾値を超えている分の
     * 超過消費量がそのままヘッドスタートとして反映される
     * （＝旧システムで頑張った分がここで自然に還元される）。
     */
    private void advanceMasoStageIfNeeded() {
        if (!pendingMasoEvolutionTrigger) return;

        MasoEvolutionStage next = masoStage.getNext();
        if (next == null) {
            pendingMasoEvolutionTrigger = false; // 最終段階、これ以上進化しない
            return;
        }

        double sinceStageStart = totalConsumedMaso - stageStartConsumedMaso;
        double threshold = masoStage.getEvolutionThreshold();
        if (sinceStageStart < threshold) {
            return; // トリガーは消費せず持ち越す。消費量が追いついたら次回呼び出しで進化する
        }

        // 旧ステージ内での「閾値超過分」を算出し、その一部を新ステージのヘッドスタートとして持ち越す。
        // 非効率だった旧スケーリングで規定値を大きく超えて頑張った人ほど、
        // 新ステージ突入直後のmaxMasoが高くなる＝速度バフとして体感できる。
        double overflow = sinceStageStart - threshold;
        double carryHeadStart = overflow * STAGE_CARRYOVER_RATIO;

        masoStage = next;
        stageStartConsumedMaso = totalConsumedMaso - carryHeadStart;
        // 回復速度側もステージ切り替わりのタイミングで起点をリセットし、新ステージのregenFloorから始まるようにする
        stageStartRegeneratedMaso = totalRegeneratedMaso;
        pendingMasoEvolutionTrigger = false;

        onMasoStageEvolved(masoStage);
    }

    /** ステージ進化時のフック。実績解放・通知・エフェクト送信などをここに繋ぐ想定 */
    private void onMasoStageEvolved(MasoEvolutionStage newStage) {
        // TODO: 進化演出・実績通知・ネットワーク同期パケット送信などをここに実装
    }

    public float getMasoRegenRate(){
        double sinceStageStart = Math.max(0.0, this.totalRegeneratedMaso - stageStartRegeneratedMaso);
        double scaledInput = sinceStageStart / 100.0;
        return (float)(masoStage.getRegenFloor() + masoStage.getRegenScaleFactor() * Math.log(1.0 + scaledInput));
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

    public void addCurretMaso(double d){
        currentMaso += (float) d;
    }


    private void checkEvolution(ServerPlayer player) {
        if (!currentUniqueSkill.equals("greedy")) return;

        double threshold = 1;

        if(greedyScore >= threshold){

            if(!completeGreedy) {
                MagiculeCircuit skillCircuit = this.getCircuit(EditorTab.SKILL);
                setSkillAccessLevel("greedy", SkillAccessLevel.READ_ONLY);

                // STAGE0（旧世界）にいる間だけ意味を持つガード。
                // 「潜在能力の片鱗に気づく」瞬間＝0→1（旧世界→新ゼロ）の専用トリガー。
                // STAGE1以降に進んだ後はこのブロックが再実行されても masoStage が
                // 既に STAGE0 でなくなっているため何も起きない（誤爆防止）。
                if (masoStage == MasoEvolutionStage.STAGE0) {
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
                unlockedNodeTypes.get(EditorTab.SKILL).add(MagiculeNodeType.HEALING);
                unlockedNodeTypes.get(EditorTab.ARTS).addAll(unlockNodeSet);

                this.completeGreedy = true;
            }

            if (predatorScore >= threshold) {
                unlockEvolutionCandidate(player, "predatorw");
            }
            if (scavengerScore >= threshold) {
                unlockEvolutionCandidate(player, "scavenger");
            }
            if (hoarderScore >= threshold) {
                unlockEvolutionCandidate(player, "hoarder");
            }
            if (usurperScore >= threshold){
                unlockEvolutionCandidate(player, "usurper");
            }
        }
    }

    private void unlockEvolutionCandidate(ServerPlayer player, String skillName) {
       if(!this.evolvableUniqueSkills.contains(skillName)){
            this.evolvableUniqueSkills.add(skillName);

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
            EditorTab tab = entry.getKey();
            MagiculeCircuit circuit = entry.getValue();

            rootTag.put(tab.name(), circuit.saveToNBT());
        }
        CompoundTag masoTag = new CompoundTag();
        masoTag.putFloat("currentMaso", currentMaso);
        masoTag.putFloat("totalRegeneratedMaso", totalRegeneratedMaso);
        masoTag.putFloat("totalConsumedMaso", totalConsumedMaso);
        masoTag.putInt("masoStage", masoStage.ordinal());
        masoTag.putDouble("stageStartConsumedMaso", stageStartConsumedMaso);
        masoTag.putDouble("stageStartRegeneratedMaso", stageStartRegeneratedMaso);
        masoTag.putBoolean("pendingMasoEvolutionTrigger", pendingMasoEvolutionTrigger);
        rootTag.put("maso", masoTag);

        CompoundTag barrierTag = new CompoundTag();
        barrierTag.putFloat("currentBarrier", barrierPoint);
        barrierTag.putFloat("maxBarrier", maxBarrierPoint);
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
            // 旧セーブ（masoStageキー無し）は STAGE0（旧世界）としてロードされる。
            // STAGE0 は元の旧式(floor=20, SF=5)そのものなので、遷移トリガーが立つまでは
            // 挙動が完全に旧システムと同一＝既存プレイヤーへの影響ゼロで移行できる。
            // 遷移トリガー(greedyScore>=1)は checkEvolution() 側にあるため、
            // ロード直後に一度評価する（下記 checkEvolution() 呼び出し参照）。
            masoStage = MasoEvolutionStage.fromIndex(masoTag.getInt("masoStage").orElse(0));
            stageStartConsumedMaso = masoTag.getDouble("stageStartConsumedMaso").orElse(0.0);
            stageStartRegeneratedMaso = masoTag.getDouble("stageStartRegeneratedMaso").orElse(0.0);
            pendingMasoEvolutionTrigger = masoTag.getBoolean("pendingMasoEvolutionTrigger").orElse(false);
        }

        if(rootTag.contains("barrier")){
            CompoundTag barrierTag = rootTag.getCompoundOrEmpty("barrier");
            barrierPoint = barrierTag.getFloatOr("currentBarrier", 0.0f);
            maxBarrierPoint = barrierTag.getFloatOr("maxBarrier", 0.0f);
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

    }
    private void migrateV0toV1(){
        //tokuninasi
    }
    private void migrateV1toV2(){
        predatorScore = 0;
        scavengerScore = 0;
        hoarderScore = 0;
        usurperScore = 0;
        evolvableUniqueSkills.clear();
    }
}