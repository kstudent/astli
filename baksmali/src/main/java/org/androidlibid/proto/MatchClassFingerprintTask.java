/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.ASTClassDefinition;
import java.util.List;
import java.util.concurrent.Callable;
import org.androidlibid.proto.ao.ClassEntityService;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchClassFingerprintTask implements Callable<FingerPrintMatchTaskResult>{
    
    private final ClassDef classDef;
    private final baksmaliOptions options;
    private final ClassEntityService service;

    public MatchClassFingerprintTask(ClassDef classDef, baksmaliOptions options, ClassEntityService service) {
        this.classDef = classDef;
        this.options = options;
        this.service = service;
    }
    
    @Override public FingerPrintMatchTaskResult call() throws Exception {
        String name = classDef.getType();
        
        ASTClassDefinition classDefinition = new ASTClassDefinition(options, classDef);
        List<Node> ast = classDefinition.createAST();
        
        ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();
        
        Fingerprint needle = new Fingerprint();
        
        for(Node node : ast) {
            Fingerprint methodFingerprint = ast2fpt.createFingerprint(node);
            needle.add(methodFingerprint);
        }
        
        needle.setName(name);
        
        if (needle.euclideanNorm() == 0.0d) {
            System.out.println(name + ": class length 0");
            return FingerPrintMatchTaskResult.CLASS_LENGTH_0;
        }
        
        FingerprintMatcher matcher = new FingerprintMatcher(service);
        
        FingerprintMatcher.FingerprintMatcherResult result = matcher.matchFingerprints(needle);
        
        Fingerprint nameMatch = result.getMatchByName();
        List<Fingerprint> matchesByDistance = result.getMatchesByDistance();

        if(nameMatch == null) {
            System.out.println(name + ": not mached by name");
            return FingerPrintMatchTaskResult.NO_MATCH_BY_NAME;
        } else {
            
            int i;
            
            for (i = 0; i < matchesByDistance.size(); i++) {
                if(matchesByDistance.get(i).getName().equals(name)) {
                    break;
                }
            }
            
            if(i == matchesByDistance.size()) {
                System.out.println(name + ": not mached by distance.");
                System.out.println(needle);
                System.out.println(nameMatch);
                return FingerPrintMatchTaskResult.NO_MATCH_BY_DISTANCE;
            } else if(i > 0) {
                System.out.println(name + ": found at position " + i);
                return FingerPrintMatchTaskResult.NOT_PERFECT;
            } else {
//                System.out.println(name + ": found first.");
                return FingerPrintMatchTaskResult.OK;
            }
        }
    }
}
