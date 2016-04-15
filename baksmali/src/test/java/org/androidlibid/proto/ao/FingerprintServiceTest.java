package org.androidlibid.proto.ao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.androidlibid.proto.Fingerprint;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FingerprintServiceTest {
    
    private FingerprintService service;
    private EntityService entityService;
    private Package[] packages;
    private Clazz[]   clazzes;
    private Method[]  methodsOfClazz0;
    private Method[]  methodsOfClazz1;
    
    @Before 
    public void setUp() {
        entityService   = mock(EntityService.class);
        service = new FingerprintService(entityService);
        
        methodsOfClazz0 = new Method[] {mock(Method.class), mock(Method.class)};
        methodsOfClazz1 = new Method[] {mock(Method.class), mock(Method.class), mock(Method.class)};
        clazzes         = new Clazz[]  {mock(Clazz.class),  mock(Clazz.class)};
        packages        = new Package[]{mock(Package.class)};
        
        when(packages[0].getClazzes()).thenReturn(clazzes);
        when(clazzes [0].getMethods()).thenReturn(methodsOfClazz0);
        when(clazzes [1].getMethods()).thenReturn(methodsOfClazz1);
        when(methodsOfClazz0[0].getClazz()).thenReturn(clazzes[0]);
        when(methodsOfClazz0[1].getClazz()).thenReturn(clazzes[0]);
        when(methodsOfClazz1[0].getClazz()).thenReturn(clazzes[1]);
        when(methodsOfClazz1[1].getClazz()).thenReturn(clazzes[1]);
        when(methodsOfClazz1[2].getClazz()).thenReturn(clazzes[1]);
        when(clazzes[0].getPackage()).thenReturn(packages[0]);
        when(clazzes[1].getPackage()).thenReturn(packages[0]);
    }
    
    @Test
    public void testFindMethodsByLength() throws SQLException {
        
        when(entityService.findMethodsByLength(1, 1)).thenReturn(Arrays.asList(methodsOfClazz0));
        
        List<Fingerprint> methodFingerprints = service.findMethodsByLength(1, 1);
        
        assert(methodFingerprints.size() == 2);
        for(int i = 0; i < 2; i++) {
            assert(methodsOfClazz0[i].equals(methodFingerprints.get(i).getEntity()));
        }
    }

    @Test
    public void testFindMethodsByPackageDepth() throws SQLException {
        
        when(entityService.findPackagesByDepth(10)).thenReturn(Arrays.asList(packages));
        
        List<Fingerprint> methodFingerprints = service.findMethodsByPackageDepth(10);
        
        List<Method> expectedMethods = new ArrayList<>(Arrays.asList(methodsOfClazz0));
        expectedMethods.addAll(Arrays.asList(methodsOfClazz1));
        
        assert(methodFingerprints.size() == 5);
        for(int i = 0; i < 5; i++) {
            assert(expectedMethods.get(i).equals(methodFingerprints.get(i).getEntity()));
        }
    
    }
    
    @Test
    public void testFindClassesByPackageDepth() throws SQLException {
        
        when(entityService.findPackagesByDepth(10)).thenReturn(Arrays.asList(packages));
        
        List<Fingerprint> classFingerprints = service.findClassesByPackageDepth(10);
        
        assert(classFingerprints.size() == 2);
        assert(clazzes[0].equals(classFingerprints.get(0).getEntity()));
        assert(clazzes[1].equals(classFingerprints.get(1).getEntity()));
    } 
    
    @Test
    public void testFindPackagesDepth() throws SQLException {
        
        when(entityService.findPackagesByDepth(10)).thenReturn(Arrays.asList(packages));
        
        List<Fingerprint> packagePrints = service.findPackagesByDepth(10);
        
        assert(packagePrints.size() == 1);
        assert(packages[0].equals(packagePrints.get(0).getEntity()));
    } 
    
    @Test
    public void testFindPackagesByName() throws SQLException {
        
        String packageName = "tld.pckg";
        when(entityService.findPackagesByName(packageName)).thenReturn(Arrays.asList(packages));
        
        List<Fingerprint> packageFingerprints = service.findPackagesByName(packageName);
        
        assert(packageFingerprints.size() == 1);
        assert(packageFingerprints.get(0).getEntity().equals(packages[0]));
        
    }
    
    @Test
    public void testFindPackages() throws SQLException {
        
        when(entityService.findPackages()).thenReturn(Arrays.asList(packages));
        
        List<Fingerprint> packageFingerprints = service.findPackages();
        
        assert(packageFingerprints.size() == 1);
        assert(packageFingerprints.get(0).getEntity().equals(packages[0]));
        
    }
    
    @Test
    public void testGetPackageHierarchy() throws SQLException {
        
        Fingerprint packagePrint = new Fingerprint(packages[0]);
        
        Fingerprint packageHierarchy = service.getPackageHierarchy(packagePrint);
        
        List<Fingerprint> classesOfPackage = packageHierarchy.getChildFingerprints();
        
        assert(classesOfPackage.size() == 2);
        
        Fingerprint classFingerprint0 = classesOfPackage.get(0);
        Fingerprint classFingerprint1 = classesOfPackage.get(1);
        
        assert(classFingerprint0.getEntity().equals(clazzes[0]));
        assert(classFingerprint1.getEntity().equals(clazzes[1]));
        
        List<Fingerprint> methodFingerprintsOfClass0 = classFingerprint0.getChildFingerprints();
        List<Fingerprint> methodFingerprintsOfClass1 = classFingerprint1.getChildFingerprints();
        
        assert(methodFingerprintsOfClass0.size() == methodsOfClazz0.length);
        assert(methodFingerprintsOfClass1.size() == methodsOfClazz1.length);
        assert(methodFingerprintsOfClass0.get(0).getEntity() == methodsOfClazz0[0]);
        assert(methodFingerprintsOfClass0.get(1).getEntity() == methodsOfClazz0[1]);
        assert(methodFingerprintsOfClass1.get(0).getEntity() == methodsOfClazz1[0]);
        assert(methodFingerprintsOfClass1.get(1).getEntity() == methodsOfClazz1[1]);
        assert(methodFingerprintsOfClass1.get(2).getEntity() == methodsOfClazz1[2]);

    }
    
    @Test
    public void testGetPackageByMethod() {
        Fingerprint methodFingerprint = new Fingerprint(methodsOfClazz1[0]);
        
        Fingerprint packageHierarchy = service.getPackageByMethod(methodFingerprint);
        
        assert(packageHierarchy.getEntity() == packages[0]);
    }
    
    @Test
    public void testGetPackageHierarchyByMethod() {
        
        Fingerprint pckgPrint = new Fingerprint(packages[0]);
        
        Fingerprint packageHierarchy = service.getPackageHierarchy(pckgPrint);
        
        List<Fingerprint> classesOfPackage = packageHierarchy.getChildFingerprints();
        
        assert(classesOfPackage.size() == 2);
        
        Fingerprint classFingerprint0 = classesOfPackage.get(0);
        Fingerprint classFingerprint1 = classesOfPackage.get(1);
        
        assert(classFingerprint0.getEntity().equals(clazzes[0]));
        assert(classFingerprint1.getEntity().equals(clazzes[1]));
        
        List<Fingerprint> methodFingerprintsOfClass0 = classFingerprint0.getChildFingerprints();
        List<Fingerprint> methodFingerprintsOfClass1 = classFingerprint1.getChildFingerprints();
        
        assert(methodFingerprintsOfClass0.size() == methodsOfClazz0.length);
        assert(methodFingerprintsOfClass1.size() == methodsOfClazz1.length);
        assert(methodFingerprintsOfClass0.get(0).getEntity() == methodsOfClazz0[0]);
        assert(methodFingerprintsOfClass0.get(1).getEntity() == methodsOfClazz0[1]);
        assert(methodFingerprintsOfClass1.get(0).getEntity() == methodsOfClazz1[0]);
        assert(methodFingerprintsOfClass1.get(1).getEntity() == methodsOfClazz1[1]);
        assert(methodFingerprintsOfClass1.get(2).getEntity() == methodsOfClazz1[2]);
        
    }
    
    @Test
    public void testSaveClass() throws SQLException {
        Fingerprint classFingerprint = new Fingerprint("pckg:class");
        Fingerprint methodFingerprint = new Fingerprint("<init>():void");
        classFingerprint.addChildFingerprint(methodFingerprint);
        
        String mvnIdentifier = "libX:v1.2";
        Clazz clazzEntity = mock(Clazz.class);
        
        when(entityService.saveClass(
                classFingerprint.getFeatureVector().toBinary(), 
                classFingerprint.getName(), "pckg", mvnIdentifier))
            .thenReturn(clazzEntity);
        
        service.saveClass(classFingerprint, mvnIdentifier);
        
        verify(entityService, times(1)).saveClass(
                classFingerprint.getFeatureVector().toBinary(), 
                classFingerprint.getName(), "pckg", mvnIdentifier);
        
        verify(entityService, times(1)).saveMethod(
                methodFingerprint.getFeatureVector().toBinary(), 
                methodFingerprint.getName(), methodFingerprint.getLength(), 
                clazzEntity);
    
    }
    
    
    
}
