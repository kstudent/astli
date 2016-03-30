package org.androidlibid.proto;

import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class NameExtractorTest {
    
    @Test
    public void testCleanClassName() throws Exception {
        String rawClassName = "Ltld/domain/subdomain/project/package/ClassName;";
        String cleanedClassName = SmaliNameConverter.convertTypeFromSmali(rawClassName);
        assert(cleanedClassName.equals("tld.domain.subdomain.project.package.ClassName"));
    }
    
    @Test
    public void testExtractPackageName() throws Exception {
        String className = "tld.domain.subdomain.project.package.ClassName";
        String packageName = SmaliNameConverter.extractPackageNameFromClassName(className);
        assert(packageName.equals("tld.domain.subdomain.project.package"));
    }
   
    @Test
    public void testBoolean() throws Exception {
        String type = "Z";
        String transformedType = SmaliNameConverter.convertTypeFromSmali(type);
        assert(transformedType.equals("boolean"));
    }
    
    @Test
    public void testByte() throws Exception {
        String type = "B";
        String transformedType = SmaliNameConverter.convertTypeFromSmali(type);
        assert(transformedType.equals("byte"));
    }
    
    @Test
    public void testShort() throws Exception {
        String type = "S";
        String transformedType = SmaliNameConverter.convertTypeFromSmali(type);
        assert(transformedType.equals("short"));
    }
    
    @Test
    public void testChar() throws Exception {
        String type = "C";
        String transformedType = SmaliNameConverter.convertTypeFromSmali(type);
        assert(transformedType.equals("char"));
    }
    
    @Test
    public void testInteger() throws Exception {
        String type = "I";
        String transformedType = SmaliNameConverter.convertTypeFromSmali(type);
        assert(transformedType.equals("int"));
    }

    @Test
    public void testFloat() throws Exception {
        String type = "F";
        String transformedType = SmaliNameConverter.convertTypeFromSmali(type);
        assert(transformedType.equals("float"));
    }

    @Test
    public void testDouble() throws Exception {
        String type = "D";
        String transformedType = SmaliNameConverter.convertTypeFromSmali(type);
        assert(transformedType.equals("double"));
    }
    
    @Test
    public void testOneDimensionalArray() throws Exception {
        String type = "[S";
        String transformedType = SmaliNameConverter.convertTypeFromSmali(type);
        assert(transformedType.equals("short[]"));
    }

    @Test
    public void testTwoDimensionalArray() throws Exception {
        String type = "[[B";
        String transformedType = SmaliNameConverter.convertTypeFromSmali(type);
        assert(transformedType.equals("byte[][]"));
    }
}
