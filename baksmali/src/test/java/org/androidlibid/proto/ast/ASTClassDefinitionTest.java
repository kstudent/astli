package org.androidlibid.proto.ast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ASTClassDefinitionTest {
    
    private baksmaliOptions options;
    
    private List<Method> virtualMethods;
    private List<Method> directMethods;
    private ASTBuilderFactory factory;
    private ASTBuilder astBuilder;
    
    private static final Logger LOGGER = LogManager.getLogger(ASTClassDefinitionTest.class);
    
    @Before
    public void setUp() throws IOException {
        options    = new baksmaliOptions();
        factory    = mock(ASTBuilderFactory.class);
        astBuilder = mock(ASTBuilder.class);
        
        virtualMethods = new ArrayList<Method>();
        directMethods  = new ArrayList<Method>();
        
        when(factory.createASTBuilder(
                any(ASTClassDefinition.class), 
                any(Method.class), 
                any(MethodImplementation.class), 
                any(Boolean.class)))
            .thenReturn(astBuilder);
        when(astBuilder.buildAST()).thenReturn(new Node(NodeType.METHOD));
    }
        
    @Test
    public void testASTClassDef() throws IOException {
        ClassDef classDef = createClassDef();
        
        ASTClassDefinition astClassDef = new ASTClassDefinition(options, classDef, factory);
        
        addMethod(true,  "doVirtualSuchAndSuch", "I",                "I", "Z");
        addMethod(false, "doDirectSuchAndSuch",  "Ltld/pckg/Class;", "I", "Ltld/pckg/AnotherClass;");
        
        Map<String, Node> asts = astClassDef.createASTwithNames();
        
        for(String signature : asts.keySet()) {
            LOGGER.info("{} : {}", signature, asts.get(signature));
        }
                
        Node virtualMethod = asts.get("doVirtualSuchAndSuch(int,boolean):int");
        Node directMethod  = asts.get("doDirectSuchAndSuch(int,tld.pckg:AnotherClass):tld.pckg:Class");
        
        assert(asts.size() == 2); 
        assert(virtualMethod != null);
        assert(virtualMethod.getType().equals(NodeType.METHOD));
        assert(directMethod != null);
        assert(directMethod.getType().equals(NodeType.METHOD));
    }
    
    @Test
    public void testASTClassDefwithDexBackendClassDef() throws IOException {
        ClassDef classDef = createClassDefWithDexBackend();
        
        ASTClassDefinition astClassDef = new ASTClassDefinition(options, classDef, factory);
        addMethod(true,  "doVirtualSuchAndSuch", "I",                "I", "Z");
        addMethod(false, "doDirectSuchAndSuch",  "Ltld/pckg/Class;", "I", "Ltld/pckg/AnotherClass;");
        
        Map<String, Node> asts = astClassDef.createASTwithNames();
        
        for(String signature : asts.keySet()) {
            LOGGER.info("{} : {}", signature, asts.get(signature));
        }
                
        Node virtualMethod = asts.get("doVirtualSuchAndSuch(int,boolean):int");
        Node directMethod  = asts.get("doDirectSuchAndSuch(int,tld.pckg:AnotherClass):tld.pckg:Class");
        
        assert(asts.size() == 2); 
        assert(virtualMethod != null);
        assert(virtualMethod.getType().equals(NodeType.METHOD));
        assert(directMethod != null);
        assert(directMethod.getType().equals(NodeType.METHOD));
    }
    
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

    private ClassDef createClassDef() {
        ClassDef classdef = mock(ClassDef.class);
        when(classdef.getVirtualMethods()).thenReturn((Iterable)virtualMethods);
        when(classdef.getDirectMethods()) .thenReturn((Iterable)directMethods);
        return classdef; 
    }
    
    private DexBackedClassDef createClassDefWithDexBackend() {
        DexBackedClassDef classdef = mock(DexBackedClassDef.class);
        when(classdef.getVirtualMethods(any(Boolean.class))).thenReturn((Iterable)virtualMethods);
        when(classdef.getDirectMethods(any(Boolean.class))) .thenReturn((Iterable)directMethods);
        return classdef; 
    }
}
