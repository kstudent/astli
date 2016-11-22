package org.androidlibid.proto.match;

import org.androidlibid.proto.utils.ProGuardMappingFileParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import org.junit.Test;
    
/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MappingFileParserTest {
    
    private static final String CLASS_MAPPING_STRING  = 
        "org.spongycastle.jcajce.provider.config.ConfigurableProvider -> a.a.a.a.a.a:\n" +
        "org.spongycastle.jcajce.provider.config.ProviderConfigurationPermission -> a.a.a.a.a.c:\n" +
        "org.spongycastle.jcajce.provider.config.ProviderConfiguration -> a.a.a.a.a.b:\n"
    ;
    
    private static final String METHOD_MAPPING_STRING =
        "org.spongycastle.math.ec.SimpleBigDecimal -> a.a.f.a.ah:\n" +
        "    long serialVersionUID -> a\n" +
        "    java.math.BigInteger bigInt -> b\n" +
        "    org.spongycastle.math.ec.SimpleBigDecimal add(org.spongycastle.math.ec.SimpleBigDecimal) -> a\n" +
        "    org.spongycastle.math.ec.SimpleBigDecimal negate() -> a\n" +
        "    org.spongycastle.math.ec.SimpleBigDecimal subtract(org.spongycastle.math.ec.SimpleBigDecimal) -> b\n" +
        "    org.spongycastle.math.ec.SimpleBigDecimal subtract(java.math.BigInteger) -> b\n" +
        "    org.spongycastle.math.ec.SimpleBigDecimal multiply(org.spongycastle.math.ec.SimpleBigDecimal) -> c\n" +
        "    org.spongycastle.math.ec.SimpleBigDecimal divide(org.spongycastle.math.ec.SimpleBigDecimal) -> d\n" +
        "    org.spongycastle.math.ec.SimpleBigDecimal shiftLeft(int) -> b\n" +
        "    int compareTo(org.spongycastle.math.ec.SimpleBigDecimal) -> e\n" +
        "org.spongycastle.pqc.math.linearalgebra.ByteUtils -> a.a.g.d.a.c:\n" +
        "    char[] HEX_CHARS -> a\n" +
        "    void <init>() -> <init>\n" +
        "    boolean equals(byte[],byte[]) -> a\n" +
        "    boolean equals(byte[][],byte[][]) -> a\n" +
        "    boolean equals(byte[][][],byte[][][]) -> a\n" +
        "    int deepHashCode(byte[]) -> a\n" +
        "    int deepHashCode(byte[][]) -> a\n" +
        "    int deepHashCode(byte[][][]) -> a\n" +
        "    byte[] clone(byte[]) -> b\n" +
        "    byte[] fromHexString(java.lang.String) -> a\n" +
        "    java.lang.String toHexString(byte[]) -> c\n" +
        "    java.lang.String toHexString(byte[],java.lang.String,java.lang.String) -> a\n" +
        "    java.lang.String toBinaryString(byte[]) -> d\n" +
        "    byte[] xor(byte[],byte[]) -> b\n" +
        "    byte[] concatenate(byte[],byte[]) -> c\n" +
        "    byte[] concatenate(byte[][]) -> b\n" +
        "    byte[][] split(byte[],int) -> a\n" +
        "    byte[] subArray(byte[],int,int) -> a\n" +
        "    byte[] subArray(byte[],int) -> b\n" +
        "    char[] toCharArray(byte[]) -> e\n" +
        "    void <clinit>() -> <clinit>\n" +
        "org.spongycastle.pqc.jcajce.spec.RainbowPrivateKeySpec -> a.a.g.c.b.k:\n" +
        "    short[][] A1inv -> a\n" +
        "    short[] b1 -> b\n" +
        "    short[][] A2inv -> c\n" +
        "    short[] b2 -> d\n" +
        "    int[] vi -> e\n" +
        "    org.spongycastle.pqc.crypto.rainbow.Layer[] layers -> f\n" +
        "    void <init>(short[][],short[],short[][],short[],int[],org.spongycastle.pqc.crypto.rainbow.Layer[]) -> <init>\n" +
        "    short[] getB1() -> a\n" +
        "    short[][] getInvA1() -> b\n" +
        "    short[] getB2() -> c\n" +
        "    short[][] getInvA2() -> d\n" +
        "    org.spongycastle.pqc.crypto.rainbow.Layer[] getLayers() -> e\n" +
        "    int[] getVi() -> f\n" +
        "org.spongycastle.pqc.crypto.rainbow.Layer -> a.a.g.b.d.a:\n"
    ;
    
    @Test
    public void testMappingFileParserClassLevel() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(CLASS_MAPPING_STRING));
        ProGuardMappingFileParser parser = new ProGuardMappingFileParser();
        
        Map<String, String> mapping = parser.parseMappingFileOnClassLevel(reader);
        
        assert(mapping.get("a.a.a.a.a:a") != null);
        assert(mapping.get("a.a.a.a.a:a").equals("org.spongycastle.jcajce.provider.config:ConfigurableProvider"));
        assert(mapping.size() > 0);
        assert(mapping.get("a.a.a.a.a") != null);
        assert(mapping.get("a.a.a.a.a").equals("org.spongycastle.jcajce.provider.config"));
    }
    
    @Test
    public void testMappingFileParserMethodLevel() throws IOException {
        BufferedReader classReader = new BufferedReader(new StringReader(METHOD_MAPPING_STRING));
        BufferedReader methodReader = new BufferedReader(new StringReader(METHOD_MAPPING_STRING));
        
        ProGuardMappingFileParser parser = new ProGuardMappingFileParser();
        
        Map<String, String> mapping = parser.parseMappingFileOnMethodLevel(classReader, methodReader);
            
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