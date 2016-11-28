package astli.learn;

import astli.db.Clazz;
import astli.main.AndroidLibIDAlgorithm;
import java.sql.SQLException;
import java.util.stream.Stream;
import astli.pojo.ASTLIOptions;
import astli.db.EntityService;
import astli.db.EntityServiceFactory;
import astli.pojo.Fingerprint;
import astli.pojo.PackageHierarchy;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class LearnAlgorithm implements AndroidLibIDAlgorithm {
    
    private static final Logger LOGGER = LogManager.getLogger();
    private EntityService service;
    private final Stream<PackageHierarchy> packages;
    private final String libName;

    public LearnAlgorithm(Stream<PackageHierarchy> packages, ASTLIOptions astliOptions) {
        this.libName = astliOptions.mvnIdentifier;
        this.packages = packages;
    }
    
    @Override
    public void run() {   
        long t1 = System.currentTimeMillis();
        
        try {
            service = EntityServiceFactory.createService();

            checkLibName(libName);
            packages.forEach(hierarchy -> saveHierarchy(hierarchy));

            LOGGER.info("Time Diff for {} : {}", libName, System.currentTimeMillis() - t1);
            
        } catch (SQLException | RuntimeException ex ) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public void saveHierarchy(PackageHierarchy hierarchy) {
        try {
            String packageName = hierarchy.getName();
            
            for (String className : hierarchy.getClassNames()) {
                Clazz clazz = service.saveClass(className, packageName, libName);
                
                Map<String, Fingerprint> methods = hierarchy.getMethodsByClassName(className);
                
                for (String methodName : methods.keySet()) {
                    
                    Fingerprint methodPrint = methods.get(methodName);
                    
                    service.saveMethod(methodPrint.getBinaryFeatureVector(),
                            methodName, methodPrint.getSignature(), clazz);
                    
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void checkLibName(String libName) {
        try {
            if(service.findLibraryByMvnIdentifier(libName) != null) {
                throw new RuntimeException(libName + " already stored in database!");
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        
    }
}
