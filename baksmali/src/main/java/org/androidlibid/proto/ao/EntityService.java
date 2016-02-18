/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto.ao;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import net.java.ao.EntityManager;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class EntityService {

    private final EntityManager em;

    public EntityService(EntityManager em) {
        this.em = em;
    }
    
    public void deleteAllFingerprints() throws SQLException {
        em.deleteWithSQL(ClassEntity.class, "1 = 1");
    }
    
    public int countFingerprints() throws SQLException {
        return em.count(ClassEntity.class);
    }

    public ClassEntity saveFingerprint(byte[] vector, String className, String packageName, String mvnIdentifier) throws SQLException {
        ClassEntity print = em.create(ClassEntity.class);
        print.setClassName(className);
        print.setVector(vector);
//        print.setPackage(findPackageByName(packageName));
        print.save();
        return print;
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
                            Logger.getLogger(EntityService.class.getName()).log(
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

    public @Nullable PackageEntity findPackageByNameAndLib(String packageName, LibraryEntity library) throws SQLException {
        
        PackageEntity[] packageEntities = em.find(PackageEntity.class, "PACKAGE_NAME = ? AND PARENT_LIBRARY_ID = ?", packageName, library.getID());
        
        if (packageEntities.length > 1) {
            throw new SQLWarning("Multiple Packages with the same Package / Library Identifier");
        }
        
        if(packageEntities.length == 0) {
            return null;
        }
        
        else return packageEntities[0];
        
    }
}
