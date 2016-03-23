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

import org.androidlibid.proto.ao.VectorEntity;
import org.androidlibid.proto.ast.NodeType;
import org.junit.Before;
import org.junit.Test;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;
import org.mockito.Mockito;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintTest {

    private VectorEntity vector;

    @Before
    public void setUp() {
        vector = Mockito.mock(VectorEntity.class);
        Mockito.when(vector.getName()).thenReturn("vector x");
    }
    
    @Test
    public void testFingerprint() {
                
        Mockito.when(vector.getVector()).thenReturn(null);
                
        Fingerprint f = new Fingerprint(vector);
        
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
        
    }
    
    @Test
    public void testFingerprintWithNullVectorEntity() {
        Mockito.when(vector.getVector()).thenReturn(null);
        Fingerprint f = new Fingerprint(vector);
        Vector fvector = f.getVector();
        assert(fvector.length() == Fingerprint.getFeaturesSize());
    }

    @Test
    public void testFingerprintWithPredefinedSize() {
        int predefinedSize = 79;
        BasicVector predefinedVector = new BasicVector(predefinedSize);
        
        Mockito.when(vector.getVector()).thenReturn(predefinedVector.toBinary());
        Fingerprint f = new Fingerprint(vector);
        
        Vector fvector = f.getVector();
                
        assert(fvector.length() == predefinedSize);
        assert(fvector.length() != Fingerprint.getFeaturesSize());
        assert(predefinedSize   != Fingerprint.getFeaturesSize());
    }
    
    @Test
    public void testAdd() {
        Mockito.when(vector.getVector()).thenReturn(null);
        
        Fingerprint f1 = new Fingerprint(vector);
        Fingerprint f2 = new Fingerprint(vector);
        assert(f1.getFeatureCount(NodeType.METHOD) == 0);
        f1.incrementFeature(NodeType.METHOD);
        assert(f1.getFeatureCount(NodeType.METHOD) == 1);
        f2.incrementFeature(NodeType.METHOD);
        assert(f2.getFeatureCount(NodeType.METHOD) == 1);
        f1.add(f2);
        assert(f1.getFeatureCount(NodeType.METHOD) == 2);    
    }
    
    @Test 
    public void testSimilarityBetweenEqualVectors() {
        Fingerprint f1 = new Fingerprint(1, 1);
        Fingerprint f2 = new Fingerprint(1, 1);
        double similarity = f1.computeSimilarityScore(f2);
        assert(doubleEquals(similarity, Math.sqrt(2)));
    }
    
    @Test 
    public void testSimilarityBetweenCloseVectors() {
        Fingerprint f1 = new Fingerprint( 1, 2);
        Fingerprint f2 = new Fingerprint(.5, 1);
        
        double similarity = f1.computeSimilarityScore(f2);
        double expectedSimilarity = Math.max(f1.euclideanNorm(), f2.euclideanNorm()) - f1.euclideanDiff(f2);
        assert(doubleEquals(similarity, expectedSimilarity));
    }
    
    @Test 
    public void testSimilarityBetweenDistantVectors() {
        Fingerprint f1 = new Fingerprint(  1,  2);
        Fingerprint f2 = new Fingerprint(-.5, -1);
        double similarity = f1.computeSimilarityScore(f2);
        assert(doubleEquals(similarity, 0));
    }
    
    @Test 
    public void testSimilarityReflexivity() {
        Fingerprint f1 = new Fingerprint(  49,     35);
        Fingerprint f2 = new Fingerprint(  17,  8.014);
        double similarity1 = f1.computeSimilarityScore(f2);
        double similarity2 = f2.computeSimilarityScore(f1);
        assert(doubleEquals(similarity1, similarity2));
    }
    
    private boolean doubleEquals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }

}
