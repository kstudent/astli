/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import org.androidlibid.proto.ast.NodeType;
import org.androidlibid.proto.ast.Node;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprinterTest {
    
    @Test
    public void testFingerprinter() {
    
        Node root   = new Node(NodeType.METHOD);
        Node child1 = new Node(NodeType.ARGUMENT);
        Node child2 = new Node(NodeType.DIRECT);
        Node child3 = new Node(NodeType.VIRTUAL);
        Node child4 = new Node(NodeType.LOCAL);
        Node child5 = new Node(NodeType.LOCAL);
        Node child6 = new Node(NodeType.PARAMETER);

        root.addChild(child1);
        root.addChild(child2);
        root.addChild(child3);
        child2.addChild(child4);
        child3.addChild(child5);
        child3.addChild(child6);
        
        ASTToFingerprintTransformer fingerPrinter = new ASTToFingerprintTransformer();
        
        Fingerprint fingerprint = fingerPrinter.createFingerprint(root);
        
        System.out.println(root);
        System.out.println(fingerprint);
        
        
    }
}
