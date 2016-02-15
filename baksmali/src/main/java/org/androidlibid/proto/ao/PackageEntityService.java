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
public class PackageEntityService {

//    private final EntityManager em;

//    public PackageEntityService(EntityManager em) {
//        this.em = em;
//    }
//    
//    public void deleteAllPackages() throws SQLException {
//        em.deleteWithSQL(PackageEntity.class, "1 = 1");
//    }
//    
//    public int countPackages() throws SQLException {
//        return em.count(PackageEntity.class);
//    }
//
//    public PackageEntity savePackage(Vector vector, String name) throws SQLException {
//        PackageEntity packageEntity = em.create(PackageEntity.class);
//        packageEntity.setPackageName(name);
//        packageEntity.setVector(vector.toBinary());
//        packageEntity.save();
//        return packageEntity;
//    }
//    
//    public Iterable<ClassEntity> getFingerprintEntities() {
//
//        return new Iterable<ClassEntity>() {
//            @Override
//            public Iterator<ClassEntity> iterator() {
//                return new Iterator<ClassEntity>() {
//
//                    private List<ClassEntity> prints;
//                    private Iterator<ClassEntity> iterator;
//
//                    {
//                        try {
//                            prints = Arrays.asList(em.find(ClassEntity.class));
//                            iterator = prints.iterator();
//                        } catch (SQLException ex) {
//                            Logger.getLogger(PackageEntityService.class.getName()).log(
//                                    Level.SEVERE, "could not find FingerprintEntity class", ex);
//                        }
//                    }
//
//                    @Override
//                    public boolean hasNext() {
//                        return iterator.hasNext();
//                    }
//
//                    @Override
//                    public ClassEntity next() {
//                        return iterator.next();
//                    }
//
//                    @Override
//                    public void remove() {
//                        iterator.remove();
//                    }
//
//                };
//            }
//        };
//    }
}
