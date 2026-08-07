package com.github.sweetfish111.reincarnated.magic.nodes.control;

import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.casting.ActiveMagicManager;
import com.github.sweetfish111.reincarnated.magic.casting.TimerCastingManager;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.reincarnated;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WhileNode extends AbstractMagicNode {
    public WhileNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();
        boolean b = pullBoolean(1, context);
        reincarnated.LOGGER.info("whileNode:" + b);
        System.out.println("while");
        if(b){
            double srottoringTime = pullDouble(2, context);
            int srottoringTicks = (int)(srottoringTime * 20);
            AbstractMagicNode nextNode = (AbstractMagicNode) this.getNextNode(0);
            TimerCastingManager.registerTimer(nextNode.getId(), this.id, context, srottoringTicks, srottoringTicks, -1);
            pushExecute(context);
        }else{
            List<UUID> cancel = new ArrayList<>();
            cancel.add(this.id);
            TimerCastingManager.cancelTasksByRepeatNode(cancel);
        }

    }
}
