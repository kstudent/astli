package org.androidlibid.proto;

import java.util.Map;
import org.androidlibid.proto.ast.Node;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.ASTClassDefinition;
import java.util.concurrent.Callable;
import org.androidlibid.proto.ao.Clazz;
import org.androidlibid.proto.ao.EntityService;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StoreMethodFingerprint implements Callable<Void> {
    
    private final ClassDef classDef;
    private final baksmaliOptions options;
    private final EntityService service;

    public StoreMethodFingerprint(ClassDef classDef, baksmaliOptions options, EntityService service) {
        this.classDef = classDef;
        this.options = options;
        this.service = service;
    }
    
    @Override public Void call() throws Exception {
        
        ASTClassDefinition classDefinition = new ASTClassDefinition(options, classDef);
        
        Map<String, Node> ast = classDefinition.createASTwithNames();
        
        ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();
        
        String className     = transformClassName(classDef.getType());
        String packageName   = extractPackageName(className);
        String mvnIdentifier = options.mvnIdentifier;

        Fingerprint classFingerprint = new Fingerprint(className);
                
        for(String methodName : ast.keySet()) {
            Fingerprint methodFingerprint = ast2fpt.createFingerprint(ast.get(methodName));
            if(methodFingerprint.euclideanNorm() > 1.0d) {
                methodFingerprint.setName(className + ":" + methodName);
                classFingerprint.add(methodFingerprint);
                classFingerprint.addChild(methodFingerprint);
            }
        }
        
        if(classFingerprint.euclideanNorm() > 0.0d) {
            Clazz clazz = service.saveClass(classFingerprint.getVector().toBinary(), className, packageName, mvnIdentifier);
            
            for(Fingerprint method : classFingerprint.getChildren()) {
                service.saveMethod(method.getVector().toBinary(), method.getName(), clazz);
            }
        }
        
        return null;
    }
    
    public String extractPackageName(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public String transformClassName(String className) {
        className = className.replace('/', '.');
        return className.substring(1, className.length() - 1);
    }
    
}
