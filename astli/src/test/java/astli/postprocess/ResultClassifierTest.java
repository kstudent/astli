package astli.postprocess;


import astli.db.EntityService;
import astli.pojo.Match;
import astli.pojo.Match.Item;
import astli.pojo.PackageHierarchy;
import astli.postprocess.ResultClassifier.Classification;
import astli.postprocess.ResultClassifier.ClassificationTupel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class ResultClassifierTest {
    
    @Test
    public void testTPU() throws SQLException {
        List<Item> items = new ArrayList<>();
        items.add(new Item(1.0, new PackageHierarchy("package1")));
        items.add(new Item( .8, new PackageHierarchy("package2")));
        items.add(new Item( .5, new PackageHierarchy("package3")));
        PackageHierarchy apkH = new PackageHierarchy("package1");
        Match match = new Match(items, apkH);
        ResultClassifier rc = new ResultClassifier(mockService());
        
        ClassificationTupel tupel = rc.classify(match);
        
        Classification classification = tupel.getClassification();
        assert(classification.equals(Classification.TPU));
    }
    
    @Test
    public void testTPN() throws SQLException {
        List<Item> items = new ArrayList<>();
        items.add(new Item(1.0, new PackageHierarchy("package2")));
        items.add(new Item(1.0, new PackageHierarchy("package1")));
        items.add(new Item( .5, new PackageHierarchy("package3")));
        PackageHierarchy apkH = new PackageHierarchy("package1");
        Match match = new Match(items, apkH);
        ResultClassifier rc = new ResultClassifier(mockService());
        
        ClassificationTupel tupel = rc.classify(match);
        
        Classification classification = tupel.getClassification();
        assert(classification.equals(Classification.TPN));
    }
    
    @Test
    public void testTN() throws SQLException {
        List<Item> items = new ArrayList<>();
        PackageHierarchy apkH = new PackageHierarchy("not_db_package");
        Match match = new Match(items, apkH);
        ResultClassifier rc = new ResultClassifier(mockService());
        
        ClassificationTupel tupel = rc.classify(match);
        
        Classification classification = tupel.getClassification();
        assert(classification.equals(Classification.TN));
    }
    
    @Test
    public void testFN() throws SQLException {
        List<Item> items = new ArrayList<>();
        PackageHierarchy apkH = new PackageHierarchy("db_package");
        Match match = new Match(items, apkH);
        ResultClassifier rc = new ResultClassifier(mockService());
        
        ClassificationTupel tupel = rc.classify(match);
        
        Classification classification = tupel.getClassification();
        assert(classification.equals(Classification.FN));
    }
    
    @Test
    public void testFP1() throws SQLException {
        List<Item> items = new ArrayList<>();
        items.add(new Item(1.0, new PackageHierarchy("package2")));
        items.add(new Item(0.8, new PackageHierarchy("package1")));
        items.add(new Item( .5, new PackageHierarchy("package3")));
        PackageHierarchy apkH = new PackageHierarchy("package1");
        Match match = new Match(items, apkH);
        ResultClassifier rc = new ResultClassifier(mockService());
        
        ClassificationTupel tupel = rc.classify(match);
        
        Classification classification = tupel.getClassification();
        assert(classification.equals(Classification.FP));
    }
    
    @Test
    public void testFP2() throws SQLException {
        List<Item> items = new ArrayList<>();
        items.add(new Item(1.0, new PackageHierarchy("package2")));
        PackageHierarchy apkH = new PackageHierarchy("package1");
        Match match = new Match(items, apkH);
        ResultClassifier rc = new ResultClassifier(mockService());
        
        ClassificationTupel tupel = rc.classify(match);
        
        Classification classification = tupel.getClassification();
        assert(classification.equals(Classification.FP));
    }
       
    @Test
    public void testMockService() throws SQLException {
        assert( mockService().isPackageNameInDB("db_package"));
        assert(!mockService().isPackageNameInDB("package_x"));
    }

    private EntityService mockService() throws SQLException {
        EntityService service = mock(EntityService.class);
        when(service.isPackageNameInDB(any(String.class))).thenAnswer(
            (InvocationOnMock invocation) -> {
                return "db_package".equals(invocation.getArgumentAt(0, String.class));
            }
        );
        return service;
    }
    
}
