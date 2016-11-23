package astli.pojo;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class Vector {

    private final short[] values;
    
    Vector(int size) {
        values = new short[size];
    }

    Vector(short[] values) {
        if (values == null) {
            throw new IllegalArgumentException();
        }
        this.values = values;
    }

    short get(int index) {
        return values[index];
    }
    
    void set(int index, short value) {
        values[index] = value; 
    }

    int manhattanNorm() {
        int norm = 0; 
        
        for (int i = 0; i < values.length; i++) {
            norm += Math.abs(values[i]);
        }
        return norm;
    }

    int manhattanDiff(Vector other) {
        if(getDimensions() != other.getDimensions()) {
            throw new IllegalArgumentException("Dimension mismatch");
        }
        
        int diff = 0; 
        for (int i = 0; i < values.length; i++) {
            diff += Math.abs(values[i] - other.values[i]);
        }
        return diff;
    }
    
    short[] getValues() {
        return values;
    }

    int getDimensions() {
        return values.length;
    }

}
