//package org.androidlibid.proto.ao;
//
//import java.sql.SQLException;
//import net.java.ao.EntityManager;
//import net.java.ao.schema.CamelCaseFieldNameConverter;
//import net.java.ao.test.converters.NameConverters;
//import net.java.ao.test.jdbc.Data;
//import net.java.ao.test.jdbc.DatabaseUpdater;
//import net.java.ao.test.jdbc.Hsql;
//import net.java.ao.test.jdbc.Jdbc;
//import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.la4j.Vector;
//import org.la4j.vector.dense.BasicVector;
//
///**
// *
// * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
// */
//@RunWith(ActiveObjectsJUnitRunner.class)
//@Data(LibraryFingerprintDBUpdaterTest.DBInit.class)
//@NameConverters(field = CamelCaseFieldNameConverter.class)
//@Jdbc(Hsql.class)
//public class LibraryFingerprintDBUpdaterTest {
//
//    private EntityManager em;
//    
//    private LibraryFingerprintDBUpdater updater;
//    private EntityService service;
//    
//    private Library lib;
//    private byte[] zeroBytes;
//    private final String libName  = "libX"; 
//    private final String pckgName = "packageX"; 
//    
//    private static final Logger LOGGER = LogManager.getLogger(LibraryFingerprintDBUpdaterTest.class);
//    
//    @Before
//    public void setUp() throws SQLException {
//        zeroBytes = new BasicVector(3).toBinary();
//        
//        service = new EntityService(em, zeroBytes);
//        
//        lib = em.create(Library.class);
//        lib.setName(libName);
//        lib.setVector(zeroBytes);
//        lib.save();
//        
//        Package pckg = em.create(Package.class);
//        pckg.setName(pckgName);
//        pckg.setVector(zeroBytes);
//        pckg.setLibrary(lib);
//        pckg.save();
//        
//        Clazz clazz0 = em.create(Clazz.class);
//        clazz0.setName("clazz0");
//        clazz0.setPackage(pckg);
//        clazz0.setVector(new BasicVector(new double[] {1, 2, 3}).toBinary());
//        clazz0.save();
//        
//        Clazz clazz1 = em.create(Clazz.class);
//        clazz1.setName("clazz1");
//        clazz1.setPackage(pckg);
//        clazz1.setVector(new BasicVector(new double[] {4, 5, 6}).toBinary());
//        clazz1.save();
//        
//        updater = new LibraryFingerprintDBUpdater(service);
//    }
//    
//    @Test
//    public void testUpdate() throws SQLException {
//        updater.update(libName);
//        
//        BasicVector v0 = new BasicVector(new double[] {1, 2, 3});
//        BasicVector v1 = new BasicVector(new double[] {4, 5, 6});
//        Vector expectedVector = v0.add(v1);
//   
//        Library[] updatedLibs = em.find(Library.class, "NAME = ?", libName);
//        assert(updatedLibs.length == 1);
//        
//        BasicVector libVector = BasicVector.fromBinary(updatedLibs[0].getVector()); 
//        assert(libVector.equals(expectedVector));
//        
//        Package[] updatedPckgs = em.find(Package.class, "NAME = ?", pckgName);
//        assert(updatedPckgs.length == 1);
//        
//        BasicVector pckgVector = BasicVector.fromBinary(updatedPckgs[0].getVector()); 
//        assert(pckgVector.equals(expectedVector));
//    }
//    
//    public static final class DBInit implements DatabaseUpdater {
//        @Override
//        @SuppressWarnings("unchecked")
//        public void update(EntityManager entityManager) throws Exception
//        {
//            entityManager.migrate(Method.class, Clazz.class, Package.class, Library.class);
//        }
//    }
//    
//}
