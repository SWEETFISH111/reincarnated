package com.github.sweetfish111.reincarnated.magic.nodes.trigger;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class EventKeyOneNode extends AbstractMagicNode {
    public EventKeyOneNode(UUID id) {
        super(id);
        this.isTrigger = true;
        this.triggerType = "event_key_one";
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();
        pushExecute(context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context){
        return null;
    }
}
