package com.github.sweetfish111.reincarnated.magic.nodes.value;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class BooleanNode extends AbstractMagicNode {
    public BooleanNode(UUID id){
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        boolean booleanValue = false;
        if(context.getCircuit().getNodeParam(this.id, "value", false) instanceof Boolean b){
            booleanValue = b;
        }
        return booleanValue;
    }
}
