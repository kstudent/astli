/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author chri
 */
public enum NodeType {
    METHOD("method", 0, false),
    VIRTUAL("virtual", 1, false),
    DIRECT("direct", 1, false), 
    ARGUMENT("argument", 1, true),
    PARAMETER("parameter", 2, true),
    LOCAL("local", 2, true);
    
    private final String name;
    private final int level;
    private final boolean isLeaf;

    private NodeType(String name, int level, boolean isLeaf) {
        this.name  = name;
        this.level = level;
        this.isLeaf = isLeaf;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public boolean isLeaf() {
        return isLeaf;
    }
}
