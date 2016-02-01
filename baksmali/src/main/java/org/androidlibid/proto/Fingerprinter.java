/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.la4j.Vector;
import org.la4j.vector.VectorFactory;
import org.la4j.vector.dense.BasicVector;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class Fingerprinter {
    
    public Fingerprinter() {
    
    }; 
    
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
        LinkedList<NodeType> feature = new LinkedList<>();
        
        do {
            feature.addFirst(current.getType());
            fingerprint.incrementFeature(feature);
            current = current.getParent();
        }
        while(current != null);
        
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


