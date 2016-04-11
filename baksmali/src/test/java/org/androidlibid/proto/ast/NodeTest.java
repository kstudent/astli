/*
 * Copyright 2016 Christof Rabensteiner <christof.rabensteiner@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidlibid.proto.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class NodeTest {

    @Test
    public void testToString() {
        Node root = new Node(NodeType.METHOD);
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

        String rootString = root.toString();
        
        String expectedString = 
            "method\n" +
            "  argument\n" +
            "  direct\n" +
            "    local\n" +
            "  virtual\n" +
            "    local\n" +
            "    parameter\n";
        
        assert(rootString.equals(expectedString));
    }

    @Test
    public void testHorizontalFeatures() {
        FeatureGenerator fg = new FeatureGenerator();
        
        List<List<NodeType>> expectedFeatures = new ArrayList<>();
        expectedFeatures.add(createFeature(NodeType.ARGUMENT, NodeType.ARGUMENT));
        expectedFeatures.add(createFeature(NodeType.PARAMETER, NodeType.PARAMETER));
        expectedFeatures.add(createFeature(NodeType.PARAMETER, NodeType.LOCAL));
        expectedFeatures.add(createFeature(NodeType.LOCAL, NodeType.PARAMETER));
        expectedFeatures.add(createFeature(NodeType.LOCAL, NodeType.LOCAL));
        
        List<List<NodeType>> horizontalFeatures = fg.generateHorizontalFeatures();
        
        assert(horizontalFeatures.size() == expectedFeatures.size());
        
        for(List<NodeType> feature : horizontalFeatures) {
            assert(expectedFeatures.contains(feature));
        }
    }
    
    @Test
    public void testVerticalFeatures() {
        FeatureGenerator fg = new FeatureGenerator();
        
        List<List<NodeType>> expectedFeatures = new ArrayList<>();
        expectedFeatures.add(createFeature(NodeType.METHOD));
        expectedFeatures.add(createFeature(NodeType.VIRTUAL));
        expectedFeatures.add(createFeature(NodeType.DIRECT));
        expectedFeatures.add(createFeature(NodeType.ARGUMENT));
        expectedFeatures.add(createFeature(NodeType.PARAMETER));
        expectedFeatures.add(createFeature(NodeType.LOCAL));
        expectedFeatures.add(createFeature(NodeType.VIRTUAL, NodeType.PARAMETER));
        expectedFeatures.add(createFeature(NodeType.VIRTUAL, NodeType.LOCAL));
        expectedFeatures.add(createFeature(NodeType.DIRECT,  NodeType.PARAMETER));
        expectedFeatures.add(createFeature(NodeType.DIRECT,  NodeType.LOCAL));
        
        List<List<NodeType>> verticalFeatures = fg.generateVerticalFeatures();
        
        assert(verticalFeatures.size() == expectedFeatures.size());
        
        for(List<NodeType> feature : verticalFeatures) {
            assert(expectedFeatures.contains(feature));
        }
    }
    
    private List<NodeType> createFeature(NodeType... types) {
        return Arrays.asList(types);
    }
}
