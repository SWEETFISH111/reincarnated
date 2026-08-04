package com.github.sweetfish111.reincarnated.magic.slill.node.trigger;

import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class onTickNode extends AbstractMagicNode {

    boolean ready = false;

    public onTickNode(UUID id) {
        super(id);
        this.isTrigger = true;
        this.triggerType = "on_tick";
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();
        boolean b = pullBoolean(1, context);
        if(b){
            if(!ready){
                double intervalTime = pullDouble(2, context);
                ActiveMagicManager.registerActiveNode(context.getCaster(), this.id, this, (int)intervalTime * 20);
            }
            pushExecute(context);
            System.out.println("onticknode:hakka");
        }else{
            ActiveMagicManager.unregisterActiveNode(context.getCaster(), this.id);
        }
    }
}
