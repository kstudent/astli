/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidlibid.proto;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.androidlibid.proto.logger.MyLogger;
import org.junit.Test;

/**
 *
 * @author Christof Rabensteiner <christof.rabensteiner@gmail.com>
 */
public class LoggerTest {
   
   @Test
   public void testMyLogger() {
       String loggerName = "franz";
       
       Logger l = MyLogger.getLogger(loggerName);
       l.warning("warning!");
       l.log(Level.WARNING, "warning!");
       l.severe("severe!");
       l.log(Level.SEVERE,   "severe!");
       l.info("info!");
       l.log(Level.INFO,   "info!");
       
       l.fine("fine!");
       l.log(Level.FINE, "fine!");
       l.finer("finer!");
       l.log(Level.FINER, "finer!");
       l.finest("finest!");
       l.log(Level.FINEST, "finest!");
       
       MyLogger.setLogLevel(loggerName, Level.FINEST);
       l.fine("fine!");
       l.finer("finer!");
       l.finest("finest!");
       
       l = MyLogger.getLogger("anotherone");
       l.warning("warning!");
       l.fine("fine!");
       MyLogger.setLogLevel("anotherone", Level.FINEST);
       l.fine("fine!");
   }
   
}
