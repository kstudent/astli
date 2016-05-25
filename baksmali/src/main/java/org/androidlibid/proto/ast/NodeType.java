package org.androidlibid.proto.ast;

/**
 *
 * @author chri
 */
public enum NodeType {
    METHOD("method"),
    VIRTUAL("virtual"),
    DIRECT("direct"), 
    SIGNATURE("signature"),
    PARAMETER("parameter"),
    LOCAL("local");
    
    private final String name;

    private NodeType(String name) {
        this.name  = name;
    }

    public String getName() {
        return name;
    }
}
