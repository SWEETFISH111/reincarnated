package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;

public class EventKeyOneNode extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        System.out.println("kiten node kidou! migi no zikkoupinn he shori wo push");
        pushExecute(0, context);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context){
        return null;
    }
}
