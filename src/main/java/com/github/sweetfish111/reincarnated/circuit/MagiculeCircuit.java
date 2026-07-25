package com.github.sweetfish111.reincarnated.circuit;

import com.github.sweetfish111.reincarnated.client.screen.NodePort;
import com.github.sweetfish111.reincarnated.magic.nodes.AbstractMagicNode;
import com.github.sweetfish111.reincarnated.magic.nodes.MagicNode;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.w3c.dom.Node;

import java.util.*;

public class MagiculeCircuit {
    private final List<NodeData> nodes = new ArrayList<>();
    private final List<CompoundNodeData> compoundNodes = new ArrayList<>();
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
    public void setCompoundNodes(List<CompoundNodeData> nodes){
        this.compoundNodes.clear();
        this.compoundNodes.addAll(nodes);
    }

    // ゲッター
    public List<NodeData> getNodes(){ return this.nodes; }
    public List<WireData> getWires(){ return this.wires; }
    public NodeData getNode(UUID nodeId){
        if(nodeId == null) return null;
        for (NodeData node : this.nodes){
            if(node.id.equals(nodeId)){
                return node;
            }
        }
        return null;
    }


    public Object getNodeParam(UUID nodeId, String key, Object defaultValue){
        if(nodeParameters.containsKey(nodeId)){
            return nodeParameters.get(nodeId).getOrDefault(key, defaultValue);
        }
        return defaultValue;
    }

    public List<CompoundNodeData> getCompoundNodes(){return this.compoundNodes;}

    public void collapseNodes(List<UUID> targetNodeIds, String customName){
        if(targetNodeIds.isEmpty())return;

        List<NodeData> innerNodes = new ArrayList<>();
        List<WireData> innerWires = new ArrayList<>();

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for(UUID id : targetNodeIds){
            NodeData node  = getNode(id);
            if(node != null){
                innerNodes.add(node);
                minX = Math.min(minX, node.x);
                minY = Math.min(minY, node.y);
            }
        }

        if(innerNodes.isEmpty()) return;

        Set<UUID> idSet = new HashSet<>(targetNodeIds);
        Iterator<WireData> wireIterator = this.wires.iterator();
        while(wireIterator.hasNext()){
            WireData wire = wireIterator.next();
            if(idSet.contains(wire.sourceId) && idSet.contains(wire.targetId)){
                innerWires.add(wire);
                wireIterator.remove();
            }
        }

        for(UUID id : targetNodeIds){
            removeNodeAndWires(id);
        }

        UUID newCompoundId = UUID.randomUUID();
        CompoundNodeData compoundNode = new CompoundNodeData(
                newCompoundId,
                customName,
                innerNodes,
                innerWires,
                minX,minY
        );

        this.compoundNodes.add(compoundNode);
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

        ListTag compoundNodesTag = new ListTag();
        for(CompoundNodeData cNode : this.compoundNodes){
            CompoundTag cnTag = new CompoundTag();
            cnTag.putString("Id", cNode.id.toString());
            cnTag.putString("Name", cNode.customName);
            cnTag.putInt("X", cNode.x);
            cnTag.putInt("Y", cNode.y);

            ListTag innerNodeTag = new ListTag();
            for(NodeData innerNode : cNode.innerNodes){
                CompoundTag inTag = new CompoundTag();
                inTag.putString("Id", innerNode.id.toString());
                inTag.putString("Type", innerNode.type.getId());
                inTag.putInt("X", innerNode.x);
                inTag.putInt("Y", innerNode.y);

                Map<String, Object> params = this.nodeParameters.get(innerNode.id);
                if(params != null && !params.isEmpty()){
                    CompoundTag paramsTag = new CompoundTag();
                    for(Map.Entry<String, Object> entry : params.entrySet()){
                        Tag convertedTag = toNbtTag(entry.getValue());
                        if (convertedTag != null) {
                            paramsTag.put(entry.getKey(), convertedTag);
                        }
                    }
                    inTag.put("Parameters", paramsTag);
                }
                innerNodeTag.add(inTag);
            }
            cnTag.put("InnerNodes", innerNodeTag);

            ListTag innerWireTag = new ListTag();
            for(WireData innerWire : cNode.innerWires){
                CompoundTag iwTag = new CompoundTag();
                iwTag.putString("SourceId", innerWire.sourceId.toString());
                iwTag.putInt("SourcePort", innerWire.sourcePortIndex);
                iwTag.putString("TargetId", innerWire.targetId.toString());
                iwTag.putInt("TargetPort", innerWire.targetPortIndex);
                iwTag.putBoolean("IsDataFlow", innerWire.isDataFlow);
                innerWireTag.add(iwTag);
            }
            cnTag.put("InnerWires", innerWireTag);
            compoundNodesTag.add(cnTag);
        }
        tag.put("CompoundNodes", compoundNodesTag);

        return tag;
    }

