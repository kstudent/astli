package org.androidlibid.proto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.androidlibid.proto.NodeType;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FeatureGenerator {
    
    public List<List<NodeType>> generateHorizontalFeatures() {
        List<List<NodeType>> horizontalFeatures = new ArrayList<>(); 
        for (int level = 1; level <= 2; level++) {
            for(NodeType type0 : filterByLevelAndLeaf(level, 1)) {
                for(NodeType type1 : filterByLevelAndLeaf(level, 1)) {
                   List<NodeType> feature = new ArrayList<>();
                   feature.add(type0);
                   feature.add(type1);
                   horizontalFeatures.add(feature);
                }
            }
        }
        return horizontalFeatures;
    }
    
    public List<List<NodeType>> generateVerticalFeatures() {
    
        List<List<NodeType>> verticalFeatures = new ArrayList<>(); 
        LinkedList<NodeType> feature;
                
        for(NodeType type : NodeType.values()) {
            feature = new LinkedList<>();
            feature.add(type);
            verticalFeatures.add(feature);
        }
            
        for(NodeType type0 : filterByLevel(0)) {
            
            if (type0.isLeaf()) continue;
            
            for(NodeType type1 : filterByLevel(1)) {
                
//                feature = new LinkedList<>();
//                feature.add(type0);
//                feature.add(type1);
//                verticalFeatures.add(feature);
                
                if (type1.isLeaf()) continue;
                
                for(NodeType type2 : filterByLevel(2)) {
                    feature = new LinkedList<>();
                    feature.add(type1);
                    feature.add(type2);
                    verticalFeatures.add(feature);

//                    feature = new LinkedList<>(feature);
//                    feature.addFirst(type0);
//                    verticalFeatures.add(feature);
                }
            }
        }
        
        return verticalFeatures;
        
    }

    private List<NodeType> filterByLevel(int level) {
        return filterByLevelAndLeaf(level, -1);
    }
    
    private List<NodeType> filterByLevelAndLeaf(int level, int isLeaf) {
        List<NodeType> list = new ArrayList<>();
        
        for(NodeType type : NodeType.values()) {
            if( type.getLevel() == level && 
                (
                    (isLeaf == 0 && !type.isLeaf()) || 
                    (isLeaf == 1 && type.isLeaf()) || 
                    (isLeaf == -1)
                )
            ) {
                list.add(type);
            }
        }
        
        return list;
        
    }
    
}
