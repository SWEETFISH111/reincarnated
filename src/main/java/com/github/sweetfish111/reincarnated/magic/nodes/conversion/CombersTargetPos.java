package com.github.sweetfish111.reincarnated.magic.nodes.conversion;

import com.github.sweetfish111.reincarnated.magic.CasterSnapshot;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class CombersTargetPos extends AbstractMagicNode {
    public CombersTargetPos(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        super.getOutputData(portIndex, context);
        Object target = pullData(0, context);
        if(target instanceof Entity e){
            return e.getPosition(1);
        }else if(target instanceof CasterSnapshot c){
            return c.position();
        }else{
            return null;
        }
    }
}
