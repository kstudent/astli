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
public class ClassEntityService {

    private final EntityManager em;

    public ClassEntityService(EntityManager em) {
        this.em = em;
    }
    
    public void deleteAllFingerprints() throws SQLException {
        em.deleteWithSQL(ClassEntity.class, "1 = 1");
    }
    
    public int countFingerprints() throws SQLException {
        return em.count(ClassEntity.class);
    }

    public ClassEntity saveFingerprint(Vector vector, String name) throws SQLException {
        ClassEntity print = em.create(ClassEntity.class);
        print.setClassName(name);
        print.setVector(vector.toBinary());
        print.save();
        return print;
    }
    
    public ClassEntity saveFingerprint(Fingerprint fingerprint) throws SQLException {
        ClassEntity entity = em.create(ClassEntity.class);
        entity.setClassName(fingerprint.getName());
        entity.setVector(fingerprint.getVector().toBinary());
        entity.save();
        return entity;
    }
    
    public Iterable<ClassEntity> getFingerprintEntities() {

        return new Iterable<ClassEntity>() {
            @Override
            public Iterator<ClassEntity> iterator() {
                return new Iterator<ClassEntity>() {

                    private List<ClassEntity> prints;
                    private Iterator<ClassEntity> iterator;

                    {
                        try {
                            prints = Arrays.asList(em.find(ClassEntity.class));
                            iterator = prints.iterator();
                        } catch (SQLException ex) {
                            Logger.getLogger(ClassEntityService.class.getName()).log(
                                    Level.SEVERE, "could not find FingerprintEntity class", ex);
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public ClassEntity next() {
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
