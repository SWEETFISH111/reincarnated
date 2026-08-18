package com.github.sweetfish111.reincarnated.magic.nodes.math;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

/**
 * 2つの数値のうち大きい方を返すノード。MinNodeと対を成す。
 * ポート型はANYのため、MASO型のポートにも直結できる。
 */
public class MaxNode extends AbstractMagicNode {
    public MaxNode(UUID id) {
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
            return Math.max(a.doubleValue(), b.doubleValue());
        }
        return 0.0;
    }
}
