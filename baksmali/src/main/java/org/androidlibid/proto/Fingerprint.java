package org.androidlibid.proto;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.androidlibid.proto.ast.NodeType;
import org.androidlibid.proto.ast.FeatureGenerator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.androidlibid.proto.ao.Method;
import org.apache.commons.lang.NotImplementedException;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;

public class Fingerprint {

    private String name;
    private Vector vector;
    private String signature;

    private static final List<List<NodeType>> FEATURES;
    private static final int LONGEST_FEATURE_LENGTH;
    private static final NumberFormat FRMTR = new DecimalFormat("#0.00");
    
    static {
        FEATURES = new FeatureGenerator().generateFeatures();
        int longestFeatureslength = 0; 
        for(List<NodeType> feature : FEATURES) {
            int featureLength = 0;
            for(NodeType type : feature) {
                featureLength += type.getName().length() + 2;
            }
            if(featureLength > longestFeatureslength) {
                longestFeatureslength = featureLength;
            }
        }
        LONGEST_FEATURE_LENGTH = longestFeatureslength;
    }
    
    public static int getFeaturesSize() {
        return FEATURES.size();
    }

    public Fingerprint(Method method) {
        this.signature = method.getSignature();
        this.name = method.getName();
        
        byte[] byteVector = method.getVector();
        if(byteVector == null) {
            this.vector = new BasicVector(FEATURES.size());
        } else {
            this.vector = BasicVector.fromBinary(byteVector); 
        }
    }
    
    public Fingerprint(String name) {
        this.vector = new BasicVector(FEATURES.size());
        this.name = name;
        this.signature = "";
    }
    
    public Fingerprint() {
        this("");
    } 
    
    public Fingerprint(double... array) {
        this.vector = BasicVector.fromArray(array);
        this.name = "";
        this.signature = ""; 
    }
    
    public Fingerprint(Fingerprint copy) {
        this.vector = copy.vector.copy();
        this.name = copy.name;
        this.signature = copy.signature;
    }
    
    public void incrementFeature(NodeType... dimension) {
        Fingerprint.this.incrementFeatureBy(1, Arrays.asList(dimension));
    }
    
    public void incrementFeatureBy(int value, NodeType... dimension) {
        Fingerprint.this.incrementFeatureBy(value, Arrays.asList(dimension));
    }

    public void incrementFeature(List<NodeType> dimension) {
        incrementFeatureBy(1, dimension);
    }
    
    public void incrementFeatureBy(int value, List<NodeType> dimension) {
        if(value == 0) {
            return;
        }
        
        int index = FEATURES.indexOf(dimension);
        if(index == -1) {
            throw new IllegalArgumentException("Dimension not found");
        }
        vector.set(index, vector.get(index) + value);
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

    public void sumFeatures(Fingerprint that) {
        this.vector = this.vector.add(that.vector);
    }
    
    public void subtractFeatures(Fingerprint that) {
        this.vector = this.vector.subtract(that.vector);
    }
    
    public void abs() {
        for (int i = 0; i < vector.length(); i++) {
            if(vector.get(i) < 0) vector.set(i, vector.get(i) * -1);
        }
    }
    
    public double getLength() {
        return this.vector.euclideanNorm();
    }
    
    public double getDistanceToFingerprint(Fingerprint that) {
        return vector.subtract(that.vector).euclideanNorm();
    }
    
    public double getNonCommutativeSimilarityScoreToFingerprint(Fingerprint that) {
        double diff = vector.subtract(that.vector).euclideanNorm();
        double length = this.vector.euclideanNorm(); 
        double similarityScore = length - diff; 
        return (similarityScore > 0)? similarityScore : 0;
    }
    
    public double getSimilarityScoreToFingerprint(Fingerprint that) {
        double diff = vector.subtract(that.vector).euclideanNorm();
        double length = Math.max(this.vector.euclideanNorm(), that.vector.euclideanNorm()); 
        double similarityScore = length - diff; 
        return (similarityScore > 0)? similarityScore : 0;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vector getFeatureVector() {
        return vector;
    }

    public void setFeatureVector(Vector vector) {
        this.vector = vector;
    }
    
    public void setFeatureValues(int... values) {
        for(int i = 0; i < values.length; i++) {
            this.vector.set(i, (double) values[i]);
        }
    }
    
    public double getFeatureCount(int index) {
        return this.vector.get(index);
    }
   
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(name).append(":\n");
        string.append("Signature: ").append(signature).append(":\n");
        
        int numEntries = Math.min(FEATURES.size(), vector.length());
        
        for (int i = 0; i < numEntries; i++) {
            List<NodeType> feature = FEATURES.get(i);
            String featureString = String.format("%-" + LONGEST_FEATURE_LENGTH + "s", feature.toString());
            string = string.append(featureString).append(" : ").append(FRMTR.format(vector.get(i))).append("\n");
        }
        return string.toString();
    }
//
//    @Override
//    public int hashCode() {
//        int hash = 5;
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final MethodFingerprint other = (MethodFingerprint) obj;
//        if (!Objects.equals(this.name, other.name)) {
//            return false;
//        }
//        return true;
//    }
    
    public static Comparator<Fingerprint> sortByLengthDESC = new Comparator<Fingerprint>() {
        @Override
        public int compare(Fingerprint that, Fingerprint other) {
            double thatNeedleLength  = that.getLength();
            double otherNeedleLength = other.getLength();
            if (thatNeedleLength > otherNeedleLength) return -1;
            if (thatNeedleLength < otherNeedleLength) return  1;
            return 0;
        }
    };
    
    public static Comparator<Fingerprint> sortBySimScoreDESC = new Comparator<Fingerprint>() {
        @Override
        public int compare(Fingerprint that, Fingerprint other) {
            throw new NotImplementedException();
//            double scoreNeedleThat  = that.getComputedSimilarityScore();
//            double scoreNeedleOther = other.getComputedSimilarityScore();
//            if (scoreNeedleThat > scoreNeedleOther) return -1;
//            if (scoreNeedleThat < scoreNeedleOther) return  1;
//            return 0;
        }
    };
    
}
