package org.androidlibid.proto.match;

import java.util.ArrayList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class InclusionCalculatorTest {

    private FingerprintMatcher matcher;
    private ArrayList<Fingerprint> methods;
    
    @Before 
    public void setUp() {
        matcher = new FingerprintMatcher(1000);
        methods = new ArrayList<Fingerprint>();
        methods.add(new Fingerprint(1,   1,  1));
        methods.add(new Fingerprint(10, 10, 10));
        methods.add(new Fingerprint(50, 50, 50));
        methods.add(new Fingerprint(70, 10,  0));
        methods.add(new Fingerprint(5,   9, 99));
    }
    
    @Test
    public void testClassAIsSuperSetOfB() {
        InclusionCalculator calculator = new InclusionCalculator(matcher);
        
        List<Fingerprint> classA = new ArrayList<>();
        List<Fingerprint> classB = new ArrayList<>();
        
        classA.addAll(methods);
                
        classB.add(methods.get(0));
        classB.add(methods.get(2));
        classB.add(methods.get(4));
        
        double score = calculator.computeClassInclusion(classA, classB);
        score /= classB.size();
        
        assert(score > .99999d && score < 1.00001d); 
    }
    
    @Test
    public void testClassAIsNotSuperSetOfB() {
    
        InclusionCalculator calculator = new InclusionCalculator(matcher);
        
        List<Fingerprint> classA = new ArrayList<>();
        List<Fingerprint> classB = new ArrayList<>();
                
        classA.add(methods.get(0));
        classA.add(methods.get(2));
        classA.add(methods.get(4));
        
        classB.addAll(methods);
        
        double score = calculator.computeClassInclusion(classA, classB);
        score /= classB.size();
        
        System.out.println(score);
        assert(score <= 3.0/5.0);
        
        
    }
    
}
