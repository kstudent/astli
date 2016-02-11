/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import java.util.List;
import java.util.concurrent.Callable;
import org.androidlibid.proto.ao.FingerprintService;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchClassFingerprintTask implements Callable<Boolean>{
    
    private final ClassDef classDef;
    private final baksmaliOptions options;
    private final FingerprintService service;

    public MatchClassFingerprintTask(ClassDef classDef, baksmaliOptions options, FingerprintService service) {
        this.classDef = classDef;
        this.options = options;
        this.service = service;
    }
    
    @Override public Boolean call() throws Exception {
        ASTClassDefinition classDefinition = new ASTClassDefinition(options, classDef);
        List<Node> ast = classDefinition.createAST();
        
        ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();
        
        Fingerprint needle = new Fingerprint();
        
        for(Node node : ast) {
            Fingerprint methodFingerprint = ast2fpt.createFingerprint(node);
            needle.add(methodFingerprint);
        }
        
        if (needle.euclideanNorm() == 0.0d) {
            System.out.println("No Match for " + classDef.getType() + " because too small :(");
        }
        
        FingerprintMatcher matcher = new FingerprintMatcher(service);
        
        System.out.println("Matches for " + classDef.getType());
        
        int i = 0; 
        for(Fingerprint match : matcher.matchFingerprints(needle)) {
            System.out.println("   " + match.getName() + " (" + match.euclideanDiff(needle) + ")");
            if (i++ > 10) {
                System.out.println("...");
                break;
            }
        }
        
        return true;
    }
}
