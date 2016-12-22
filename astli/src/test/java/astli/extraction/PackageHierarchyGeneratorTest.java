package astli.extraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import astli.extraction.ClassASTBuilder;
import astli.extraction.ASTToFingerprintTransformer;
import astli.extraction.Node;
import astli.extraction.PackageHierarchyGenerator;
import astli.pojo.Fingerprint;
import astli.pojo.NodeType;
import astli.pojo.PackageHierarchy;
import java.util.stream.Collectors;
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

    List<ClassASTBuilder> astClassBuilders;
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
        
        String packageName         = "tld.pckg.sub";
        String className           = "Class";
        String returnType          = "int";
    
        for(int i = 1; i <= 3; i++) {
            astClassBuilders.add(
                prepareClassDefinition(i, packageName, className, returnType)); 
        }        
        
        Map<String, String> mappings = new HashMap<>();
        
        PackageHierarchyGenerator phg = new PackageHierarchyGenerator(ast2fpt, mappings);
        
        List<PackageHierarchy> hierarchies = 
                phg.generatePackageHierarchiesFromClassBuilders(astClassBuilders.stream())
                .collect(Collectors.toList());
        
        assert(hierarchies.size() == 1);
        
        PackageHierarchy hierarchy = hierarchies.get(0); 

        assert(hierarchy.getClassNames().size() == 3);
        
        hierarchy.getClassNames().stream()
            .map(name -> hierarchy.getMethodsByClassName(name))
            .forEach(methods -> {
                assert(methods.size() == 1);
            });
    } 
    
    private ClassASTBuilder prepareClassDefinition(int i, String packageName, String className, String returnValueType) throws IOException {
        ClassASTBuilder astC = mock(ClassASTBuilder.class);
        
        when(astC.getClassName()).thenReturn("L" + packageName + "." + className + i + ";");

        Node methodNode = new Node(NodeType.MTH);
        Map<String, Node> methodsofClass = new HashMap<>();
        methodsofClass.put("method" + i + "():" + returnValueType, methodNode);

        when(astC.build()).thenReturn(methodsofClass);

        Fingerprint methodFingerprint = perpareMethodFingerprint();
        when(ast2fpt.createFingerprint(methodNode)).thenReturn(methodFingerprint);
        
        return astC;
    }

    private Fingerprint perpareMethodFingerprint() {
        Fingerprint methodFingerprint = new Fingerprint();
        methodFingerprint.incrementFeature(NodeType.DRC);

        return methodFingerprint;
    }
}
