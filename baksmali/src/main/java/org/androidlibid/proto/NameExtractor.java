package org.androidlibid.proto;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class NameExtractor {
    
    private static final Map<String, String> PRIMITIVE_TYPES;
    
    static {
        PRIMITIVE_TYPES = new HashMap<>();
        PRIMITIVE_TYPES.put("Z", "boolean");
        PRIMITIVE_TYPES.put("B", "byte");
        PRIMITIVE_TYPES.put("S", "short");
        PRIMITIVE_TYPES.put("C", "char");
        PRIMITIVE_TYPES.put("I", "int");
        PRIMITIVE_TYPES.put("J", "long");
        PRIMITIVE_TYPES.put("F", "float");
        PRIMITIVE_TYPES.put("D", "double");
    }
    
    public static String extractPackageNameFromClassName(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public static String transformClassNameFromSmali(String type) {
        
        int arrayDimensions = 0;
        
        while (type.startsWith("[")) {
            arrayDimensions++;
            type = type.substring(1);
        }
        
        if(isPrimitiveType(type)) {
            type = PRIMITIVE_TYPES.get(type);
        } else {
            if(!type.startsWith("L") || !type.endsWith(";") || !type.equals("void")) {
                throw new RuntimeException("Type format error: " + type);
            }
            
            type = type.replace('/', '.');
            type = type.substring(1, type.length() - 1);
        }
        
        StringBuilder typeBuilder = new StringBuilder(type);
        
        for (int i = 0; i < arrayDimensions; i++) {
            typeBuilder.append("[]");
        }
        
        return typeBuilder.toString();
        
    }
    
    public static boolean isPrimitiveType(String type) {
        return PRIMITIVE_TYPES.containsValue(type);
    }
}
