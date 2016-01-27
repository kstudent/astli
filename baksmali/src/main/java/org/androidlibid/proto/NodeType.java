/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

/**
 *
 * @author chri
 */
public enum NodeType {
    METHOD("method"),
    VIRTUAL("virtual"),
    DIRECT("direct"), 
    ARGUMENT("argument"),
    PARAMETER("parameter"),
    LOCAL("local");
    
    private final String name;

    private NodeType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
