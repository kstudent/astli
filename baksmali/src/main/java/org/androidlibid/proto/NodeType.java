/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import java.util.ArrayList;
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
    
    public static List<List<NodeType>> generateHorizontalFeatures() {
        List<List<NodeType>> horizontalFeatures = new ArrayList<>(); 
        for (int level = 0; level <= 2; level++) {
            for(NodeType type0 : filterByLevel(level)) {
                for(NodeType type1 : filterByLevel(level)) {
                   List<NodeType> feature = new ArrayList<>();
                   feature.add(type0);
                   feature.add(type1);
                   horizontalFeatures.add(feature);
                }
            }
        }
        return horizontalFeatures;
    }
    
    public static List<List<NodeType>> generateVerticalFeatures() {
    
        List<List<NodeType>> verticalFeatures = new ArrayList<>(); 
        
        for(NodeType type0 : filterByLevel(0)) {
            
            List<NodeType> feature = new ArrayList<>();
            feature.add(type0);
            verticalFeatures.add(feature);
            
            if (type0.isLeaf()) continue;
            
            for(NodeType type1 : filterByLevel(1)) {
                
                feature = new ArrayList<>();
                feature.add(type0);
                feature.add(type1);
                verticalFeatures.add(feature);
                
                if (type1.isLeaf()) continue;
                
                for(NodeType type2 : filterByLevel(2)) {
                    feature = new ArrayList<>();
                    feature.add(type0);
                    feature.add(type1);
                    feature.add(type2);
                    verticalFeatures.add(feature);
                }
            }
        }
        
        return verticalFeatures;
        
    }

    private static List<NodeType> filterByLevel(int level) {
        List<NodeType> list = new ArrayList<>();
        
        for(NodeType type : NodeType.values()) {
            if(type.getLevel() == level) {
                list.add(type);
            }
        }
        
        return list;
        
    }
    
}
