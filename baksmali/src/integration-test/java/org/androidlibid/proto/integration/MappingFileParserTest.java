package org.androidlibid.proto.integration;

import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import org.androidlibid.proto.match.ProGuardMappingFileParser;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MappingFileParserTest {
    
    @Test
    public void testMappingFileParserClassLevel() throws IOException {
        String mappingFile = "./src/integration-test/resources/MappingFiles/mapping.txt";
        
        ProGuardMappingFileParser parser = new ProGuardMappingFileParser(mappingFile);
        Map<String, String> mapping = parser.parseMappingFileOnClassLevel();
        
        assert(mapping.get("a.a.a.a.a:a") != null);
        assert(mapping.get("a.a.a.a.a:a").equals("org.spongycastle.jcajce.provider.config:ConfigurableProvider"));
        assert(mapping.size() > 0);
        assert(mapping.get("a.a.a.a.a") != null);
        assert(mapping.get("a.a.a.a.a").equals("org.spongycastle.jcajce.provider.config"));
    }
    
    @Test
    public void testMappingFileParserMethodLevel() throws IOException {
        String mappingFile = "./src/integration-test/resources/MappingFiles/mapping.obflvl1.txt";
        
        ProGuardMappingFileParser parser = new ProGuardMappingFileParser(mappingFile);
        
        Map<String, String> mapping = parser.parseMappingFileOnMethodLevel();
            
        String[] obfuscatedMethods = {
            "a.a.f.a:ah:b(a.a.f.a:ah):a.a.f.a:ah",
            "a.a.g.d.a:c:a(byte[][][],byte[][][]):boolean",
            "a.a.g.c.b:k:<init>(short[][],short[],short[][],short[],int[],a.a.g.b.d:a[]):void"
        };
        
        String[] clearMethods = {
            "org.spongycastle.math.ec:SimpleBigDecimal:subtract(org.spongycastle.math.ec:SimpleBigDecimal):org.spongycastle.math.ec:SimpleBigDecimal",
            "org.spongycastle.pqc.math.linearalgebra:ByteUtils:equals(byte[][][],byte[][][]):boolean", 
            "org.spongycastle.pqc.jcajce.spec:RainbowPrivateKeySpec:<init>(short[][],short[],short[][],short[],int[],org.spongycastle.pqc.crypto.rainbow:Layer[]):void"
        };
        
        
        assert(mapping.size() > 0);
        
        for(int i = 0; i < obfuscatedMethods.length; i++) {
            assert(mapping.containsKey(obfuscatedMethods[i]));
            assert(mapping.get(obfuscatedMethods[i]).equals(clearMethods[i]));
        }
    }
}