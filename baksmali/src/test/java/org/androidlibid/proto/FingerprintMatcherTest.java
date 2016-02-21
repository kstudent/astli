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
import org.androidlibid.proto.ao.Class;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintMatcherTest {
    
    private List<Class> fingerprints;
    private Class entity1;
    private Class entity2;
    private Class entity3;
    private Class needleEntity;
    private EntityService service;
    private Fingerprint needle;
    private FingerprintMatcher matcher;
    private BasicVector v3;
    private BasicVector v2;
    private BasicVector v1;
    private BasicVector needleVector;
    
    @Before
    public void setUp() throws SQLException {
        entity1 = mock(Class.class);
        entity2 = mock(Class.class);
        entity3 = mock(Class.class);
        
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
        
        fingerprints = new ArrayList<>();
        fingerprints.add(entity1);
        fingerprints.add(entity2);
        fingerprints.add(entity3);
        service = mock(EntityService.class);
        when(service.getClasses()).thenReturn(fingerprints);
        
        needleVector = new BasicVector(3);
        needleVector.set(0, 1.0);
        needleVector.set(1, 2.0);
        needleVector.set(2, 4.0);
        
        needleEntity = mock(Class.class);
        when(needleEntity.getVector()).thenReturn(needleVector.toBinary());
        when(needleEntity.getName()).thenReturn("n1");
        
        needle = new Fingerprint(needleEntity);
        matcher = new FingerprintMatcher(service);
    }
    
    @Test 
    public void testMatchSortedFingerprint() throws SQLException {
        
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
    public void testMatchEqualFingerprints() throws SQLException {
        
        v1.set(0, 1.0);
        v1.set(1, 2.0);
        v1.set(2, 3.0);
        
        when(entity1.getVector()).thenReturn(v1.toBinary());
        when(entity2.getVector()).thenReturn(v1.toBinary());
        when(entity3.getVector()).thenReturn(v1.toBinary());
        
        List<Fingerprint> matchedPrints = matcher.matchFingerprints(needle).getMatchesByDistance();
        
        double diffto0 = needle.euclideanDiff(matchedPrints.get(0));
        double diffto1 = needle.euclideanDiff(matchedPrints.get(1));
        double diffto2 = needle.euclideanDiff(matchedPrints.get(2));
        
        assert(diffto0 == diffto1);
        assert(diffto1 == diffto2);
    }
}
