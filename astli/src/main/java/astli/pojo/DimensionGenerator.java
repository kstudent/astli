package astli.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class DimensionGenerator {
    
    public List<List<NodeType>> generate() {
        List<List<NodeType>> dimensions = new ArrayList<>();
        dimensions.add(createFeature(NodeType.VRT));
        dimensions.add(createFeature(NodeType.DRC));
        dimensions.add(createFeature(NodeType.PAR));
        dimensions.add(createFeature(NodeType.LOC));
        dimensions.add(createFeature(NodeType.VRT, NodeType.PAR));
        dimensions.add(createFeature(NodeType.VRT, NodeType.LOC));
        dimensions.add(createFeature(NodeType.DRC, NodeType.PAR));
        dimensions.add(createFeature(NodeType.DRC, NodeType.LOC));
        dimensions.add(createFeature(NodeType.LOC, NodeType.LOC));
        dimensions.add(createFeature(NodeType.PAR, NodeType.PAR));
        return dimensions;
    }
    
    private List<NodeType> createFeature(NodeType... features) {
        return Arrays.asList(features);
    } 
}
