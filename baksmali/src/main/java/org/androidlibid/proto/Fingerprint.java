/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import java.util.Arrays;
import java.util.List;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;

public class Fingerprint {

    private final Vector vector;
    private static final List<List<NodeType>> FEATURES;
    private static final int LONGEST_FEATURE;
    
    static {
        FEATURES = NodeType.generateVerticalFeatures();
        FEATURES.addAll(NodeType.generateHorizontalFeatures());
        int longestFeature = 0; 
        for(List<NodeType> feature : FEATURES){
            int featureLength = 0;
            for(NodeType type : feature) {
                featureLength += type.getName().length() + 2;
            }
            if(featureLength > longestFeature) {
                longestFeature = featureLength;
            }
        }
        LONGEST_FEATURE = longestFeature;
    }

    Fingerprint() {
        vector = new BasicVector(FEATURES.size());
    } 
    
    void incrementFeature(NodeType... dimension) {
        Fingerprint.this.incrementFeature(Arrays.asList(dimension));
    }

    void incrementFeature(List<NodeType> dimension) {
        int index = FEATURES.indexOf(dimension);
        if(index == -1) {
            throw new IllegalArgumentException("Dimension not found");
        }
        vector.set(index, vector.get(index) + 1);
    }
    
    double getFeatureCount(List<NodeType> feature) {
        int index = FEATURES.indexOf(feature);
        if(index == -1) {
            throw new IllegalArgumentException("Dimension not found");
        }
        return vector.get(index);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < FEATURES.size(); i++) {
            if (vector.get(i) != 0.0) {
                List<NodeType> feature = FEATURES.get(i);
                String featureString = String.format("%-" + LONGEST_FEATURE + "s", feature.toString());
                string = string.append(featureString).append(" : ").append(vector.get(i)).append("\n");
            }
        }
        return string.toString();
    }
    
    
}
