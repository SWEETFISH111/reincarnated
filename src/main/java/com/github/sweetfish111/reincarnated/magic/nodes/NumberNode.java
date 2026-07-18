package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

import java.util.UUID;

public class NumberNode extends AbstractMagicNode{

    public NumberNode(UUID id){
        super(id);
    }
    @Override
    public void execute(MagicContext context) {
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        if(portIndex == 0){
            if(context != null && context.getCircuit() != null){
                System.out.println("======" + context.getCircuit().getNodeParam(this.id, "value", 0.0) + "=====");
                return context.getCircuit().getNodeParam(this.id, "value", 0.0);
            }
            return 0.0;
        }
        return null;
    }

    public UUID getId(){
        return this.id;
    }
}
