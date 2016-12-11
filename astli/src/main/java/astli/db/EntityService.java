package astli.db;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.java.ao.DBParam;
import net.java.ao.EntityManager;
import net.java.ao.EntityStreamCallback;
import net.java.ao.Query;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class EntityService {

    private final EntityManager em;
    
    public EntityService(EntityManager em) {
        this.em = em;
    }
    
    public void truncateTables() throws SQLException {
        em.deleteWithSQL(Method.class,  "1 = 1");
        em.deleteWithSQL(Clazz.class,   "1 = 1");
        em.deleteWithSQL(Package.class, "1 = 1");
        em.deleteWithSQL(Library.class, "1 = 1");
    }
    
    public Method saveMethod(byte[] vector, String methodName, String signature, Clazz clazz) 
            throws SQLException
    {
        
        DBParam sigP = new DBParam("signature", signature);
        DBParam metP = new DBParam("name", methodName);
        DBParam claP = new DBParam("clazzID", clazz);
        DBParam vecP = new DBParam("vector", vector);
        
        Method entity = em.create(Method.class, vecP, sigP, metP, claP);
        return entity;
    }
    
    public synchronized Clazz saveClass(String className, 
        String packageName, String mvnIdentifier) throws SQLException {

        Library lib  = saveLibrary(mvnIdentifier);
        Package pckg = savePackage(packageName, lib);
        
        Clazz clazz = em.create(Clazz.class, 
                new DBParam("name", className), 
                new DBParam("packageID", pckg));

        return clazz;
    }
    
    public Package savePackage(String packageName, Library lib) throws SQLException {
        Package entity = findPackageByNameAndLib(packageName, lib);
        
        if (entity == null) {
            entity = em.create(Package.class, 
                    new DBParam("name", packageName), 
                    new DBParam("libraryID", lib));
        }
        
        return entity;
    }
    
    public Library saveLibrary(String mvnIdentifier) throws SQLException {
        
        Library entity = findLibraryByMvnIdentifier(mvnIdentifier);
        
        if (entity == null) {
            entity = em.create(Library.class, new DBParam("name", mvnIdentifier));
        }
        
        return entity;
    }

    public int countLibraries() throws SQLException {
        return em.count(Library.class);
    }    

    public int countPackages() throws SQLException {
        return em.count(Package.class); 
    }
    
    public int countClasses() throws SQLException {
        return em.count(Clazz.class);
    }    

    public int countMethods() throws SQLException {
        return em.count(Method.class); 
    }
    
    public List<Clazz> findClasses() throws SQLException {
        return Arrays.asList(em.find(Clazz.class));
    }
    
    public List<Package> findPackages() throws SQLException {
        return Arrays.asList(em.find(Package.class));
    }
    
    public List<Package> findPackagesByDepth(int depth) throws SQLException {
        List<Package> packages = new LinkedList<>();
        for(Package pckg : em.find(Package.class)) {
            if (StringUtils.countMatches(pckg.getName(), ".") == depth) {
                packages.add(pckg);
            } 
        }
        return packages;
    }
    
    public @Nullable Package findPackageByNameAndLib(
            String packageName, Library library) throws SQLException {
        
        Package[] packageEntities = em.find(Package.class, 
                "NAME = ? AND LIBRARYID = ?", 
                packageName, library.getID());
        
        if (packageEntities.length > 1) {
            throw new SQLWarning("Multiple Packages (" + packageEntities.length + ") with the same Package ("
                    + packageName + ")/ Library Identifier (" + library.getName() + ") found. Database inconsistent?");
        }
        
        if(packageEntities.length == 0) {
            return null;
        }
        
        return packageEntities[0];
        
    }

    public List<Library> findLibraries() throws SQLException {
        return Arrays.asList(em.find(Library.class));
    }
    

    public @Nullable Library findLibraryByMvnIdentifier(String mvnIdentifier) 
            throws SQLException {
        
        Library[] libraries = em.find(Library.class, 
                "NAME = ?", mvnIdentifier);
        
        if (libraries.length > 1) {
            throw new SQLWarning("Multiple libraries (" + libraries.length + ") with the mvn Identifier " + mvnIdentifier 
                    + " found. Database inconsistent?");
        }
        
        if(libraries.length == 0) {
            return null;
        }
        
        return libraries[0];
    }

    public List<Package> findPackagesByName(String name) throws SQLException {
        return Arrays.asList(em.find(Package.class, "NAME = ?", name ));
    }

    public List<Method> findMethodsBySignature(String signature) throws SQLException {
        return Arrays.asList(em.find(Method.class, "SIGNATURE = ?", signature));
    }
    
    public List<Method> findMethodsBySignatureAndVector(String signature, byte[] vector) throws SQLException {
        return Arrays.asList(em.find(Method.class, "SIGNATURE = ? AND VECTOR = ?", signature, vector));
    }
    
    public List<Package> findPackageCandidateBySignatureAndVector(String signature, byte[] vector) throws SQLException {
        return Arrays.asList(em.find(Package.class, "ID", Query.select().where(
                "ID IN( "
                +    "SELECT PACKAGEID FROM CLAZZ WHERE ID IN ( "
                +       "SELECT CLAZZID FROM FINGERPRINTENTITY "
                +       "WHERE SIGNATURE = ? and VECTOR = ? "
                +    ") "
                + ") ", signature, vector)));
    }
    
    public void findFingerprints(Consumer<Method> consumer) throws SQLException {
          
        em.stream(
                Method.class, 
                (EntityStreamCallback<Method, Integer>) (Method p) -> {
                   consumer.accept(p);
                }
        );
    }
}


