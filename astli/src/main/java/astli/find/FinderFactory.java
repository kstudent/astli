package astli.find;

import astli.db.EntityService;
import astli.pojo.ASTLIOptions;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class FinderFactory {

    public static CandidateFinder createFinder(ASTLIOptions options, EntityService service) {
        
        FindByNeedle partFinder = new FindByNeedle(
                service, 
                options.minimumNeedleParticularity, 
                options.maxNeedleAmount
            );
        
        if(FindByNeedle.class.equals(options.finder)) {
            return partFinder; 
        } else if(FindByNameOrNeedle.class.equals(options.finder)) {
            return new FindByNameOrNeedle(new FindByName(service), partFinder);
        } else if (FindByName.class.equals(options.finder)) {
            return new FindByName(service);
        }
        
        throw new UnsupportedOperationException("Finder not found.");
    }
}
