package org.androidlibid.proto.ast;

import java.util.LinkedList;
import java.util.List;
import org.androidlibid.proto.Fingerprint;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ASTToFingerprintTransformer {
    
    public Fingerprint createFingerprint(Node root) {
        Fingerprint fingerprint = new Fingerprint();
        
        countVerticalFeatures(root, fingerprint);
        countHorizontalFeatures(root, fingerprint);
        
        return fingerprint;
    } 
    
    private void countVerticalFeatures(Node current, Fingerprint fingerprint) {
    
        generateVerticalFeature(current, fingerprint);
        
        for (Node child : current.getChildren()) {
            countVerticalFeatures(child, fingerprint);
        }
        
    }
    
    private void generateVerticalFeature(Node current, Fingerprint fingerprint) {
        
        if(current.getParent() == null) {
            fingerprint.incrementFeature(current.getType());
        } else {
            LinkedList<NodeType> feature = new LinkedList<>();

            feature.addFirst(current.getType());
            fingerprint.incrementFeature(feature);

            while (current.getParent().getParent() != null) {
                current = current.getParent();
                feature.addFirst(current.getType());
                fingerprint.incrementFeature(feature);
            }
        }
    }

    private void countHorizontalFeatures(Node current, Fingerprint fingerprint) {
        for (Node child1 : current.getChildren()) {
            
            if(child1.getType().isLeaf()) {
                List<Node> otherChildren = new LinkedList<>(current.getChildren());
                otherChildren.remove(child1);

                for (Node child2 : otherChildren) {
                    if(child2.getType().isLeaf()) {
                        fingerprint.incrementFeature(child1.getType(), child2.getType());
                    }
                }
            } else {
                countHorizontalFeatures(child1, fingerprint);
            }
        }
    }
}


