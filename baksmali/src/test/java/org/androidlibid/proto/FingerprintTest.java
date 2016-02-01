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
        
        List<NodeType> dimension = new ArrayList<>();
        dimension.add(NodeType.METHOD);
        
        assert(f.getFeatureCount(dimension) == 0);
        f.incrementFeature(dimension);
        assert(f.getFeatureCount(dimension) == 1);
        
        dimension.add(NodeType.VIRTUAL);
        assert(f.getFeatureCount(dimension) == 0);
        f.incrementFeature(dimension);
        f.incrementFeature(dimension);
        assert(f.getFeatureCount(dimension) == 2);
        
        dimension.add(NodeType.LOCAL);
        assert(f.getFeatureCount(dimension) == 0);
        f.incrementFeature(dimension);
        assert(f.getFeatureCount(dimension) == 1);
        f.incrementFeature(dimension);
        assert(f.getFeatureCount(dimension) == 2);
        
        dimension = new ArrayList<>();
        dimension.add(NodeType.PARAMETER);
        dimension.add(NodeType.LOCAL);
        assert(f.getFeatureCount(dimension) == 0);
        f.incrementFeature(dimension);
        assert(f.getFeatureCount(dimension) == 1);
        f.incrementFeature(dimension);
        assert(f.getFeatureCount(dimension) == 2);
        
        System.out.println(f.toString());
        
    }
}
