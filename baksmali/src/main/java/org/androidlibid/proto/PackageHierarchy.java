package org.androidlibid.proto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public String getName() {
        return name;
    }
    
    public Set<String> getClassNames() {
        return classes.keySet();
    }
    
    public Map<String, Fingerprint> getMethodsByClassName (String className) {
        Map<String, Fingerprint> methods = classes.get(className);
        return (methods == null) 
                ? new HashMap<String, Fingerprint>() : methods; 
    } 
    
}
