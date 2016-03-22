package org.androidlibid.proto.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageInclusionCalculatorTest {

    private ClassInclusionCalculator classInclusionCalculator;

    List<Fingerprint> package1classes, package2classes, package3classes, package4classes;
    List<List<Fingerprint> > allMethods;
    List<Fingerprint> allClasses;
    double[][] table;
    
    @Before
    public void setUp() {
        classInclusionCalculator = mock(ClassInclusionCalculator.class);

        allClasses = new ArrayList<>();
        allMethods = new ArrayList<>();
        
        for(int i = 0; i < 8; i++) {
            Fingerprint clazz = new Fingerprint(); 
            Fingerprint method = new Fingerprint();
            clazz.addChild(method);
            allMethods.add(clazz.getChildren());
            allClasses.add(clazz); 
        }
        
        double[][] scoreTable = {
        //    | p1  | p2  | p3     | p4|
        /*1*/ {1, 0, 1, 0, 1, 0, 0, 0 },
        /*1*/ {0, 1, 0, 1, 0, 0, 1,.5 },
        /*2*/ {1, 0, 1, 0, 0, 0, 0, 0 },
        /*2*/ {0, 1, 0, 1, 0, 0, 0, 0 },
        /*3*/ {1, 0, 0, 0, 1, 0, 0, 0 },
        /*3*/ {0, 0, 0, 0, 0, 1, 0, 0 },
        /*3*/ {0, 1, 0, 0, 0, 0, 1, 0 },
        /*4*/ {0,.5, 0, 0, 0, 0, 0, 1 }
         };
        
        table = scoreTable;
        
        when(classInclusionCalculator.computeClassInclusion(
                any(List.class ), any(List.class )
            )).thenAnswer(getAnswer());
        
        package1classes = new ArrayList<>(); 
        package1classes.add(allClasses.get(0));
        package1classes.add(allClasses.get(1));

        package2classes = new ArrayList<>(); 
        package2classes.add(allClasses.get(2));
        package2classes.add(allClasses.get(3));

        package3classes = new ArrayList<>(); 
        package3classes.add(allClasses.get(4));
        package3classes.add(allClasses.get(5));
        package3classes.add(allClasses.get(6));

        package4classes = new ArrayList<>(); 
        package4classes.add(allClasses.get(7));

    }
    
    @Test
    public void testPackage1IsSimilarToPackage2() {
        PackageInclusionCalculator calc = new PackageInclusionCalculator(classInclusionCalculator);
        
        double score = calc.computePackageInclusion(package1classes, package2classes);
        
        assert(doubleEquals(score, 1));
    }
    
    @Test
    public void testPackage1IsSimilarToPackage3() {
        PackageInclusionCalculator calc = new PackageInclusionCalculator(classInclusionCalculator);
        
        double score = calc.computePackageInclusion(package1classes, package3classes);
        
        assert(doubleEquals(score, 1));
    }
    
    @Test
    public void testPackage1IsSimilarToPackage4() {
        PackageInclusionCalculator calc = new PackageInclusionCalculator(classInclusionCalculator);
        
        double score = calc.computePackageInclusion(package1classes, package4classes);
        
        assert(doubleEquals(score,.5));
    }
    
    private boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
    
    /*
    private Answer<?> getAnswer() {
        
    }
    
    */

    private Answer<Double> getAnswer() {
        return new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocation) throws Throwable {
                List<Fingerprint> methodsA = (List<Fingerprint>) invocation.getArguments()[0];
                List<Fingerprint> methodsB = (List<Fingerprint>) invocation.getArguments()[1];
                
                int indexI = allMethods.indexOf(methodsA);
                int indexJ = allMethods.indexOf(methodsB);
                
                return table[indexI][indexJ]; 
            }
        };
    }
    
}
