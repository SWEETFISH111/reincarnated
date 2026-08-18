package com.github.sweetfish111.reincarnated.magic.nodes.conversion;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

/**
 * 指定したアクションノード種別の基礎コスト係数（BASECOST）をBalanceConfigから取得するノード。
 * required_masoノードの「基礎コスト」入力に直結することで、
 * DamageNode等のBASECOSTがconfig変更で動いても手動同期が不要になる
 * （単一の情報源＝BalanceConfigをどちらも参照するため、静かにズレることがなくなる）。
 * <p>
 * 入力ポート0：アクション種別のインデックス（{@link ActionNodeType}のid）
 * 　0=Damage, 1=Healing, 2=Explosion, 3=Dig, 4=CollectItems, 5=Summon
 * 出力ポート0：対応するBASECOST値
 */
public class GetBaseCostNode extends AbstractMagicNode {

    public GetBaseCostNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //何もしない（値ノード扱い）
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        if (portIndex != 0) return null;

        int actionIndex = (int) Math.round(pullDouble(0, context));
        ActionNodeType type = ActionNodeType.fromId(actionIndex);
        return (double) type.getBaseCost();
    }
}
