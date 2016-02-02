/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import java.util.List;
import java.util.concurrent.Callable;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ClassFingerprintingTask implements Callable<Boolean>{
    
    private final ClassDef classDef;
    private final baksmaliOptions options;

    public ClassFingerprintingTask(ClassDef classDef, baksmaliOptions options) {
        this.classDef = classDef;
        this.options = options;
    }
    
    @Override public Boolean call() throws Exception {
        ASTClassDefinition classDefinition = new ASTClassDefinition(options, classDef);
        List<Node> ast = classDefinition.createAST();
        Fingerprinter fp = new Fingerprinter();
        
        Fingerprint classFingerprint = new Fingerprint();
        
        for(Node node : ast) {
            Fingerprint methodFingerprint = fp.createFingerprint(node);
            classFingerprint.add(methodFingerprint);
        }
        
        StringBuilder string = new StringBuilder();
        string = string.append("class ")
                        .append(classDef.getType())
                        .append(" has this fingerprint:\n")
                        .append(classFingerprint);
        
        synchronized (System.out) {
            System.out.println(string.toString());
        }


        
//        for(Node node : ast) {
//            Fingerprint fingerprint = fp.createFingerprint(node);
//            StringBuilder string = new StringBuilder();
//            string = string.append("begin of fingerprint ")
//                        .append(classDef.getType())
//                        .append(": \n")
//                        .append(fingerprint.toString())
//                        .append("\n");
//            
//            synchronized (System.out) {
//                System.out.println(string.toString());
//            }
//        }
        
        return true;
    }
}
