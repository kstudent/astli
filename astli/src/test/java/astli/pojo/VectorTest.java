package astli.pojo;

import astli.pojo.Vector;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class VectorTest {
    
    @Test
    public void testInit() {
        Vector vector = new Vector(10);
        for(int i = 0; i < 10; i++) {
            assert(vector.get(i) == 0);
        }
    }
    
    @Test
    public void testInitWithByteArray() {
        short[] array = new short[]{1, 2, 5, 0};
        
        Vector vector = new Vector(array);
        
        for(int i = 0; i < array.length; i++) {
            assert(vector.get(i) == array[i]);
        }
    }
    
    @Test
    public void testManhattanDiff() {
        Vector v1 = new Vector(new short[] {1,2,3,4});
        Vector v2 = new Vector(new short[] {2,1,4,3});
         
        int diff = v1.manhattanDiff(v2);
        assert(diff == 4);
        
        diff = v2.manhattanDiff(v1);
        assert(diff == 4);
        
    
    }
    
    @Test
    public void testManhattanNorm() {
        Vector v1 = new Vector(new short[] {1,2,3,4});
        assert(v1.manhattanNorm() == (1 + 2 + 3 + 4));
    }
    
    @Test
    public void testGetDimensions() {
        Vector v = new Vector(10);
        assert(v.getDimensions() == 10);
        v = new Vector(new short[7]);
        assert(v.getDimensions() == 7);
    }
    
}
