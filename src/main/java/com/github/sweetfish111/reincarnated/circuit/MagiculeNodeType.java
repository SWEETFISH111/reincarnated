package com.github.sweetfish111.reincarnated.circuit;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public enum MagiculeNodeType {
    EVENT_KEY_ONE("event_key_1", Component.translatable("node.reincarnated.event_key_1").getString(),
            new PortDataType[]{},
            new PortDataType[]{PortDataType.EXEC},
            List.of(EditorTab.MAGIC)
    ),
    ON_TICK("on_tick", Component.translatable("node.reincarnated.on_tick").getString(),
            new PortDataType[]{},
            new PortDataType[]{PortDataType.EXEC},
            List.of(EditorTab.SKILL)
    ),
    ON_XP_PICKUP("on_xp_pickup", Component.translatable("node.reincarnated.on_xp_pickup").getString(),
            new PortDataType[]{},
            new PortDataType[]{PortDataType.EXEC, PortDataType.EXP},
            List.of(EditorTab.SKILL)
    ),
    ON_DAMAGE("on_damage", Component.translatable("node.reincarnated.on_damage").getString(),
            new PortDataType[]{},
            new PortDataType[]{PortDataType.EXEC},
            List.of(EditorTab.SKILL)
    ),
    DIG("dig", Component.translatable("node.reincarnated.dig").getString(),
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE},
            new PortDataType[]{PortDataType.EXEC},
            List.of(EditorTab.MAGIC)
    ),
    SHOOT_PROJECTILE("shoot_projectile", Component.translatable("node.reincarnated.shoot_projectile").getString(),
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE, PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE,PortDataType.ENTITY},
            ContentWidgetType.SWITCH,
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE, PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            1
    ),
    LIGHTNING("lightning", Component.translatable("node.reincarnated.lightning").getString(),
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE},
            new PortDataType[]{PortDataType.EXEC},
            null,null,
                    12
    ),
    EXPLOSION("explosion",Component.translatable("node.reincarnated.explosion").getString(),
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC},
            ContentWidgetType.SWITCH,
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE, PortDataType.NUMBER},
            8
    ),
    DAMAGE("damage", Component.translatable("node.reincarnated.damage").getString(),
            new PortDataType[]{PortDataType.EXEC, PortDataType.ENTITY, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC},
            null, null,
            3
    ),
    HEALING("healing", Component.translatable("node.reincarnated.healing").getString(),
            new PortDataType[]{PortDataType.EXEC, PortDataType.ENTITY, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC},
            null, null,
            5
    ),
    ADD_MASO("add_maso", Component.translatable("node.reincarnated.add_maso").getString(),
            new PortDataType[]{PortDataType.EXEC, PortDataType.MASO},
            new PortDataType[]{PortDataType.EXEC},
            List.of(EditorTab.SKILL)
    ),
    GET_LOOK_TARGET("get_look_target",Component.translatable("node.reincarnated.get_look_target").getString(),
            new PortDataType[]{},
            new PortDataType[]{PortDataType.ENTITY}
    ),
    RETURN_CASTER("return_caster", Component.translatable("node.reincarnated.return_caster").getString(),
            new PortDataType[]{},
            new PortDataType[]{PortDataType.ENTITY},
            ContentWidgetType.SWITCH,
            new PortDataType[]{},
            1
    ),
    OFFSET("offset", Component.translatable("node.reincarnated.offset").getString(),
            new PortDataType[]{PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.VECTORE},
            ContentWidgetType.MODE_SELECT,
            new PortDataType[]{PortDataType.VECTORE,PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            5
    ),
    GET_LOOK_FORWARD("get_look_forward", Component.translatable("node.reincarnated.get_look_forward").getString(),
            new PortDataType[]{PortDataType.NUMBER},
            new PortDataType[]{PortDataType.VECTORE},
            ContentWidgetType.SWITCH,
            new PortDataType[]{PortDataType.NUMBER},
            5
    ),
    COMBERS_TARGET_POS("combers_target_pos", Component.translatable("node.reincarnated.combers_target_pos").getString(),
            new PortDataType[]{PortDataType.ENTITY},
            new PortDataType[]{PortDataType.VECTORE},
            null,null,
            5
    ),
    COMBERS_LOOK_DIRECTION("combers_look_direction", Component.translatable("node.reincarnated.combers_look_direction").getString(),
            new PortDataType[]{PortDataType.ENTITY},
            new PortDataType[]{PortDataType.VECTORE}
    ),
    CONBERS_XP_TO_MASO("combers_xp_to_maso", Component.translatable("node.reincarnated.combers_xp_to_maso").getString(),
            new PortDataType[]{PortDataType.EXP},
            new PortDataType[]{PortDataType.MASO},
            List.of(EditorTab.SKILL)
    ),
    IF("if", Component.translatable("node.reincarnated.if").getString(),
            new PortDataType[]{PortDataType.EXEC,PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.EXEC, PortDataType.EXEC},
            null, null,
            20
    ),
    REPEAT("repeat", Component.translatable("node.reincarnated.repeat").getString(),
            new PortDataType[]{PortDataType.EXEC, PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC, PortDataType.NUMBER, PortDataType.EXEC},
            null,null,
            8
    ),
    TOGGLE("toggle", Component.translatable("node.reincarnated.toggle").getString(),
            new PortDataType[]{PortDataType.EXEC},
            new PortDataType[]{PortDataType.EXEC, PortDataType.BOOLEAN}
    ),
    DELAY("delay", Component.translatable("node.reincarnated.delay").getString(),
            new PortDataType[]{PortDataType.EXEC, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC}
    ),
    WHILE("while", Component.translatable(  "node.reincarnated.while").getString(),
            new PortDataType[]{PortDataType.EXEC,PortDataType.BOOLEAN, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC},
            List.of(EditorTab.MAGIC)
    ),
    NUMBER("number", Component.translatable("node.reincarnated.number").getString(),
            new PortDataType[]{},
            new PortDataType[]{PortDataType.NUMBER},
            ContentWidgetType.NUMBER_INPUT,
            null,
            1,
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    BOOLEAN("boolean", Component.translatable("node.reincarnated.boolean").getString(),
            new PortDataType[]{},
            new PortDataType[]{PortDataType.BOOLEAN},
            ContentWidgetType.SWITCH,
            null,
            1,
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    ADD("add", Component.translatable("node.reincarnated.add").getString(),
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    SUBTACT("subtract", Component.translatable("node.reincarnated.subtract").getString(),
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    MULTIPLY("multiply", Component.translatable("node.reincarnated.multiply").getString(),
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    DIVIDE("divide", Component.translatable("node.reincarnated.divide").getString(),
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    MODULO("modulo", Component.translatable("node.reincarnated.modulo").getString(),
            new PortDataType[]{PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.NUMBER},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    EQUAL("equal", Component.translatable("node.reincarnated.equal").getString(),
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    NOT("not", Component.translatable("node.reincarnated.not").getString(),
            new PortDataType[]{PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    OR("or", Component.translatable("node.reincarnated.or").getString(),
            new PortDataType[]{PortDataType.BOOLEAN, PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    AND("and", Component.translatable("node.reincarnated.and").getString(),
            new PortDataType[]{PortDataType.BOOLEAN, PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    GREATER_THAN("greater_than", Component.translatable("node.reincarnated.greater_than").getString(),
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    GRATER_OR_EQUAL("greater_or_equal", Component.translatable("node.reincarnated.greater_or_equal").getString(),
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    LESS_THAN("less_than", Component.translatable("node.reincarnated.less_than").getString(),
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    LESS_OR_EQUAL("less_or_equal", Component.translatable("node.reincarnated.less_or_equal").getString(),
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    COMPOUND("compound", Component.translatable("node.reincarnated.compound").getString(),
            new PortDataType[]{},
            new PortDataType[]{},
            List.of()
    ),
    INPUT_PROXY("input_proxy", Component.translatable("node.reincarnated.input_proxy").getString(),
            new PortDataType[]{},
            new PortDataType[]{PortDataType.ANY},
            ContentWidgetType.NUMBER_INPUT,
            null,
            1,
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    OUTPUT_PROXY("output_proxy", Component.translatable("node.reincarnated.output_proxy").getString(),
            new PortDataType[]{PortDataType.ANY},
            new PortDataType[]{},
            ContentWidgetType.NUMBER_INPUT,
            null,
            1,
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    );

    private final String id;
    public final String displayName;
    private ContentWidgetType content;
    public final PortDataType[] inputs;
    public PortDataType[] anotherInputs;
    public final PortDataType[] outputs;
    private int castCost;
    private List<EditorTab> targetTab = new ArrayList<>();

    MagiculeNodeType(String id, String displayName, PortDataType[] inputs, PortDataType[] outputs){
        this.id = id;
        this.displayName = displayName;
        this.inputs = inputs;
        this.outputs = outputs;
        this.content = ContentWidgetType.NONE;
        this.castCost = 1;
        this.targetTab.add(EditorTab.MAGIC);
    }

    MagiculeNodeType(String id, String displayName, PortDataType[] inputs, PortDataType[] outputs, List<EditorTab> targetTab){
        this.id = id;
        this.displayName = displayName;
        this.inputs = inputs;
        this.outputs = outputs;
        this.content = ContentWidgetType.NONE;
        this.castCost = 1;
        this.targetTab = targetTab;
    }

    MagiculeNodeType(String id, String displayName, PortDataType[] inputs, PortDataType[] outputs, ContentWidgetType content, PortDataType[] anotherInputs, int castCost){
        this(id, displayName, inputs, outputs);
        this.content = (content != null) ? content : ContentWidgetType.NONE;
        this.castCost = castCost;
        this.anotherInputs = anotherInputs;
        this.targetTab.add(EditorTab.MAGIC);
    }

    MagiculeNodeType(String id, String displayName, PortDataType[] inputs, PortDataType[] outputs, ContentWidgetType content, PortDataType[] anotherInputs, int castCost, List<EditorTab> targetTab){
        this(id, displayName, inputs, outputs);
        this.content = (content != null) ? content : ContentWidgetType.NONE;
        this.castCost = castCost;
        this.anotherInputs = anotherInputs;
        this.targetTab = targetTab;
    }

    public String getId(){
        return this.id;
    }
    public ContentWidgetType getContent(){
        return this.content != null ? this.content : ContentWidgetType.NONE;
    }

    public String getDisplayName(){return this.displayName;}

    public static MagiculeNodeType fromId(String id){
        for (MagiculeNodeType type : values()){
            if(type.id.equals(id)){
                return type;
            }
        }
        return null;
    }

    public int getCastCost(){return castCost;}

    public boolean isAvailableFor(EditorTab currentTab){
        return this.targetTab.contains(currentTab);
    }
}
