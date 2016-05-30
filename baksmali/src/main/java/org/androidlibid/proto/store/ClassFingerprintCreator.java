package org.androidlibid.proto.store;

import java.io.IOException;
import java.util.Map;
import org.androidlibid.proto.ast.Node;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.SmaliNameConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ClassFingerprintCreator {
    
    private final ASTToFingerprintTransformer ast2fpt;
    
    private static final Logger LOGGER = LogManager.getLogger(ClassFingerprintCreator.class);

    public ClassFingerprintCreator(ASTToFingerprintTransformer ast2fpt) {
        this.ast2fpt = ast2fpt;
    }

    public Fingerprint createClassFingerprint(Map<String, Node> methodASTs, 
            String smaliClassName, boolean addMethodFingerprintsAsChildren) throws IOException {
        
//        String className = SmaliNameConverter.convertTypeFromSmali(smaliClassName);
//        MethodFingerprint classFingerprint = new MethodFingerprint(className);
//                
//        for(String methodSignature : methodASTs.keySet()) {
//            
//            Node node = methodASTs.get(methodSignature);
//            MethodFingerprint methodFingerprint = ast2fpt.createFingerprint(node);
//            
//            logMethod(methodSignature, node, methodFingerprint);
//            
//            if(methodFingerprint.getLength() > 1.0d) {
//                classFingerprint.sumFeatures(methodFingerprint);
//                
//                if(addMethodFingerprintsAsChildren) {
//                    methodFingerprint.setName(className + ":" + methodSignature);
//                    classFingerprint.addChildFingerprint(methodFingerprint);
//                }
//            } 
//        }
        
//        return classFingerprint;
        return null;
    }

    private void logMethod(String methodSignature, Node ast, Fingerprint methodFingerprint) {
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("* {}", methodSignature);
            LOGGER.debug("** ast" );
            LOGGER.debug(ast);
            LOGGER.debug("** fingerprint" );
            LOGGER.debug(methodFingerprint);
        }
    }
}
