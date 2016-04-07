package org.androidlibid.proto.store;

import org.androidlibid.proto.ao.LibraryFingerprintDBUpdater;
import org.androidlibid.proto.AndroidLibIDAlgorithm;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;
import org.androidlibid.proto.ast.ASTBuilderFactory;
import org.androidlibid.proto.ast.ASTClassBuilder;
import org.androidlibid.proto.ast.Node;
import org.androidlibid.proto.ao.FingerprintService;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class StoreFingerprintsAlgorithm implements AndroidLibIDAlgorithm {

    private final List<? extends ClassDef> classDefs;
    private final baksmaliOptions options;
    
    private static final Logger LOGGER = LogManager.getLogger(StoreFingerprintsAlgorithm.class);
    private EntityService service;

    public StoreFingerprintsAlgorithm(baksmaliOptions options, List<? extends ClassDef> classDefs) {
        this.options   = options;
        this.classDefs = classDefs;
    }
    
    @Override
    public boolean run() {   
        try {
            service = EntityServiceFactory.createService();
            generateClassFingerprints();
            LibraryFingerprintDBUpdater updater = new LibraryFingerprintDBUpdater(service);
            updater.update(options.mvnIdentifier);
        } catch (SQLException | InterruptedException | ExecutionException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return true;
    }
    
    private void generateClassFingerprints() throws InterruptedException, ExecutionException, SQLException {
        ExecutorService executor = Executors.newFixedThreadPool(options.jobs);
        
        CompletionService<Void> completionService = new ExecutorCompletionService<>(executor);
        
        final ASTBuilderFactory astBuilderFactory = new ASTBuilderFactory(options);
        
        final FingerprintService fpService = new FingerprintService(service);
        
        for (final ClassDef classDef: classDefs) {
            
            Callable<Void> storeFingerprints = new Callable<Void>() {
                
                @Override
                public Void call() throws Exception {
                    
                    ASTClassBuilder astClassBuilder = new ASTClassBuilder(classDef, astBuilderFactory);
                    
                    Map<String, Node> methodASTs = astClassBuilder.buildASTs();
                    
                    ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();
                    ClassFingerprintCreator classFPCreator = new ClassFingerprintCreator(ast2fpt);
                    
                    Fingerprint classFingerprint = classFPCreator.createClassFingerprint(methodASTs, classDef.getType(), options.storeOnMethodLevel);
                    
                    if(classFingerprint.getLength() > 0.0d) {
                        fpService.saveClass(classFingerprint, options.mvnIdentifier);
                    }
                    
                    return null;
                }
            };
                    
            completionService.submit(storeFingerprints);
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
    
}
