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
import org.androidlibid.proto.ao.Class;
import org.jf.baksmali.baksmali;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;

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
        ExecutorService executor = Executors.newFixedThreadPool(options.jobs);
        
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        
        for (final ClassDef classDef: classDefs) {
            completionService.submit(new StoreClassFingerprintTask(classDef, options, service));
        }
        
        int count = 0;
        
        try {
            while (count++ < classDefs.size()) {
                completionService.take();
            }
        } finally {
            executor.shutdown();
        }
    }

    private void generateLibAndPackageFingerprints() throws SQLException {
        for(Library lib : service.getLibraries()) {
            
            Fingerprint libFingerprint = new Fingerprint(lib);
            
            //could go multithread
            for(Package pckg : lib.getPackages()) {
                Fingerprint pckgFingerprint = new Fingerprint(pckg);
                
                for(Class clazz : pckg.getClasses()) {
                    Fingerprint clazzFingerprint = new Fingerprint(clazz);
                    
                    pckgFingerprint.add(clazzFingerprint);
                }
                
                pckg.setVector(pckgFingerprint.getVector().toBinary());
                pckg.save();
                
                libFingerprint.add(pckgFingerprint);
                System.out.println("updated " + pckgFingerprint);
            }
            
            lib.setVector(libFingerprint.getVector().toBinary());
            lib.save();
            System.out.println("updated " + libFingerprint);
        }
    }
    
    
    
}
