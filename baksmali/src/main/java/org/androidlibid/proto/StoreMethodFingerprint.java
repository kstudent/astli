package org.androidlibid.proto;

import com.google.common.collect.Multimap;
import java.util.Map;
import org.androidlibid.proto.ast.Node;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.ASTClassDefinition;
import java.util.concurrent.Callable;
import org.androidlibid.proto.ao.Clazz;
import org.androidlibid.proto.ao.EntityService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    
    private static final Logger LOGGER = LogManager.getLogger(StoreMethodFingerprint.class);

    public StoreMethodFingerprint(ClassDef classDef, baksmaliOptions options, EntityService service) {
        this.classDef = classDef;
        this.options = options;
        this.service = service;
    }
    
    @Override public Void call() throws Exception {
        
        String className     = transformClassName(classDef.getType());
                
        if(!className.equals("org.spongycastle.crypto.agreement.kdf.DHKEKGenerator")) {
            return null; 
        }
        
        ASTClassDefinition classDefinition = new ASTClassDefinition(options, classDef);
        
        Multimap<String, Node> ast = classDefinition.createASTwithNames();
        
        ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();
        
        String packageName   = extractPackageName(className);
        String mvnIdentifier = options.mvnIdentifier;

        Fingerprint classFingerprint = new Fingerprint(className);
        
        for(Map.Entry<String, Node> entry : ast.entries()) {
            String methodName = entry.getKey();
            Node   node = entry.getValue();
        
            Fingerprint methodFingerprint = ast2fpt.createFingerprint(node);
            
            LOGGER.info("* {}", methodName);
            LOGGER.info("** ast" );
            LOGGER.info(ast.get(methodName));
            LOGGER.info("** fingerprint" );
            LOGGER.info(methodFingerprint);
            
            if(methodFingerprint.euclideanNorm() > 1.0d) {
                methodFingerprint.setName(className + ":" + methodName);
                classFingerprint.add(methodFingerprint);
                classFingerprint.addChild(methodFingerprint);
            }
        }
        
        if(classFingerprint.euclideanNorm() > 0.0d) {
            Clazz clazz = service.saveClass(classFingerprint.getVector().toBinary(), className, packageName, mvnIdentifier);
            
            for(Fingerprint method : classFingerprint.getChildren()) {
                service.saveMethod(method.getVector().toBinary(), method.getName(), method.euclideanNorm(), clazz);
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
