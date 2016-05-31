package org.androidlibid.proto;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchy {
    
    String name;
    Map<String, Map<String, Fingerprint>> classes = new HashMap<>();

    public PackageHierarchy(String name) {
        this.name = name;
    }

    void addMethods(String className, Map<String, Fingerprint> prints) {
        
        if(classes.containsKey(className)) {
            classes.get(className).putAll(prints);
        }
        
        classes.put(className, prints);
    }
}
