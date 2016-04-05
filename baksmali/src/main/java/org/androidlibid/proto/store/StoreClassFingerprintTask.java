package org.androidlibid.proto.store;

import java.util.Collection;
import org.androidlibid.proto.ast.Node;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.ASTClassDefinition;
import java.util.concurrent.Callable;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.SmaliNameConverter;
import org.androidlibid.proto.ao.EntityService;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StoreClassFingerprintTask implements Callable<Void> {
    
    private final ClassDef classDef;
    private final baksmaliOptions options;
    private final EntityService service;

    public StoreClassFingerprintTask(ClassDef classDef, baksmaliOptions options, EntityService service) {
        this.classDef = classDef;
        this.options = options;
        this.service = service;
    }
    
    @Override public Void call() throws Exception {
        
        ASTClassDefinition classDefinition = new ASTClassDefinition(options, classDef);
        Iterable<Node> ast = classDefinition.createASTwithNames().values();
        ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();
        
        String className     = SmaliNameConverter.convertTypeFromSmali(classDef.getType());
        String packageName   = SmaliNameConverter.extractPackageNameFromClassName(className);
        String mvnIdentifier = options.mvnIdentifier;

        Fingerprint classFingerprint = new Fingerprint(className);
        
        for(Node node : ast) {
            Fingerprint methodFingerprint = ast2fpt.createFingerprint(node);
                classFingerprint.add(methodFingerprint);
            }
        
        if(classFingerprint.euclideanNorm() > 0.0d) {
            service.saveClass(classFingerprint.getVector().toBinary(), className, packageName, mvnIdentifier);
        }
        
        return null;
    }
}
