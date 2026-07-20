package com.github.sweetfish111.reincarnated.magic.nodes;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class CombersTargetPos extends AbstractMagicNode{
    @Override
    public void execute(MagicContext context) {
        //何もしない
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        Entity targetEntity = pullEntity(0, context);
        if(targetEntity != null){
            Vec3 targetPos = targetEntity.position();
            return targetPos;
        }
        return null;
    }
}
