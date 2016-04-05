package org.androidlibid.proto.store;

import org.androidlibid.proto.match.AndroidLibIDAlgorithm;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.androidlibid.proto.ao.Library;
import org.androidlibid.proto.ao.Package;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;
import org.androidlibid.proto.ao.Clazz;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StoreFingerprintsAlgorithm implements AndroidLibIDAlgorithm {

    private final List<? extends ClassDef> classDefs;
    private final baksmaliOptions options;
    private EntityService service;
    
    private static final Logger LOGGER = LogManager.getLogger(StoreFingerprintsAlgorithm.class);

    public StoreFingerprintsAlgorithm(baksmaliOptions options, List<? extends ClassDef> classDefs) {
        this.options = options;
        this.classDefs = classDefs;
    }
    
    @Override
    public boolean run() {   
        
        try {
            this.service = EntityServiceFactory.createService();
            generateClassFingerprints();
            generateLibAndPackageFingerprints();
        } catch (SQLException | InterruptedException | ExecutionException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return true;
    }
    
    private void generateClassFingerprints() throws InterruptedException, ExecutionException {
//        ExecutorService executor = Executors.newFixedThreadPool(options.jobs);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        
        for (final ClassDef classDef: classDefs) {
//            completionService.submit(new StoreClassFingerprintTask(classDef, options, service));
            completionService.submit(new StoreMethodFingerprint(classDef, options, service));
        }
        
        int count = 0;
        
        try {
            while (count++ < classDefs.size()) {
                Future<Void> future = completionService.take();
                
                if(future.isDone()) future.get();
                
                if(count % 20 == 0) {
                    LOGGER.info("{}%", ((float)(count) / classDefs.size()) * 100); 
                }
            }
        } finally {
            executor.shutdown();
        }
    }

    private void generateLibAndPackageFingerprints() throws SQLException {
        String libname = options.mvnIdentifier; 
        Library lib = service.findLibraryByMvnIdentifier(libname);
        
        if(lib == null) {
            throw new RuntimeException("The Library " + libname + " could not be found.");
        }
            
        Fingerprint libFingerprint = new Fingerprint(lib);  
        
        if(libFingerprint.getLength() > 0.0d) {
            throw new RuntimeException("The Library " + libname + " already has a fingerprint.");
        }

        //could go multithread
        for(Package pckg : lib.getPackages()) {
            Fingerprint pckgFingerprint = new Fingerprint(pckg);

            for(Clazz clazz : pckg.getClazzes()) {
                Fingerprint clazzFingerprint = new Fingerprint(clazz);

                pckgFingerprint.sumFeatures(clazzFingerprint);
            }

            pckg.setVector(pckgFingerprint.getFeatureVector().toBinary());
            pckg.save();

            libFingerprint.sumFeatures(pckgFingerprint);
        }

        lib.setVector(libFingerprint.getFeatureVector().toBinary());
        lib.save();
    }
    
    
    
}