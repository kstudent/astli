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
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintTest {

    @Test
    public void testFingerprint() {
        Fingerprint f = new Fingerprint();
        
        
        assert(f.getFeatureCount(NodeType.METHOD) == 0);
        f.incrementFeature(NodeType.METHOD);
        assert(f.getFeatureCount(NodeType.METHOD) == 1);
        
        assert(f.getFeatureCount(NodeType.VIRTUAL) == 0);
        f.incrementFeature(NodeType.VIRTUAL);
        f.incrementFeature(NodeType.VIRTUAL);
        assert(f.getFeatureCount(NodeType.VIRTUAL) == 2);
        
        assert(f.getFeatureCount(NodeType.LOCAL) == 0);
        f.incrementFeature(NodeType.LOCAL);
        assert(f.getFeatureCount(NodeType.LOCAL) == 1);
        f.incrementFeature(NodeType.LOCAL);
        assert(f.getFeatureCount(NodeType.LOCAL) == 2);
        
        assert(f.getFeatureCount(NodeType.PARAMETER, NodeType.LOCAL) == 0);
        f.incrementFeature(NodeType.PARAMETER, NodeType.LOCAL);
        assert(f.getFeatureCount(NodeType.PARAMETER, NodeType.LOCAL) == 1);
        f.incrementFeature(NodeType.PARAMETER, NodeType.LOCAL);
        assert(f.getFeatureCount(NodeType.PARAMETER, NodeType.LOCAL) == 2);
        
        System.out.println(f.toString());
        
    }
    
    @Test
    public void testAdd() {
        Fingerprint f1 = new Fingerprint();
        Fingerprint f2 = new Fingerprint();
        assert(f1.getFeatureCount(NodeType.METHOD) == 0);
        f1.incrementFeature(NodeType.METHOD);
        assert(f1.getFeatureCount(NodeType.METHOD) == 1);
        f2.incrementFeature(NodeType.METHOD);
        assert(f2.getFeatureCount(NodeType.METHOD) == 1);
        f1.add(f2);
        assert(f1.getFeatureCount(NodeType.METHOD) == 2);    
    }
}
