/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import org.androidlibid.proto.ast.NodeType;
import org.androidlibid.proto.ast.FeatureGenerator;
import java.util.Arrays;
import java.util.List;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;
import org.androidlibid.proto.ao.ClassEntity;

public class Fingerprint {

    private String name;
    private Vector vector;

    public Fingerprint(ClassEntity entity) {
        this.vector = BasicVector.fromBinary(entity.getVector());
        this.name   = entity.getClassName();
    }
    
    private static final List<List<NodeType>> FEATURES;
    private static final int LONGEST_FEATURE;
    private static final FeatureGenerator FEATURE_GENERATOR;
    
    static {
        FEATURE_GENERATOR = new FeatureGenerator();
        FEATURES = FEATURE_GENERATOR.generateVerticalFeatures();
        FEATURES.addAll(FEATURE_GENERATOR.generateHorizontalFeatures());
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

    public Fingerprint() {
        vector = new BasicVector(FEATURES.size());
    } 
    
    public void incrementFeature(NodeType... dimension) {
        Fingerprint.this.incrementFeature(Arrays.asList(dimension));
    }

    public void incrementFeature(List<NodeType> dimension) {
        int index = FEATURES.indexOf(dimension);
        if(index == -1) {
            throw new IllegalArgumentException("Dimension not found");
        }
        vector.set(index, vector.get(index) + 1);
    }
    
    public double getFeatureCount(NodeType... feature) {
        return Fingerprint.this.getFeatureCount(Arrays.asList(feature));
    }
    
    public double getFeatureCount(List<NodeType> feature) {
        int index = FEATURES.indexOf(feature);
        if(index == -1) {
            throw new IllegalArgumentException("Dimension not found");
        }
        return vector.get(index);
    }

    public void add(Fingerprint that) {
        this.vector = this.vector.add(that.vector);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(name).append(":\n");
        
        int numEntries = Math.min(FEATURES.size(), vector.length());
        
        for (int i = 0; i < numEntries; i++) {
            if (vector.get(i) != 0.0) {
                List<NodeType> feature = FEATURES.get(i);
                String featureString = String.format("%-" + LONGEST_FEATURE + "s", feature.toString());
                string = string.append(featureString).append(" : ").append(vector.get(i)).append("\n");
            }
        }
        return string.toString();
    }
    
    public double euclideanDiff(Fingerprint that) {
        return vector.subtract(that.vector).euclideanNorm();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }
    
    public double euclideanNorm() {
        return this.vector.euclideanNorm();
    }
}
