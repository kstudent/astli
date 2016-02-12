/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.ao.EntityManager;
import org.androidlibid.proto.Fingerprint;
import org.la4j.Vector;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintService {

    private final EntityManager em;

    public FingerprintService(EntityManager em) {
        this.em = em;
    }
    
    public void deleteAllFingerprints() throws SQLException {
        em.deleteWithSQL(FingerprintEntity.class, "1 = 1");
    }
    
    public int countFingerprints() throws SQLException {
        return em.count(FingerprintEntity.class);
    }

    public FingerprintEntity saveFingerprint(Vector vector, String name) throws SQLException {
        FingerprintEntity print = em.create(FingerprintEntity.class);
        print.setName(name);
        print.setVector(vector.toBinary());
        print.save();
        return print;
    }
    
    public FingerprintEntity saveFingerprint(Fingerprint fingerprint) throws SQLException {
        FingerprintEntity entity = em.create(FingerprintEntity.class);
        entity.setName(fingerprint.getName());
        entity.setVector(fingerprint.getVector().toBinary());
        entity.save();
        return entity;
    }
    
    public Iterable<FingerprintEntity> getFingerprintEntities() {

        return new Iterable<FingerprintEntity>() {
            @Override
            public Iterator<FingerprintEntity> iterator() {
                return new Iterator<FingerprintEntity>() {

                    private List<FingerprintEntity> prints;
                    private Iterator<FingerprintEntity> iterator;

                    {
                        try {
                            prints = Arrays.asList(em.find(FingerprintEntity.class));
                            iterator = prints.iterator();
                        } catch (SQLException ex) {
                            Logger.getLogger(FingerprintService.class.getName()).log(
                                    Level.SEVERE, "could not find FingerprintEntity class", ex);
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public FingerprintEntity next() {
                        return iterator.next();
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }

                };
            }
        };
    }
}
