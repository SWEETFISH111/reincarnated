package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class DigAllNode extends AbstractMagicNode {
    // 1ブロック破壊あたりの基礎魔素コスト
    private static final float COST_PER_BLOCK = 0.5f;
    // ノードの入力値がどうであろうと絶対に超えないサーバー保護上限
    private static final int HARD_CAP = 2048;

    public DigAllNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        // 発動コスト自体は掘削量確定後に払うため、ここでは0でスルー
        masoCost = 0;
        super.execute(context);

        Vec3 posVec = pullVector3(1, context);
        if (posVec == null) return;

        int requestedCount = (int) pullDouble(2, context);
        int maxCount = Math.min(HARD_CAP, Math.max(1, requestedCount));

        boolean sameTypeOnly = true;
        if (context.getCircuit() != null
                && context.getCircuit().getNodeParam(this.id, "value", true) instanceof Boolean b) {
            sameTypeOnly = b;
        }

        ServerLevel level = context.getLevel();
        BlockPos seedPos = BlockPos.containing(posVec);
        if (!level.isLoaded(seedPos)) return;

        BlockState seedState = level.getBlockState(seedPos);
        Block seedBlock = seedState.getBlock();

        // 空気・破壊不能ブロック（bedrock等）は起点にできない
        if (seedState.isAir() || seedState.getDestroySpeed(level, seedPos) < 0) return;

        // ===== フェーズ1：フラッドフィル探索（コストなし） =====
        Deque<BlockPos> frontier = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<BlockPos> collected = new ArrayList<>();

        frontier.add(seedPos);
        visited.add(seedPos);

        while (!frontier.isEmpty() && collected.size() < maxCount) {
            context.incrementAndCheck(); // 演算容量オーバーの安全弁（既存の術式暴走処理と連動）

            BlockPos current = frontier.poll();
            collected.add(current);

            for (Direction dir : Direction.values()) {
                if (collected.size() + frontier.size() >= maxCount) break;

                BlockPos next = current.relative(dir);
                if (visited.contains(next) || !level.isLoaded(next)) continue;

                BlockState nextState = level.getBlockState(next);
                if (nextState.isAir()) continue;
                if (nextState.getDestroySpeed(level, next) < 0) continue; // bedrock等は突破しない
                if (sameTypeOnly && nextState.getBlock() != seedBlock) continue;

                visited.add(next);
                frontier.add(next);
            }
        }

        // ===== フェーズ2：所持魔素で賄える分だけ実際に破壊 =====
        float currentMaso = context.getCaster().getMasoAmount();
        int affordableCount = (int) Math.floor(currentMaso / COST_PER_BLOCK);
        int actualCount = Math.min(collected.size(), Math.max(0, affordableCount));

        for (int i = 0; i < actualCount; i++) {
            level.destroyBlock(collected.get(i), true, context.getCaster().getCasterEntity());
        }

        if (actualCount > 0) {
            consumeMaso(actualCount * COST_PER_BLOCK, context.getCaster());
        }

        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return null;
    }
}