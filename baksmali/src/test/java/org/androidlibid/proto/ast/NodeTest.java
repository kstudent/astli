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
        Node root = new Node(NodeType.MTH);
        Node child2 = new Node(NodeType.DRC);
        Node child3 = new Node(NodeType.VRT);
        Node child4 = new Node(NodeType.LOC);
        Node child5 = new Node(NodeType.LOC);
        Node child6 = new Node(NodeType.PAR);

        root.addChild(child2);
        root.addChild(child3);
        child2.addChild(child4);
        child3.addChild(child5);
        child3.addChild(child6);

        String rootString = root.toString();
        
        String expectedString = 
            "method\n" +
            "  signature:\n" +
            "  direct\n" +
            "    local\n" +
            "  virtual\n" +
            "    local\n" +
            "    parameter\n";
        
        assert(rootString.equals(expectedString));
    }

    private List<NodeType> createFeature(NodeType... types) {
        return Arrays.asList(types);
    }
}
