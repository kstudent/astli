package org.androidlibid.proto.ast;

import org.androidlibid.proto.Fingerprint;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ASTToFingerprintTransformerTest {
    
    @Test
    public void testASTToFingerprintTransformer() {
    
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
