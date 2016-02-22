/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import com.google.common.collect.Lists;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.EntityServiceFactory;
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
        List<Future<FingerPrintMatchTaskResult>> tasks = Lists.newArrayList();

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
        Map<FingerPrintMatchTaskResult, Integer> stats = new HashMap<>();
        for(FingerPrintMatchTaskResult key : FingerPrintMatchTaskResult.values()) {
            stats.put(key, 0);
        }
        
        try {
            for (Future<FingerPrintMatchTaskResult> task: tasks) {
                FingerPrintMatchTaskResult key = task.get();
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
        
        for(FingerPrintMatchTaskResult key : FingerPrintMatchTaskResult.values()) {
            System.out.println(key.toString() + ": " + stats.get(key));
        }
        
        return true;
    }
    
}
