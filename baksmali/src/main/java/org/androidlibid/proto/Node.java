/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Node {
    
    @Nullable private final List<Node> children;
    @Nonnull  private final NodeType type;

    public Node(NodeType type) {
        this.type = type;
        children = new LinkedList<>();
    }
    
    public NodeType getType() {
        return type;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    @Override
    public String toString() {
        return toString(0);
    }
    
    private String toString(@Nonnegative int level) {
        StringBuffer indentation = new StringBuffer();
        for(int i = 0; i < level; i++) {
            indentation.append("  ");
        }
        
        StringBuffer s = new StringBuffer(indentation.toString() + type.getName() + "\n");
        
        for (Node child : children) {
            s.append(child.toString(level + 1));
        }
        
        return s.toString(); 
    }
    
    
    
    
    
    
    
}
