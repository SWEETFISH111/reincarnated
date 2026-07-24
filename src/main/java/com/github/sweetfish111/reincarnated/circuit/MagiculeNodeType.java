package com.github.sweetfish111.reincarnated.circuit;

import com.github.sweetfish111.reincarnated.client.screen.MagicEditorScreen;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;

public enum MagiculeNodeType {
    EVENT_KEY_ONE("event_key_1", "実行キー１",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.EXEC}
    ),
    LIGHTNING("lightning", "雷ノード",
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE},
            new PortDataType[]{PortDataType.EXEC},
            null,null,
                    12,
            MagicEditorScreen.EditorTab.MAGIC
    ),
    EXPLOSION("explosion","爆発ノード",
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC},
            null,null,
            8,
            MagicEditorScreen.EditorTab.MAGIC
    ),
    GET_LOOK_TARGET("get_look_target","見た相手",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.ENTITY}
    ),
    CASTER_POS("caster_pos", "発動者",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.ENTITY}
    ),
    OFFSET("offset", "オフセット",
            new PortDataType[]{PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.VECTORE},
            ContentWidgetType.MODE_SELECT,
            new PortDataType[]{PortDataType.VECTORE,PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER, PortDataType.NUMBER},
            5,
            MagicEditorScreen.EditorTab.MAGIC
    ),
    GET_LOOK_FORWARD("get_look_forward", "視線の方向",
            new PortDataType[]{PortDataType.NUMBER},
            new PortDataType[]{PortDataType.VECTORE},
            ContentWidgetType.SWITCH,
            null,
            5,
            MagicEditorScreen.EditorTab.MAGIC
    ),
    NUMBER("number", "数字",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.NUMBER},
            ContentWidgetType.NUMBER_INPUT,
            null,
            1,
            null
    ),
    COMBERS_TARGET_POS("combers_target_pos", "座標変換",
            new PortDataType[]{PortDataType.ENTITY},
            new PortDataType[]{PortDataType.VECTORE},
            null,null,
            5,
            MagicEditorScreen.EditorTab.MAGIC
    ),
    COMBERS_LOOK_DIRECTION("combers_look_direction", "向き",
            new PortDataType[]{PortDataType.ENTITY},
            new PortDataType[]{PortDataType.VECTORE}
    ),
    IF("if", "IF",
            new PortDataType[]{PortDataType.EXEC,PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.EXEC, PortDataType.EXEC},
            null, null,
            20,
            MagicEditorScreen.EditorTab.MAGIC
    ),
    BOOLEAN("boolean", "真偽値",
            new PortDataType[]{},
            new PortDataType[]{PortDataType.BOOLEAN},
            ContentWidgetType.SWITCH,
            null,
            1,
            null
    ),
    REPEAT("repeat", "繰り返し",
            new PortDataType[]{PortDataType.EXEC, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC, PortDataType.NUMBER, PortDataType.EXEC},
            null,null,
            8,
            MagicEditorScreen.EditorTab.MAGIC
    ),
    ADD("add", "足し算",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            null
    ),
    SUBTACT("subtract", "引き算",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            null
    ),
    MULTIPLY("multiply", "掛け算",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            null
    ),
    DIVIDE("divide", "割り算",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.ANY},
            null
    ),
    MODULO("modulo", "剰余",
            new PortDataType[]{PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.NUMBER},
            null
    ),
    EQUAL("equal", "等しい",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            null
    ),
    NOT("not", "否定",
            new PortDataType[]{PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.BOOLEAN},
            null
    ),
    OR("or", "または",
            new PortDataType[]{PortDataType.BOOLEAN, PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.BOOLEAN},
            null
    ),
    AND("and", "かつ",
            new PortDataType[]{PortDataType.BOOLEAN, PortDataType.BOOLEAN},
            new PortDataType[]{PortDataType.BOOLEAN},
            null
    ),
    GREATER_THAN("greater_than", "より大きい",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            null
    ),
    GRATER_OR_EQUAL("greater_or_equal", "以上",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            null
    ),
    LESS_THAN("less_than", "より小さい",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            null
    ),
    LESS_OR_EQUAL("less_or_equal", "以下",
            new PortDataType[]{PortDataType.ANY, PortDataType.ANY},
            new PortDataType[]{PortDataType.BOOLEAN},
            null
    ),
    SHOOT_PROJECTILE("shoot_projectile", "発射",
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE, PortDataType.VECTORE, PortDataType.NUMBER, PortDataType.NUMBER},
            new PortDataType[]{PortDataType.EXEC, PortDataType.VECTORE,PortDataType.ENTITY}
    );

    private final String id;
    public final String displayName;
    private ContentWidgetType content;
    public final PortDataType[] inputs;
    public PortDataType[] anotherInputs;
    public final PortDataType[] outputs;
    private int castCost;
    private MagicEditorScreen.EditorTab targetTab;

    MagiculeNodeType(String id, String displayName, PortDataType[] inputs, PortDataType[] outputs){
        this.id = id;
        this.displayName = displayName;
        this.inputs = inputs;
        this.outputs = outputs;
        this.content = ContentWidgetType.NONE;
        this.castCost = 1;
        this.targetTab = MagicEditorScreen.EditorTab.MAGIC;
    }

    MagiculeNodeType(String id, String displayName, PortDataType[] inputs, PortDataType[] outputs, MagicEditorScreen.EditorTab targetTab){
        this.id = id;
        this.displayName = displayName;
        this.inputs = inputs;
        this.outputs = outputs;
        this.content = ContentWidgetType.NONE;
        this.castCost = 1;
        this.targetTab = targetTab;
    }

    MagiculeNodeType(String id, String displayName, PortDataType[] inputs, PortDataType[] outputs, ContentWidgetType content, PortDataType[] anotherInputs, int castCost, MagicEditorScreen.EditorTab targetTab){
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

    public static MagiculeNodeType fromId(String id){
        for (MagiculeNodeType type : values()){
            if(type.id.equals(id)){
                return type;
            }
        }
        return null;
    }

    public int getCastCost(){return castCost;}

    public boolean isAvailableFor(MagicEditorScreen.EditorTab currentTab){
        return this.targetTab == null || this.targetTab == currentTab;
    }
}
