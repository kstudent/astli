package org.androidlibid.proto;

import java.util.Arrays;
import org.junit.Test;
import org.la4j.Vector;
import org.la4j.iterator.VectorIterator;
import org.la4j.vector.SparseVector;
import org.la4j.vector.dense.BasicVector;
import org.la4j.vector.sparse.CompressedVector;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class VectorTest {
    
    @Test
    public void testBasicVsSparseVector() {
        
        Vector v1 = new BasicVector(11);
        Vector v2 = new CompressedVector(22);
        v2.set(0,  1);
        v2.set(21, 1);
        
        System.out.println(v1);
        System.out.println(v2);
        
        System.out.println(Arrays.toString(v1.toBinary()));
        System.out.println(Arrays.toString(v2.toBinary()));
        
        Vector v3 = CompressedVector.fromBinary(v2.toBinary());
        
        System.out.println(v3);
        System.out.println(Arrays.toString(v3.toBinary()));
        
        System.out.println("done");
        
    }
    
}
