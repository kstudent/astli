package org.androidlibid.proto.store;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.Node;
import org.androidlibid.proto.ast.NodeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ClassFingerprintCreatorTest {
    
    ClassFingerprintCreator creator;
    Map<String, Node> methodASTs;
    ASTToFingerprintTransformer ast2fpt;
    
    String smaliClassName = "Ltld/pckg/ClassA;"; 
    String className      = "tld.pckg:ClassA";
    
    private static final Logger LOGGER = LogManager.getLogger(ClassFingerprintCreatorTest.class);
    
    
    @Before
    public void setUp() {
        ast2fpt = mock(ASTToFingerprintTransformer.class);
        creator = new ClassFingerprintCreator(ast2fpt);
        methodASTs = new HashMap<>();
        
    }
    
    @Test
    public void testCreateFingerprint() throws IOException {
        
        String[] methodNames = new String[] {"<init>", "update()", "resolve():byte[]"};
        
        addMethod(methodNames[0], 1, 2, 1);
        addMethod(methodNames[1], 1, 2, 3);
        addMethod(methodNames[2], 3, 4, 5);
        
        boolean addMethods = true;
        String[] fullMethodNames = new String[]{
            className + ":" + methodNames[0], 
            className + ":" + methodNames[1], 
            className + ":" + methodNames[2]
        };
        
        Fingerprint classPrint = creator.createClassFingerprint(
                methodASTs, smaliClassName, addMethods);
        
        assert(classPrint.getName().equals(className));
        
        List<Fingerprint> methodPrints = classPrint.getChildFingerprints();

        assert(methodPrints.size() == 3);

        for(String fullMethodName : fullMethodNames) {
            boolean methodNameInArray = false;
            
            for(int i = 0; i < methodPrints.size(); i++) {
                String printMethodName = methodPrints.get(i).getName();
                if(printMethodName.equals(fullMethodName)) {
                    methodNameInArray = true;
                }
            }
            assert(methodNameInArray);
        }
        
    }
    
    @Test
    public void testCreateFingerprintWithoutMethods() throws IOException {
        
        String[] methodNames = new String[] {"<init>", "update()", "resolve():byte[]"};
        
        addMethod(methodNames[0], 1, 2, 1);
        addMethod(methodNames[1], 1, 2, 3);
        addMethod(methodNames[2], 3, 4, 5);

        boolean addMethods = false;
        
        Fingerprint classPrint = creator.createClassFingerprint(
                methodASTs, smaliClassName, addMethods);
        
        assert(classPrint.getName().equals(className));
        
        List<Fingerprint> methodPrints = classPrint.getChildFingerprints();

        assert(methodPrints.isEmpty());
        
    }
    
    @Test
    public void testCreateFingerprintDropShortMethods() throws IOException {
        
        addMethod("short", 1, 0, 0);
        
        boolean addMethods = true;
        
        Fingerprint classPrint = creator.createClassFingerprint(
                methodASTs, smaliClassName, addMethods);
        
        List<Fingerprint> methodPrints = classPrint.getChildFingerprints();

        assert(methodPrints.isEmpty());
    
    }
    
    private void addMethod(String methodSignature, int... values) {
        Node node = new Node(NodeType.METHOD);
        methodASTs.put(methodSignature, node);
        
        Fingerprint methodprint = new Fingerprint();
        methodprint.setName(methodSignature);
        methodprint.setFeatureValues(values);
        
        when(ast2fpt.createFingerprint(node)).thenReturn(methodprint);
    }
    
}
