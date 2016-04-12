package org.androidlibid.proto.match;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.androidlibid.proto.ao.EntityService;
import org.androidlibid.proto.ao.Library;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        
        LOGGER.info("* Setup");
        LOGGER.info("- Jobs: {}",          options.jobs);
        LOGGER.info("- Input File: {}",    options.inputFileName);
        
        logAlgorithmSetup();
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
    
}
