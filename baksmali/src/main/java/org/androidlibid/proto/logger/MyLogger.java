package org.androidlibid.proto.logger;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class MyLogger {

    private static final Formatter MYFORMATTER;
    private static Handler FILEHANDLER;
    
    
    static {
        LogManager.getLogManager().reset();
        
        MYFORMATTER = new Formatter() {
            Formatter formatter; 
            {
                this.formatter = new SimpleFormatter();
            }
            @Override
            public String format(LogRecord lr) {
                return formatter.formatMessage(lr) + "\n";
            }
        };
        
        try {
            FILEHANDLER = new FileHandler("log/logfile", true);
            FILEHANDLER.setFormatter(MYFORMATTER);
        } catch (IOException ex) {
            Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(MyLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        
        addHandlerIfNeccessary(logger);
        
        return logger;
    }
    
    public static void setLogLevel(String name, Level level) {
        Logger logger = Logger.getLogger(name);
        
        addHandlerIfNeccessary(logger);
        
        for(Handler hndlr : logger.getHandlers()) {
            hndlr.setLevel(level);
        }
        
        logger.setLevel(level);
    }

    private static void addHandlerIfNeccessary(Logger logger) {
//        for(Handler h : logger.getHandlers()) {
//            logger.removeHandler(h);
//        }
        
        if(logger.getHandlers().length == 0) {
            System.out.println("creating handler for " + logger.getName());
//            Handler hndlr = new ConsoleHandler();
            logger.addHandler(FILEHANDLER);
        }
        
        if(logger.getHandlers().length > 1) {
            throw new RuntimeException("wtf??");
        }
        
    }
    
}

//    java.util.logging.Logger.getLogger("").getHandlers()[0].setLevel(Level.SEVERE);
//       prpbl not needed
////       for(Handler hnd : LOG.getHandlers()) {
////           LOG.removeHandler(hnd);
////       }
//       