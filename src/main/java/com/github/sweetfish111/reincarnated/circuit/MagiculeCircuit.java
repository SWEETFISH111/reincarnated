package com.github.sweetfish111.reincarnated.circuit;

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
    private final List<CompoundNodeData> compoundNodes = new ArrayList<>();
    private final List<WireData> wires = new ArrayList<>();
    private Map<UUID, Map<String, Object>> nodeParameters = new HashMap<>();

    public MagiculeCircuit(){}

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
    public void setNodes(List<NodeData> nodes){
        this.nodes.clear();
        this.nodes.addAll(nodes);
    }
    public void setNodeParam(UUID nodeId, String key, Object value){
        nodeParameters.computeIfAbsent(nodeId, k -> new HashMap<>()).put(key, value);
    }
    public void setCompoundNodes(List<CompoundNodeData> nodes){
        this.compoundNodes.clear();
        this.compoundNodes.addAll(nodes);
    }
    public void setWires(List<WireData> wires){
        this.wires.clear();
        this.wires.addAll(wires);
    }
    public void setNodeParameters(Map<UUID, Map<String, Object>> nodeParameters){this.nodeParameters = nodeParameters;}

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
    public CompoundNodeData getCNode(UUID cNodeId){
        if(cNodeId == null) return null;
        for (CompoundNodeData node : this.compoundNodes){
            if(node.id.equals(cNodeId)){
                return node;
            }
        }
        return null;
    }
    public Object getNodeParam(UUID nodeId, String key, Object defaultValue){
        if(nodeParameters.containsKey(nodeId)){
            return nodeParameters.get(nodeId).getOrDefault(key, defaultValue);
        }
        Object foundInCompound = searchParamInCompounds(this.compoundNodes, nodeId, key);
        if(foundInCompound != null){
            return foundInCompound;
        }
        return defaultValue;
    }
    public Map<UUID, Map<String, Object>> getNodeParameters(){return this.nodeParameters;}
    private Object searchParamInCompounds(List<CompoundNodeData> compounds, UUID nodeId, String key){
        for(CompoundNodeData cNode : compounds){
            if(cNode.innerNodeParameters.containsKey(nodeId)){
                Map<String, Object> params = cNode.innerNodeParameters.get(nodeId);
                if(params.containsKey(key)){
                    return params.get(key);
                }
            }
            Object found = searchParamInCompounds(cNode.getCompoundCircuit().getCompoundNodes(), nodeId, key);
            if(found != null){
                return found;
            }
        }
        return null;
    }
    public List<CompoundNodeData> getCompoundNodes(){return this.compoundNodes;}

    public void collapseNodes(List<UUID> targetNodeIds, String customName){
        if(targetNodeIds.isEmpty())return;

        List<NodeData> innerNodes = new ArrayList<>();
        List<CompoundNodeData> innerCompoundNodes = new ArrayList<>();
        List<WireData> innerWires = new ArrayList<>();
        Map<UUID, Map<String, Object>> innerNodeParams = new HashMap<>();

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        Set<UUID> idSet = new HashSet<>(targetNodeIds);

        for(UUID id : targetNodeIds){
            NodeData node  = getNode(id);
            if(node != null){
                innerNodes.add(node);
                minX = Math.min(minX, node.x);
                minY = Math.min(minY, node.y);

                if(nodeParameters.containsKey(id)){
                    innerNodeParams.put(id, new HashMap<>(nodeParameters.get(id)));
                }
            }
        }

        Iterator<CompoundNodeData> compoundIterator = this.compoundNodes.iterator();
        while(compoundIterator.hasNext()){
            CompoundNodeData cNode = compoundIterator.next();
            if(idSet.contains(cNode.id)){
                innerCompoundNodes.add(cNode);
                minX = Math.min(minX, cNode.x);
                minY = Math.min(minY, cNode.y);
                compoundIterator.remove(); // 元の階層からは取り除く
            }
        }

        if(innerNodes.isEmpty() && innerCompoundNodes.isEmpty()) return;

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
                innerCompoundNodes,
                innerWires,
                minX,minY
        );

        compoundNode.innerNodeParameters.putAll(innerNodeParams);

        this.compoundNodes.add(compoundNode);
    }

    // 消す系
    public void removeWiresByPort(UUID nodeId, PortType portType, int portIndex){
        this.wires.removeIf(wire ->
                (portType == PortType.OUTPUT && wire.sourceId.equals(nodeId) && wire.sourcePortIndex == portIndex) ||
                        (portType == PortType.INPUT && wire.targetId.equals(nodeId) && wire.targetPortIndex == portIndex));
    }

    public void removeWiresByNode(UUID nodeId){
        this.wires.removeIf(wire -> wire.sourceId.equals(nodeId) || wire.targetId.equals(nodeId));
    }

    public void removeNodeAndWires(UUID nodeId){
        this.nodes.removeIf(node -> node.id.equals(nodeId));
        this.compoundNodes.removeIf(node -> node.id.equals(nodeId));
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
        for(CompoundNodeData cNode : this.compoundNodes) {
            compoundNodesTag.add(serializeCompoundNode(cNode));
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
                    CompoundNodeData cData = deserializeCompoundNode(cTag.get());
                    if(cData != null){
                        this.compoundNodes.add(cData);
                    }

                }

            }

        }

    }



    private CompoundTag serializeCompoundNode(CompoundNodeData cNode) {
        CompoundTag cnTag = new CompoundTag();
        cnTag.putString("Id", cNode.id.toString());
        cnTag.putString("Name", cNode.customName);
        cnTag.putInt("X", cNode.x);
        cnTag.putInt("Y", cNode.y);

        // 1. InnerNodes
        ListTag innerNodeTag = new ListTag();
        for(NodeData innerNode : cNode.getCompoundCircuit().getNodes()){
            CompoundTag inTag = new CompoundTag();
            inTag.putString("Id", innerNode.id.toString());
            inTag.putString("Type", innerNode.type.getId());
            inTag.putInt("X", innerNode.x);
            inTag.putInt("Y", innerNode.y);
            innerNodeTag.add(inTag);
        }
        cnTag.put("InnerNodes", innerNodeTag);

        // 2. InnerCompoundNodes (再帰的！)
        ListTag innerCompoundNodesTag = new ListTag();
        for(CompoundNodeData innerCompound : cNode.getCompoundCircuit().getCompoundNodes()){
            innerCompoundNodesTag.add(serializeCompoundNode(innerCompound));
        }
        cnTag.put("InnerCompoundNodes", innerCompoundNodesTag);

        // 3. InnerWires
        ListTag innerWireTag = new ListTag();
        for(WireData innerWire : cNode.getCompoundCircuit().getWires()){
            CompoundTag iwTag = new CompoundTag();
            iwTag.putString("SourceId", innerWire.sourceId.toString());
            iwTag.putInt("SourcePort", innerWire.sourcePortIndex);
            iwTag.putString("TargetId", innerWire.targetId.toString());
            iwTag.putInt("TargetPort", innerWire.targetPortIndex);
            iwTag.putBoolean("IsDataFlow", innerWire.isDataFlow);
            innerWireTag.add(iwTag);
        }
        cnTag.put("InnerWires", innerWireTag);

        // 4. InnerNodeParameters
        ListTag innerParamsTag = new ListTag();
        for(Map.Entry<UUID, Map<String, Object>> entry : cNode.innerNodeParameters.entrySet()){
            CompoundTag pEntryTag = new CompoundTag();
            pEntryTag.putString("NodeId", entry.getKey().toString());
            CompoundTag pTag = new CompoundTag();
            for(Map.Entry<String, Object> paramEntry : entry.getValue().entrySet()){
                Tag convertedTag = toNbtTag(paramEntry.getValue());
                if(convertedTag != null) {
                    pTag.put(paramEntry.getKey(), convertedTag);
                }
            }
            pEntryTag.put("Params", pTag);
            innerParamsTag.add(pEntryTag);
        }
        cnTag.put("InnerNodeParameters", innerParamsTag);

        return cnTag;
    }

    private CompoundNodeData deserializeCompoundNode(CompoundTag c){
        String id = c.getString("Id").orElse("");
        String customName = c.getString("Name").orElse("");
        int x = c.getInt("X").orElse(0);
        int y = c.getInt("Y").orElse(0);
        List<MagiculeCircuit.NodeData> innerNodes = new ArrayList<>();
        List<CompoundNodeData> innerCompoundNodes = new ArrayList<>();
        List<MagiculeCircuit.WireData> innerWires = new ArrayList<>();
        Map<UUID, Map<String, Object>> innerNodeParams = new HashMap<>();


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
                }
            }
        }

        Optional<ListTag> innerCompoundsTag = c.getList("InnerCompoundNodes");
        if(innerCompoundsTag.isPresent()){
            for(int ic = 0; ic < innerCompoundsTag.get().size(); ic++){
                Optional<CompoundTag> icTag = innerCompoundsTag.get().getCompound(ic);
                if(icTag.isPresent()){
                    innerCompoundNodes.add(deserializeCompoundNode(icTag.get()));
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

        Optional<ListTag> innerParamsTag = c.getList("InnerNodeParameters");
        if(innerParamsTag.isPresent()){
            for(int q = 0; q < innerParamsTag.get().size(); q++){
                Optional<CompoundTag> pEntryOpt = innerParamsTag.get().getCompound(q);
                if(pEntryOpt.isPresent()){
                    CompoundTag pEntry = pEntryOpt.get();
                    String nIdStr = pEntry.getString("NodeId").orElse("");
                    if(!nIdStr.isEmpty()){
                        UUID nId = UUID.fromString(nIdStr);
                        CompoundTag pTag = pEntry.getCompoundOrEmpty("Params");
                        Map<String, Object> params = new HashMap<>();
                        for(String key : pTag.keySet()){
                            Tag rawTag = pTag.get(key);
                            Object val = fromNbtTag(rawTag);
                            if(val != null){
                                params.put(key, val);
                            }
                        }
                        innerNodeParams.put(nId, params);

                    }
                }
            }
        }
        if(id.isEmpty())return null;
        CompoundNodeData compoundNode = new CompoundNodeData(
                UUID.fromString(id),
                customName,
                innerNodes,
                innerCompoundNodes,
                innerWires,
                x,
                y
        );
        compoundNode.innerNodeParameters.putAll(innerNodeParams);
        return compoundNode;
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
        MagiculeCircuit compoundCircuit = new MagiculeCircuit();
        public final Map<UUID, Map<String, Object>> innerNodeParameters = new HashMap<>();
        public int x, y;

        public CompoundNodeData(UUID id, String customName, List<MagiculeCircuit.NodeData> innerNodes,List<MagiculeCircuit.CompoundNodeData> innerCompoundNodes, List<MagiculeCircuit.WireData> innerWires, int x, int y){
            this.id = id;
            this.customName = customName;
            this.compoundCircuit.setNodes(innerNodes);
            this.compoundCircuit.setCompoundNodes(innerCompoundNodes);
            this.compoundCircuit.setWires(innerWires);
            this.x = x;
            this.y = y;
        }

        public MagiculeCircuit getCompoundCircuit(){return this.compoundCircuit;}
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