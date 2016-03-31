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
public class ClassInclusionCalculatorTest {

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
    public void testClassAIsClassA() {
        ClassInclusionCalculator calculator = new ClassInclusionCalculator(matcher);
        List<Fingerprint> classA = new ArrayList<>();
        classA.addAll(methods);
        
        double score = calculator.computeClassInclusion(classA, classA);
        double expectedScore = methods.get(0).euclideanNorm() 
                + methods.get(1).euclideanNorm()  
                + methods.get(2).euclideanNorm()  
                + methods.get(3).euclideanNorm()  
                + methods.get(4).euclideanNorm(); 
        
        assert(doubleEquals(score, expectedScore));
    }
    
    @Test
    public void testClassAIsSuperSetOfB() {
        ClassInclusionCalculator calculator = new ClassInclusionCalculator(matcher);
        
        List<Fingerprint> classA = new ArrayList<>();
        List<Fingerprint> classB = new ArrayList<>();
        
        classA.addAll(methods);
        classB.add(methods.get(0));
        classB.add(methods.get(2));
        classB.add(methods.get(4));
        
        double score = calculator.computeClassInclusion(classA, classB);
        double expectedScore = methods.get(0).euclideanNorm() 
                + methods.get(2).euclideanNorm() 
                + methods.get(4).euclideanNorm();
        
        assert(doubleEquals(score, expectedScore));
    }
    
    @Test
    public void testClassAIsSubSetOfB() {
        ClassInclusionCalculator calculator = new ClassInclusionCalculator(matcher);
        
        Fingerprint classA = new Fingerprint();
        classA.setName("pckg:classA");
        
        for (int i = 0; i < methods.size(); i++) {
            Fingerprint method = methods.get(i);
            classA.addChild(method);
            method.setName("method" + i + "()");
        }
        
        Fingerprint classB = new Fingerprint();
        classB.setName("pckg:classB");
        classB.addChild(methods.get(0));
        classB.addChild(methods.get(2));
        classB.addChild(methods.get(4));
        
        double score = calculator.computeClassInclusion(classA.getChildren(), classB.getChildren());
        double expectedScore = methods.get(0).euclideanNorm() 
                + methods.get(2).euclideanNorm() 
                + methods.get(4).euclideanNorm();
        
        double perfectScore = calculator.computeClassInclusion(classB.getChildren(), classB.getChildren());
        
        assert(doubleEquals(score, expectedScore));
        assert(doubleEquals(score, perfectScore));
    }
    
    @Test
    public void testClassAIsSomehowSimilarToB() {
        ClassInclusionCalculator calculator = new ClassInclusionCalculator(matcher);
        
        List<Fingerprint> classA = new ArrayList<>();
        List<Fingerprint> classB = new ArrayList<>();
        
        classA.addAll(methods);
        classB.add(new Fingerprint(1,   1,  2));
        classB.add(new Fingerprint(10, 10, 12));
        classB.add(new Fingerprint(50, 50, 49));
        classB.add(new Fingerprint(70, 10,  5));
        classB.add(new Fingerprint(5,   9, 70));

        double score = calculator.computeClassInclusion(classB, classA);

        double maxScore = methods.get(0).euclideanNorm() 
                + methods.get(1).euclideanNorm()  
                + methods.get(2).euclideanNorm()  
                + methods.get(3).euclideanNorm()  
                + methods.get(4).euclideanNorm(); 
        
        assert(score < maxScore);
        assert(score > maxScore * .8);
    }
    
    @Test
    public void testClassAIsBarelySimilarToB() {
        ClassInclusionCalculator calculator = new ClassInclusionCalculator(matcher);
        
        List<Fingerprint> classA = new ArrayList<>();
        List<Fingerprint> classB = new ArrayList<>();
        
        classA.addAll(methods);
        classB.add(new Fingerprint(1,   1,  1));
        classB.add(new Fingerprint(1,   1,  1));
        classB.add(new Fingerprint(1,   1,  1));
        classB.add(new Fingerprint(1,   1,  1));
        classB.add(new Fingerprint(1,   1,  1));

        double score = calculator.computeClassInclusion(classB, classA);
        
        double maxScore = methods.get(0).euclideanNorm() 
                + methods.get(1).euclideanNorm()  
                + methods.get(2).euclideanNorm()  
                + methods.get(3).euclideanNorm()  
                + methods.get(4).euclideanNorm(); 
        
        assert(score < maxScore * .1);
    }
    
    @Test
    public void testEmptyClasses() {
        ClassInclusionCalculator calculator = new ClassInclusionCalculator(matcher);
        
        List<Fingerprint> classA = new ArrayList<>();
        List<Fingerprint> classB = new ArrayList<>();
        classA.addAll(methods);
        
        double score = calculator.computeClassInclusion(classB, classA);
        assert(doubleEquals(score, 0));
        
        score = calculator.computeClassInclusion(classA, classB);
        assert(doubleEquals(score, 0));
    }
    
    private boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
}
