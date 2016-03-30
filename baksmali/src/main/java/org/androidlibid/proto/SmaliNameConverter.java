package org.androidlibid.proto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class SmaliNameConverter {
    
    private static final Map<String, String> PRIMITIVE_TYPES;
    public static final String TYPE_DELIMITER = ",";
    
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
        PRIMITIVE_TYPES.put("V", "void");
    }
    
    public static String extractPackageNameFromClassName(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

    public static String convertTypeFromSmali(String smaliType) {
        
        int arrayDimensions = 0;
        
        while (smaliType.startsWith("[")) {
            arrayDimensions++;
            smaliType = smaliType.substring(1);
        }
        
        String type;
        
        if(isPrimitiveSmaliType(smaliType)) {
            type = PRIMITIVE_TYPES.get(smaliType);
        } else {
            if(!smaliType.startsWith("L") || !smaliType.endsWith(";")) {
                throw new RuntimeException("Type format error: " + smaliType);
            }
            
            type = smaliType.replace('/', '.');
            type = type.substring(1, type.length() - 1);
        }
        
        StringBuilder typeBuilder = new StringBuilder(type);
        
        for (int i = 0; i < arrayDimensions; i++) {
            typeBuilder.append("[]");
        }
        
        return typeBuilder.toString();
        
    }
    
    /**
     * Tells, whether <type> is primitive or not.
     * 
     * @param type, e.g. "void", "int", "byte"...
     * @return 
     */
    public static boolean isPrimitiveType(String type) {
        return PRIMITIVE_TYPES.containsValue(type);
    }

    /**
     * Tells, whether <smaliType> is primitive or not.
     * 
     * @param smaliType, e.g. "V", "I", "B"...
     * @return 
     */
    public static boolean isPrimitiveSmaliType(String smaliType) {
        return PRIMITIVE_TYPES.containsKey(smaliType);
    }

    public static String buildMethodSignature(String methodName, List<? extends CharSequence> smaliParameterTypes, String smaliReturnType) {
        
        StringBuilder methodSignature = new StringBuilder(methodName);
        methodSignature.append("(");
        
        for(int i = 0; i < smaliParameterTypes.size(); i++) {
        
            CharSequence smaliType = smaliParameterTypes.get(i);
            String type = convertTypeFromSmali(smaliType.toString()); 
            methodSignature.append(type);
            
            if(i < smaliParameterTypes.size() - 1) {
                methodSignature.append(TYPE_DELIMITER);
            }
        }
        
        methodSignature
            .append("):")
            .append(convertTypeFromSmali(smaliReturnType));
        
        return methodSignature.toString();
    }
}
