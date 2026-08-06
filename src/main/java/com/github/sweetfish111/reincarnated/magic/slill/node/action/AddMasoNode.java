package com.github.sweetfish111.reincarnated.magic.slill.node.action;

import com.github.sweetfish111.reincarnated.init.ModAttachments;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.MasoAmount;
import com.github.sweetfish111.reincarnated.player.PlayerMagicData;

import java.util.UUID;

public class AddMasoNode extends AbstractMagicNode {
    public AddMasoNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        context.incrementAndCheck();
        MasoAmount maso = pullMaso(1, context);
        context.getCaster().addMaso((float) maso.masoAmount());
        context.getCaster().addTotalRegeneratedMaso((float) maso.masoAmount());
        pushExecute(context);
    }

}
