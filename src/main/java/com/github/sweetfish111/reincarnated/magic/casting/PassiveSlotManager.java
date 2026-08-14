package com.github.sweetfish111.reincarnated.magic.casting;

import com.github.sweetfish111.reincarnated.circuit.MagiculeCircuit;
import com.github.sweetfish111.reincarnated.circuit.RuntimeMagicCircuit;
import com.github.sweetfish111.reincarnated.magic.caster.IMagicCaster;
import com.github.sweetfish111.reincarnated.magic.caster.PlayerCasterAdapter;
import com.github.sweetfish111.reincarnated.magic.compiler.MagicCompiler;
import com.github.sweetfish111.reincarnated.magic.context.MagicContext;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.control.WhileNode;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PassiveSlotManager {

    /** 指定スロットのパッシブ回路を起動する（on_slot_enabledトリガーを実行） */
    public static void startSlot(ServerPlayer player, MagiculeCircuit slotCircuit) {
        if (slotCircuit == null) return;

        IMagicCaster caster = new PlayerCasterAdapter(player);
        RuntimeMagicCircuit runtime = MagicCompiler.compileCircuit(caster, slotCircuit);
        if (runtime == null) return;

        MagicContext context = new MagicContext(slotCircuit, runtime);

        for (Map.Entry<UUID, AbstractMagicNode> entry : runtime.getInstancedNodes().entrySet()) {
            AbstractMagicNode node = entry.getValue();
            if (Objects.equals(node.getTriggerType(), "on_slot_enabled")) {
                node.execute(context);
            }
        }
    }

    /** 指定スロットのパッシブ回路（Whileループ等）を停止する */
    public static void stopSlot(ServerPlayer player, MagiculeCircuit slotCircuit) {
        if (slotCircuit == null) return;

        IMagicCaster caster = new PlayerCasterAdapter(player);
        RuntimeMagicCircuit runtime = MagicCompiler.compileCircuit(caster, slotCircuit);
        if (runtime == null) return;

        List<UUID> whileNodeIds = new ArrayList<>();
        for (Map.Entry<UUID, AbstractMagicNode> entry : runtime.getInstancedNodes().entrySet()) {
            if (entry.getValue() instanceof WhileNode) {
                whileNodeIds.add(entry.getKey());
            }
        }
        if (!whileNodeIds.isEmpty()) {
            TimerCastingManager.cancelTasksByRepeatNode(whileNodeIds);
        }
    }
}