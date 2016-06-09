package org.androidlibid.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchy {
    
    private final String name;
    private final Map<String, Map<String, Fingerprint>> classes = new HashMap<>();
    private List<List<String>> signatureTable;
    private List<List<Fingerprint>> printTable;
    
    private int computedEntropy = -1;

    private static final Logger LOGGER = LogManager.getLogger();
    
    public PackageHierarchy(String name) {
        this.name = name;
    }

    public synchronized void addMethods(String className, Map<String, Fingerprint> prints) {
        
        if(prints.isEmpty()) {
            return;
        }
        
        if(classes.containsKey(className)) {
            classes.get(className).putAll(prints);
        }
        
        classes.put(className, prints);
        resetMembers();
    }

    public String getName() {
        return name;
    }
    
    public Set<String> getClassNames() {
        return classes.keySet();
    }
    
    public Map<String, Fingerprint> getMethodsByClassName(String className) {
        Map<String, Fingerprint> methods = classes.get(className);
        return (methods == null) 
                ? new HashMap<String, Fingerprint>() : methods; 
    } 
    
    public String getClassNameByMethod(Fingerprint method) {
        for(String className : classes.keySet()) {
            Map<String, Fingerprint> methods = classes.get(className);
            
            if(methods.containsValue(method)) {
                return className;
            }
        }
        
        throw new RuntimeException("Method " + method.getName() + " not in hierarchy");
    }
    
    public int getClassesSize() {
        return classes.size();
    }
    
    private void fillSignatureAndPrintTable() {
        signatureTable = new ArrayList<>();
        printTable = new ArrayList<>();
        
        classes.values().stream().forEachOrdered((methods) -> {
            ArrayList<Fingerprint> printsOfClass = new ArrayList<>(methods.values()); 
            printTable.add(printsOfClass);
            signatureTable.add(printsOfClass.stream()
                    .map(print -> print.getSignature())
                    .collect(Collectors.toList()));
        });
    }
    
    public synchronized List<List<String>> getSignatureTable() {
        if(signatureTable == null) {
            fillSignatureAndPrintTable();
        }
        return signatureTable;
    } 
    
    public synchronized List<List<Fingerprint>> getPrintTable() {
        if(printTable == null) {
            fillSignatureAndPrintTable();
        }
        return printTable;
    } 
    
    public synchronized int getEntropy() {
        
        if(computedEntropy < 0) {
            computedEntropy = computeEntropy();
        }
        
        return computedEntropy;
    }

    private int computeEntropy() {
        return classes.values().stream()
                .flatMap(prints -> prints.values().stream())
                .mapToInt(method -> method.getEntropy())
                .sum();
    }
    
    private void resetMembers() {
        this.signatureTable = null;
        this.printTable = null;
        this.computedEntropy = -1;
    }
    
}
