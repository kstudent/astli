package org.androidlibid.proto.match;

import java.util.ArrayList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;
import org.la4j.vector.dense.BasicVector;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchyGenerator {

    List<Fingerprint> generatePackageHierarchy() {
        List<Fingerprint> packageHierarchy = new ArrayList<>();
        
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
                method111.setFeatureVector(new BasicVector(array111));       
                method112.setFeatureVector(new BasicVector(array112));
                method113.setFeatureVector(new BasicVector(array113));
                class11.addChildFingerprint(method111);
                class11.addChildFingerprint(method112);
                class11.addChildFingerprint(method113);
            pckg1.addChildFingerprint(class11);
            
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
                method121.setFeatureVector(new BasicVector(array121));
                method122.setFeatureVector(new BasicVector(array122));
                method123.setFeatureVector(new BasicVector(array123));
                class12.addChildFingerprint(method121);
                class12.addChildFingerprint(method122);
                class12.addChildFingerprint(method123);
            pckg1.addChildFingerprint(class12);
            
            String class13Name   = pckg1Name + ".class3";
            Fingerprint class13  = new Fingerprint(class13Name);
                String method131Name = class13Name + ":method31";
                String method132Name = class13Name + ":method32";
                Fingerprint method131 = new Fingerprint(method131Name);
                Fingerprint method132 = new Fingerprint(method132Name);
                double[] array131 = { 1, 7, 2 };
                double[] array132 = { 6, 8, 3 };
                method131.setFeatureVector(new BasicVector(array131));
                method132.setFeatureVector(new BasicVector(array132));
                class13.addChildFingerprint(method131);
                class13.addChildFingerprint(method132);
            pckg1.addChildFingerprint(class13);
            
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
                method211.setFeatureVector(new BasicVector(array211));       
                method212.setFeatureVector(new BasicVector(array212));
                method213.setFeatureVector(new BasicVector(array213));
                class21.addChildFingerprint(method211);
                class21.addChildFingerprint(method212);
                class21.addChildFingerprint(method213);
            pckg2.addChildFingerprint(class21);
            
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
                method221.setFeatureVector(new BasicVector(array221));
                method222.setFeatureVector(new BasicVector(array222));
                method223.setFeatureVector(new BasicVector(array223));
                class22.addChildFingerprint(method221);
                class22.addChildFingerprint(method222);
                class22.addChildFingerprint(method223);
            pckg2.addChildFingerprint(class22);
            
            String class23Name   = pckg2Name + ".class3";
            Fingerprint class23  = new Fingerprint(class23Name);
                String method231Name = class23Name + ":method31";
                String method232Name = class23Name + ":method32";
                Fingerprint method231 = new Fingerprint(method231Name);
                Fingerprint method232 = new Fingerprint(method232Name);
                double[] array231 = { 3, 9, 0 };
                double[] array232 = { 4, 8, 2 };
                method231.setFeatureVector(new BasicVector(array231));
                method232.setFeatureVector(new BasicVector(array232));
                class23.addChildFingerprint(method231);
                class23.addChildFingerprint(method232);
            pckg2.addChildFingerprint(class23);
        
        packageHierarchy.add(pckg1);
        packageHierarchy.add(pckg2);
              
        return packageHierarchy;
    }
    
    
}
