package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class ReturnCaster extends AbstractMagicNode {
    public ReturnCaster(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object nodeParam = context.getCircuit().getNodeParam(this.id, "value", false);
        if(nodeParam instanceof Boolean b){
            return b ? context.getSnapshot() : context.getCaster();
        }
        return context.getCaster();
    }
}
