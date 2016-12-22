package astli.extraction;

import astli.pojo.NodeType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.any;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ClassASTBuilderTest {
    
    private List<Method> virtualMethods;
    private List<Method> directMethods;
    private MethodASTBuilderFactory factory;
    private MethodASTBuilder astBuilder;
    
    @Before
    public void setUp() throws IOException {
        factory    = mock(MethodASTBuilderFactory.class);
        astBuilder = mock(MethodASTBuilder.class);
        
        virtualMethods = new ArrayList<Method>();
        directMethods  = new ArrayList<Method>();
        
        when(factory.create(
                any(ClassDef.class), 
                any(Method.class), 
                any(MethodImplementation.class)
            )).thenReturn(astBuilder);
        when(astBuilder.build()).thenReturn(new Node(NodeType.MTH));
    }
        
    @Test
    public void testBuildClassASTs() throws IOException {
        ClassDef classDef = createClassDef();
        
        ClassASTBuilder builder = new ClassASTBuilder(classDef, factory);
        
        addMethod(true,  "doVirtualSuchAndSuch", "I",                "I", "Z");
        addMethod(false, "doDirectSuchAndSuch",  "Ltld/pckg/Class;", "I", "Ltld/pckg/AnotherClass;");
        
        Map<String, Node> asts = builder.build();
        
        Node virtualMethod = asts.get("doVirtualSuchAndSuch(int,boolean):int");
        Node directMethod  = asts.get("doDirectSuchAndSuch(int,tld.pckg:AnotherClass):tld.pckg:Class");
        
        assert(asts.size() == 2); 
        assert(virtualMethod != null);
        assert(virtualMethod.getType().equals(NodeType.MTH));
        assert(directMethod != null);
        assert(directMethod.getType().equals(NodeType.MTH));
    }
    
    @Test
    public void testBuildClassASTsWithDexBackendClassDef() throws IOException {
        ClassDef classDef = createClassDefWithDexBackend();
        
        ClassASTBuilder builder = new ClassASTBuilder(classDef, factory);
        addMethod(true,  "doVirtualSuchAndSuch", "I",                "I", "Z");
        addMethod(false, "doDirectSuchAndSuch",  "Ltld/pckg/Class;", "I", "Ltld/pckg/AnotherClass;");
        
        Map<String, Node> asts = builder.build();
        
        Node virtualMethod = asts.get("doVirtualSuchAndSuch(int,boolean):int");
        Node directMethod  = asts.get("doDirectSuchAndSuch(int,tld.pckg:AnotherClass):tld.pckg:Class");
        
        assert(asts.size() == 2); 
        assert(virtualMethod != null);
        assert(virtualMethod.getType().equals(NodeType.MTH));
        assert(directMethod != null);
        assert(directMethod.getType().equals(NodeType.MTH));
    }
    
    @SuppressWarnings("unchecked")
    private void addMethod(boolean isVirtual, String name, String returnType, String... parameterTypes) {
        
        Method method = mock(Method.class);
        MethodImplementation implementation = mock(MethodImplementation.class);
        
        when(method.getImplementation()).thenReturn(implementation);
        when(method.getName()).thenReturn(name);
        when(method.getReturnType()).thenReturn(returnType);
        when(method.getParameterTypes()).thenReturn((List)Arrays.asList(parameterTypes));
        
        if(isVirtual) {
            virtualMethods.add(method);
        } else {
            directMethods.add(method);
        } 
    }

    @SuppressWarnings("unchecked")
    private ClassDef createClassDef() {
        ClassDef classdef = mock(ClassDef.class);
        when(classdef.getVirtualMethods()).thenReturn((Iterable)virtualMethods);
        when(classdef.getDirectMethods()) .thenReturn((Iterable)directMethods);
        return classdef; 
    }
    
    @SuppressWarnings("unchecked")
    private DexBackedClassDef createClassDefWithDexBackend() {
        DexBackedClassDef classdef = mock(DexBackedClassDef.class);
        when(classdef.getVirtualMethods(any(Boolean.class))).thenReturn((Iterable)virtualMethods);
        when(classdef.getDirectMethods(any(Boolean.class))) .thenReturn((Iterable)directMethods);
        return classdef; 
    }
}
