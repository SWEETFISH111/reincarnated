package com.github.sweetfish111.reincarnated.circuit;

import com.github.sweetfish111.reincarnated.client.screen.NodePort;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public class MagiculeCircuit {
    private final List<NodeData> nodes = new ArrayList<>();
    private final List<WireData> wires = new ArrayList<>();
    private final Map<UUID, Map<String, Object>> nodeParameters = new HashMap<>();

    public void addNode(NodeData node){
        this.nodes.add(node);
    }

    public void addWire(UUID sorceNodeId, int sourcePortIndex, UUID targetNodeId, int targetPortIndex, boolean isDataFlow){
        this.wires.removeIf(wire ->
                wire.targetId.equals(targetNodeId) && wire.targetPortIndex == targetPortIndex
        );
        this.wires.removeIf(wire ->
                wire.sourceId.equals(sorceNodeId) && wire.sourcePortIndex == sourcePortIndex
        );
        this.wires.add(new WireData(sorceNodeId, sourcePortIndex, targetNodeId, targetPortIndex, isDataFlow));
    }

    // セッター
    public void setNodeParam(UUID nodeId, String key, Object value){
        nodeParameters.computeIfAbsent(nodeId, k -> new HashMap<>()).put(key, value);
    }

    // ゲッター
    public List<NodeData> getNodes(){ return this.nodes; }
    public List<WireData> getWires(){ return this.wires; }

    public Object getNodeParam(UUID nodeId, String key, Object defaultValue){
        if(nodeParameters.containsKey(nodeId)){
            return nodeParameters.get(nodeId).getOrDefault(key, defaultValue);
        }
        return defaultValue;
    }

    // 消す系
    public void removeWiresByPort(UUID nodeId, NodePort.Type portType, int portIndex){
        this.wires.removeIf(wire ->
                (portType == NodePort.Type.OUTPUT && wire.sourceId.equals(nodeId) && wire.sourcePortIndex == portIndex) ||
                        (portType == NodePort.Type.INPUT && wire.targetId.equals(nodeId) && wire.targetPortIndex == portIndex));
    }

    public void removeWiresByNode(UUID nodeId){
        this.wires.removeIf(wire -> wire.sourceId.equals(nodeId) || wire.targetId.equals(nodeId));
    }

    public void removeNodeAndWires(UUID nodeId){
        this.nodes.removeIf(node -> node.id.equals(nodeId));
        this.wires.removeIf(wire -> wire.sourceId.equals(nodeId) || wire.targetId.equals(nodeId));
        this.nodeParameters.remove(nodeId);
    }

    // セーブ＆ロード
    public CompoundTag saveToNBT(){
        CompoundTag tag = new CompoundTag(); // nodes + wiresのNBT

        ListTag nodesTag = new ListTag(); // nodesのNBT
        for(NodeData node : this.nodes){
            CompoundTag nTag = new CompoundTag(); // node一つ分のNBT
            nTag.putString("Id", node.id.toString());
            nTag.putString("Type", node.type.getId());
            nTag.putInt("X", node.x);
            nTag.putInt("Y", node.y);

            Map<String, Object> params = this.nodeParameters.get(node.id);
            if(params != null && !params.isEmpty()){
                CompoundTag paramsTag = new CompoundTag();
                for(Map.Entry<String, Object> entry : params.entrySet()){
                    Tag convertedTag = toNbtTag(entry.getValue());
                    if (convertedTag != null) {
                        paramsTag.put(entry.getKey(), convertedTag);
                    }
                }
                nTag.put("Parameters", paramsTag);
            }

            nodesTag.add(nTag);
        }
        tag.put("Nodes", nodesTag);

        ListTag wiresTag = new ListTag(); // wiresのNBT
        for(WireData wire : this.wires){
            CompoundTag wTag = new CompoundTag(); // wire一つ分のNBT
            wTag.putString("SourceId", wire.sourceId.toString());
            wTag.putInt("SourcePort", wire.sourcePortIndex);
            wTag.putString("TargetId", wire.targetId.toString());
            wTag.putInt("TargetPort", wire.targetPortIndex);
            wTag.putBoolean("IsDataFlow", wire.isDataFlow);
            wiresTag.add(wTag);
        }
        tag.put("Wires", wiresTag);

        return tag;
    }

    public void loadFromNBT(CompoundTag tag){
        this.nodes.clear();
        this.wires.clear();
        this.nodeParameters.clear();

        Optional<ListTag> nodesTag = tag.getList("Nodes");
        if(nodesTag.isPresent()){
            for(int i = 0; i < nodesTag.get().size(); i++){
                Optional<CompoundTag> nTag = nodesTag.get().getCompound(i);
                if(nTag.isPresent()){
                    CompoundTag n = nTag.get();

                    String id = n.getString("Id").orElse("");
                    String type = n.getString("Type").orElse("");
                    int x = n.getInt("X").orElse(0);
                    int y = n.getInt("Y").orElse(0);
                    if(!id.isEmpty() && !type.isEmpty()){
                        this.nodes.add(new NodeData(
                                UUID.fromString(id),
                                MagiculeNodeType.fromId(type),
                                x,
                                y
                        ));
                    }

                    if (n.contains("Parameters")) {
                        CompoundTag paramsTag = n.getCompoundOrEmpty("Parameters");
                        Map<String, Object> params = new HashMap<>();
                        for (String key : paramsTag.keySet()) {
                            Tag rawTag = paramsTag.get(key);
                            Object val = fromNbtTag(rawTag);
                            if (val != null) {
                                params.put(key, val);
                            }
                        }
                        this.nodeParameters.put(UUID.fromString(id), params);
                    }
                }
            }
        }

        Optional<ListTag> wiresTag = tag.getList("Wires");
        if(wiresTag.isPresent()){
            for(int i = 0; i < wiresTag.get().size(); i++){
                Optional<CompoundTag> wTag = wiresTag.get().getCompound(i);
                if(wTag.isPresent()){
                    CompoundTag w = wTag.get();

                    String sourceId = w.getString("SourceId").orElse("");
                    int sourcePort = w.getInt("SourcePort").orElse(0);
                    String targetId = w.getString("TargetId").orElse("");
                    int targetPort = w.getInt("TargetPort").orElse(0);
                    boolean isDataFlow = w.getBoolean("IsDataFlow").orElse(false);

                    if(!sourceId.isEmpty() && !targetId.isEmpty()){
                        this.wires.add(new WireData(
                                UUID.fromString(sourceId),
                                sourcePort,
                                UUID.fromString(targetId),
                                targetPort,
                                isDataFlow
                        ));
                    }
                }
            }
        }
    }

    // JavaのObject -> NBT Tagへの変換
    private Tag toNbtTag(Object value) {
        if (value == null) return null;
        return switch (value) {
            case Boolean b -> ByteTag.valueOf(b); // BooleanはByte(1/0)に変換
            case Double d -> DoubleTag.valueOf(d);
            case Integer i -> IntTag.valueOf(i);
            case Float f -> FloatTag.valueOf(f);
            case String s -> StringTag.valueOf(s);
            case Tag t -> t;
            default -> null;
        };
    }

    // NBT Tag -> JavaのObjectへの復元
    private Object fromNbtTag(Tag tag) {
        if (tag == null) return null;
        if (tag instanceof ByteTag byteTag) {
            System.out.println(byteTag.byteValue());
            return byteTag.byteValue() != 0; // Byte(1/0)をBooleanへ戻す
        } else if (tag instanceof DoubleTag doubleTag) {
            return doubleTag.doubleValue();
        } else if (tag instanceof IntTag intTag) {
            return intTag.intValue();
        } else if (tag instanceof FloatTag floatTag) {
            return floatTag.floatValue();
        } else if (tag instanceof StringTag stringTag) {
            return stringTag.toString();
        }
        return null;
    }

    /*
    ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
          内部データクラス
    ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
     */

    public static class NodeData{
        public final UUID id;
        public final MagiculeNodeType type;
        public int x, y;

        public NodeData(UUID id, MagiculeNodeType type, int x, int y){
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    public static class WireData {
        public final UUID sourceId;
        public final int sourcePortIndex;
        public final UUID targetId;
        public final int targetPortIndex;
        public final boolean isDataFlow;

        public WireData(UUID sourceId, int sourcePortIndex, UUID targetId, int targetPortIndex, boolean isDataFlow){
            this.sourceId = sourceId;
            this.sourcePortIndex = sourcePortIndex;
            this.targetId = targetId;
            this.targetPortIndex = targetPortIndex;
            this.isDataFlow = isDataFlow;
        }
    }
}