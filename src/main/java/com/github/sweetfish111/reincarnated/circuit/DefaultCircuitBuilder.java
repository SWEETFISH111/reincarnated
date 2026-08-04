package com.github.sweetfish111.reincarnated.circuit;


import java.util.UUID;

public class DefaultCircuitBuilder {

    /**
     * スキルタブの初期回路（貪欲者）を構築する
     */
    public static void buildDefaultSkillCircuit(MagiculeCircuit circuit) {
        if (!circuit.getNodes().isEmpty()) return; // すでに存在する場合は何もしない

        // 1. 起点ノード（OnXpPickupNode）をキャンバスの中心付近（例: x=100, y=100）に生成
        MagiculeCircuit.NodeData xpNodeData = new MagiculeCircuit.NodeData(UUID.randomUUID(), MagiculeNodeType.ON_XP_PICKUP, 100, 100);

        // 回路のノードマップに追加
        circuit.addNode(xpNodeData);

        // 必要であれば、初期状態の演算ノードや出力ノードをここで一緒に配置し、
        // WireData（ワイヤー）で結線しておくことで、最初から「経験値→魔素変換」の回路が完成した状態にします。
    }
}