package astli.pojo;

import astli.db.Clazz;
import astli.db.Library;
import astli.db.Package;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import astli.db.MethodE;


/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class PackageHierarchyTest {

    @Test
    public void testEntityConstructor() {
        PackageHierarchy p = new PackageHierarchy(generatePackageHierarchyMock());
        
        assert("pckg1".equals(p.getName()));
        assert("lib0".equals(p.getLib()));
        
        Set<String> classes = new HashSet<String>();
        classes.add("classX");
        assert(p.getClassNames().equals(classes));
        
        Set<String> methodNames = new HashSet<String>();
        methodNames.add("method1");
        Map<String, Fingerprint> methods = p.getMethodsByClassName(classes.iterator().next()); 
        assert(methods.keySet().equals(methodNames));
        
    }
    
    @Test
    public void testGetSignatureTable() {
        PackageHierarchy p = new PackageHierarchy(generatePackageHierarchyMock());
        
        List<List<String>> signatureTable = p.getSignatureTable();
        
        assert(signatureTable.size() == 1);
        assert(signatureTable.get(0).size() == 1);
        assert("V:E".equals(signatureTable.get(0).get(0)));
    }
    
    @Test
    public void testClassNameByMethod() {
        PackageHierarchy p = new PackageHierarchy("pck", "lib");
        Map<String, Fingerprint> prints = new HashMap<>();
        Fingerprint methodPrint = new Fingerprint();
        prints.put("methodX", methodPrint);
        p.addMethods("myClass", prints);
        
        String className = p.getClassNameByMethod(methodPrint);
        
        assert("myClass".equals(className));
    }
    
    @Test
    public void testGetParticularity() {
        PackageHierarchy p = new PackageHierarchy("pck", "lib");
        Map<String, Fingerprint> prints = new HashMap<>();
        Fingerprint methodPrint = mock(Fingerprint.class);
        when(methodPrint.getParticularity()).thenReturn(37);
        prints.put("methodX", methodPrint);
        p.addMethods("myClass", prints);
        
        int particularity = p.getParticularity();
        
        assert(37 == particularity);
    }
    
    private Package generatePackageHierarchyMock() {
        Library l = mock(Library.class);
        MethodE  m = mock(MethodE.class);
        Clazz   c = mock(Clazz.class);
        Package p = mock(Package.class);
        
        when(m.getName()).thenReturn("method1");
        when(m.getSignature()).thenReturn("V:E");
        when(m.getVector()).thenReturn(new byte[10]);
        when(m.getClazz()).thenReturn(c);
        when(c.getMethods()).thenReturn(new MethodE[]{m});
        when(c.getName()).thenReturn("classX");
        when(c.getPackage()).thenReturn(p);
        when(p.getClazzes()).thenReturn(new Clazz[]{c});
        when(p.getName()).thenReturn("pckg1");
        when(p.getLibrary()).thenReturn(l);
        when(l.getPackages()).thenReturn(new Package[]{p});
        when(l.getName()).thenReturn("lib0");
        
        return p;
    }

    
    
}
