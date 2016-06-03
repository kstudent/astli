package org.androidlibid.proto;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.androidlibid.proto.ast.NodeType;
import org.androidlibid.proto.ast.FeatureGenerator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang.NotImplementedException;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;
import org.androidlibid.proto.ao.FingerprintEntity;

public class Fingerprint {

    private String name;
    private Vector vector;
    private String signature;
    
    @Nullable
    private final FingerprintEntity method;

    private static final List<List<NodeType>> FEATURES;
    private static final int LONGEST_FEATURE_LENGTH;
    private static final NumberFormat FRMTR = new DecimalFormat("#0.00");
    
    static {
        FEATURES = new FeatureGenerator().generateFeatures();
        LONGEST_FEATURE_LENGTH = 12;
    }
    
    public static int getFeaturesSize() {
        return FEATURES.size();
    }

    public Fingerprint(FingerprintEntity method) {
        this.signature = method.getSignature();
        this.name = method.getName();
        
        byte[] byteVector = method.getVector();
        if(byteVector == null) {
            this.vector = new BasicVector(FEATURES.size());
        } else {
            this.vector = BasicVector.fromBinary(byteVector);
        }
        
        this.method = method;
        
    }
    
    public Fingerprint(String name) {
        this.vector = new BasicVector(FEATURES.size());
        this.name = name;
        this.signature = "";
        this.method = null;
    }
    
    public Fingerprint() {
        this("");
    } 
    
    public Fingerprint(double... array) {
        this("");
        this.vector = BasicVector.fromArray(array);
    }
    
    public Fingerprint(Fingerprint copy) {
        this.vector = copy.vector.copy();
        this.name = copy.name;
        this.signature = copy.signature;
        this.method = copy.method;
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
    
    public int getLength() {
        return new Double(vector.manhattanNorm()).intValue();
    }
    
    public double getEuclideanLength() {
        return vector.euclideanNorm();
    }
    
    public double getDistanceToFingerprint(Fingerprint that) {
        return vector.subtract(that.vector).manhattanNorm();
    }
    
    public double getNonCommutativeSimilarityScoreToFingerprint(Fingerprint that) {
        double diff = vector.subtract(that.vector).manhattanNorm();
        double length = this.vector.manhattanNorm(); 
        double similarityScore = length - diff; 
        return (similarityScore > 0)? similarityScore : 0;
    }
    
    public double getSimilarityScoreToFingerprint(Fingerprint that) {
        double diff = vector.subtract(that.vector).manhattanNorm();
        double length = Math.max(this.vector.manhattanNorm(), that.vector.manhattanNorm()); 
        double similarityScore = length - diff; 
        return (similarityScore > 0)? similarityScore : 0;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getBinaryFeatureVector() {
        return vector.toBinary();
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
    
    public int getEntropy() {
        return (this.signature.length() - 1) * 3 + this.getLength();
    }

    public FingerprintEntity getMethod() {
        
        if(method == null) {
            throw new RuntimeException("Fingerprint does not have Reference to "
                    + "its entity");
        }
        return method;
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
    
    public static Comparator<Fingerprint> sortByLengthDESC = (Fingerprint that, Fingerprint other) -> {
        double thatNeedleLength  = that.getEuclideanLength();
        double otherNeedleLength = other.getEuclideanLength();
        if (thatNeedleLength > otherNeedleLength) return -1;
        if (thatNeedleLength < otherNeedleLength) return  1;
        return 0;
    };
    
    public static Comparator<Fingerprint> sortBySimScoreDESC = (Fingerprint that, Fingerprint other) -> {
        throw new NotImplementedException();
//            double scoreNeedleThat  = that.getComputedSimilarityScore();
//            double scoreNeedleOther = other.getComputedSimilarityScore();
//            if (scoreNeedleThat > scoreNeedleOther) return -1;
//            if (scoreNeedleThat < scoreNeedleOther) return  1;
//            return 0;
    };
    
}
