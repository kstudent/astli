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
    private int computedEntropy = -1;

    private static final Logger LOGGER = LogManager.getLogger();
    
    public PackageHierarchy(String name) {
        this.name = name;
    }

    public void addMethods(String className, Map<String, Fingerprint> prints) {
        
        if(prints.isEmpty()) {
            return;
        }
        
        if(classes.containsKey(className)) {
            classes.get(className).putAll(prints);
        }
        
        classes.put(className, prints);
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
    
    public List<List<String>> getSignatureTable() {
        List<List<String>> table = new ArrayList<>();
        
        classes.values().stream().forEach((methods) -> {
            table.add(methods.values().stream()
                    .map(print -> print.getSignature())
                    .collect(Collectors.toList()));
        });
        
        return table;
    } 
    
    public int getEntropy() {
        
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
}
