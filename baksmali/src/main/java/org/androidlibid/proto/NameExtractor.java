package org.androidlibid.proto;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class NameExtractor {
    
    public static String extractPackageName(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public static String transformClassName(String className) {
        className = className.replace('/', '.');
        return className.substring(1, className.length() - 1);
    }
    
}
