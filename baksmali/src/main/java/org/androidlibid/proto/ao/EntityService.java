package org.androidlibid.proto.ao;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.java.ao.EntityManager;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class EntityService {

    private final EntityManager em;
    private final byte[] defaultVector;
    
    public EntityService(EntityManager em, byte[] defaultVector) {
        this.em = em;
        this.defaultVector = defaultVector;
    }
    
    public void truncateTables() throws SQLException {
        em.deleteWithSQL(Class.class,   "1 = 1");
        em.deleteWithSQL(Package.class, "1 = 1");
        em.deleteWithSQL(Library.class, "1 = 1");
    }
    
    public int countClasses() throws SQLException {
        return em.count(Class.class);
    }

    public synchronized Class saveClass(byte[] vector, String className, 
        String packageName, String mvnIdentifier) throws SQLException {

        Class print = em.create(Class.class);
        Library lib  = saveLibrary(mvnIdentifier);
        Package pckg = savePackage(packageName, lib);
        
        print.setName(className);
        print.setVector(vector);
        print.setPackage(pckg);
        print.save();

        return print;
    }
    
    public List<Class> getClasses() throws SQLException {
        return Arrays.asList(em.find(Class.class));
    }
    
    public List<Library> getLibraries() throws SQLException {
        return Arrays.asList(em.find(Library.class));
    }

    public @Nullable Package findPackageByNameAndLib(
            String packageName, Library library) throws SQLException {
        
        Package[] packageEntities = em.find(Package.class, 
                "NAME = ? AND LIBRARYID = ?", 
                packageName, library.getID());
        
        if (packageEntities.length > 1) {
            throw new SQLWarning("Multiple Packages (" + packageEntities.length + ") with the same Package ("
                    + packageName + ")/ Library Identifier (" + library.getName() + ") found. Database inconsitent?");
        }
        
        if(packageEntities.length == 0) {
            return null;
        }
        
        return packageEntities[0];
        
    }

    public Package savePackage(String packageName, Library lib) throws SQLException {
        Package entity = findPackageByNameAndLib(packageName, lib);
        
        if (entity == null) {
            entity = em.create(Package.class);
            entity.setName(packageName);
            entity.setLibrary(lib);
            entity.setVector(defaultVector);
            entity.save();
        }
        
        return entity;
    }

    public @Nullable Library findLibraryByMvnIdentifier(String mvnIdentifier) 
            throws SQLException {
        
        Library[] libraries = em.find(Library.class, 
                "NAME = ?", mvnIdentifier);
        
        if (libraries.length > 1) {
            throw new SQLWarning("Multiple libraries (" + libraries.length + ") with the mvn Identifier " + mvnIdentifier 
                    + " found. Database inconsitent?");
        }
        
        if(libraries.length == 0) {
            return null;
        }
        
        return libraries[0];
    }

    public Library saveLibrary(String mvnIdentifier) throws SQLException {
        
        Library entity = findLibraryByMvnIdentifier(mvnIdentifier);
        
        if (entity == null) {
            entity = em.create(Library.class);
            entity.setName(mvnIdentifier);
            entity.setVector(defaultVector);
            entity.save();
        }
        
        return entity;
    }
}
