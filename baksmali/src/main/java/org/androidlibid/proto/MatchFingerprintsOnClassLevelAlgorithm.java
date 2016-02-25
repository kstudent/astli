package org.androidlibid.proto;

import com.google.common.collect.Lists;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.androidlibid.proto.ao.VectorEntity;
import org.androidlibid.proto.ast.ASTClassDefinition;
import org.androidlibid.proto.ast.ASTToFingerprintTransformer;
import org.androidlibid.proto.ast.Node;
import org.jf.baksmali.baksmali;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.iface.ClassDef;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MatchFingerprintsOnClassLevelAlgorithm implements AndroidLibIDAlgorithm {

    private final List<? extends ClassDef> classDefs;
    private final baksmaliOptions options;

    public MatchFingerprintsOnClassLevelAlgorithm(baksmaliOptions options, List<? extends ClassDef> classDefs) {
        this.options = options;
        this.classDefs = classDefs;
    }
    @Override
    public boolean run() {
//        ExecutorService executor = Executors.newFixedThreadPool(options.jobs);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        List<Future<FingerprintMatchTaskResult>> tasks = Lists.newArrayList();

        EntityService service;
        try {
            service = EntityServiceFactory.createService();
        } catch (SQLException ex) {
            Logger.getLogger(baksmali.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        for (final ClassDef classDef: classDefs) {
            if (!classDef.getType().startsWith("Landroid/")) {
                tasks.add(executor.submit(new MatchClassFingerprintTask(classDef, options, service)));
            }
        }

        int count_total = 0;
        Map<FingerprintMatchTaskResult, Integer> stats = new HashMap<>();
        for(FingerprintMatchTaskResult key : FingerprintMatchTaskResult.values()) {
            stats.put(key, 0);
        }
        
        try {
            for (Future<FingerprintMatchTaskResult> task: tasks) {
                FingerprintMatchTaskResult key = task.get();
                stats.put(key, stats.get(key) + 1);
                count_total++;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(baksmali.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(baksmali.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            executor.shutdown();
        }
        
        System.out.println("Stats: ");
        System.out.println("Total: " + count_total);
        
        for(FingerprintMatchTaskResult key : FingerprintMatchTaskResult.values()) {
            System.out.println(key.toString() + ": " + stats.get(key));
        }
        
        return true;
    }
    
    class MatchClassFingerprintTask implements Callable<FingerprintMatchTaskResult>{
    
        private final ClassDef classDef;
        private final baksmaliOptions options;
        private final EntityService service;

        public MatchClassFingerprintTask(ClassDef classDef, baksmaliOptions options, EntityService service) {
            this.classDef = classDef;
            this.options = options;
            this.service = service;
        }

        @Override public FingerprintMatchTaskResult call() throws Exception {
            String name = classDef.getType();

            ASTClassDefinition classDefinition = new ASTClassDefinition(options, classDef);
            Collection<Node> ast = classDefinition.createAST();

            ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();

            Fingerprint needle = new Fingerprint(name);

            for(Node node : ast) {
                Fingerprint methodFingerprint = ast2fpt.createFingerprint(node);
                needle.add(methodFingerprint);
            }

            if (needle.euclideanNorm() == 0.0d) {
                System.out.println(name + ": class length 0");
                return FingerprintMatchTaskResult.CLASS_LENGTH_0;
            }

            FingerprintMatcher matcher = new FingerprintMatcher(100.0d);

            List<VectorEntity> classEntities = new ArrayList<VectorEntity>(service.findClasses());
            List<Fingerprint>  classFingerprints  = new ArrayList<>(classEntities.size());
            
            for(VectorEntity v : classEntities) {
                classFingerprints.add(new Fingerprint(v));
            }

            FingerprintMatcher.Result result = matcher.matchFingerprints(classFingerprints, needle);

            Fingerprint nameMatch = result.getMatchByName();
            List<Fingerprint> matchesByDistance = result.getMatchesByDistance();

            if(nameMatch == null) {
                System.out.println(name + ": not mached by name");
                return FingerprintMatchTaskResult.NO_MATCH_BY_NAME;
            } else {

                int i;

                for (i = 0; i < matchesByDistance.size(); i++) {
                    if(matchesByDistance.get(i).getName().equals(name)) {
                        break;
                    }
                }

                if(i == matchesByDistance.size()) {
                    System.out.println(name + ": not mached by distance.");
                    System.out.println(needle);
                    System.out.println(nameMatch);
                    return FingerprintMatchTaskResult.NO_MATCH_BY_DISTANCE;
                } else if(i > 0) {
                    System.out.println(name + ": found at position " + i);
                    return FingerprintMatchTaskResult.NOT_PERFECT;
                } else {
    //                System.out.println(name + ": found first.");
                    return FingerprintMatchTaskResult.OK;
                }
            }
        }
    }
    
}
