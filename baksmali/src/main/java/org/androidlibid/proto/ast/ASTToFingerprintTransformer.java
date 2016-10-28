package org.androidlibid.proto.ast;

import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ASTToFingerprintTransformer {
    
    public Fingerprint createFingerprint(Node root) {
        Fingerprint fingerprint = new Fingerprint();

        countVerticalFeatures(fingerprint, root);
        countHorizontalFeatures(fingerprint, root);
        setFingeprintSignature(fingerprint, root);
        
        return fingerprint;
    } 
    
    private void countVerticalFeatures(Fingerprint print, Node root) {
    
        for(Node lvl1 : root.getChildren()) {
            
            if (!lvl1.getType().equals(NodeType.SGN)) {
                print.incrementFeature(lvl1.getType());
            }
            
            for(Node lvl2 : lvl1.getChildren()) {
                print.incrementFeature(lvl2.getType());
                print.incrementFeature(lvl1.getType(), lvl2.getType());
            }
        }
        
    }
    
    private void countHorizontalFeatures(Fingerprint print, Node root) {
        
        for(Node lvl1 : root.getChildren()) { 
            
            int locals = 0, params = 0;
            
            for(Node lvl2 : lvl1.getChildren()) {
                if(lvl2.getType().equals(NodeType.LOC)) locals++;
                if(lvl2.getType().equals(NodeType.PAR)) params++;
            }
            
            int llPairs = fromNChoose2(locals); 
            int ppPairs = fromNChoose2(params); 
            
            print.incrementFeatureBy(llPairs, NodeType.LOC, NodeType.LOC);
            print.incrementFeatureBy(ppPairs, NodeType.PAR, NodeType.PAR);
            
        }
    }
    
    private int fromNChoose2(int n){
        return (n * (n-1)) / 2;
    }   

    private void setFingeprintSignature(Fingerprint fingerprint, Node root) {
        
        Node signatureNode = root.getChildren().get(0);
        
        if(!signatureNode.getType().equals(NodeType.SGN)) {
            throw new RuntimeException("First Child of AST root should be the "
                    + "signature node!");
        }
        
        fingerprint.setSignature(signatureNode.getSignature());
        
    }
}


