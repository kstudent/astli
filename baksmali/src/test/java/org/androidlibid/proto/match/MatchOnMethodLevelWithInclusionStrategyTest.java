package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.junit.Before;
import org.junit.Test;
import org.la4j.vector.dense.BasicVector;
import org.mockito.Mockito;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchOnMethodLevelWithInclusionStrategyTest {
    
    FingerprintService service; 
    ResultEvaluator    evaluator;
    FingerprintMatcher matcher;
    private Map<String, Fingerprint> appPackagePrints;
    
    @Before 
    public void setUp() throws SQLException {
        service   = Mockito.mock(FingerprintService.class);
        evaluator = new ResultEvaluator();
        matcher   = new FingerprintMatcher(1000);
        
        //prepare haystack hierarchy
        List<Fingerprint> haystackMethods = new ArrayList<>();
        
        String pckg1Name     = "org.pckg1"; 
        Fingerprint pckg1     = new Fingerprint(pckg1Name);
            
            String class11Name   = pckg1Name + ".class1";
            Fingerprint class11  = new Fingerprint(class11Name);
                String method111Name = class11Name + ":method11";
                String method112Name = class11Name + ":method12";
                String method113Name = class11Name + ":method13";
                Fingerprint method111 = new Fingerprint(method111Name);
                Fingerprint method112 = new Fingerprint(method112Name);
                Fingerprint method113 = new Fingerprint(method113Name);
                double[] array111 = { 0, 1, 2 };
                double[] array112 = { 4, 3, 1 };
                double[] array113 = { 2, 3, 6 };
                method111.setVector(new BasicVector(array111));       
                method112.setVector(new BasicVector(array112));
                method113.setVector(new BasicVector(array113));
                class11.addChild(method111);
                class11.addChild(method112);
                class11.addChild(method113);
            pckg1.addChild(class11);
            
            String class12Name   = pckg1Name + ".class2";
            Fingerprint class12  = new Fingerprint(class12Name);
                String method121Name = class12Name + ":method21";
                String method122Name = class12Name + ":method22";
                String method123Name = class12Name + ":method23";
                Fingerprint method121 = new Fingerprint(method121Name);
                Fingerprint method122 = new Fingerprint(method122Name);
                Fingerprint method123 = new Fingerprint(method123Name);
                double[] array121 = { 1, 1, 0 };
                double[] array122 = { 9, 0, 1 };
                double[] array123 = { 0, 2, 7 };
                method121.setVector(new BasicVector(array121));
                method122.setVector(new BasicVector(array122));
                method123.setVector(new BasicVector(array123));
                class12.addChild(method121);
                class12.addChild(method122);
                class12.addChild(method123);
            pckg1.addChild(class12);
            
            String class13Name   = pckg1Name + ".class3";
            Fingerprint class13  = new Fingerprint(class13Name);
                String method131Name = class13Name + ":method31";
                String method132Name = class13Name + ":method32";
                Fingerprint method131 = new Fingerprint(method131Name);
                Fingerprint method132 = new Fingerprint(method132Name);
                double[] array131 = { 1, 7, 2 };
                double[] array132 = { 6, 8, 3 };
                method131.setVector(new BasicVector(array131));
                method132.setVector(new BasicVector(array132));
                class13.addChild(method131);
                class13.addChild(method132);
            pckg1.addChild(class13);
            
        String pckg2Name     = "org.pckg2"; 
        Fingerprint pckg2     = new Fingerprint(pckg2Name);
            
            String class21Name   = pckg2Name + ".class1";
            Fingerprint class21  = new Fingerprint(class21Name);
                String method211Name = class21Name + ":method11";
                String method212Name = class21Name + ":method12";
                String method213Name = class21Name + ":method13";
                Fingerprint method211 = new Fingerprint(method211Name);
                Fingerprint method212 = new Fingerprint(method212Name);
                Fingerprint method213 = new Fingerprint(method213Name);
                double[] array211 = { 9,  1, 0 };
                double[] array212 = { 6,  8, 1 };
                double[] array213 = { 3, 21, 7 };
                method211.setVector(new BasicVector(array211));       
                method212.setVector(new BasicVector(array212));
                method213.setVector(new BasicVector(array213));
                class21.addChild(method211);
                class21.addChild(method212);
                class21.addChild(method213);
            pckg2.addChild(class21);
            
            String class22Name   = pckg2Name + ".class2";
            Fingerprint class22  = new Fingerprint(class22Name);
                String method221Name = class22Name + ":method21";
                String method222Name = class22Name + ":method22";
                String method223Name = class22Name + ":method23";
                Fingerprint method221 = new Fingerprint(method221Name);
                Fingerprint method222 = new Fingerprint(method222Name);
                Fingerprint method223 = new Fingerprint(method223Name);
                double[] array221 = { 0, 6 ,9 };
                double[] array222 = { 6, 0, 7 };
                double[] array223 = { 4, 5, 7 };
                method221.setVector(new BasicVector(array221));
                method222.setVector(new BasicVector(array222));
                method223.setVector(new BasicVector(array223));
                class22.addChild(method221);
                class22.addChild(method222);
                class22.addChild(method223);
            pckg2.addChild(class22);
            
            String class23Name   = pckg2Name + ".class3";
            Fingerprint class23  = new Fingerprint(class23Name);
                String method231Name = class23Name + ":method31";
                String method232Name = class23Name + ":method32";
                Fingerprint method231 = new Fingerprint(method231Name);
                Fingerprint method232 = new Fingerprint(method232Name);
                double[] array231 = { 3, 9, 0 };
                double[] array232 = { 4, 8, 2 };
                method231.setVector(new BasicVector(array231));
                method232.setVector(new BasicVector(array232));
                class23.addChild(method231);
                class23.addChild(method232);
            pckg2.addChild(class23);
            
        haystackMethods.add(method111);
        haystackMethods.add(method112);
        haystackMethods.add(method113);
        haystackMethods.add(method121);
        haystackMethods.add(method122);
        haystackMethods.add(method123);
        haystackMethods.add(method131);
        haystackMethods.add(method132);
        haystackMethods.add(method211);
        haystackMethods.add(method212);
        haystackMethods.add(method213);
        haystackMethods.add(method221);
        haystackMethods.add(method222);
        haystackMethods.add(method223);
        haystackMethods.add(method231);
        haystackMethods.add(method232);
        
        Mockito.when(service.findMethodsByPackageDepth(1)).thenReturn(haystackMethods);
        
        //Prepare app package hierarchy
        appPackagePrints = new HashMap<>();
        Fingerprint needlePackage = new Fingerprint(pckg2Name);
            
            Fingerprint needleClass1 = new Fingerprint(class21Name);
                needleClass1.addChild(method212);
                needleClass1.addChild(method213);
                needlePackage.addChild(needleClass1);
                
            Fingerprint needleClass2 = new Fingerprint(class22Name);
                needleClass2.addChild(method222);
                needleClass2.addChild(method223);
                needlePackage.addChild(needleClass2);
        
        appPackagePrints.put("needlePackage", needlePackage);
    }
    
    @Test 
    public void testMatching() throws SQLException {
        MatchOnMethodLevelWithInclusionStrategy strategy = new MatchOnMethodLevelWithInclusionStrategy(service, matcher, evaluator);
        Map<MatchingStrategy.Status, Integer> stats = strategy.matchPrints(appPackagePrints);
        assert(stats.get(MatchingStrategy.Status.OK) == 1);
//        System.out.println("Stats: ");
//        for(MatchingStrategy.Status key : MatchingStrategy.Status.values()) {
//            System.out.println(key.toString() + ": " + stats.get(key));
//        }
    }
}
