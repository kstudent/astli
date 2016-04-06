package org.androidlibid.proto.ast;

import org.jf.baksmali.Adaptors.MethodDefinitionImpl;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ASTBuilderFactory {
    
    public ASTMethodDefinition createASTBuilder(
            ASTClassDefinition classDefinition, 
            Method method, 
            MethodImplementation methodImpl, 
            boolean noParameterRegisters) {
        
        return new ASTMethodDefinition(new MethodDefinitionImpl(
                classDefinition, method, methodImpl), 
            noParameterRegisters);
        
    }
}
