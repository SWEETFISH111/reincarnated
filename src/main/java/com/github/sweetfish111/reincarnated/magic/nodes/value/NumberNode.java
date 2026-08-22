package com.github.sweetfish111.reincarnated.magic.nodes.value;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class NumberNode extends AbstractMagicNode {

    public NumberNode(UUID id){
        super(id);
    }
    @Override
    public void execute(MagicContext context) {
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        if(portIndex == 0){
            if(context.getCircuit() != null){
                return context.getCircuit().getNodeParam(this.id, "value", 0.0);
            }
            return 0.0;
        }
        return null;
    }
}
