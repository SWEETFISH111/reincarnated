package com.github.sweetfish111.reincarnated.magic.nodes.value;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

/**
 * 「投入する魔素量」を指定するための値ノード。
 * 見た目・保存方式はNumberNodeと同じ（NBTにvalueパラメータとして保持）だが、
 * ポート型はPortDataType.MASOであり、アクションノードの「効果量」入力ポートは
 * このノードのみを受け付ける（NumberNodeとは接続できない）。
 * <p>
 * NumberNodeとの決定的な違いは「値の意味」：
 * NumberNodeの値は最終的な効果量そのものを表していたが、
 * MasoInvestmentNodeの値は「投入する魔素の量」を表す。
 * 実際の効果量は、これを受け取ったアクションノード側で
 * {@link com.github.sweetfish111.reincarnated.magic.casting.MasoInvestmentScaling#computeEffect}
 * によって事後的に算出される。
 */
public class MasoInvestmentNode extends AbstractMagicNode {

    public MasoInvestmentNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //何もしない（NumberNode同様、値ノードは実行フローを持たない）
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        if (portIndex == 0) {
            return pullDouble(0, context);
        }
        return null;
    }
}
