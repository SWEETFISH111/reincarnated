package com.github.sweetfish111.reincarnated.circuit;

public enum PortDataType {
    EXEC(0XFFFFFFFF),
    NUMBER(0XFF00AAFF),
    VECTORE(0XFF55FF55),
    ENTITY(0XFFFF5555),
    BOOLEAN(0xCFF77EB5);

    public final int color;

    PortDataType(int color){
        this.color = color;
    }
}

