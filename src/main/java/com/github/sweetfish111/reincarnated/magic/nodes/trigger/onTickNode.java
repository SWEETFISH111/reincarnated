package com.github.sweetfish111.reincarnated.magic.nodes.trigger;

import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class onTickNode extends AbstractMagicNode {

    boolean ready = false;

    public onTickNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        super.execute(context);
        if(!ready){
            double intervalTime = pullDouble(1, context);
            ActiveMagicManager.registerActiveNode(context.getCaster(), this.id, this, (int)intervalTime * 20);
        }
        pushExecute(context);
    }
}
