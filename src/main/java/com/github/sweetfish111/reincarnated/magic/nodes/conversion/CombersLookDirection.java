package com.github.sweetfish111.reincarnated.magic.nodes.conversion;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;

import java.util.UUID;

public class CombersLookDirection extends AbstractMagicNode {
    public CombersLookDirection(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //なにもしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        return pullEntity(0, context).getLookAngle();
    }
}
