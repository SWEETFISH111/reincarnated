package com.github.sweetfish111.reincarnated.magic.nodes.conversion;

import com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

/**
 * 「欲しい効果量」から「それを得るために投入すべき魔素量」を逆算するノード。
 * アクションノード側が採用した投入魔素量方式（投入量→効果量）とは逆向きの計算で、
 * MasoInvestmentScaling.computeCost（効果量→コスト）をそのままグラフに露出させたもの。
 * <p>
 * baseCostPerUnitは接続先のアクションノードが内部で使っている基礎コスト定数と
 * 一致させる必要がある（例：DamageNode=2, HealingNode=5, ExplosionNode=4,
 * DigNode=0.7, CollectItemsNode=0.2, SummonNode=1.2）。
 * <p>
 * 例：「視線の先の対象のHPを取得→必要魔素量算出→現在魔素とMinノードで比較
 * →Damageノードへ投入」とすることで、
 * 「殺しきれるならその分だけ、無理なら持てる全てを投入する」という分岐が組める。
 */
public class RequiredMasoNode extends AbstractMagicNode {

    public RequiredMasoNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //なにもしない（値ノード扱い）
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        if (portIndex != 0) return null;

        double desiredEffect = pullDouble(0, context);
        double baseCostPerUnit = pullDouble(1, context);
        float availableMaso = context.getCaster().getMasoAmount();

        MasoInvestmentScaling.CostResult costResult =
                MasoInvestmentScaling.computeCost((float) baseCostPerUnit, (float) desiredEffect, availableMaso);

        return costResult.cost();
    }
}