    public void loadFromNBT(CompoundTag tag){
        this.nodes.clear();
        this.wires.clear();
        this.compoundNodes.clear();
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

        Optional<ListTag> compoundTag = tag.getList("CompoundNodes");
        if(compoundTag.isPresent()){
            for(int i = 0; i < compoundTag.get().size(); i++){
                Optional<CompoundTag> cTag = compoundTag.get().getCompound(i);
                if(cTag.isPresent()){
                    CompoundTag c = cTag.get();

                    String id = c.getString("Id").orElse("");
                    String customName = c.getString("Name").orElse("");
                    int x = c.getInt("X").orElse(0);
                    int y = c.getInt("Y").orElse(0);
                    List<MagiculeCircuit.NodeData> innerNodes = new ArrayList<>();
                    List<MagiculeCircuit.WireData> innerWires = new ArrayList<>();

                    Optional<ListTag> innerNodesTag = c.getList("InnerNodes");
                    if(innerNodesTag.isPresent()){
                        for(int n = 0; n < innerNodesTag.get().size(); n++){
                            Optional<CompoundTag> inTag = innerNodesTag.get().getCompound(n);
                            if(inTag.isPresent()){
                                CompoundTag in = inTag.get();

                                String inId = in.getString("Id").orElse("");
                                String inType = in.getString("Type").orElse("");
                                int inX = in.getInt("X").orElse(0);
                                int inY = in.getInt("Y").orElse(0);
                                if(!inId.isEmpty() && !inType.isEmpty()){
                                    innerNodes.add(new NodeData(
                                            UUID.fromString(inId),
                                            MagiculeNodeType.fromId(inType),
                                            inX,
                                            inY
                                    ));
                                }

                                if(in.contains("Parameters")){
                                    CompoundTag paramsTag = in.getCompoundOrEmpty("Parameters");
                                    Map<String, Object> params = new HashMap<>();
                                    for(String key : paramsTag.keySet()){
                                        Tag rawTag = paramsTag.get(key);
                                        Object val = fromNbtTag(rawTag);
                                        if(val != null){
                                            params.put(key, val);
                                        }
                                    }
                                    this.nodeParameters.put(UUID.fromString(inId), params);
                                }
                            }
                        }
                    }
                    Optional<ListTag> innerWiresTag = c.getList("InnerWires");
                    if(innerWiresTag.isPresent()){
                        for(int p = 0; p < innerWiresTag.get().size(); p++){
                            Optional<CompoundTag> wTag = innerWiresTag.get().getCompound(p);
                            if(wTag.isPresent()){
                                CompoundTag w = wTag.get();

                                String sourceId = w.getString("SourceId").orElse("");
                                int sourcePort = w.getInt("SourcePort").orElse(0);
                                String targetId = w.getString("TargetId").orElse("");
                                int targetPort = w.getInt("TargetPort").orElse(0);
                                boolean isDataFlow = w.getBoolean("IsDataFlow").orElse(false);

                                if(!sourceId.isEmpty() && !targetId.isEmpty()){
                                    innerWires.add(new WireData(
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
                    if(!id.isEmpty()){
                        this.compoundNodes.add(new CompoundNodeData(
                                UUID.fromString(id),
                                customName,
                                innerNodes,
                                innerWires,
                                x,
                                y
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

    public static class CompoundNodeData{
        public final UUID id;
        public final String customName;
        public final List<MagiculeCircuit.NodeData> innerNodes;
        public final List<MagiculeCircuit.WireData> innerWires;
        public int x, y;

        public CompoundNodeData(UUID id, String customName, List<MagiculeCircuit.NodeData> innerNodes, List<MagiculeCircuit.WireData> innerWires, int x, int y){
            this.id = id;
            this.customName = customName;
            this.innerNodes = innerNodes;
            this.innerWires = innerWires;
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