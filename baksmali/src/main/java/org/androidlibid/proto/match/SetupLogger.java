package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.androidlibid.proto.ASTLIOptions;
import org.androidlibid.proto.ao.EntityService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class SetupLogger {

    private final EntityService service;
    private final ASTLIOptions options;
    
    private static final Logger LOGGER = LogManager.getLogger(SetupLogger.class);
    private final NumberFormat frmt = new DecimalFormat("#0.00");

    public SetupLogger(EntityService service, ASTLIOptions options) {
        this.service = service;
        this.options = options;
    }
    
    public void logSetup() throws SQLException {
        
        String algorithm = options.process.getSimpleName(); 
        
        LOGGER.info("* {} / {} / {}", options.apkName, options.obfLvl, algorithm);
        LOGGER.info("** Setup");
        LOGGER.info("- Time: {}", (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()));
        
        logAlgorithmSetup();
        logLoggerLevels();
        logDBStatus();
        
    }

    private void logAlgorithmSetup() {
         if(options.isInMatchingPhase()) {
            LOGGER.info("- Matching Phase");
            LOGGER.info("- IsObfusctated: {}", options.isObfuscated());
            LOGGER.info("- MappingFile: {}",   options.mappingFile);
        } else {
            LOGGER.info("- LearningPhase");
            LOGGER.info("- MVN Lib Identifier: {}"    , options.mvnIdentifier);
        }
    }

    private void logDBStatus() throws SQLException {
        LOGGER.info("- Methods  : {}", service.countMethods());
        LOGGER.info("- Classes  : {}", service.countClasses());
        LOGGER.info("- Packages : {}", service.countPackages());
        LOGGER.info("- Libs     : {}", service.countLibraries());
        
        StringBuilder libs = new StringBuilder();
        service.findLibraries().stream()
                .map(lib -> lib.getName() + ", ")
                .forEach(lib -> libs.append(lib));
        
        LOGGER.info("    - {}", libs);
    }

    private void logLoggerLevels() {
        LOGGER.info("- LOG4j levels:");
        final LoggerContext context = LoggerContext.getContext(false);
        final Configuration config = context.getConfiguration();
        
        for(String loggerName : config.getLoggers().keySet()) {
            LoggerConfig lc = config.getLoggers().get(loggerName);
            if(loggerName.isEmpty()) loggerName = "root";
            LOGGER.info("  | {} | {} |", loggerName, lc.getLevel());
        }
    }
}
