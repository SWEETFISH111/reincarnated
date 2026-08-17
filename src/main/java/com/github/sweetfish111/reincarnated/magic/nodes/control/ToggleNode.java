package com.github.sweetfish111.reincarnated.magic.nodes.control;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.system.ReincarnatedPlaySound;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.UUID;

public class ToggleNode extends AbstractMagicNode {

    public ToggleNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        masoCost = 0;
        context.incrementAndCheck();
        boolean currentState = false;

        if(context.getCircuit() != null){
            currentState = (boolean)context.getCircuit().getNodeParam(this.id, "toggled", false);
        }

        boolean nextState = !currentState;

        if(context.getCircuit() != null){
            context.getCircuit().setNodeParam(this.id,"toggled", nextState);
        }
        if(currentState){
            ReincarnatedPlaySound.playToggleOnSound(context.getCaster().getCasterLevel(), context.getCaster().getCasterPosition());
        }else{
            ReincarnatedPlaySound.playToggleOffSound(context.getCaster().getCasterLevel(), context.getCaster().getCasterPosition());
        }
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        if (context.getCircuit() == null) {
            return false;
        }
        return context.getCircuit().getNodeParam(this.id, "toggled", false);
    }
}
