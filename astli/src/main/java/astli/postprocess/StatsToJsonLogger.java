package astli.postprocess;

import com.google.gson.GsonBuilder;
import java.util.function.Consumer;
import astli.postprocess.StatsCounter.Stats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
class StatsToJsonLogger implements Consumer<Stats> {

    private final static Logger LOGGER = LogManager.getLogger();
    
    @Override
    public void accept(Stats stats) {
        LOGGER.info(new GsonBuilder().create().toJson(stats));
    }
}
