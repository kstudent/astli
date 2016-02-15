/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import java.util.ArrayList;
import java.util.List;
import org.androidlibid.proto.ao.FingerprintEntity;
import org.androidlibid.proto.ao.FingerprintService;
import org.junit.Before;
import org.junit.Test;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintMatcherTest {
    
    List<FingerprintEntity> fingerprints;
    FingerprintEntity entity1;
    FingerprintEntity entity2;
    FingerprintEntity entity3;
    FingerprintEntity needleEntity;
    FingerprintService service;
    
    @Before
    public void setUp() {
        entity1 = mock(FingerprintEntity.class);
        entity2 = mock(FingerprintEntity.class);
        entity3 = mock(FingerprintEntity.class);
        fingerprints = new ArrayList<>();
        fingerprints.add(entity1);
        fingerprints.add(entity2);
        fingerprints.add(entity3);
        service = mock(FingerprintService.class);
        when(service.getFingerprintEntities()).thenReturn(fingerprints);
        needleEntity = mock(FingerprintEntity.class);
    }
    
    @Test 
    public void testMatchSortedFingerprint() {
        
        Vector v1 = new BasicVector(3);
        v1.set(0, 1.0);
        v1.set(1, 2.0);
        v1.set(2, 3.0);
        when(entity1.getVector()).thenReturn(v1.toBinary());

        Vector v2 = new BasicVector(3);
        v2.set(0, 1.0);
        v2.set(1, 2.0);
        v2.set(2, 4.0);
        when(entity2.getVector()).thenReturn(v2.toBinary());

        Vector v3 = new BasicVector(3);
        v3.set(0, 1.0);
        v3.set(1, 2.0);
        v3.set(2, 6.0);
        when(entity3.getVector()).thenReturn(v3.toBinary());
        
        Vector needleVector = new BasicVector(3);
        needleVector.set(0, 1.0);
        needleVector.set(1, 2.0);
        needleVector.set(2, 4.0);
        when(needleEntity.getVector()).thenReturn(needleVector.toBinary());

        Fingerprint needle = new Fingerprint(needleEntity);
        FingerprintMatcher matcher = new FingerprintMatcher(service);
                
        List<Fingerprint> matchedPrints = matcher.matchFingerprints(needle).getMatchesByDistance();
        
        double diffto0 = needle.euclideanDiff(matchedPrints.get(0));
        double diffto1 = needle.euclideanDiff(matchedPrints.get(1));
        double diffto2 = needle.euclideanDiff(matchedPrints.get(2));
        
        assert(diffto0 <= diffto1);
        assert(diffto1 <= diffto2);
        assert(matchedPrints.get(0).getVector().equals(v2));
        assert(matchedPrints.get(1).getVector().equals(v1));
        assert(matchedPrints.get(2).getVector().equals(v3));
        
    }
    
    @Test 
    public void testMatchEqualFingerprints() {
        Vector v1 = new BasicVector(3);
        v1.set(0, 1.0);
        v1.set(1, 2.0);
        v1.set(2, 3.0);
        when(entity1.getVector()).thenReturn(v1.toBinary());

        Vector v2 = new BasicVector(3);
        v2.set(0, 1.0);
        v2.set(1, 2.0);
        v2.set(2, 3.0);
        when(entity2.getVector()).thenReturn(v2.toBinary());

        Vector v3 = new BasicVector(3);
        v3.set(0, 1.0);
        v3.set(1, 2.0);
        v3.set(2, 3.0);
        when(entity3.getVector()).thenReturn(v3.toBinary());
        
        Vector needleVector = new BasicVector(3);
        needleVector.set(0, 1.0);
        needleVector.set(1, 2.0);
        needleVector.set(2, 3.0);
        when(needleEntity.getVector()).thenReturn(needleVector.toBinary());

        Fingerprint needle = new Fingerprint(needleEntity);
        FingerprintMatcher matcher = new FingerprintMatcher(service);
                
        List<Fingerprint> matchedPrints = matcher.matchFingerprints(needle).getMatchesByDistance();
        
        double diffto0 = needle.euclideanDiff(matchedPrints.get(0));
        double diffto1 = needle.euclideanDiff(matchedPrints.get(1));
        double diffto2 = needle.euclideanDiff(matchedPrints.get(2));
        
        assert(diffto0 == diffto1);
        assert(diffto1 == diffto2);
    }
}
