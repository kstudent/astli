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
package org.androidlibid.proto;

import java.util.ArrayList;
import java.util.LinkedList;
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

        System.out.println(root.toString());
    }

    @Test
    public void testHorizontalFeatures() {
        System.out.println("horizontal features: ");
        
        FeatureGenerator fg = new FeatureGenerator();
        
        for(List<NodeType> feature : fg.generateHorizontalFeatures()) {
            System.out.println(feature);
        }
    }
    
    @Test
    public void testVerticalFeatures() {
        System.out.println("vertical features: ");
        
        FeatureGenerator fg = new FeatureGenerator();
        
        for(List<NodeType> feature : fg.generateVerticalFeatures()) {
            System.out.println(feature);
        }
    }

    @Test
    public void testEqualsOnLists() {
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new LinkedList<>();
        
        list1.add(new Integer(17));
        list1.add(new Integer(18));
        list1.add(new Integer(19));
        
        list2.add(new Integer(17));
        list2.add(new Integer(18));
        list2.add(new Integer(19));
        
        assert(list1.equals(list2));
        
    
    }
    
}
