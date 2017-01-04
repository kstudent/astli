package astli.pojo;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import astli.db.MethodE;

public class Fingerprint {

    private String name;
    private String signature;
    private final Vector vector;
    
    @Nullable
    private final MethodE method;

    private static final List<List<NodeType>> DIMENSIONS;
    private static final int LONGEST_DIMENSION;
    private static final NumberFormat FRMTR = new DecimalFormat("#0.00");
    
    static {
        DIMENSIONS = new DimensionGenerator().generate();
        LONGEST_DIMENSION = 12;
    }
    
    public Fingerprint(MethodE method) {
        this.signature = method.getSignature();
        this.name = method.getName();
        
        byte[] byteVector = method.getVector();
        if(byteVector == null) {
            this.vector = new Vector(DIMENSIONS.size());
        } else {
            this.vector = new Vector(ArrayUtils.lEByte2Short(byteVector));
        }
        
        this.method = method;
    }
    
    public Fingerprint() {
        this.vector = new Vector(DIMENSIONS.size());
        this.name = "";
        this.signature = "";
        this.method = null;
    } 
    
    public void incrementFeature(NodeType... dimension) {
        Fingerprint.this.incrementFeatureBy((short)1, dimension);
    }
    
    public void incrementFeatureBy(short value, NodeType... dimension) {
        if(value == 0) {
            return;
        }
        
        List dimensionList = Arrays.asList(dimension);
        
        int index = DIMENSIONS.indexOf(dimensionList);
        if(index == -1) {
            throw new IllegalArgumentException("Dimension not found");
        }
        
        vector.set(index, (short) (value + vector.get(index)));
    }
    
    public int getSimilarityTo(Fingerprint that) {
        int diff = vector.manhattanDiff(that.vector);
        int length = this.vector.manhattanNorm(); 
        int similarityScore = length - diff; 
        return (similarityScore > 0)? similarityScore : 0;
    }
       
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getBinaryFeatureVector() {
        return ArrayUtils.short2LEByte(vector.getValues());
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public int getParticularity() {
        return (this.signature.length() - 1) * 3 + this.getLength();
    }

    public MethodE getMethod() {
        
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
        
        int numEntries = Math.min(DIMENSIONS.size(), vector.getDimensions());
        
        for (int i = 0; i < numEntries; i++) {
            List<NodeType> feature = DIMENSIONS.get(i);
            String featureString = String.format("%-" + LONGEST_DIMENSION + "s", feature.toString());
            string = string.append(featureString).append(" : ").append(FRMTR.format(vector.get(i))).append("\n");
        }
        return string.toString();
    }
    
    public int getLength() {
        return vector.manhattanNorm();
    }
    
    /* my test only section */
    public short getFeatureValue(int index) {
        return this.vector.get(index);
    }
    
    public short getFeatureValue(NodeType... dimension) {
        int index = DIMENSIONS.indexOf(Arrays.asList(dimension));
        if(index == -1) {
            throw new IllegalArgumentException("Dimension not found");
        }
        return vector.get(index);
    }
    
    public void incrementFeatureBy(int index, short value) {
        
        if(index >= DIMENSIONS.size() || index < 0) {
            throw new IndexOutOfBoundsException("Dimension not found");
        }
        
        vector.set(index, (short) (vector.get(index) + value));
    }
    
    public void incrementFeature(int index) {
        Fingerprint.this.incrementFeatureBy(index, (short)1);
    }

}
