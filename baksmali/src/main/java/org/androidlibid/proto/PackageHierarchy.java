package org.androidlibid.proto;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchy {
    
    Map<String, String> classes = new HashMap<>();
    Map<String, Fingerprint> methodsOfClass = new HashMap<>();
    
    
    
}
