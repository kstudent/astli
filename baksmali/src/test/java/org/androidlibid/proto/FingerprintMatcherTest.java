/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import java.sql.SQLException;
import java.util.ArrayList; 
import java.util.List;
import org.androidlibid.proto.ao.EntityService;
import org.junit.Before;
import org.junit.Test;
import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.androidlibid.proto.ao.VectorEntity;
import org.androidlibid.proto.ao.Clazz;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintMatcherTest {
    
    private List<Fingerprint>  fingerprints;
    private Clazz entity1;
    private Clazz entity2;
    private Clazz entity3;
    private Clazz needleEntity;
    private Fingerprint needle;
    private FingerprintMatcher matcher;
    private BasicVector v3;
    private BasicVector v2;
    private BasicVector v1;
    private BasicVector needleVector;
    
    @Before
    public void setUp() throws SQLException {
        entity1 = mock(Clazz.class);
        entity2 = mock(Clazz.class);
        entity3 = mock(Clazz.class);
        
        v1 = new BasicVector(3);
        v1.set(0, 1.0);
        v1.set(1, 2.0);
        v1.set(2, 3.0);
        when(entity1.getVector()).thenReturn(v1.toBinary());
        when(entity1.getName()).thenReturn("c1");

        v2 = new BasicVector(3);
        v2.set(0, 1.0);
        v2.set(1, 2.0);         
        v2.set(2, 4.0);
        when(entity2.getVector()).thenReturn(v2.toBinary());
        when(entity2.getName()).thenReturn("c2");

        v3 = new BasicVector(3);
        v3.set(0, 1.0);
        v3.set(1, 2.0);
        v3.set(2, 6.0);
        when(entity3.getVector()).thenReturn(v3.toBinary());
        when(entity3.getName()).thenReturn("c3");
        
        
        needleVector = new BasicVector(3);
        needleVector.set(0, 1.0);
        needleVector.set(1, 2.0);
        needleVector.set(2, 4.0);
        
        needleEntity = mock(Clazz.class);
        when(needleEntity.getVector()).thenReturn(needleVector.toBinary());
        when(needleEntity.getName()).thenReturn("n1");
        
        needle = new Fingerprint(needleEntity);
        matcher = new FingerprintMatcher(100.0d);
    }
    
    @Test 
    public void testMatchSortedFingerprint() throws SQLException {
        
        fingerprints = new ArrayList<>();
        fingerprints.add(new Fingerprint(entity1));
        fingerprints.add(new Fingerprint(entity2));
        fingerprints.add(new Fingerprint(entity3));
        
        List<Fingerprint> matchedPrints = matcher.matchFingerprints(fingerprints, needle).getMatchesByDistance();
        
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
    public void testMatchEqualFingerprints() throws SQLException {
        v1.set(0, 1.0);
        v1.set(1, 2.0);
        v1.set(2, 3.0);
        
        when(entity1.getVector()).thenReturn(v1.toBinary());
        when(entity2.getVector()).thenReturn(v1.toBinary());
        when(entity3.getVector()).thenReturn(v1.toBinary());
        
        fingerprints = new ArrayList<>();
        fingerprints.add(new Fingerprint(entity1));
        fingerprints.add(new Fingerprint(entity2));
        fingerprints.add(new Fingerprint(entity3));
        
        List<Fingerprint> matchedPrints = matcher.matchFingerprints(fingerprints, needle).getMatchesByDistance();
        
        double diffto0 = needle.euclideanDiff(matchedPrints.get(0));
        double diffto1 = needle.euclideanDiff(matchedPrints.get(1));
        double diffto2 = needle.euclideanDiff(matchedPrints.get(2));
        
        assert(diffto0 == diffto1);
        assert(diffto1 == diffto2);
    }
}
