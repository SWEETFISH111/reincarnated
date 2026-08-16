package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.config.BalanceConfig;
import com.github.sweetfish111.reincarnated.event.CalculationCapacityOverException;
import com.github.sweetfish111.reincarnated.event.MasoShortageException;
import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//魔法の計算コストから詠唱時間を算出して実際に発動させるクラス
public class CastingManager {
    private static final Map<UUID, CastingTask>activeCasts = new ConcurrentHashMap<>();

    public static void startCasting(MagicContext context) {
        if(activeCasts.containsKey(context.getCaster().getCasterId())){
            return;
        }

        int baseCastTimeTicks = CastCostCalculator.calculateCastTimeTicks(context.getCircuit());
        int totalCalcCost = baseCastTimeTicks;

        if (context.getCaster().getCasterEntity() instanceof ServerPlayer player) {
            PlayerMagicData magicData = player.getData(ModAttachments.PLAYER_MAGIC_DATA);
            double computeCapacity = magicData.getMaxComputeCapacity();
            double speedMultiplier = computeCastSpeedMultiplier(computeCapacity);
            totalCalcCost = (int) Math.ceil(baseCastTimeTicks * speedMultiplier);

            magicData.recordCastTime(baseCastTimeTicks); // ★演算能力の成長源として記録（短縮前の"本来の重さ"を記録する）
        }

        boolean hasChantCancellation = false;
        IMagicCaster iMagicCaster = context.getCaster();
        if(iMagicCaster.getCasterEntity() instanceof ServerPlayer player){
            checkChantCancellationSkill(player);
        }

        if (hasChantCancellation || totalCalcCost <= 0) {
            RuntimeMagicCircuit runtimeMagicCircuit = MagicCompiler.compileCircuit(context.getCaster(), context.getCircuit());
            Objects.requireNonNull(runtimeMagicCircuit).start(context);
            return;
        }

        CastingTask task = new CastingTask(context, totalCalcCost);
        activeCasts.put(context.getCaster().getCasterId(), task);
    }

    /**
     * 演算能力に応じた詠唱時間の速度倍率。
     * capacity=0で1.0（短縮なし）、capacityが増えるほど下限(MIN_CAST_MULTIPLIER)へ漸近する。
     * 決してゼロにはならない（即時詠唱化を防ぐ）。
     */
    private static double computeCastSpeedMultiplier(double computeCapacity) {
        double min = BalanceConfig.COMPUTE_CAPACITY_MIN_CAST_MULTIPLIER.get();
        double halfLife = BalanceConfig.COMPUTE_CAPACITY_CAST_HALF_LIFE.get();
        return min + (1.0 - min) / (1.0 + computeCapacity / halfLife);
    }

    public static void releaseCasting(IMagicCaster caster){
        CastingTask task = activeCasts.get(caster.getCasterId());

        if(task == null){
            activeCasts.remove(caster.getCasterId());
            return;
        }

        if(task.isReady()){
            RuntimeMagicCircuit runtimeMagicCircuit = MagicCompiler.compileCircuit(task.getContext().getCaster(), task.getContext().getCircuit());
            Objects.requireNonNull(runtimeMagicCircuit).start(task.getContext());
        }

        activeCasts.remove(caster.getCasterId());
    }

    public static void onServerTick() {
        if (!activeCasts.isEmpty()) {

            Iterator<Map.Entry<UUID, CastingTask>> iterator = activeCasts.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, CastingTask> entry = iterator.next();
                CastingTask task = entry.getValue();
                IMagicCaster caster = task.getPlayer();

                if (caster == null) {
                    iterator.remove();
                    continue;
                }

                // 詠唱中の演出（足元に魔素のパーティクルを出す）
                ServerLevel level = caster.getCasterLevel();
                boolean isReady = task.tick();
                Vec3 position = caster.getCasterPosition();

                if (!isReady) {
                    level.sendParticles(
                            ParticleTypes.ENCHANT,
                            position.x, position.y + 1.5, position.z,
                            3, 0.5, 0.1, 0.5, 0.05
                    );
                } else {
                    level.sendParticles(
                            ParticleTypes.CRIT,
                            position.x, position.y + 1.5, position.z,
                            2, 0.5, 0.3, 0.5, 0.1
                    );
                }
            }
        }
    }
    private static boolean checkChantCancellationSkill(ServerPlayer player) {
        // TODO: 魂のデータ（Data Attachment等）から『詠唱破棄』スキルを持っているか判定する
        return false;
    }

    public static void cancelCasting(UUID casterId) {
        activeCasts.remove(casterId);
    }

}
