package com.github.sweetfish111.reincarnated.magic.nodes.action;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;

import java.util.UUID;


public class DamageNode extends AbstractMagicNode implements MagicNode {
    float BASECOST = 3;

    public DamageNode(UUID id) {
        super(id);
    }

    @Override
    public void execute(MagicContext context) {
        double damageAmount = pullDouble(2, context);
        masoCost = BASECOST * (float) damageAmount;
        super.execute(context);
        Object target = pullData(1, context);
        if(target instanceof Entity targetEntity){
            DamageSource source = new DamageSource(
                    context.getCaster().level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MAGIC),
                    null,context.getCaster(),null
            );
            targetEntity.hurtServer(context.getLevel(), source, (float) damageAmount);
        }
    }

    @Override
    public Object getOutputData(int portIndex, MagicContext context) {
        return super.getOutputData(portIndex, context);
    }
}
