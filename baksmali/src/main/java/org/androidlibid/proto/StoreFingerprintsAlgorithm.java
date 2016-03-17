package org.androidlibid.proto;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.androidlibid.proto.ao.Library;
import org.androidlibid.proto.ao.Package;
import org.jf.baksmali.baksmali;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;
import org.androidlibid.proto.ao.Clazz;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StoreFingerprintsAlgorithm implements AndroidLibIDAlgorithm {

    private final List<? extends ClassDef> classDefs;
    private final baksmaliOptions options;
    private EntityService service;

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
        } catch (Exception ex) {
            Logger.getLogger(baksmali.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    private void generateClassFingerprints() throws InterruptedException {
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
                completionService.take();
                
                if(count % 20 == 0) {
                    System.out.println(((float)(count) / classDefs.size()) * 100 + "%"); 
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
        
        if(libFingerprint.euclideanNorm() > 0.0d) {
            throw new RuntimeException("The Library " + libname + " already has a fingerprint.");
        }

        //could go multithread
        for(Package pckg : lib.getPackages()) {
            Fingerprint pckgFingerprint = new Fingerprint(pckg);

            for(Clazz clazz : pckg.getClasses()) {
                Fingerprint clazzFingerprint = new Fingerprint(clazz);

                pckgFingerprint.add(clazzFingerprint);
            }

            pckg.setVector(pckgFingerprint.getVector().toBinary());
            pckg.save();

            libFingerprint.add(pckgFingerprint);
        }

        lib.setVector(libFingerprint.getVector().toBinary());
        lib.save();
    }
    
    
    
}
