package org.androidlibid.proto.match.inclusion;

import org.androidlibid.proto.match.inclusion.ClassInclusionCalculator;
import java.util.ArrayList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.match.FingerprintMatcher;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ClassInclusionCalculatorTest {

    private FingerprintMatcher matcher;
    private ArrayList<Fingerprint> methods;
    private ClassInclusionCalculator calculator;
    
    @Before 
    public void setUp() {
        matcher = new FingerprintMatcher();
        methods = new ArrayList<Fingerprint>();
        methods.add(new Fingerprint(1,   1,  1));
        methods.add(new Fingerprint(10, 10, 10));
        methods.add(new Fingerprint(50, 50, 50));
        methods.add(new Fingerprint(70, 10,  0));
        methods.add(new Fingerprint(5,   9, 99));
        calculator = new ClassInclusionCalculator(matcher, false);
        
    }
    
    @Test
    public void testClassAIsClassA() {
        List<Fingerprint> classA = new ArrayList<>();
        classA.addAll(methods);
        
        double score = calculator.computeInclusion(classA, classA);
        double expectedScore = methods.get(0).getLength() 
                + methods.get(1).getLength()  
                + methods.get(2).getLength()  
                + methods.get(3).getLength()  
                + methods.get(4).getLength(); 
        
        assert(doubleEquals(score, expectedScore));
    }
    
    @Test
    public void testClassAIsSuperSetOfB() {
        List<Fingerprint> classA = new ArrayList<>();
        List<Fingerprint> classB = new ArrayList<>();
        
        classA.addAll(methods);
        classB.add(methods.get(0));
        classB.add(methods.get(2));
        classB.add(methods.get(4));
        
        double score = calculator.computeInclusion(classA, classB);
        double expectedScore = methods.get(0).getLength() 
                + methods.get(2).getLength() 
                + methods.get(4).getLength();
        
        assert(doubleEquals(score, expectedScore));
    }
    
    @Test
    public void testClassAIsSubSetOfB() {
//        MethodFingerprint classA = new MethodFingerprint();
//        classA.setName("pckg:classA");
//        
//        for (int i = 0; i < methods.size(); i++) {
//            MethodFingerprint method = methods.get(i);
//            classA.addChildFingerprint(method);
//            method.setName("method" + i + "()");
//        }
//        
//        MethodFingerprint classB = new MethodFingerprint();
//        classB.setName("pckg:classB");
//        classB.addChildFingerprint(methods.get(0));
//        classB.addChildFingerprint(methods.get(2));
//        classB.addChildFingerprint(methods.get(4));
//        
//        double score = calculator.computeInclusion(classA.getChildFingerprints(), classB.getChildFingerprints());
//        double expectedScore = methods.get(0).getLength() 
//                + methods.get(2).getLength() 
//                + methods.get(4).getLength();
//        
//        double perfectScore = calculator.computeInclusion(classB.getChildFingerprints(), classB.getChildFingerprints());
//        
//        assert(doubleEquals(score, expectedScore));
//        assert(doubleEquals(score, perfectScore));
    }
    
    @Test
    public void testClassAIsSomehowSimilarToB() {
        List<Fingerprint> classA = new ArrayList<>();
        List<Fingerprint> classB = new ArrayList<>();
        
        classA.addAll(methods);
        classB.add(new Fingerprint(1,   1,  2));
        classB.add(new Fingerprint(10, 10, 12));
        classB.add(new Fingerprint(50, 50, 49));
        classB.add(new Fingerprint(70, 10,  5));
        classB.add(new Fingerprint(5,   9, 70));

        double score = calculator.computeInclusion(classB, classA);

        double maxScore = methods.get(0).getLength() 
                + methods.get(1).getLength()  
                + methods.get(2).getLength()  
                + methods.get(3).getLength()  
                + methods.get(4).getLength(); 
        
        assert(score < maxScore);
        assert(score > maxScore * .8);
    }
    
    @Test
    public void testClassAIsBarelySimilarToB() {
        List<Fingerprint> classA = new ArrayList<>();
        List<Fingerprint> classB = new ArrayList<>();
        
        classA.addAll(methods);
        classB.add(new Fingerprint(1,   1,  1));
        classB.add(new Fingerprint(1,   1,  1));
        classB.add(new Fingerprint(1,   1,  1));
        classB.add(new Fingerprint(1,   1,  1));
        classB.add(new Fingerprint(1,   1,  1));

        double score = calculator.computeInclusion(classB, classA);
        
        double maxScore = methods.get(0).getLength() 
                + methods.get(1).getLength()  
                + methods.get(2).getLength()  
                + methods.get(3).getLength()  
                + methods.get(4).getLength(); 
        
        assert(score < maxScore * .1);
    }
        
    @Test
    public void testEmptyClasses() {
        List<Fingerprint> classA = new ArrayList<>();
        List<Fingerprint> classB = new ArrayList<>();
        classA.addAll(methods);
        
        double score = calculator.computeInclusion(classB, classA);
        assert(doubleEquals(score, 0));
        
        score = calculator.computeInclusion(classA, classB);
        assert(doubleEquals(score, 0));
    }
    
    private boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
}
