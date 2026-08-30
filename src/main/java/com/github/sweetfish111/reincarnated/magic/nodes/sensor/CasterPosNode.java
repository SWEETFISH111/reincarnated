package com.github.sweetfish111.reincarnated.magic.nodes.sensor;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CasterPosNode extends AbstractMagicNode {
    public CasterPosNode(UUID id) {
        super(id);
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return context.getCaster().getCasterPosition();
    }

}
