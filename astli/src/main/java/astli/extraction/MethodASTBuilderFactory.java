package astli.extraction;

import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.Adaptors.ClassDefinitionImpl;
import org.jf.baksmali.Adaptors.MethodDefinition;
import org.jf.baksmali.Adaptors.MethodDefinitionImpl;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MethodASTBuilderFactory {
    
    private final baksmaliOptions options;

    public MethodASTBuilderFactory(baksmaliOptions options) {
        this.options = options;
    }
    
    public MethodASTBuilder create(
            ClassDef classDef, 
            Method method, 
            MethodImplementation methodImpl) {
        
        ClassDefinition classDefinition   = new ClassDefinitionImpl(options, classDef);
        MethodDefinition methodDefinition = new MethodDefinitionImpl(classDefinition, method, methodImpl);
        
        return new MethodASTBuilder(methodDefinition, options.noParameterRegisters, classDef.getType());
    }
}
