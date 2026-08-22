package com.github.sweetfish111.reincarnated.magic.nodes.value;

import com.github.sweetfish111.reincarnated.magic.PoolType;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class MasoPoolNode extends AbstractMagicNode {
    public MasoPoolNode(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return context.getCircuit().getNodeParam(this.id, "value", PoolType.PLAYER);
    }
}
