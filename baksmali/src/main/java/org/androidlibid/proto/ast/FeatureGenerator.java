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
        features.add(createFeature(NodeType.VRT));
        features.add(createFeature(NodeType.DRC));
        features.add(createFeature(NodeType.PAR));
        features.add(createFeature(NodeType.LOC));
        features.add(createFeature(NodeType.VRT, NodeType.PAR));
        features.add(createFeature(NodeType.VRT, NodeType.LOC));
        features.add(createFeature(NodeType.DRC, NodeType.PAR));
        features.add(createFeature(NodeType.DRC, NodeType.LOC));
        features.add(createFeature(NodeType.LOC, NodeType.LOC));
        features.add(createFeature(NodeType.PAR, NodeType.PAR));
        return features;
    }
    
    private List<NodeType> createFeature(NodeType... features) {
        return Arrays.asList(features);
    } 
}
