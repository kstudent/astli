package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.Library;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jf.baksmali.baksmaliOptions;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class SetupLogger {

    private final baksmaliOptions options;
    private final EntityService service;
    
    private static final Logger LOGGER = LogManager.getLogger(SetupLogger.class);
    private final NumberFormat frmt = new DecimalFormat("#0.00");

    public SetupLogger(baksmaliOptions options, EntityService service) {
        this.options = options;
        this.service = service;
        
    }
    
    public void logSetup() throws SQLException {
        
        String[] pieces = options.inputFileName.split("/");
        
        String obfLvl    = pieces[pieces.length - 1];
        String apkName   = (pieces.length > 1) ? pieces[pieces.length - 2] : "<unknown>";
        String algorithm = (options.useVectorDiffStrategy) ? "vectorDiff" : "inclusion" ; 
        
        LOGGER.info("* Setup for {} / {} / {}", apkName, obfLvl, algorithm);
        LOGGER.info("- Time: {}", (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()));
        LOGGER.info("- Jobs: {}",          options.jobs);
        
        logAlgorithmSetup();
        logLoggerLevels();
        logDBStatus();
        
    }

    private void logAlgorithmSetup() {
         if(options.aliFingerprintAPK) {
            LOGGER.info("- APK matching activated");
            LOGGER.info("- IsObfusctated: {}", options.isObfuscated);
            LOGGER.info("- MappingFile: {}",   options.mappingFile);
            
            if(options.useVectorDiffStrategy) {
                LOGGER.info("- Vector Diff Match activated");
                LOGGER.info("- Vector Diff Level: {}",    options.vectorDiffLevel);
                LOGGER.info("- Similarity Threshold: {}", frmt.format(options.similarityThreshold));
            } else {
                LOGGER.info("- Inclusion Match activated");
                LOGGER.info("- Allow repeated Matching: {}", options.allowRepeatedMatching);
                LOGGER.info("- Settings: {}", options.inclusionSettings);
            }
        } else {
            LOGGER.info("- Lib Storing activated");
            LOGGER.info("- Store on Method Level: {} ", options.storeOnMethodLevel);
            LOGGER.info("- MVN Lib Identifier: {}"    , options.mvnIdentifier);
        }
    }

    private void logDBStatus() throws SQLException {
        LOGGER.info("- Methods  : {}", service.countMethods());
        LOGGER.info("- Classes  : {}", service.countClasses());
        LOGGER.info("- Packages : {}", service.countPackages());
        LOGGER.info("- Libs     : {}", service.countLibraries());
        
        for(Library lib : service.findLibraries()) {
            LOGGER.info("  - {}", lib.getName());
        }
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
