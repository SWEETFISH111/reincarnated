package com.github.sweetfish111.reincarnated.magic.nodes.conversion;

import com.github.sweetfish111.reincarnated.magic.caster.CasterSnapshot;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.world.entity.Entity;

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
        Object target = pullData(0, context);
        if(target instanceof Entity e){
            return e.getLookAngle();
        }else if(target instanceof CasterSnapshot c){
            return c.lookVector();
        }else{
            return null;
        }
    }
}
