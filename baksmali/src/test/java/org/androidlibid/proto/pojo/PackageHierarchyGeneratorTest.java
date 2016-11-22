package org.androidlibid.proto.pojo;

import org.androidlibid.proto.pojo.Fingerprint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.ast.ASTClassBuilder;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.Node;
import org.androidlibid.proto.ast.NodeType;
import org.jf.baksmali.baksmaliOptions;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchyGeneratorTest {

    List<ASTClassBuilder> astClassBuilders;
    ASTToFingerprintTransformer ast2fpt;
    baksmaliOptions options; 
    
    @Before
    public void setup() throws IOException {
        
        options = new baksmaliOptions();
        ast2fpt = mock(ASTToFingerprintTransformer.class);
        astClassBuilders = new ArrayList<>();
    }
    
    @Test 
    public void testGeneratePackageHierarchyFromClassDefs() throws IOException {
        
//        String packageName         = "tld.pckg.sub";
//        String className           = "Class";
//        String expectedPckgClassID = packageName + ":" + className;
//        String returnType          = "int";
//    
//        for(int i = 1; i <= 3; i++) {
//            astClassBuilders.add(
//                prepareClassDefinition(i, packageName, className, returnType)); 
//        }        
//        
//        Map<String, String> mappings = new HashMap<>();
//        options.isObfuscated = false;
//        
//        PackageHierarchyGenerator phg = new PackageHierarchyGenerator(options, ast2fpt, mappings);
//        
//        Map<String, MethodFingerprint> hierarchy = phg.generatePackageHierarchyFromClassBuilders(astClassBuilders);
//        
//        assert(hierarchy.containsKey(packageName));
//        
//        MethodFingerprint pckg = hierarchy.get(packageName);
//        List<MethodFingerprint> classes = pckg.getChildFingerprints();
//
//        assert(classes.size() == 3);
//        
//        for(MethodFingerprint clazz : classes) {
//            String pckgClassID = clazz.getName(); 
//            
//            int classIndex = extractClassIndexFromClassIdentifier(pckgClassID, className);
//            
//            assert(classIndex >= 1 && classIndex <= 3);
//            assert(clazz.getChildFingerprints().size() == 1);
//            
//            MethodFingerprint methodFP = clazz.getChildFingerprints().get(0);
//            MethodFingerprint expectedMethodFP = perpareMethodFingerprint(classIndex);
//            
//            String expectedMethodName = expectedPckgClassID + classIndex + ":method" + classIndex + "():" + returnType;
//            assert(methodFP.getName().equals(expectedMethodName));
//            assert(methodFP.getFeatureVector().equals(expectedMethodFP.getFeatureVector()));
//        }
    } 
    
    @Test 
    public void testGeneratePackageHierarchyFromObfuscatedClassDefs() throws IOException {
//        
//        String obfuscatedPackageName = "a.b.c";
//        String obfuscatedClassName   = "D";
//        String expectedPackageName   = "tld.pckg.sub";
//        String expectedClassName     = "Class";
//        String obfuscatedPckgClassID =  obfuscatedPackageName + ":" + obfuscatedClassName ; 
//        String expectedPckgClassID   =  expectedPackageName   + ":" + expectedClassName;
//        
//        Map<String, String> mappings = new HashMap<>();
//        mappings.put(obfuscatedPackageName, expectedPackageName);
//        mappings.put(obfuscatedPckgClassID, expectedPckgClassID);
//        
//        for(int i = 1; i <= 3; i++) {
//            astClassBuilders.add(
//                    prepareClassDefinition(
//                            i, obfuscatedPackageName, obfuscatedClassName, 
//                            obfuscatedPckgClassID)
//            );
//            
//            mappings.put(obfuscatedPckgClassID + i , expectedPckgClassID + i);
//            mappings.put(
//                obfuscatedPckgClassID + i + ":method" + i + "():" + obfuscatedPckgClassID, 
//                expectedPckgClassID   + i + ":method" + i + "():" + expectedPckgClassID
//            );
//        }        
//        
//        options.isObfuscated = true;
//        
//        PackageHierarchyGenerator phg = new PackageHierarchyGenerator(options, ast2fpt, mappings);
//        
//        Map<String, MethodFingerprint> hierarchy = phg.generatePackageHierarchyFromClassBuilders(astClassBuilders);
//        
//        assert(hierarchy.containsKey(expectedPackageName));
//        
//        MethodFingerprint pckg = hierarchy.get(expectedPackageName);
//        List<MethodFingerprint> classes = pckg.getChildFingerprints();
//
//        assert(classes.size() == 3);
//        
//        for(MethodFingerprint clazz : classes) {
//            String pckgClassID = clazz.getName(); 
//            
//            int classIndex = extractClassIndexFromClassIdentifier(pckgClassID, expectedClassName);
//            
//            assert(classIndex >= 1 && classIndex <= 3);
//            assert(clazz.getChildFingerprints().size() == 1);
//            
//            MethodFingerprint methodFP = clazz.getChildFingerprints().get(0);
//            MethodFingerprint expectedMethodFP = perpareMethodFingerprint(classIndex);
//            
//            String expectedMethodName = expectedPckgClassID + classIndex + ":method" + classIndex + "():" + expectedPckgClassID;
//            assert(methodFP.getName().equals(expectedMethodName));
//            assert(methodFP.getFeatureVector().equals(expectedMethodFP.getFeatureVector()));
//        }
    } 
    
    //test if empty methdos stay away!

    private ASTClassBuilder prepareClassDefinition(int i, String packageName, String className, String returnValueType) throws IOException {
        ASTClassBuilder astC = mock(ASTClassBuilder.class);
        
        when(astC.getClassName()).thenReturn("L" + packageName + "." + className + i + ";");

        Node methodNode = new Node(NodeType.MTH);
        Map<String, Node> methodsofClass = new HashMap<>();
        methodsofClass.put("method" + i + "():" + returnValueType, methodNode);

        when(astC.buildASTs()).thenReturn(methodsofClass);

        Fingerprint methodFingerprint = perpareMethodFingerprint();
        when(ast2fpt.createFingerprint(methodNode)).thenReturn(methodFingerprint);
        
        return astC;
    }

    private Fingerprint perpareMethodFingerprint() {
        Fingerprint methodFingerprint = new Fingerprint();
        methodFingerprint.incrementFeature(NodeType.MTH);

        return methodFingerprint;
    }

    private int extractClassIndexFromClassIdentifier(String classIdentifier, String className) {
        String classIndexString = classIdentifier.substring(
                    classIdentifier.indexOf(className) + className.length(), 
                    classIdentifier.indexOf(className) + className.length() + 1);
        return Integer.parseInt(classIndexString);
        
    }
}
