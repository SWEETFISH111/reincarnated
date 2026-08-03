package com.github.sweetfish111.reincarnated.magic.nodes.trigger;

import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import net.minecraft.world.entity.Entity;

import java.util.UUID;


public class OnDamageNode extends AbstractMagicNode {
    public OnDamageNode(UUID id) {
        super(id);
    }

    public void trigger(MagicContext context, float damageAmount, Entity attacker) {
        // 1. コンテキスト（履歴書）に被弾データを一時保存する（後続のセンサーが引き出せるようにする）
        context.setMagicValue("damage_amount", (double) damageAmount);
        context.setMagicValue("attacker_entity", attacker);

        // 2. 接続されている次のノードへ実行フロー（白い線）を押し出す！
        executeOutputPort(0, context);
    }

}
