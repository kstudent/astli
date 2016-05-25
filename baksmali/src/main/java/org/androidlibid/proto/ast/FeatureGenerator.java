package org.androidlibid.proto.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FeatureGenerator {
    
    public List<List<NodeType>> generateFeatures() {
        List<List<NodeType>> features = new ArrayList<>();
        features.add(createFeature(NodeType.VIRTUAL));
        features.add(createFeature(NodeType.DIRECT));
        features.add(createFeature(NodeType.PARAMETER));
        features.add(createFeature(NodeType.LOCAL));
        features.add(createFeature(NodeType.VIRTUAL,   NodeType.PARAMETER));
        features.add(createFeature(NodeType.VIRTUAL,   NodeType.LOCAL));
        features.add(createFeature(NodeType.DIRECT,    NodeType.PARAMETER));
        features.add(createFeature(NodeType.DIRECT,    NodeType.LOCAL));
        features.add(createFeature(NodeType.LOCAL,     NodeType.LOCAL));
        features.add(createFeature(NodeType.LOCAL,     NodeType.PARAMETER));
        features.add(createFeature(NodeType.PARAMETER, NodeType.PARAMETER));
        return features;
    }
    
    private List<NodeType> createFeature(NodeType... features) {
        return Arrays.asList(features);
    } 
}
