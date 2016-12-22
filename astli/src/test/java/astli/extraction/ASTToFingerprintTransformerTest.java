package astli.extraction;

import astli.pojo.Fingerprint;
import astli.pojo.NodeType;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ASTToFingerprintTransformerTest {
    
    @Test
    public void testASTToFingerprintTransformer() {
    
        Node root = new Node(NodeType.MTH);
        Node sig  = new Node("V:E");
        Node dir  = new Node(NodeType.DRC);
        Node vir  = new Node(NodeType.VRT);
        Node loc1 = new Node(NodeType.LOC);
        Node loc2 = new Node(NodeType.LOC);
        Node par  = new Node(NodeType.PAR);

        root.addChild(sig);
        root.addChild(dir);
        root.addChild(vir);
        dir.addChild(loc1);
        vir.addChild(loc2);
        vir.addChild(par);

        ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();

        Fingerprint print = ast2fpt.createFingerprint(root);

        assert(doubleEquals(print.getFeatureValue(NodeType.VRT), 1));
        assert(doubleEquals(print.getFeatureValue(NodeType.DRC), 1));
        assert(doubleEquals(print.getFeatureValue(NodeType.PAR), 1));
        assert(doubleEquals(print.getFeatureValue(NodeType.LOC), 2));
        assert(doubleEquals(print.getFeatureValue(NodeType.VRT, NodeType.LOC), 1));
        assert(doubleEquals(print.getFeatureValue(NodeType.VRT, NodeType.PAR), 1));
        assert(doubleEquals(print.getFeatureValue(NodeType.DRC, NodeType.LOC), 1));
        assert(doubleEquals(print.getFeatureValue(NodeType.DRC, NodeType.PAR), 0));
        assert(doubleEquals(print.getFeatureValue(NodeType.PAR, NodeType.PAR), 0));
        assert(doubleEquals(print.getFeatureValue(NodeType.LOC, NodeType.LOC), 0));
    }
    
    @Test
    public void testASTToFingerprintTransformerWithMorePairs() {
    
        Node root = new Node(NodeType.MTH);
        Node sig  = new Node("V:E");
        Node dir  = new Node(NodeType.DRC);
        Node loc1 = new Node(NodeType.LOC);
        Node loc2 = new Node(NodeType.LOC);
        Node par1 = new Node(NodeType.PAR);
        Node par2 = new Node(NodeType.PAR);
        Node par3 = new Node(NodeType.PAR);

        root.addChild(sig);
        root.addChild(dir);
        dir.addChild(loc1);
        dir.addChild(loc2);
        dir.addChild(par1);
        dir.addChild(par2);
        dir.addChild(par3);

        ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();
        
        Fingerprint print = ast2fpt.createFingerprint(root);
        
        assert(doubleEquals(print.getFeatureValue(NodeType.VRT), 0));
        assert(doubleEquals(print.getFeatureValue(NodeType.DRC), 1));
        assert(doubleEquals(print.getFeatureValue(NodeType.LOC), 2));
        assert(doubleEquals(print.getFeatureValue(NodeType.PAR), 3));
        assert(doubleEquals(print.getFeatureValue(NodeType.VRT, NodeType.LOC), 0));
        assert(doubleEquals(print.getFeatureValue(NodeType.VRT, NodeType.PAR), 0));
        assert(doubleEquals(print.getFeatureValue(NodeType.DRC, NodeType.LOC), 2));
        assert(doubleEquals(print.getFeatureValue(NodeType.DRC, NodeType.PAR), 3));
        assert(doubleEquals(print.getFeatureValue(NodeType.PAR, NodeType.PAR), 3));
        assert(doubleEquals(print.getFeatureValue(NodeType.LOC, NodeType.LOC), 1));
    }

    private boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
}
