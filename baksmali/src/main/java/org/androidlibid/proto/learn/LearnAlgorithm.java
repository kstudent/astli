package org.androidlibid.proto.learn;

import org.androidlibid.proto.AndroidLibIDAlgorithm;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import org.androidlibid.proto.pojo.ASTLIOptions;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.androidlibid.proto.ao.PackageHierarchyService;
import org.androidlibid.proto.pojo.PackageHierarchy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class LearnAlgorithm implements AndroidLibIDAlgorithm {

    private final ASTLIOptions astliOptions;
    
    private static final Logger LOGGER = LogManager.getLogger();
    private EntityService service;
    private final Stream<PackageHierarchy> packages;

    public LearnAlgorithm(Stream<PackageHierarchy> packages, ASTLIOptions astliOptions) {
        this.astliOptions = astliOptions;
        this.packages = packages;
    }
    
    @Override
    public void run() {   
        try {
            service = EntityServiceFactory.createService();
            storeFingerprints();
        } catch (SQLException | InterruptedException | ExecutionException | RuntimeException ex ) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    private void storeFingerprints() throws InterruptedException, ExecutionException, SQLException {
        
        long t1 = System.currentTimeMillis();
        
        final PackageHierarchyService phService = new PackageHierarchyService(service, astliOptions.mvnIdentifier);
        packages.forEach(hierarchy -> phService.saveHierarchy(hierarchy));
        
        LOGGER.info("Time Diff for {} : {}", astliOptions.mvnIdentifier, System.currentTimeMillis() - t1);
    }
}
