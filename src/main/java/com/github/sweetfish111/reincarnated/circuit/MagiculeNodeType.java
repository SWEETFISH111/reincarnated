package com.github.sweetfish111.reincarnated.circuit;

import java.util.ArrayList;
import java.util.List;

public enum MagiculeNodeType {
    EVENT_KEY_ONE("event_key_1", "execute_key_1",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.EXEC}
    ),
    ON_TICK("on_tick", "on tick",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.EXEC},
            List.of(EditorTab.SKILL)
    ),
    ON_XP_PICKUP("on_xp_pickup", "on xp pickup",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.EXEC, PortDataType.EXP},
            List.of(EditorTab.SKILL)
    ),
    ON_DAMAGE("on_damage", "on damage",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.EXEC},
            List.of(EditorTab.SKILL)
    ),
    SHOOT_PROJECTILE("shoot_projectile", "shoot projectile",
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE, PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE,PortDataType.ENTITY},
            ContentWidgetType.SWITCH,
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE, PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            1
    ),
    LIGHTNING("lightning", "lightning",
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE},
            new PortDataType[]{PortDataType.EXEC},
            null,null,
                    12
    ),
    EXPLOSION("explosion","explosion",
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC},
            ContentWidgetType.SWITCH,
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE, PortDataType.NUMBER},
            8
    ),
    DAMAGE("damage", "instant damage",
            new PortDataType[]{PortDataType.EXEC, PortDataType.ENTITY, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC},
            null, null,
            3
    ),
    HEALING("healing", "instant healing",
            new PortDataType[]{PortDataType.EXEC, PortDataType.ENTITY, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC},
            null, null,
            5
    ),
    ADD_MASO("add_maso", "add maso",
            new PortDataType[]{PortDataType.EXEC, PortDataType.MASO},
            new PortDataType[]{PortDataType.EXEC},
            List.of(EditorTab.SKILL)
    ),
    GET_LOOK_TARGET("get_look_target","look_target",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.ENTITY}
    ),
    CASTER_POS("caster_pos", "return caster",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.ENTITY},
            ContentWidgetType.SWITCH,
            new PortDataType[]{},
            1
    ),
    OFFSET("offset", "offset",
            new PortDataType[]{PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.VECTORE},
            ContentWidgetType.MODE_SELECT,
            new PortDataType[]{PortDataType.VECTORE,PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            5
    ),
    GET_LOOK_FORWARD("get_look_forward", "look forward",
            new PortDataType[]{PortDataType.NUMBER},
            new PortDataType[]{PortDataType.VECTORE},
            ContentWidgetType.SWITCH,
            null,
            5
    ),
    COMBERS_TARGET_POS("combers_target_pos", "target pos",
            new PortDataType[]{PortDataType.ENTITY},
            new PortDataType[]{PortDataType.VECTORE},
            null,null,
            5
    ),
    COMBERS_LOOK_DIRECTION("combers_look_direction", "direction",
            new PortDataType[]{PortDataType.ENTITY},
            new PortDataType[]{PortDataType.VECTORE}
    ),
    CONBERS_XP_TO_MASO("combers_xp_to_maso", "xp to maso",
            new PortDataType[]{PortDataType.EXP},
            new PortDataType[]{PortDataType.MASO},
            List.of(EditorTab.SKILL)
    ),
    IF("if", "IF",
            new PortDataType[]{PortDataType.EXEC,PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.EXEC, PortDataType.EXEC},
            null, null,
            20
    ),
    REPEAT("repeat", "repeat",
            new PortDataType[]{PortDataType.EXEC, PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC, PortDataType.NUMBER, PortDataType.EXEC},
            null,null,
            8
    ),
    TOGGLE("toggle", "toggle",
            new PortDataType[]{PortDataType.EXEC},
            new PortDataType[]{PortDataType.EXEC, PortDataType.BOOLEAN}
    ),
    DELAY("delay", "delay",
            new PortDataType[]{PortDataType.EXEC, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC}
    ),
    NUMBER("number", "number",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.NUMBER},
            ContentWidgetType.NUMBER_INPUT,
            null,
            1,
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    BOOLEAN("boolean", "boolean",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.BOOLEAN},
            ContentWidgetType.SWITCH,
            null,
            1,
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    ADD("add", "add",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    SUBTACT("subtract", "subtract",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    MULTIPLY("multiply", "multiply",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    DIVIDE("divide", "divide",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    MODULO("modulo", "modulo",
            new PortDataType[]{PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.NUMBER},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    EQUAL("equal", "equal",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    NOT("not", "not",
            new PortDataType[]{PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    OR("or", "or",
            new PortDataType[]{PortDataType.BOOLEAN, PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    AND("and", "and",
            new PortDataType[]{PortDataType.BOOLEAN, PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    GREATER_THAN("greater_than", "greater than",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    GRATER_OR_EQUAL("greater_or_equal", "greater or equal",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    LESS_THAN("less_than", "less than",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    LESS_OR_EQUAL("less_or_equal", "less or equal",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    COMPOUND("compound", "塊",
            new PortDataType[]{},
            new PortDataType[]{},
            List.of()
    ),
    INPUT_PROXY("input_proxy", "input proxy",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.ANY},
            ContentWidgetType.NUMBER_INPUT,
            null,
            1,
            List.of(EditorTab.MAGIC, EditorTab.SKILL, EditorTab.ARTS)
    ),
    OUTPUT_PROXY("output_proxy", "output proxy",
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
