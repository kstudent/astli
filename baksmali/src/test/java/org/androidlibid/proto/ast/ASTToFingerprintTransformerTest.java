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
        
        ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();
        
        Fingerprint fingerprint = ast2fpt.createFingerprint(root);
        
        assert(doubleEquals(fingerprint.getFeatureCount(NodeType.METHOD), 1));
        assert(doubleEquals(fingerprint.getFeatureCount(NodeType.VIRTUAL), 1));
        assert(doubleEquals(fingerprint.getFeatureCount(NodeType.DIRECT), 1));
        assert(doubleEquals(fingerprint.getFeatureCount(NodeType.ARGUMENT), 1));
        assert(doubleEquals(fingerprint.getFeatureCount(NodeType.PARAMETER), 1));
        assert(doubleEquals(fingerprint.getFeatureCount(NodeType.LOCAL), 2));
        assert(doubleEquals(fingerprint.getFeatureCount(NodeType.VIRTUAL, NodeType.LOCAL), 1));
        assert(doubleEquals(fingerprint.getFeatureCount(NodeType.DIRECT, NodeType.LOCAL), 1));
        assert(doubleEquals(fingerprint.getFeatureCount(NodeType.PARAMETER, NodeType.LOCAL), 1));
        assert(doubleEquals(fingerprint.getFeatureCount(NodeType.LOCAL, NodeType.PARAMETER), 1));
        
    }

    private boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
}
