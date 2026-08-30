package com.github.sweetfish111.reincarnated.magic.nodes.math;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.record.MasoAmount;

import java.util.UUID;

/**
 * 2つの数値のうち小さい方を返すノード。
 * ポート型はANYのため、MASO型のポート（アクションノードの投入魔素量など）にも直結できる。
 * 用途例：「殺しきるのに必要な魔素」と「現在の魔素」のうち小さい方を実際の投入量にする、等。
 */
public class MinNode extends AbstractMagicNode {
    public MinNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object valA = pullData(0, context);
        Object valB = pullData(1, context);

        if (valA instanceof Number a && valB instanceof Number b) {
            return Math.min(a.doubleValue(), b.doubleValue());
        } else if(valA instanceof MasoAmount a && valB instanceof MasoAmount b){
            return new MasoAmount(Math.min(a.amount(), b.amount()));
        }
        return 0.0;
    }
}
