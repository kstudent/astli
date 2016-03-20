package org.androidlibid.proto;

import org.androidlibid.proto.ast.NodeType;
import org.androidlibid.proto.ast.FeatureGenerator;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import org.androidlibid.proto.ao.VectorEntity;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;

public class Fingerprint {

    private String name;
    private Vector vector;
    private Fingerprint parent; 
    private List<Fingerprint> children = new LinkedList<>();
    private VectorEntity entity;
    private double inclusionScore; 

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
    

    public Fingerprint(VectorEntity entity) {
        byte[] byteVector = entity.getVector();
        String entityName = entity.getName();
        
        if(byteVector == null) {
            this.vector = new BasicVector(FEATURES.size());
        } else {
            this.vector = BasicVector.fromBinary(byteVector); 
        }
        
        if(entityName == null) {
            this.name = "";
        } else {
            this.name = entityName;
        }
        this.entity = entity;
    }
    
    public Fingerprint(String name) {
        vector = new BasicVector(FEATURES.size());
        this.name = name;
    }
    
    public Fingerprint() {
        this("");
    } 

    public static int getFeaturesSize() {
        return FEATURES.size();
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
    
    public double computeSimilarityScore(Fingerprint that) {
        double diff = vector.subtract(that.vector).euclideanNorm();
        double length = this.vector.euclideanNorm();
        if(length > 0) {
            double similarityScore = 1 - (diff / length); 
            return (similarityScore > 0)? similarityScore : 0;
        } else {
            throw new RuntimeException("Cant norm by 0 length vector");
        }
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
    
    public List<Fingerprint> getChildren() {
        return children;
    }

    public void addChild(Fingerprint child) {
        this.children.add(child);
        child.setParent(this);
    }

    public @Nullable VectorEntity getEntity() {
        return entity;
    }

    public @Nullable Fingerprint getParent() {
        return parent;
    }

    private void setParent(@Nullable Fingerprint parent) {
        this.parent = parent;
    }
    
    public double getInclusionScore() {
        return inclusionScore;
    }

    public void setInclusionScore(double score) {
        this.inclusionScore = score;
    }
    
}
