package org.androidlibid.proto.match;

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
import org.androidlibid.proto.Fingerprint;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
import org.androidlibid.proto.ao.VectorEntity;
import org.androidlibid.proto.ast.ASTBuilderFactory;
import org.androidlibid.proto.ast.ASTClassBuilder;
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
        List<Future<MatchingStrategy.Status>> tasks = Lists.newArrayList();

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
        Map<MatchingStrategy.Status, Integer> stats = new HashMap<>();
        for(MatchingStrategy.Status key : MatchingStrategy.Status.values()) {
            stats.put(key, 0);
        }
        
        try {
            for (Future<MatchingStrategy.Status> task: tasks) {
                MatchingStrategy.Status key = task.get();
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
        
        for(MatchingStrategy.Status key : MatchingStrategy.Status.values()) {
            System.out.println(key.toString() + ": " + stats.get(key));
        }
        
        return true;
    }
    
    class MatchClassFingerprintTask implements Callable<MatchingStrategy.Status>{
    
        private final ClassDef classDef;
        private final baksmaliOptions options;
        private final EntityService service;

        public MatchClassFingerprintTask(ClassDef classDef, baksmaliOptions options, EntityService service) {
            this.classDef = classDef;
            this.options = options;
            this.service = service;
        }

        @Override public MatchingStrategy.Status call() throws Exception {
            String name = classDef.getType();

            ASTBuilderFactory factory = new ASTBuilderFactory(options);
            ASTClassBuilder astClassBuilder = new ASTClassBuilder(classDef, factory);
            
            Iterable<Node> ast = astClassBuilder.buildASTs().values();

            ASTToFingerprintTransformer ast2fpt = new ASTToFingerprintTransformer();

            Fingerprint needle = new Fingerprint(name);

            for(Node node : ast) {
                Fingerprint methodFingerprint = ast2fpt.createFingerprint(node);
                needle.sumFeatures(methodFingerprint);
            }

            if (needle.getLength() == 0.0d) {
                System.out.println(name + ": class length 0");
                return MatchingStrategy.Status.CLASS_LENGTH_0;
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
                return MatchingStrategy.Status.NO_MATCH_BY_NAME;
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
                    return MatchingStrategy.Status.NO_MATCH_BY_DISTANCE;
                } else if(i > 0) {
                    System.out.println(name + ": found at position " + i);
                    return MatchingStrategy.Status.NOT_PERFECT;
                } else {
    //                System.out.println(name + ": found first.");
                    return MatchingStrategy.Status.OK;
                }
            }
        }
    }
    
}
